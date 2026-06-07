---
item_ids:
  - minformax:configuration_tool
  - minformax:speed_upgrade_tier1
  - minformax:ultimate_speed_upgrade
  - minformax:inverted_upgrade
  - minformax:auto_smelting_upgrade
  - minformax:watering_upgrade
  - minformax:compressing_upgrade
  - minformax:processing_upgrade_tier2
  - minformax:ultimate_processing_upgrade
  - minformax:fortune_upgrade_tier3
  - minformax:extra_drop_upgrade_tier4
navigation:
  title: 3. Machine Networks & Upgrades
  icon: minformax:configuration_tool
---

# Machine Networks & Upgrades

Running pipes or individual chests to every machine can quickly clutter your workspace. This section outlines how to link machines together using relays and tweak their performance with upgrades.

---

## 1. Machine Relay Networks

Every machine in this mod (excluding the Index Inscriber) has an automatic extraction protocol. If a machine doesn't see a chest right next to it, it checks for an **adjacent MinForMax machine**. If found, it treats that machine as a **logistics relay**!

### Scaling Production
Items will move down the network chain until they hit a valid chest. For example, you can stack 3 **Eternal Generators** vertically with a single chest sitting at the very top. All 3 units will pass items upwards through each other directly into that single chest!

<GameScene zoom={3}>
    <Block id="minecraft:chest" x="0" y="3" z="0" />
    <Block id="minformax:eternal_generator" x="0" y="2" z="0" />
    <Block id="minformax:eternal_generator" x="0" y="1" z="0" />
    <Block id="minformax:eternal_generator" x="0" y="0" z="0" />
</GameScene>

---

## 2. Managing Faces: Configuration Tool

Managing where items enter and exit is governed by your side configuration profiles. To duplicate these settings across your factory floor, use the <ItemLink id="minformax:configuration_tool" />.

<RecipeFor id="minformax:configuration_tool" />

* **Copy Settings:** Press **Shift + Right-Click** on a machine to save its face configuration.
* **Paste Settings:** **Right-Click** on any other machine to instantly paste those settings.

---

## 3. Upgrades

Tailor your infrastructure using upgrade items.

| Upgrade | Effect | Machine Compatibility |
| :--- | :--- | :--- |
| **Speed (Tiers 1-4)** <ItemImage id="minformax:speed_upgrade_tier1"/> | Multiplies speed: Tier 1 (2x), Tier 2 (4x), Tier 3 (8x), Tier 4 (16x). | All Automation Machines |
| **Ultimate Speed** <ItemImage id="minformax:ultimate_speed_upgrade"/> | Grants **9999x speed** and **removes energy costs** entirely. | All Automation Machines |
| **Processing (Tiers 1-4)** <ItemImage id="minformax:processing_upgrade_tier2"/> | Multiplies operations per tick: 4x, 16x, 64x, or 256x operations. | All Automation Machines |
| **Ultimate Processing** <ItemImage id="minformax:ultimate_processing_upgrade"/> | Instantly runs **9999x parallel operations**. Still requires energy. | **Ore Coalescer Only** |
| **Fortune (Tiers 1-4)** <ItemImage id="minformax:fortune_upgrade_tier3"/> | Increases drop yield rates up to 10x. | Ore Coalescer & Farmer |
| **Extra Drop (Tiers 1-4)** <ItemImage id="minformax:extra_drop_upgrade_tier4"/> | Increases your chances of getting additional drops. | Eternal Generator Only |

### Advanced Custom Modules
* <ItemLink id="minformax:inverted_upgrade" />: Swaps main and extra drop pools. Your machine will generate the extra drop as its primary output, and Extra Drop Upgrades will increase the yield of the main drop instead. *(Eternal Generator Only)*
* <ItemLink id="minformax:auto_smelting_upgrade" />: Instantly melts mined block ores down into processed resources. *(Ore Coalescer Only)*
* <ItemLink id="minformax:watering_upgrade" />: Triggers farmer to randomly boost growth speeds. *(Farmer Only)*
* <ItemLink id="minformax:compressing_upgrade" />: Processes *Mystical Agriculture* into final resources. *(Farmer Only)*
