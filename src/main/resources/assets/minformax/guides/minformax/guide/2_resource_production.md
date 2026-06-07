---
item_ids:
  - minformax:farmer
  - minformax:fluid_replicator
  - minformax:block_replicator
  - minformax:ore_coalescer
navigation:
  title: 2. Automation, Replicators & Processing
  icon: minformax:ultimate_ingot
---

# Automation, Replicators & Processing

Scale your output beyond mob drops. This chapter details agricultural production, item/fluid replication machines, and ultimate ore processing factory.

---

## 1. Automated Harvesting: The Farmer

The <ItemLink id="minformax:farmer" /> is your primary machine for handling agricultural materials. It removes the need for manual farming.

<Row>
    <BlockImage id="minformax:farmer" scale="4" />
    <RecipeFor id="minformax:farmer" />
</Row>

* **Setup:** Place down the Farmer and connect it to an FE energy source.
* **Configuration:** Insert a seed, crop, or sapling into its slot.
* **Logistics:** Ensure an **adjacent storage unit** is connected to handle your yields. The block will continuously manage the planting and harvesting cycles autonomously. All crops and outputs are fully browsable in JEI.

---

## 2. Streamlined Duplication: Replicators

When you need an infinite supply of a specific construction block or fluid, you can use Replicators. Both the <ItemLink id="minformax:block_replicator" /> and <ItemLink id="minformax:fluid_replicator" /> are minimalist, GUI-less machines designed for high-speed loops.

<Row>
  <BlockImage id="minformax:block_replicator" scale="4" />
  <RecipeFor id="minformax:block_replicator" />
  <BlockImage id="minformax:fluid_replicator" scale="4" />
  <RecipeFor id="minformax:fluid_replicator" />
</Row>

* **How to Insert Blocks:** Hold the desired physical block item in your hand and **Right-Click** the Block Replicator to insert it.
* **How to Inject Fluids:** **Right-Click** the Fluid Replicator with a filled fluid bucket or container to lock the liquid type.
* **Output Logistics:** Because these blocks have no inventory screens, they **require an adjacent storage block** to operate. They will pass outputs instantly. Review JEI to see all compatible items and fluids.

---

## 3. High-Tier Processing: The Ore Coalescer

The <ItemLink id="minformax:ore_coalescer" /> represents the ultimate refinery factory for raw mineral processing.
<Row>
    <BlockImage id="minformax:ore_coalescer" scale="4" />
    <RecipeFor id="minformax:ore_coalescer" />
</Row>
* **Block Ores Only:** The machine **only accepts block ores** (e.g., Diamond Ore, Iron Ore Blocks). It is incompatible with raw ore variants.
* **High Capacity:** Built with expanded internal inventory slot capacity to handle industrial-grade outputs.
* **Power:** Requires a continuous feed of FE energy to operate.

---

### Factory Floor Example
This setup displays an Ore Coalescer, a Block Replicator, and an automated Farmer venting drop into a chest simultaneously.

<GameScene zoom={3.5} interactive={true}>
    <Block id="minecraft:chest" x="0" y="0" z="0" />
    <Block id="minformax:ore_coalescer" x="0" y="0" z="1" />
    <Block id="minformax:block_replicator" x="-1" y="0" z="0" />
    <Block id="minformax:farmer" x="1" y="0" z="0" />
</GameScene>