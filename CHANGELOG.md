# changelog

notable changes to plinthworks. loosely follows keep a changelog.

## 0.2.3

### known issues in 0.2.2

update if you are on 0.2.2. a void plinth with no seal erased every chest, tank and
energy buffer in its range cube, the crafting sigil could duplicate items, a plinth
holding more than 99 items in its buffer threw on save and could take its chunk with
it, and the energy gauge never moved.

### fixed
- void only drains the plinth's own buffer now, never nearby containers. an unfiltered
  void is a plain trash can for whatever you feed it; add seals to destroy selectively.
- the display buffer is capped at 99, the limit the item format allows. storage
  etchings past the second no longer push a plinth into a count that cannot be saved.
- the energy gauge, its tooltip and the FE/t row read the real numbers. anything over
  32767 was being truncated on the way to the screen, so 40000 arrived as -25536.
- shift-clicking into the sigil, etching, seal and display slots marks the plinth
  changed, so a merged stack is no longer duplicated or lost on reload.
- channel cleanup ran never instead of every 200 ticks, so dead members piled up in a
  channel forever.
- a corrupt redstone-mode value on load can no longer crash the plinth.

### changed
- crafting is off by default while the transfer path is reworked. turn it back on with
  crafting.enabled if you accept the duplication risk.
- the channel name and every seal filter edit now travel as their own packets instead
  of being smuggled through gui button numbers, which is what let one click write the
  wrong filter. the gui itself is unchanged.
- saved plinths and the channel file carry a version stamp for future migrations.

## 0.2.2

### added
- resource modes: the plinth moves energy (FE), fluids, and experience now, not
  just items. cycle the mode in the gui. xp rides a fluid (defaults to create
  enchantment industry's experience, configurable), so it works with any mod that
  exposes xp as a fluid.
- world verbs: breaker, placer, planter, harvester, magnet. each acts on the cube
  around the plinth sized by the range etching. drops route into adjacent or linked
  storage, and the verb skips a block instead of spilling when there's nowhere to
  put the drops.
- ghost-slot seal editor for item, exact, and mod filters (drop a filter in instead
  of the old stage-and-add flow).

### changed
- gui first pass: a resource gauge replaces the display slot in energy/fluid/xp
  mode, icon buttons for the mode/transfer/redstone/network controls, and compact
  icon-and-value stat rows.
- world edits run through a fake player, so break events fire and land-claim mods
  can veto them. plinths, block entities, and bedrock are never broken.

### config
- knobs for the new systems: resource buffer and throughput scaling, the xp fluid
  list and mb-per-point, break safety (block entities, bedrock, a block blocklist),
  and break-event firing.

## 0.2.1

### added
- directional item transfer: import pulls from neighbours, export drains the plinth
  and its linked partners into neighbours, so a linked import/export pair shifts
  items across a channel.
- filtered-trash void, redstone control modes (always / pause when powered / run
  when powered), ore-tier etching corners.

### changed
- redesigned vanilla-style gui, slimmer plinth model, pick-block keeps the worn
  block variant.
