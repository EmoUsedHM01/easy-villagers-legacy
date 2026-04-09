<!-- modrinth_exclude.start -->

# Easy Villagers Legacy

A 1.7.10 backport of [Easy Villagers](https://github.com/henkelmax/easy-villagers) by Max Henkel, designed for [GT New Horizons](https://www.gtnewhorizons.com/).

## Links

- [Original Mod - Modrinth](https://modrinth.com/mod/easy-villagers)
- [Original Mod - CurseForge](https://www.curseforge.com/minecraft/mc-mods/easy-villagers)
- [Credits](https://github.com/henkelmax/easy-villagers)

---

<!-- modrinth_exclude.end -->

This mod lets you pick up villagers as an item.
There are also blocks that substitute every farm that includes villagers.

## The Villager Item

The villager item can be obtained by sneaking and right-clicking a villager.

The villager item can be placed by right-clicking on the ground.

## The Trader Block

The trader block allows trading with the villager without having to deal with securing the villager.
This block also allows the villager to restock in non-working hours.
The restocking time of the villager is not dependent on any external sources like day/night time or dimension.
You can customize the trading interval in the config.

## The Farmer Block

The farmer block contains a whole crop farm in a single block. This helps to reduce lag and save space.

## The Breeder Block

The breeder block produces villagers by putting in food.
This helps to reduce lag, save space and also a lot of frustration.

## The Iron Farm Block

The iron farm block contains a complete iron farm in a single block.
It produces one iron golem every four minutes.
This averages about one iron ingot per minute.

---

You can take stuff out of these blocks by sneaking and right-clicking the block with an empty hand.

Baby villagers are able to grow up inside of blocks, but the functionality of the blocks will not work until they are grown.

All blocks keep their stuff inside if they are broken.

## Changes from the Original Mod

This backport targets Minecraft 1.7.10 with Forge 10.13.4.1614. The following changes have been made compared to the original Easy Villagers:

### Removed Features

- **Auto Trader Block** - Not implemented in this version.
- **Converter Block** - Not implemented in this version.
- **Incubator Block** - Not implemented in this version.
- **Inventory Viewer Block** - Not implemented in this version.
- **Trade Cycle Keybind** - The `V` pickup key and `C` cycle key are not available; use sneak + right-click to pick up villagers and the GUI button to cycle trades.

### Changed Features

- **Breeder Block** - Produces adult villagers instead of babies. Produced villagers are assigned a random profession from all registered professions, including modded ones (e.g. GTNH-added professions).
- **Farmer Villager Straw Hat** - Farmer villagers (profession 0) display a 1.14-style straw hat. This appears on farmers inside blocks, on the villager item icon, on block item icons, and on farmer villager entities in the world.
- **Block Placement Orientation** - Each block type has a per-block facing offset so that the front face and internal entities align correctly with the player's facing direction when placed.
- **Custom Item Renderers** - Villager items render a profession-specific 3D villager model in the inventory and when held. Block items render a miniature display case with the appropriate entities inside.
- **In-Game Config GUI** - All configuration options are editable through the Forge in-game mod config menu, with changes saved immediately.

### Technical Details

- Built with RetroFuturaGradle for GTNH compatibility.
- Uses GL immediate mode for block item case rendering (Tessellator is unreliable in `IItemRenderer` context on 1.7.10).
- Straw hat uses `ModelRenderer` with the 1.14 farmer texture UV layout, rendered as a child of the head model for correct positioning and rotation.
- `RenderLivingEvent.Post` is used to overlay the straw hat on farmer villager entities in the world, with proper body yaw and head pitch/yaw interpolation.
- `VillagerRegistry.getRegisteredVillagers()` is used to include all modded professions in the breeder's random profession pool.
