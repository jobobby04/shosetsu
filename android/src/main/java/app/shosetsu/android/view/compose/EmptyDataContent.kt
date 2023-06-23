package app.shosetsu.android.view.compose

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.shosetsu.android.R
import kotlin.random.Random

/*
 * This file is part of shosetsu.
 *
 * shosetsu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * shosetsu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with shosetsu.  If not, see <https://www.gnu.org/licenses/>.
 */

@Preview
@Composable
fun PreviewErrorContent() {
	ErrorContent(
		R.string.todo,
		actions = arrayOf(
			ErrorAction(R.string.todo) { },
			ErrorAction(R.string.todo) { },
			ErrorAction(R.string.todo) { },
		),
		stackTrace = "l\nl\nl\nl\nl\n"
	)
}

private val ERROR_FACES = listOf(
	"(･o･;)",
	"Σ(ಠ_ಠ)",
	"ಥ_ಥ",
	"(˘･_･˘)",
	"(；￣Д￣)",
	"(･Д･。",
	"(┳Д┳)",
	"(☍﹏⁰)｡",
	"(;Д;)",
	"╥﹏╥",
	"(இ﹏இ`｡)",
	"༼ ༎ຶ ෴ ༎ຶ༽",
	"(⋟﹏⋞)",
	"(ノAヽ)",
	"(つ﹏⊂)",
	"（πーπ）",
	"(⊙_◎)",
	"(゜ロ゜)",
	"（￣□￣；）",
	"(_□_；)",
	"(;Ⅲ□Ⅲ;)",
	"( p_q)",
	"Σ(￣ロ￣lll)",
	"ヽ(´Д`;)ﾉ",
	"╮(╯_╰)╭",
	"┐(´д`)┌",
	"-`д´-",
	"(´-ι_-｀)",
	"(・ω・｀)………..",
	"〴⋋_⋌〵",
	"（＞μ＜＃）"
)

fun getRandomErrorFace(): String {
	return ERROR_FACES[Random.nextInt(ERROR_FACES.size)]
}

data class ErrorAction(val id: Int, val onClick: () -> Unit)

@Composable
fun ErrorContent(
	@StringRes messageRes: Int,
	actions: ErrorAction,
	modifier: Modifier = Modifier,
	stackTrace: String? = null,
) = ErrorContent(
	messageRes = messageRes,
	actions = arrayOf(actions),
	modifier = modifier,
	stackTrace = stackTrace
)

@Composable
fun ErrorContent(
	@StringRes messageRes: Int,
	modifier: Modifier = Modifier,
	actions: Array<ErrorAction> = arrayOf(),
	stackTrace: String? = null,
) =
	ErrorContent(
		modifier = modifier,
		message = stringResource(id = messageRes),
		stackTrace = stackTrace,
		actions = actions
	)

@Composable
fun ErrorContent(
	message: String,
	actions: ErrorAction,
	modifier: Modifier = Modifier,
	stackTrace: String? = null,
) = ErrorContent(
	message = message,
	actions = arrayOf(actions),
	modifier = modifier,
	stackTrace = stackTrace
)

@Composable
fun ErrorContent(
	message: String,
	modifier: Modifier = Modifier,
	actions: Array<ErrorAction> = arrayOf(),
	stackTrace: String? = null,
) {
	val face = remember { getRandomErrorFace() }
	Box(
		modifier = modifier
			.fillMaxSize()
			.padding(16.dp), contentAlignment = Alignment.Center
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			modifier = Modifier
		) {
			Text(
				face,
				fontSize = 48.sp,
				modifier = Modifier.padding(bottom = 16.dp)
			)
			Text(
				message,
				fontSize = 16.sp,
				textAlign = TextAlign.Center,
				modifier = Modifier.padding(bottom = 8.dp)
			)

			actions.forEach {
				TextButton(
					onClick = { it.onClick() },
					contentPadding = PaddingValues(16.dp)
				) {
					Text(stringResource(it.id))
				}
			}

			if (stackTrace != null) {
				var isStacktraceVisible by remember { mutableStateOf(false) }

				IconToggleButton(
					isStacktraceVisible,
					onCheckedChange = {
						isStacktraceVisible = it
					}
				) {
					Icon(
						painter = if (!isStacktraceVisible)
							painterResource(R.drawable.expand_more)
						else painterResource(R.drawable.expand_less),
						contentDescription = if (!isStacktraceVisible)
							stringResource(R.string.more)
						else stringResource(R.string.less)
					)
				}

				Text(
					if (isStacktraceVisible) {
						stackTrace
					} else {
						""
					},
					style = MaterialTheme.typography.bodySmall,
				)
			}
		}
	}
}