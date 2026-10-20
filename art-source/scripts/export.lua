-- flattens a layered source into the png the game reads, at 1:1. hidden layers stay out.
-- aseprite -b --script-param src=art-source/aseprite/x.aseprite --script-param out=src/main/resources/.../x.png --script art-source/scripts/export.lua
local spr = app.open(app.params.src)
spr:flatten()
spr:saveCopyAs(app.params.out)
spr:close()
