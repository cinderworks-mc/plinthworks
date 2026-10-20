# third party notices

plinthworks' own source code, build scripts and data files are under the mit licence (`LICENSE`). there is no separate licence file for the art, so the sources under `art-source` and the textures, models and screenshots made from them are under the same licence. these notices cover only plinthworks' own work, not any other mod.

## platform

- minecraft is a trademark of mojang ab and microsoft. this mod is not affiliated with or endorsed by them. using it needs a legitimate copy of the game and follows the minecraft eula and brand guidelines.
- the lettering in the banner and social images under `assets/branding` uses the glyph shapes of minecraft's default font. those shapes belong to mojang and are not covered by the licence.
- neoforge (lgpl-2.1-only) is the modding platform and is not bundled. official mojang mappings are used through neoforge's tooling.

## optional mods (soft dependencies, not bundled)

`neoforge.mods.toml` lists create, jei, emi and thaumaturge as optional. plinthworks does not compile against create, jei or emi, and nothing of any of them is in the jar or the repository. they keep their own licences.

- thaumaturge is all rights reserved. the essentia compat in `src/thaumaturge` calls its api and is compiled against a copy of the thaumaturge jar that each builder supplies (`libs/README.md`). that jar is never committed, hosted, cached or released by this project, and the build checks that no part of it ends up in the plinthworks jar (`.github/scripts/jar_guard.py`). plinthworks draws no aspect icons or other thaumaturge art; aspect names and colours are read from thaumaturge at runtime.

- when one of them is installed, plinthworks only writes a line to the log. recipe pages for jei and emi and kinetic support for create are not wired yet.
- experience mode needs a fluid from another mod. the config names create enchantment industry's experience fluid by default, but no code or asset from that mod is used.

## inspiration

plinthworks is inspired by mowmaster's [pedestals](https://github.com/Mowmaster/Pedestals). it is an unofficial, separate mod written from scratch with its own code and art, and it is not affiliated with mowmaster. none of its code or assets are used.

## tools

- the `.aseprite` sources for the art are under `art-source`.
- the gametests live in `src/gametest` and are not part of the released jar.
