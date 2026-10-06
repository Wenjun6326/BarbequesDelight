# Barbeque's Delight — Fabric port for Minecraft 26.1

A port of **[Barbeque's Delight](https://modrinth.com/mod/barbeques-delight)** 1.1.0 — a
[Farmer's Delight](https://modrinth.com/mod/farmers-delight) addon by
**MaoMao (Aomiaomao)** — from Minecraft 1.20.1 / Fabric / Yarn to **Minecraft 26.1**.

Ported version: **`fabric+26.1mc`**

> **This is an unofficial port.** All credit for the mod, its design, art and gameplay goes to
> the original author. See [Credits](#credits). The upstream repository is
> [AOMIAOMAO/BarbequesDelight](https://github.com/AOMIAOMAO/BarbequesDelight); this fork only
> adds the 26.1 port.

Gameplay, mechanics and textures are unchanged from 1.1.0. This is a port, not a redesign.

---

## Requirements

| Component | Version |
|---|---|
| Minecraft | **26.1, 26.1.1 or 26.1.2** |
| Fabric Loader | 0.19 or newer |
| Fabric API | any 26.1 build |
| Java | 25 or newer |
| **Farmer's Delight Refabricated** | **26.1-3.6.0 or newer — required, not bundled** |

Farmer's Delight on Fabric for 26.1 is the **Refabricated** fork. It is a hard dependency and is
**not** included in the release jar.

## Install

1. Install Fabric Loader 0.19+ for Minecraft 26.1.x.
2. Put [Fabric API](https://modrinth.com/mod/fabric-api) and
   [Farmer's Delight Refabricated](https://modrinth.com/mod/farmers-delight-refabricated) in `mods/`.
3. Put the release jar from [Releases](../../releases) in `mods/`.
4. Optionally add [REI](https://modrinth.com/mod/rei) to see the grilling and skewering recipes.

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
villager trade, a creative tab, and REI categories.

### Behaviour

- **Grill** — two independent halves. Right-click an **empty** half with a valid ingredient to
  place it. Sneak-click to **flip**; a flip is only accepted once the half is at least half
  cooked. Flip it and it cooks into the recipe result; leave it and it becomes `burnt_food`.
  Grilling recipes take priority over vanilla campfire recipes, which are used as a fallback
  (`cookingTime - 200`). The grill must be **heated** — a heat source below it, such as a
  campfire.
- **Ingredients basin** — two halves. Right-click with an ingredient to store it, then hold a
  tool (a stick) in your main hand and an optional garnish in your off hand and right-click to
  skewer.
- **Tray** — three display slots, stackable up to three high; `support=true` only for the middle
  tray of a three-high stack.
- **Seasonings** — CUMIN heals 2, PEPPER halves eating time, CHILLI deals 2 fire damage and
  restores 2 hunger, BUFFALO halves effect durations and adds +1 amplifier. Seasonings are
  applied to a skewer on a Farmer's Delight cutting board.

---

## Changes required by the port

Minecraft 26.1 is the first **unobfuscated** release: Yarn was discontinued and Mojang's official
names are used, `ItemStack` can no longer be created before registries load, and several APIs the
original relied on are gone. The notable adaptations, plus the fixes below, are listed here.
Two of them were real bugs that made the mod unplayable; both are fixed.

### Fixed: block items showed their raw translation key

26.1 resolves an item's translation key **once**, in the `Item` constructor, from a
`DependantName` that defaults to the `item.` prefix. `BlockItem`'s constructor no longer switches
it — it no longer even overrides `getDescriptionId()`, which is now `final` — so vanilla's
`Items.registerBlock` passes `Properties.useBlockDescriptionPrefix()` explicitly.

Without that call the grill, basin and tray rendered as `item.barbequesdelight.grill` etc. in the
creative tab and inventory tooltips. Jade kept working throughout, because it reads the *block's*
name (`block.barbequesdelight.grill`) instead of the item's.

### Fixed: crash when right-clicking a grill or basin (`ClassCastException`)

26.1 moved recipe lookup onto `ServerLevel.recipeAccess()`, which returns a `RecipeManager`. A
`ClientLevel` has **no reachable `RecipeManager`** — `ClientRecipeContainer` implements only the
narrower `RecipeAccess` interface, and `ClientLevel.getConnection()` is private.

This matters because block interactions (`useItemOn` / `useWithoutItem`) run on **both** sides:
the client runs them first to predict the result. The 1.20.1 original matched recipes on either
side because `World.getRecipeManager()` existed on both, so a direct port that cast
`(ServerLevel) level` threw `ClassCastException` and hard-crashed the game the moment a player
right-clicked a grill or basin — for example with flint and steel.

All lookups now go through `common/util/BBQDRecipesHelper`.

### Fixed: client could no longer predict recipe-based interactions

Fixing only the crash left a subtler problem: with no `RecipeManager` on the client, **every**
recipe-dependent interaction answered `PASS` client-side. The server still performed the action
(it always re-runs the interaction authoritatively), but the client stopped predicting placing an
item on the grill or skewering in the basin — a regression against 1.20.1.

The client recipe source is restored through Fabric API's `ClientRecipeSynchronizedEvent`: the
synced `SynchronizedRecipes` is stored and used on the client, falling back to the server's
`RecipeManager` when available. Both sides reach the same answer, so prediction matches the
authoritative result. The client branch is wrapped defensively — it is only prediction, so any
throwable degrades to "no match" rather than breaking the interaction.

### Other API adaptations

- Yarn → Mojang names throughout; `net.minecraft.resources.Identifier` (no public constructor).
- `FoodProperties` moved to `net.minecraft.world.food` and lost `statusEffect`; status effects now
  live on the `minecraft:consumable` component as `ApplyStatusEffectsConsumeEffect`.
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
- Optional integrations: **REI** is included (categories + workstations). **EMI** had to be
  dropped — it has no 26.1 build, and it is a soft dependency with no gameplay impact. **Jade** was
  not ported for the same reason it adds only tooltips.

---

## Verification

Verified against a running dedicated server and, where noted, a real client.

| Check | Result |
|---|---|
| Dedicated server start (26.1 and 26.1.2) | 1855 recipes loaded, no parse errors |
| Grill placed **on a lit campfire** | accepted; block entity created |
| Grill on a campfire is heated | `isHeated=true` (campfires are heat sources via `#minecraft:campfires`) |
| Basin ← item, then empty hand | stored in a half, taken back out |
| Tray ← item | stored in slot 0 |
| Grill ← raw skewer (cold and on a campfire) | placed in a half; only the heated one cooks |
| Flip timing | refused before the half-way point, accepted after; result `grilled_beef_skewer` |
| No flip | burns into `burnt_food` |
| Skewering a real recipe (mushroom + beef + stick) | produces `beef_skewer`, consumes ingredient, garnish and tool |
| Flint & steel on a grill | no crash |
| Client interaction path | no crash; `useItemOn` reaches the block on a `ClientLevel` |

### Not verified

The **client recipe prediction fix** (the `ClientRecipeSynchronizedEvent` integration) could not
be confirmed on a live client: the build machine's dev client stopped completing startup (it
hangs after the texture atlases, render thread spinning, no crash report — reproducible across
four runs and unrelated to this mod, since the probes were never reached). The server-side path,
the crash fix and every behaviour in the table above *were* verified. If you hit a client-only
problem, please open an issue.

---

## Notes carried over from upstream

These are pre-existing upstream behaviours, reproduced faithfully rather than fixed. The goal is a
faithful port, not a fork. **Nothing here was "repaired" on purpose.**

### Three unfinished sauce items

`honey_mustard_sauce`, `buffalo_sauce` and `barbecue_sauce` exist only in the author's development
snapshot; they are **not** in the released 1.1.0 build. Upstream registers them but never shipped
any asset or recipe: no texture, no item model, no crafting recipe, no `.tooltip` / `.title`
translation key (only `item.barbequesdelight.flavor.*` exists).

They therefore appear in the creative tab as missing-texture placeholders named after their raw
translation key and cannot be obtained in survival. This is deliberate — do not "fix" it locally,
so an upstream update can be merged without conflicts.

### Tray consumes the whole held stack

`TrayBlock.handleUse` stores via `tray.setItem(i, stack.split(stack.getCount()))`, while
`BlockEntityInv.setItem` truncates what it stores to a single item. Right-clicking a tray with a
stack therefore stores 1 item and discards the rest. Present verbatim in 1.20.1
(`TrayBlock.java:45` + `BlockEntityInv.setStack`), so it was left alone.

### Flint & steel removes the item from the targeted grill half

Using an item that does not match a grilling recipe on an **occupied** half takes that item back
into the player's inventory and returns success — it does not fall through to the item's own use.
This is exactly what `GrillBlock#onUse` does in 1.20.1 (`GrillBlock.java:125`).

### The basin's halves are addressed "crossed"

Aiming at the visually left half resolves to slot 1 and the visually right half to slot 0. The
mapping formula is copied verbatim from `BlockEntityInv#getSlotForHitting` in 1.20.1, and both
halves behave identically, so this is cosmetic.

### Other upstream issues

- `models/block/tray.json` does not exist and `blockstates/tray.json` has `support=false` variants
  pointing at unrelated/missing Farmer's Delight models.
- The grill model has an unresolved `#missing` texture reference.
- `ko_kr.json` was missing `block.barbequesdelight.tray`. One line was added to this port
  (`"쟁반"`); it is a pure asset addition.

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
download and where to put it. Farmer's Delight Refabricated is required; REI, Jade and
Architectury are optional.

These jars are `compileOnly`/`localRuntime` only and are **never bundled** into the output jar.

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
