package com.github.doomsdayrs.apps.shosetsu.ui.updates.async;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.doomsdayrs.api.shosetsu.services.core.dep.Formatter;
import com.github.doomsdayrs.api.shosetsu.services.core.objects.NovelChapter;
import com.github.doomsdayrs.api.shosetsu.services.core.objects.NovelPage;
import com.github.doomsdayrs.apps.shosetsu.R;
import com.github.doomsdayrs.apps.shosetsu.backend.Utilities;
import com.github.doomsdayrs.apps.shosetsu.backend.database.Database;
import com.github.doomsdayrs.apps.shosetsu.variables.DefaultScrapers;
import com.github.doomsdayrs.apps.shosetsu.variables.recycleObjects.NovelCard;

import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Document;

import java.util.ArrayList;

import static com.github.doomsdayrs.apps.shosetsu.backend.database.Database.DatabaseIdentification.getNovelIDFromNovelURL;
import static com.github.doomsdayrs.apps.shosetsu.backend.database.Database.DatabaseNovels.getNovel;
import static com.github.doomsdayrs.apps.shosetsu.backend.scraper.WebViewScrapper.docFromURL;

/*
 * This file is part of Shosetsu.
 *
 * Shosetsu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Shosetsu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Shosetsu.  If not, see <https://www.gnu.org/licenses/>.
 * ====================================================================
 * shosetsu
 * 13 / 08 / 2019
 *
 * @author github.com/doomsdayrs
 */
public class ChapterUpdater extends AsyncTask<Void, Void, Void> {
    private static final int ID = 1917;
    private static final String channel_ID = "shosetsu_updater";

    @NonNull
    private final ArrayList<Integer> novelCards;
    private boolean continueProcesss = true;
    @Nullable
    private final NotificationManager notificationManager;
    private Notification.Builder builder;
    private final ArrayList<NovelCard> updatedNovels = new ArrayList<>();


    public ChapterUpdater(@NotNull ArrayList<Integer> novelCards, @NonNull Context context) {
        this.novelCards = novelCards;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channel_ID, "Shosetsu Update", NotificationManager.IMPORTANCE_HIGH);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }

            builder = new Notification.Builder(context, channel_ID);
        } else builder = new Notification.Builder(context);
    }


    @Override
    protected void onPreExecute() {
        builder = builder
                .setSmallIcon(R.drawable.ic_system_update_alt_black_24dp)
                .setContentTitle("Update")
                .setContentText("Update in progress")
                .setProgress(novelCards.size(), 0, false)
                .setOngoing(true)
                .setOnlyAlertOnce(true);
        if (notificationManager != null) {
            notificationManager.notify(ID, builder.build());
        }
    }


    @Nullable
    @Override
    protected Void doInBackground(Void... voids) {
        for (int x = 0; x < novelCards.size(); x++) {
            NovelCard novelCard = getNovel(novelCards.get(x));
            int novelID = getNovelIDFromNovelURL(novelCard.novelURL);
            builder.setContentText(novelCard.title);
            builder.setProgress(novelCards.size(), x + 1, false);
            if (notificationManager != null) {
                notificationManager.notify(ID, builder.build());
            }

            Formatter formatter = DefaultScrapers.getByID(novelCard.formatterID);
            if (formatter != null) {
                int page = 1;
                NovelPage tempPage;
                if (formatter.isIncrementingChapterList()) {
                    Document document = docFromURL(formatter.novelPageCombiner(novelCard.novelURL, page), formatter.getHasCloudFlare());
                    if (document != null) {
                        tempPage = formatter.parseNovel(document, page);
                        int mangaCount = 0;
                        while (page <= tempPage.getMaxChapterPage() && continueProcesss) {
                            document = docFromURL(formatter.novelPageCombiner(novelCard.novelURL, page), formatter.getHasCloudFlare());
                            if (document != null) {
                                tempPage = formatter.parseNovel(document, page);
                                for (NovelChapter novelChapter : tempPage.getNovelChapters())
                                    add(mangaCount, novelID, novelChapter, novelCard);
                                page++;
                            }
                            Utilities.wait(300);
                        }
                    }
                } else {
                    Document document = docFromURL(novelCard.novelURL, formatter.getHasCloudFlare());
                    if (document != null) {
                        tempPage = formatter.parseNovel(document, page);
                        int mangaCount = 0;
                        for (NovelChapter novelChapter : tempPage.getNovelChapters())
                            add(mangaCount, novelID, novelChapter, novelCard);
                    }
                }
            }
            Utilities.wait(1000);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {

        StringBuilder stringBuilder = new StringBuilder();
        if (updatedNovels.size() > 0) {
            builder.setContentTitle("Completed Update");
            for (NovelCard novelCard : updatedNovels)
                stringBuilder.append(novelCard.title).append("\n");
            builder.setStyle(new Notification.BigTextStyle());
        } else stringBuilder.append("No updates found");

        builder.setContentText(stringBuilder.toString());
        builder.setProgress(0, 0, false);
        builder.setOngoing(false);
        if (notificationManager != null) {
            notificationManager.notify(ID, builder.build());
        }
    }


    private void add(int mangaCount, int novelID, @NonNull NovelChapter novelChapter, @NonNull NovelCard novelCard) {
        if (continueProcesss && Database.DatabaseChapter.isNotInChapters(novelChapter.getLink())) {
            mangaCount++;
            System.out.println("Adding #" + mangaCount + ": " + novelChapter.getLink());
            Database.DatabaseChapter.addToChapters(novelID, novelChapter);
            Database.DatabaseUpdates.addToUpdates(novelID, novelChapter.getLink(), System.currentTimeMillis());

            if (!updatedNovels.contains(novelCard))
                updatedNovels.add(novelCard);
        }
    }


}
