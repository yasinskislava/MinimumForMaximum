---
item_ids:
  - minecraft:book
navigation:
  title: GuideMe Markdown Template & Tutorial
---

# GuideMe Syntax Reference Template

This file acts as a template cheat-sheet for formatting elements inside the GuideMe engine. You can look up code patterns for recipes, game scenes, entity placement, and custom highlights here.

---

## 1. Core Text Formatting & Widgets

### Player Context Strings
Greet your reader dynamically based on their current profile username:
Welcome back, <PlayerName />!

### Hotkey/Keybinding Prompts
Display localized user keybind settings automatically:
- Menu Hotkey: <KeyBind id="key.minformax.open_index_menu" />
- Default Jump: <KeyBind id="key.jump" />

### Inline Custom Coloring
Color parts of a sentence using raw Hex notation or a theme color string defined in `guide.json`:
- Direct Hex Code: This text is <Color color="#ff0000">Danger Crimson Red</Color>!
- Json Color Config Identifier: This text utilizes a <Color id="test_color">Theme Color</Color>!

### Dynamic In-Game Hyperlinks
Add clickable links that run text commands inside the server terminal:
<CommandLink command="/give @s minformax:ultimate_ingot 1" title="Click to get an ingot!" close={true}>Get Ingot</CommandLink>

---

## 2. Materials & Recipe Cards

### Inline Visual Links
Embed small item icons directly into normal text paragraphs:
- Custom properties variant: <ItemLink id="minecraft:stick" components="rarity=epic" />
- Glint override variant: <ItemImage id="minecraft:stone" components="enchantment_glint_override=true" />

### Multi-Item Horizontal Layouts (Rows)
Keep things tidy by placing graphics horizontally side-by-side inside a `<Row>` block:
<Row>
  <BlockImage id="minformax:block_replicator" scale="2" />
  <BlockImage id="minformax:fluid_replicator" scale="2" />
  <BlockImage id="minformax:ore_coalescer" scale="2" />
</Row>

### Recipe Displayer Engines
GuideMe features automated recipe indexing using specific tags:

#### Standard Recipe Card Lookup
Renders an exact cooking, smelting, or custom recipe entry directly:
<Recipe id="minecraft:iron_nugget_from_blasting" />

#### Item Crafting Resolution (RecipeFor)
Looks up the primary recipe used to manufacture an item:
<RecipeFor id="minformax:scanner" />

#### Alternative Variant Indexing (RecipesFor)
Lists all alternative paths when an item has more than one recipe pattern:
<RecipesFor id="minecraft:green_bed" />

#### Fallback Strings
If a recipe might be disabled in custom modpacks, declare an inline warning fallback string:
<Recipe id="disabled_recipe" fallbackText="This component's recipe is currently disabled." />

---

## 3. Interactive 3D Scenes (`<GameScene>`)

The GameScene tool loads individual blocks, entities, or complete `.nbt` schematic files directly onto your documentation pages.

### Method A: Manual Grid Coordinates
Build short visual layout setups manually by declaring individual `x`, `y`, and `z` offset parameters:

<GameScene interactive="{true}" zoom="{4}">
    <Entity data="{Color: 2}" id="minecraft:sheep" x="0" y="1" z="0"/>
    <Block id="minecraft:grass_block" x="0" y="0" z="0"/>
    <Block id="minecraft:water" x="1" y="0" z="0"/>
    <Block id="minecraft:water" x="-1" y="0" z="0"/>
</GameScene>