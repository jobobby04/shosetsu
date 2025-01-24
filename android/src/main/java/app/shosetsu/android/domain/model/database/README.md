# Database Models

## Installed vs Repository entities

Some entities exist as a split between their repository and installed reflections.

Installed entities do not get deleted if their repository is removed.

A repository entity does get removed if so.

## Coupling

Many entities are coupled to collapse together in case of their parent entity being removed.

But be wary of entities that do not need this and or can exist in their own lifespan.

Example: DBNovelEntities do not need their extension entity to be present.