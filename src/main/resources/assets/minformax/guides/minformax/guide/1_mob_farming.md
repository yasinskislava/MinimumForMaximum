---
item_ids:
  - minformax:scanner
  - minformax:index_inscriber
  - minformax:eternal_generator
  - minformax:memory_shard
  - minformax:chaos_shard
navigation:
  title: 1. Mob Farming & Genetic Indexing
  icon: minformax:memory_shard
---

# Mob Farming & Genetic Indexing

Welcome to the mob farming chapter of your manual. Here, you will learn to scan entities, inscribe their genetic data, and infinitely extract their drops.

---

## 1. The Scanning Phase

To begin automating resource collection from living creatures, you must capture their information out in the field.

* **The Tool:** Craft a <ItemLink id="minformax:scanner" /> to get started.
* **How to Scan:** Equip the scanner and **Right-Click** any living entity.
* **The Master Index:** If the target entity is registered in the config, it will be added directly to your own index records.

<Row>
    <ItemImage id="minformax:scanner" scale="4" />
    <RecipeFor id="minformax:scanner" />
</Row>

> 💡 **Tip:** Want to see your progress? You can review all available mobs and your current unlocked scans at any time in the **Index Menu** by pressing your configured keybind: <KeyBind id="key.minformax.open_index_menu" />.

---

## 2. Managing Data: The Index Inscriber

Once you have gathered genetic information, you need to write them onto a shard. Place down an **Index Inscriber** block to proceed.

<Row>
    <BlockImage id="minformax:index_inscriber" scale="4"/>
    <RecipeFor id="minformax:index_inscriber" />
</Row>

* **Personal Records:** The Index Inscriber acts as a customized laboratory interface unique to your player profile, displaying all entities you have logged.
* **Inscribing Data:** Insert empty shards into the lab device to inscribe the genetic information:
  * Use a <ItemLink id="minformax:memory_shard" /> for standard entities.
  * Use a <ItemLink id="minformax:chaos_shard" /> for bosses.

---

## 3. The Eternal Generator

With a programmed shard ready, you can now craft an **Eternal Generator** to physically simulate mob drops extraction.

<Row>
    <BlockImage id="minformax:eternal_generator" scale="4" />
    <RecipeFor id="minformax:eternal_generator" />
</Row>

### Core Functionality:
* **Simulated Extraction:** Slide your inscribed memory or chaos shard inside to begin generating drops continuously. You can inspect valid outputs via JEI or directly in your Index Lab interface.
* **Power Requirements:** This machine requires an active supply of FE energy from an adjacent power storage block to maintain operation.
* **XP Accumulation:** Beyond item drops, the generator produces experience. Click the **Collect XP** button at the top of the interface to absorb it.

### Logistics & Overload️
The Eternal Generator relies on immediate item extraction.
* **Auto-Export:** The block will automatically export its produced drops to an adjacent storage block (such as a chest).
* **The Overload Rule:** If no adjacent storage is found, or if your container fills up, the machine accumulates **Overload**.
* **1 Overload = 1 Excess Item** that cannot find a home.

> 🧪 *Note: Keep an eye on your overload meters! While it stalls item output, a high overload rating is critical for endgame crafting later on.*

### Sample Setup
Below is a standard layout showing an Eternal Generator powered by adjacent energy, venting drops into a standard chest container.

<GameScene zoom={3} interactive={true}>
    <Block id="minecraft:chest" x="0" y="0" z="0" />
    <Block id="minformax:eternal_generator" x="1" y="0" z="0" />
    <Block id="minformax:creative_energy" x="1" y="0" z="1" />
</GameScene>