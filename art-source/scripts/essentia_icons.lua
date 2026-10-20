-- essentia mode art: a lidded jar glyph for the plinth's mode panel, a resource button icon and a gauge cap.
-- draws onto its own "essentia" layer in the two sheets, so rerunning replaces it and leaves the rest alone.
-- aseprite -b --script-param dir=art-source/aseprite --script art-source/scripts/essentia_icons.lua

-- brass glyph palette from plinth_glyphs: light, mid, warm, dark, shadow, deepest
local glyph = {A = 0xe6da86, B = 0xd5b751, C = 0xc09339, E = 0xa97229, D = 0x8e531f, F = 0x733817}
-- widget icon palette: highlight, body, shadow
local icon = {A = 0xe6e0d2, B = 0xcfc8b8, C = 0x1f1e20}
-- gauge caps use the body and shadow only
local cap = {A = 0xcfc8b8, B = 0x1f1e20}

local GLYPH = {
  ".ABBC.",
  "B....D",
  "B....D",
  "BAABED",
  ".EDDF.",
}

local RES = {
  "................",
  "................",
  "................",
  ".....AAAAAA.....",
  ".....ABBBBBC....",
  "......ABBBC.....",
  "....AABBBBBBC...",
  "....AB.....BC...",
  "....AB.....BC...",
  "....ABAABBBBC...",
  "....ABBBBBBBC...",
  "....ABBBBBBBC...",
  ".....CCCCCCC....",
  "................",
  "................",
  "................",
}

local CAP = {
  ".AAAA..",
  "..AAB..",
  ".AAAAB.",
  ".AAAAB.",
  "..BBBB.",
}

local function rgba(hex)
  return app.pixelColor.rgba((hex >> 16) & 255, (hex >> 8) & 255, hex & 255, 255)
end

local function paint(img, x0, y0, rows, pal)
  for y, row in ipairs(rows) do
    for x = 1, #row do
      local c = pal[row:sub(x, x)]
      if c then img:drawPixel(x0 + x - 1, y0 + y - 1, rgba(c)) end
    end
  end
end

local function redo(file, draw)
  local spr = app.open(app.params.dir .. "/" .. file)
  for _, layer in ipairs(spr.layers) do
    if layer.name == "essentia" then spr:deleteLayer(layer) break end
  end
  local layer = spr:newLayer()
  layer.name = "essentia"
  local img = Image(spr.width, spr.height, ColorMode.RGB)
  draw(img)
  spr:newCel(layer, 1, img, Point(0, 0))
  spr:saveAs(spr.filename)
  spr:close()
end

-- glyph cell 0,10 on the sheet, clear of the gauge bars at x 10 to 15
redo("block_plinth_glyphs.aseprite", function(img) paint(img, 0, 10, GLYPH, glyph) end)
redo("gui_plinth_widgets.aseprite", function(img)
  paint(img, 144, 80, RES, icon)
  paint(img, 76, 24, CAP, cap)
end)
