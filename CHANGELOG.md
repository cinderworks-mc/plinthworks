# changelog

notable changes to plinthworks. loosely follows keep a changelog.

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
