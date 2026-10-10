# contributing

bug reports are the most useful thing you can send. small focused pull requests are fine, and for anything bigger please open an issue first.

## reporting an issue

1. look through the open and closed issues first.
2. open a new issue and pick the form that fits: bug report, feature request or compatibility report. blank issues are off on purpose.
3. a good bug report has:
   - the versions: minecraft, neoforge and plinthworks.
   - the other mods you run, and whether it still happens with only plinthworks.
   - which sigil, which resource mode (items, energy, fluid or experience) and which channel were involved.
   - numbered steps to reproduce, and what you expected against what happened.
   - the logs.
4. attach the logs.
   - client or single player: `logs/latest.log` in the game folder, and the newest file in `crash-reports/` if the game crashed.
   - dedicated server: the server's `logs/latest.log`.
   - attach the file itself, not a screenshot of it. a paste link is fine for very large ones.
5. check the log for private things first. it holds your player name, the ip of the server you joined, and your computer's user name in file paths. replace what you don't want to share.

a way around a claim, duplicating or deleting items, or crashing a server should not go in a public issue. see [SECURITY.md](SECURITY.md).

## labels

`bug`, `feature`, `compat`, `docs`, `art`, `gui`, `perf`, `security`, `claims`, `needs-repro`, `needs-info`, `good-first-issue`, `help-wanted`, `question`, plus area labels like `sigils`, `seals`, `etchings`, `channels`, `world-verbs`, `energy`, `fluid`, `xp`, `link-tool` and `config`.

## building and testing

you need java 21. gradle fetches the rest.

```sh
./gradlew build              # compiles, runs the unit tests, builds the jar in build/libs
./gradlew test               # unit tests only
./gradlew runGameTestServer  # the gametests on a real server
```

the gametests live in `src/gametest` and are not part of the jar.

## generated resources

models, lang, recipes and tags under `src/generated/resources` come from the data providers. do not edit them by hand. the datagen drift check wipes the generated tree and runs the generators again, so a pull request that changes a provider has to commit the result. to check it yourself:

```sh
rm -rf src/generated/resources/assets src/generated/resources/data
./gradlew runData
git status
```

`git status` must show nothing changed. generators never delete files, which is why the tree is wiped first.

## what not to add

- no textures, models, sounds or screenshots from any other mod. plinthworks art is drawn from scratch.
- plinthworks is inspired by mowmaster's pedestals, and that is all it is. do not copy its code, assets, names or text. write your own.
- player facing text goes through the lang provider, not hard coded strings.

## licence

code is mit (`LICENSE`). by sending a pull request you agree it can be shipped under that licence. see [THIRD_PARTY.md](../THIRD_PARTY.md).
