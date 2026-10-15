# Custom changes

## Colours
- The 16 vanilla colours are gone. Teams use 20 hex colours: green, blue, red, pink, purple, yellow, orange, cyan,
  rose, brown, white, black, gray, navy, mint, lavender, gold, peach, sky, lime (`TeamColor.java`).
- New teams get a **random** colour. `/team color <name>` changes it. Ban colours with `colors.banned` in config.yml.
- Old teams are converted to the closest new colour the first time they load.

## Below-name team tag (TAB plugin)
- `%teams_belowname%` -> `[TAG]` in the team colour. See `tab-example.yml` (saved in the plugin folder).
- Also: `%teams_colorhex%`, `%teams_color%`, `%teams_colorname%`.

## Tab list / chat
- `display.chat` and `display.tab` in config.yml each have `enabled` and `show` (`name` or `tag`).
- The tab list is normal by default. Vanilla scoreboard teams are only created when they are needed.

## Balance removed
- `/team bal|baltop|deposit|withdraw`, `/teama money ...`, team bank storage, balance placeholders / leaderboard,
  bank limit in levels. Level-ups cost score only. (Vault is still used for command costs in team.yml.)

## Menu (GUI)
- `/team` opens the menu (`gui.enabled`). `/team top` opens the Team Top menu.
- Menus: main, team top, members (promote / demote / kick), settings, warps, allies, all teams.
- Colours have **no menu**: use `/team color <name>`. The menu buttons just print the colour list in chat.
- Permission: `teams.gui` (default true).

## Theme
- Messages and menus use #A7C7E7, #ADD8E6 and #89CFF0 (see `theme` in config.yml and messages.yml).

## Team top stats
- Team top can be ranked by **score, kills, deaths, K/D ratio and playtime** (buttons on the top row).
- Kills = kills of players from other teams. Deaths = deaths of team members. K/D = kills / deaths (kills if no deaths).
- Playtime = time team members spent online while in the team (saved every 5 minutes and on quit).
- Stored in the team meta (stat_kills, stat_deaths, stat_playtime). Placeholders: `%teams_kills%`,
  `%teams_deaths%`, `%teams_kdr%`, `%teams_playtime%`.
- Stats only count from now on, old teams start at 0.

## Cleaner config.yml
- The first 4 sections are the main features: **display** (chat / tab), **teams** (max teams, members, admins,
  owners, warps, chests, name lengths), **allies** (on/off, limit, chests) and **gui**.
- Level 1 comes from `teams:`. `levels:` only lists the extra levels (l2, l3 ...).
- Many keys moved into groups (chat.*, combat.*, teams.*, allies.*). Delete your old config.yml before updating.
- New: `teams.maxTeams` (limit on how many teams exist) and `allies.enabled` (turn allies off).

## Renamed to "Teams"
- Plugin name, jar, messages, prefix and package are now `Teams` / `teams` (permissions are `teams.*`, placeholders are `%teams_*%`, plugin folder is `plugins/Teams`).
- Because of the new name, move your old data from `plugins/BetterTeams` to `plugins/Teams`, rename permissions from `betterteams.*` to `teams.*`, and delete the old config.yml.
- Removed: wiki, tests, extra translations (English only), the Discord / zKoth / LuckPerms extension modules, the update checker and bStats.

## Sounds and click-to-join
- New `sounds:` section in config.yml: create, join, leave, disband, invite and ally request each play a sound. Team chat is silent.
- New `chat.announceTeamCreate`; all four announcements are on by default. Create / join / leave messages, invites and ally requests are clickable.
- Team name / tag shown with the player name is in small capitals (display.smallCaps).
- Team owners get a crown in front of their tag (display.ownerCrown), above the head and in chat.
