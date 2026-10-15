# Barbeque's Delight — Fabric port for Minecraft 26.1

A port of **[Barbeque's Delight](https://modrinth.com/mod/barbeques-delight)** — a
[Farmer's Delight](https://modrinth.com/mod/farmers-delight) addon by
**MaoMao (Aomiaomao)** — from Minecraft 1.20.1 / Fabric / Yarn to **Minecraft 26.1**.

Port version: **`2.0.1` / `fabric+26.1mc`**

> **This is an unofficial port.** All credit for the mod, its design, art and gameplay goes to
> the original author. See [Credits](#credits). The upstream repository is
> [AOMIAOMAO/BarbequesDelight](https://github.com/AOMIAOMAO/BarbequesDelight); this fork adds the
> 26.1 port, and brings the Fabric release to parity with the author's
> [Forge original](https://github.com/Minecraft-LightLand/BarbequesDelight).

Gameplay, mechanics and textures follow the Forge original. The **Forge release is the
reference** for behaviour, because the Fabric 1.1.0 release was missing features and shipped a
few mechanics that did not work; this port implements what Forge does.

---

## Requirements

| Component | Version |
|---|---|
| Minecraft | **26.1, 26.1.1 or 26.1.2** |
| Fabric Loader | 0.19 or newer |
| Fabric API | any 26.1 build |
| Java | 25 or newer |
| **Farmer's Delight Refabricated** | **26.1-3.6.0 or newer — required, not bundled** |
| Jade *(optional)* | any 26.1 build — adds grill tooltips |
| JEI *(optional)* | any 26.1 build — adds the Grilling and Skewering pages |

Farmer's Delight on Fabric for 26.1 is the **Refabricated** fork. It is a hard dependency and is
**not** included in the release jar.

## Install

1. Install Fabric Loader 0.19+ for Minecraft 26.1.x.
2. Put [Fabric API](https://modrinth.com/mod/fabric-api) and
   [Farmer's Delight Refabricated](https://modrinth.com/mod/farmers-delight-refabricated) in `mods/`.
3. Put the release jar from [Releases](../../releases) in `mods/`.
4. Optionally add **Jade** (grill tooltips) and/or **JEI** (recipe pages). Both are soft
   dependencies — the mod runs without them.

One jar covers 26.1, 26.1.1 and 26.1.2.

### Why the version range is what it is

`fabric.mod.json` declares:

```json
"fabricloader": ">=0.19",
"minecraft":    "~26.1",
"fabric-api":   "*",
"java":         ">=25"
```

In Fabric's version-predicate syntax `~X.Y` means `[X.Y, X.(Y+1))`, so `~26.1` covers 26.1,
26.1.1 and 26.1.2. **Do not "tidy" this into `~26.1.2`** — that evaluates to `[26.1.2, 26.2)` and
would reject 26.1 and 26.1.1. Both ranges match what Farmer's Delight Refabricated declares.

---

## Content

Three blocks (grill, ingredients basin, tray), nine raw skewers, nine grilled skewers, six
seasoning powders/sauces, `kebab_wrap`, `kebab_sandwich`, `bibimbap`, `burnt_food`, a butcher
villager trade, and a creative tab.

### Behaviour

- **Grill** — two independent halves. Right-click an **empty** half with a valid ingredient to
  place it. **Sneak-right-click to flip**; a flip is only accepted once the half is at least half
  cooked. Flip it and it cooks into the recipe result; leave it and it becomes `burnt_food`.
  Grilling recipes take priority over vanilla campfire recipes, which are used as a fallback.
  The grill must be **heated** — a heat source below it, or its own mounted campfire.

  While a half is ready to be turned over, a **"sneak right click to flip" hint** appears under
  the crosshair, and Jade shows per-half status (remaining time, *Ready to Flip!*, *Cooked!*,
  *Burnt!!*). Remove the heat and progress **cools back down** towards raw.
- **Ingredients basin** — two halves. Right-click with an ingredient to store it, then hold a
  tool (a stick) in your main hand with an optional garnish in your off hand and right-click to
  skewer. JEI's *Skewering* page spells out the two-hand requirement.
- **Tray** — three display slots, stackable up to three high; `support=true` only for the middle
  tray of a three-high stack. **Sneak = one item, otherwise the whole stack**, in both directions.
- **Seasonings** — three powders (64 uses) and three sauces (16 uses). Seasoning a skewer
  rewrites its food effects:
  - **CUMIN** heals 2 when eaten
  - **PEPPER** halves the eating time
  - **CHILLI** deals 2 fire damage and makes the skewer edible even when full
  - **HONEY_MUSTARD** halves the effect duration and doubles its chance
  - **BUFFALO** deals 2 fire damage, halves the effect duration and raises its amplifier by 1
  - **BARBEQUE** doubles the effect duration

  Skewers can be seasoned on a Farmer's Delight **cutting board**, or directly on the **grill**
  while they are cooking.

### Cooking times

`grillingtime` in the recipe JSON is **per side**. `grilled_beef_skewer` is 360 ticks, so: place
it, and from 180 ticks (9 s) it can be flipped; flip before 360 ticks or it becomes `burnt_food`.

---

## The grill can hold a campfire

The Forge grill carries a `has_campfire` block state: it can be placed **on a lit campfire**, or
a campfire can be **right-clicked onto the grill**. A mounted campfire:

- lights the block (light level 15, like the Forge original),
- renders the grill combined with a campfire model (`block/grill_with_campfire.json`),
- heats the grill on its own, so it cooks without any separate heat source,
- is returned as an item when the grill is broken (the loot table adds a campfire when
  `has_campfire=true`).

Rendering the campfire requires combining two models. The Forge original uses the Forge-only
`forge:composite` model loader, which does not exist in vanilla, so the campfire geometry is
**inlined** into `grill_with_campfire.json` instead. The model is plain vanilla format and needs
no loader.

---

## What the Forge original has that 1.1.0 was missing

Beyond the campfire mount above, the Fabric 1.1.0 release registered three sauces but never gave
them any assets, so they rendered as missing-texture placeholders named after their raw
translation key. The Forge original has always had all six flavours and ships sauce textures, so
this port completes them:

- textures, item models and 26.1 client item definitions,
- translation keys in all four languages (the Chinese names come from the Forge release:
  蜂蜜芥末酱 / 布法罗酱 / 烤肉酱),
- 16 uses each, versus 64 for the powders, matching Forge,
- the Forge flavour effects listed above.

`barbeque_sauce` uses the Forge spelling (`barbeque`, not `barbecue`), which matches the item id
in the Forge original.

**Note:** like the Forge original, the sauces have **no crafting recipe** — they are creative-mode
items. This was confirmed against both the Forge source and the Forge 1.0.6 release.

---

## Mechanics fixed for 2.0.1

Everything below was a real, reproducible difference from the Forge original or from 1.20.1.
Each was found by comparing against the Forge source and 1.20.1 branch, then verified.

### The grill never cooled down

Heat loss ran the timer backwards, but the result was clamped to the *cooking duration* rather
than to zero, so anything at or past that point — a flipped half — stayed frozen once the heat was
removed. Now clamped to zero, matching `ItemEntry#tick` (`time -= 2; if (time <= 0) time = 0`).

### Tray sneak behaviour was inverted

`insert` ignored the sneak flag and always added a **single** item; `take` interpreted sneaking as
"take the whole stack". The Forge original's `StorageTile#insert` / `ClickStorageMethod` mean the
opposite: **sneak = one item, otherwise the whole stack**, for both inserting and taking. Both
sides are now implemented to match, including the "top up a matching slot, otherwise use the first
empty one" order and the stack-size cap.

### The tray and basin changed inventories on the client

Both moved items in `useItemOn`, which runs on **both** sides. The Forge original guards these with
`if (!level.isClientSide())` and only assembles on the server. Without the guard the client both
consumed ingredients and granted a result locally, so results appeared twice and the block showed
stale contents until the next sync. All state changes now happen server-side only.

### The grill would not mount a campfire

`getStateForPlacement` read the block **at the placement position** while the variable was named
`below` — so it only ever saw air, `has_campfire` stayed `false`, and the grill ended up one block
above the fire with no fire of its own. It now reads the block underneath, and `GrillBlockItem`
exists to match the Forge original's item class.

### Client could not find this mod's recipes

Fabric only synchronises recipes whose **serializer** has been declared for sync. Neither of this
mod's serializers was, so the client received **zero** `barbequesdelight:grilling` recipes while
the server had all nine. Every client-side recipe lookup then missed, and vanilla turns a missed
block-interaction prediction into "send `ServerboundUseItemPacket` instead of
`ServerboundUseItemOnPacket`" — so the server never saw the click at all.

Symptoms were: placing a skewer played the sound but nothing appeared, and flipping did nothing.
Both are fixed by registering the serializers with
`RecipeSynchronization.synchronizeRecipeSerializer`.

### Sneak-right-click never reached the grill

Vanilla's `MultiPlayerGameMode.performUseItemOn` deliberately **skips the block interaction**
when the player is sneaking *and* holding an item. That rule exists so sneaking with a block in
hand places it instead of opening a chest; it also meant the grill never heard the click. (The
Forge original is unaffected because Forge rewires the interaction path.)

A client mixin (`GrillFlipMixin`) now intercepts that case and sends an explicit
`FlipGrillPayload`, which the server validates (reach, grill present, half actually ready) and
applies from its own state. The client predicts locally so the skewer turns immediately.

### The flip prompt could never appear

`canFlip` carried a `!level.isClientSide()` guard that the Forge original does not have, so the
client always answered "not ready" — and the client is exactly who asks. It also treated an
unknown duration as "ready" (`0 >= 0`). Both fixed, and the missing HUD hint was ported from the
Forge original's `BBQOverlay` as `GrillFlipOverlay`.

### Grilled items were invisible

The item transform applied the vertical lift *after* rotating 90° about X, which maps local +Y onto
world −Z — so `lift = 0.96` pushed items roughly a block out of the block instead of raising them
onto the grate. The order now matches 1.20.1's renderer exactly: translate (including the lift),
then rotate, then offset, then scale.

### Tray textures were missing

`blockstates/tray.json` pointed its `support=false` variants at
`barbequesdelight:block/rice_roll_medley_block_leftover`, which does not exist, and the tray models
referenced `farmersdelight:block/tray_top`, a sprite Farmer's Delight renamed to `tray`. Both tray
models were also missing their `parent`. The original models were recovered from the upstream git
history (`1.0.9-Preview`) and restored, using `farmersdelight:block/crate_bottom` as upstream does.

### The grill model had missing textures

Twelve `#missing` references, several on visible faces — including the leg tops and the grate rim —
which rendered as the magenta/black checker. All now reference the existing `grill_side` and
`grill_top` sprites.

### JEI showed no recipes at all

26.1 exposes recipes to viewers through `RecipeDisplay`, but JEI still needs a category to lay one
out in, and this port had no JEI plugin. The recipes existed, synced, and produced displays — with
nothing to claim them. `BBQDJeiPlugin` now registers a **Grilling** and a **Skewering** page, and
because each page renders the recipe's own `RecipeDisplay`, it cannot drift from actual behaviour.

Two follow-on problems were fixed with it: JEI distinguishes entries by `getIdentifier`, and
`RecipeDisplay` carries no id, so all nine recipes of a type collapsed into a single page
("1/1") — `JeiDisplayIds` now files each display under its recipe id. And the category captions used
a doubled horizontal offset with slots starting only 1 px below the text, which overlapped.

---

## Changes required by the port

Minecraft 26.1 is the first **unobfuscated** release: Yarn was discontinued and Mojang's official
names are used, `ItemStack` can no longer be created before registries load, and several APIs the
original relied on are gone.

### Block items showed their raw translation key

26.1 resolves an item's translation key **once**, in the `Item` constructor, from a
`DependantName` that defaults to the `item.` prefix. `BlockItem`'s constructor no longer switches
it — it no longer even overrides `getDescriptionId()`, which is now `final` — so vanilla's
`Items.registerBlock` passes `Properties.useBlockDescriptionPrefix()` explicitly.

Without that call the grill, basin and tray rendered as `item.barbequesdelight.grill` etc. in the
creative tab and inventory tooltips. Jade kept working throughout, because it reads the *block's*
name (`block.barbequesdelight.grill`) instead of the item's.

### Crash when right-clicking a grill or basin (`ClassCastException`)

26.1 moved recipe lookup onto `ServerLevel.recipeAccess()`, which returns a `RecipeManager`. A
`ClientLevel` has **no reachable `RecipeManager`** — `ClientRecipeContainer` implements only the
narrower `RecipeAccess` interface, and `ClientLevel.getConnection()` is private.

This matters because block interactions run on **both** sides: the client runs them first to
predict the result. The 1.20.1 original matched recipes on either side because
`World.getRecipeManager()` existed on both, so a direct port that cast `(ServerLevel) level`
hard-crashed the game the moment a player right-clicked a grill or basin.

All lookups now go through `common/util/BBQDRecipesHelper`.

### Other API adaptations

- Yarn → Mojang names throughout; `net.minecraft.resources.Identifier` (no public constructor).
- `FoodProperties` is a record in `net.minecraft.world.food`; status effects live on the
  `minecraft:consumable` component as `ApplyStatusEffectsConsumeEffect`.
- Item flavour data moved from raw NBT to the `minecraft:custom_data` component (same `seasoning`
  key).
- Recipes rewritten for 26.1: `RecipeInput`/`assemble`, `ItemStackTemplate` results,
  `RecipeSerializer` as a `MapCodec` + `StreamCodec` record, and `Recipe#display()` for viewers.
- Datapack folders singularised (`recipe/`, `loot_table/`, `tags/item/`, `tags/block/`) and recipe
  ingredient JSON moved to bare id strings (`"minecraft:beef"`, `"#c:crops/onion"`).
- Convention tags renamed (`c:foods/breads` → `c:foods/bread`, `c:salad_ingredients` →
  `c:foods/vegetable`); alternative ingredients use Fabric's `fabric:any` type.
- Villager trades became data-driven: `TradeOfferHelper` is gone, replaced by
  `data/barbequesdelight/villager_trade/` plus a `minecraft:tags/villager_trade/butcher/level_1`
  tag.
- Block entities save via `ValueInput`/`ValueOutput`; the grill's cooking-progress tag names are
  kept verbatim for save compatibility.
- Block entity renderers use the new `createRenderState`/`extractRenderState`/`submit` split
  (`ItemRenderer` and `RenderType.cutout()` no longer exist; terrain layering is automatic).
- Optional integrations: **Jade** and **JEI** are both implemented as soft dependencies. **EMI**
  was dropped — it has no 26.1 build, and no gameplay depends on it.

---

## Verification

Verified against a running dedicated server, a development client, and a real modpack instance
(215 mods) where noted.

| Check | Result |
|---|---|
| Dedicated server start (26.1 and 26.1.2) | 1855 recipes loaded, no parse errors |
| Client recipe sync | all nine grilling and nine skewering recipes present client-side |
| Grill placed **on a lit campfire** | mounts the campfire: `has_campfire=true`, light level 15 |
| Right-click a campfire onto a bare grill | `has_campfire` becomes `true`, held campfire consumed |
| Grill with a mounted campfire is heated | `isHeated=true` **without any other heat source** |
| Breaking a grill with a campfire | drops both the grill and the campfire |
| Grill **cools** when heat is removed | progress returns to 0 (was frozen before 2.0.1) |
| Grill ← raw skewer | `Success`; the client sees `items=[1 beef_skewer]`, `total=[360,0]` |
| Flip timing | `canFlip` false before half-way, true after; flip applies on the server |
| **Skewer renders on the grate** | visible in-game (was pushed outside the block before 2.0.1) |
| **Sneak-right-click flips** | flips, item turns 180° |
| Flip hint under the crosshair | appears while a half is ready |
| Jade tooltip | per-half item and status: remaining time / *Ready to Flip!* / *Cooked!* / *Burnt!!* |
| Tray insert, not sneaking | whole stack, capped by remaining room |
| Tray insert, sneaking | exactly one item |
| Tray take, not sneaking | whole stack |
| Tray take, sneaking | exactly one item |
| Tray and basin on the client | no local inventory mutation |
| Skewering a real recipe (mushroom + beef + stick) | produces `beef_skewer`, consumes ingredient, garnish and tool |
| Seasoning an item **while it is on the grill** | flavour is applied |
| Sauce / powder durability | sauces `maxDamage=16`, powders `maxDamage=64` |
| Seasoning effect changes | HONEY_MUSTARD 1200→600 ticks at chance 0.5→1.0; BUFFALO 600 ticks at amplifier +1; BARBEQUE 1200→2400 ticks; CHILLI `alwaysEat=true`; PEPPER 1.6s→0.8s |
| JEI plugin | loads; registers 9 grilling and 9 skewering entries under distinct ids |
| Flint & steel on a grill | no crash |
| Models | no "missing model" or "missing texture" warnings for this mod |

### Not verified

- The **JEI page layout** (caption placement, slot spacing) has not been seen rendered — only
  confirmed to load with the right entry counts and unique ids. Please report overlaps.
- The **`grill_with_campfire` model** has been confirmed to load without warnings and the block
  state is correct; its rendered appearance has not been screenshotted.

If you hit a client-only problem, please open an issue.

---

## Notes carried over from upstream

These are pre-existing upstream behaviours, reproduced faithfully. The goal is a faithful port,
not a fork.

### Flint & steel removes the item from the targeted grill half

Using an item that does not match a grilling recipe on an **occupied** half takes that item back
into the player's inventory and returns success — it does not fall through to the item's own use.
This is exactly what `GrillBlock#onUse` does in 1.20.1.

### The basin's halves are addressed "crossed"

Aiming at the visually left half resolves to slot 1 and the visually right half to slot 0. The
mapping formula is copied verbatim from `BlockEntityInv#getSlotForHitting` in 1.20.1, and both
halves behave identically, so this is cosmetic.

---

## Building from source

```bash
./gradlew build          # jar lands in build/libs/
./gradlew runServer      # dev server (needs run/eula.txt: eula=true)
./gradlew runClient      # dev client
```

### Dependencies you must supply

`libs/compile` and `libs/runtime` hold vendored non-Maven dependencies. **They are not committed**
— they are other people's binaries. See [`libs/README.md`](libs/README.md) for exactly what to
download and where to put it. Farmer's Delight Refabricated is required; JEI, Jade and
Architectury are optional.

These jars are `compileOnly`/`localRuntime` only and are **never bundled** into the output jar.

### About the release jar

The jar attached to a release is named for the download, while a local `./gradlew build` produces
`build/libs/barbeques-delight-<version>.jar`. **The contents are identical**; the two files differ
only in ZIP metadata, because a Java jar is not byte-for-byte reproducible by default (each ZIP
entry carries a timestamp).

One thing that *is* kept consistent: all text resources are stored with **LF** line endings, via
[`.gitattributes`](.gitattributes). A Windows checkout would otherwise rewrite them to CRLF and
produce a jar with different resource bytes than the one published here. If you want a build that
matches the release, make sure the working tree has not been converted — `git ls-files --eol`
should report `w/lf` for everything except `gradlew.bat`.

---

## Credits

- **Original mod, design, code and assets:** MaoMao (Aomiaomao) —
  [AOMIAOMAO/BarbequesDelight](https://github.com/AOMIAOMAO/BarbequesDelight),
  [Modrinth](https://modrinth.com/mod/barbeques-delight)
- **Farmer's Delight:** vectorwing and contributors
- **26.1 port:** [Wenjun6326](https://github.com/Wenjun6326)

## License

MIT, following upstream. See [LICENSE](LICENSE).

This repository redistributes **no** third-party binaries: neither Farmer's Delight nor the
optional integration jars are committed or bundled into the release jar.
