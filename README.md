# Minimum For Maximum

This is a Minecraft mod that introduces new ways to generate resources.

## Customizing Drop Data

Modpack developers can customize the drop data for both mobs and modules by editing the `drops.json` file located in the `config/minformax/` directory. If the file does not exist, it will be created with the default values when the game is launched.

### Mob Drops

Mob drops are defined under the `mob_drops` key. Each entry consists of a mob's entity ID followed by its drop data.

**Example:**

```json
{
  "mob_drops": {
    "entity.minecraft.cow": {
      "main_drop": "minecraft:beef",
      "additional_drop": [
        "minecraft:leather"
      ],
      "xp": 3,
      "duration": 256
    }
  }
}
```

-   `main_drop`: The primary item dropped by the mob.
-   `additional_drop`: A list of additional items that may be dropped.
-   `xp`: The amount of experience points dropped.
-   `duration`: The time it takes to generate the drops.

### Module Drops

Module drops are defined under the `module_drops` key. Each entry consists of a module's item ID followed by its drop data.

**Example:**

```json
{
  "module_drops": {
    "minformax:farmer_module": {
      "allowed_items": [],
      "prohibited_items": ["minecraft:torchflower_seeds"],
      "allowed_tags": ["c:seeds"],
      "prohibited_tags": [],
      "xp": 0
    }
  }
}
```

-   `allowed_items`: A list of specific item IDs that are allowed to be dropped.
-   `prohibited_items`: A list of specific item IDs that are not allowed to be dropped.
-   `allowed_tags`: A list of item tags that are allowed to be dropped.
-   `prohibited_tags`: A list of item tags that are not allowed to be dropped.
-   `xp`: The amount of experience points gained from the module.

### Block Replicator Data

Block replicator data is defined under the `block_replicator_data` key. Each entry consists of a block's ID followed by its replication data.

**Example:**

```json
{
  "block_replicator_data": {
    "minecraft:cobblestone": {
      "duration": 100,
      "energy_multiplier": 1,
      "basic_amount_generated": 1
    }
  }
}
```

-   `duration`: The base time it takes to replicate the block.
-   `energy_multiplier`: A multiplier for the energy cost of replication.
-   `basic_amount_generated`: The base amount of the block generated per cycle.

### Fluid Replicator Data

Fluid replicator data is defined under the `fluid_replicator_data` key. Each entry consists of a fluid's ID followed by its replication data.

**Example:**

```json
{
  "fluid_replicator_data": {
    "minecraft:water": {
      "duration": 100,
      "energy_multiplier": 1,
      "basic_amount_generated": 100
    }
  }
}
```

-   `duration`: The base time it takes to replicate the fluid.
-   `energy_multiplier`: A multiplier for the energy cost of replication.
-   `basic_amount_generated`: The base amount of the fluid generated per cycle (in millibuckets).

To customize the drops, simply modify the values in the `drops.json` file. The changes will be applied the next time the game is launched.
