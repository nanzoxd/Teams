# Teams

A Spigot / Paper (1.21) plugin for player teams: team chat, homes, warps, allies, score, a menu (`/team`) and a team top.

## Build

```
mvn package
```

The plugin jar is created in `target/Teams-1.0.0.jar`. Put it in your server's `plugins` folder.

## Layout

```
pom.xml
src/main/java/teams/        all the code
src/main/resources/         config.yml, messages.yml and the other settings files
```

## Sounds and click-to-join

- Messages for a team being **created**, **joined**, **left** and **disbanded** play a sound (`sounds:` in `config.yml`; use `sound: 'none'` to turn one off, or `sounds.enabled: false` for all).
- The create / join / leave messages, team invites and ally requests can be **clicked** (join the team, or accept the ally request).
- Team chat (`name [tag] » hello`) never plays a sound.

See `CUSTOM-CHANGES.md` for the other changes in this version (colours, menu, team top, config layout).
