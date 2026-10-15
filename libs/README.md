# libs/ — vendored dependencies (not committed)

The jars in this directory are **other people's builds** and are deliberately
**not** committed to this repository. You must place them here yourself before
building.

The build expects two directories:

```
libs/compile/   # compiled against, never bundled into the output jar
libs/runtime/   # only added to the dev-run classpath
```

Farmer's Delight must be in both. The optional integration jars only need to be
in `libs/compile` — they are `compileOnly`, so a dev run picks them up from the
mods folder instead.

## What to download

| File | Where to get it | |
|---|---|---|
| `FarmersDelight-26.1-3.6.26+refabricated.jar` | [Farmer's Delight Refabricated on Modrinth](https://modrinth.com/mod/farmers-delight-refabricated) — a **26.1** Fabric build | **Required** |
| `Jade-mc26.1-Fabric-26.1.11.jar` | [Jade on Modrinth](https://modrinth.com/mod/jade) | Optional |
| `jei-26.1.2-fabric-29.16.0.47.jar` | [JEI on Modrinth](https://modrinth.com/mod/jei) — a **26.1 Fabric** build | Optional |

Version numbers in the file names only need to match loosely — `build.gradle`
picks up whatever `*.jar` files it finds in those directories. If you use
different versions, prefer 26.1-compatible builds.

## Running without the optional integrations

Farmer's Delight is the only hard dependency. You can build with just that jar
present: the Jade and JEI classes are referenced only from their own packages,
which are loaded through the `jade` and `jei_mod_plugin` entrypoints and are
therefore never touched when those mods are absent. Both are declared under
`suggests` in `fabric.mod.json`.

Neither integration jar is ever bundled into the output jar — `mezz/*` and
`snownee/*` must stay out of it, and the build is arranged so they do.

## Note on REI

Earlier versions of this port shipped a RoughlyEnoughItems integration. It has
been removed: REI has no 26.1 build, and the recipe pages it would have provided
are now implemented for JEI. Nothing in the mod depends on REI or Architectury
any more.
