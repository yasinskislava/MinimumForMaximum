---
item_ids:
  - minformax:quantum_ingot
  - minformax:quantum_foam
  - minformax:linker
  - minformax:pandora_box
  - minformax:babylon_key
  - minformax:void_ingot
  - minformax:aether_ingot
  - minformax:sakuradite_casing
  - minformax:sakuradite_panel
  - minformax:sakuradite_input_hatch
  - minformax:sakuradite_output_hatch
navigation:
  title: Advanced Materials, Pandora Box & Babylon
  icon: minformax:babylon_key
---

# Advanced Materials, Pandora Box & Babylon

Welcome to the apex tier of your journey. This final chapter covers sub-atomic alloy transmutations, siphon generators, and localized tick acceleration fields.

---

## 1. Quantum Metallurgy

To create advanced materials, you must master the behaviors of <ItemLink id="minformax:quantum_foam" />.

* **Void Ingot:** Take your raw Quantum Foam to **The End** dimension and throw the physical item directly out into the void. It will condense into a <ItemLink id="minformax:void_ingot" />.
* **Aether Ingot:** Take Quantum Foam to the **Overworld** and throw it upward at an altitude of **Y = 1000 or higher**. It will crystallize into an <ItemLink id="minformax:aether_ingot" />.

### Cultivating Quantum Foam
To make foam, you must intentionally overload an active system:
1. Hold a <ItemLink id="minformax:quantum_ingot" /> in your main hand.
2. Find an active **Eternal Generator** that has accumulated **5120 or more Overload** units inside its buffer.
3. **Shift + Right-Click** the generator. This vents the overload and converts your ingot into Quantum Foam.

---

## 2. Endgame Power: The Pandora Box

The **Pandora Box** is an advanced generator that creates massive amounts of power by siphoning the stress (Overload) of remote machinery.
<GameScene zoom={6} interactive={true}>
    <Block id="minformax:pandora_box"/>
</GameScene>
### Setting Up a Siphon Link:
1. Craft a wireless <ItemLink id="minformax:linker" />.
2. Target your remote Eternal Generators and press **Shift + Right-Click** to link or unlink them. Bound machines will highlight when you hold the tool.
3. Place the configured Linker item into the controller slot inside the Pandora Box interface.
4. Every **60 seconds (1 minute)**, the core will scan all linked generators and gather their overload.

> ⚡ **The Overload Energy Ratio:** The box outputs **1 FE for every 1 unit of Overload** tracked. Best of all, **Overload is never consumed** during this harvest cycle—it is simply read!

* **XP Multiplier:** Click the top interface button to deposit your own player levels. This permanently applies a production multiplier to all energy generated.

---

## 3. Reality Distortion: Gate Of Babylon

The **Gate Of Babylon** is an advanced multiblock structure capable of multiplying the tick speeds of everything enclosed within its boundary from **2x up to 256x**.

### Construction Rules:
* **Dimensions:** Minimum size of 3x3x3 blocks. Supports a default configuration maximum of 256x256x256 blocks to prevent server performance lag.
* **The Frame:** Construct all edges and corners using <ItemLink id="minformax:sakuradite_casing" />.
* **The Facade:** Fill all remaining walls and flat faces completely using any Sakuradite blocks (such as <ItemLink id="minformax:sakuradite_panel" />).
* **Power Injection:** Integrate <ItemLink id="minformax:sakuradite_input_hatch" /> into the wall casing to accept FE power.
* **Power Extraction:** Integrate <ItemLink id="minformax:sakuradite_output_hatch" /> into the wall casing to extract FE power.

<Row>
    <BlockImage id="minformax:sakuradite_casing" scale="4"/>
    <BlockImage id="minformax:sakuradite_panel" scale="4"/>
    <BlockImage id="minformax:sakuradite_input_hatch" scale="4"/>
    <BlockImage id="minformax:sakuradite_output_hatch" scale="4"/>
    <ItemImage id="minformax:babylon_key" scale="5" />
</Row>

### Ignition Protocol:
Once fully built, take your <ItemLink id="minformax:babylon_key" /> and **Right-Click** the framework to lock the structure. Once active, it consumes energy, tick-accelerates everything inside its boundaries, and grants **Creative-style Free Flight** to any players within its field.

<GameScene zoom={2} interactive={true}>
    <ImportStructure src="./gate_of_babylon.nbt" />
    <BlockAnnotationTemplate id="minformax:sakuradite_input_hatch">
        <DiamondAnnotation pos="0.5 0.5 0.5" color="#00ff00">
            Connect high-tier power cables here to supply the distortion matrix.
        </DiamondAnnotation>
    </BlockAnnotationTemplate>
    <BlockAnnotationTemplate id="minformax:sakuradite_output_hatch">
        <DiamondAnnotation pos="0.5 0.5 0.5" color="#0000cd">
            Sakuradite Hatches are phantom, so they can be utilized as entrance and exit points for the player.
        </DiamondAnnotation>
    </BlockAnnotationTemplate>
</GameScene>