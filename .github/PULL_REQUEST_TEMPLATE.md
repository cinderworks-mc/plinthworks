<!-- small focused pull requests are welcome. for anything bigger please open an issue first. -->

## what and why

closes #

## checked

- [ ] `./gradlew build` passes
- [ ] `./gradlew runGameTestServer` passes (needed when sigil, network or world verb code changed)
- [ ] if a data provider changed: wiped `src/generated/resources/assets` and `data`, ran `./gradlew runData`, and `git status` shows only the intended files
- [ ] no texture, model, sound or screenshot from another mod added (see `THIRD_PARTY.md`)
- [ ] player facing text goes through the lang provider
