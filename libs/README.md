# libs/ — vendored dependencies (not committed)

The jars in this directory are **other people's builds** and are deliberately
**not** committed to this repository. You must place them here yourself before
building.

The build expects two directories:

```
libs/compile/   # compiled against, never bundled into the output jar
libs/runtime/   # only added to the dev-run classpath
```

The same four jars go into both directories.

## What to download

| File | Where to get it |
|---|---|
| `FarmersDelight-26.1-3.6.26+refabricated.jar` | [Farmer's Delight Refabricated on Modrinth](https://modrinth.com/mod/farmers-delight-refabricated) — pick a **26.1** Fabric build. **Required.** |
| `RoughlyEnoughItems-api-fabric-26.1.819.jar` | [REI on CurseForge](https://www.curseforge.com/minecraft/mc-mods/roughly-enough-items) / [Modrinth](https://modrinth.com/mod/rei) — the **API** jar. Optional integration. |
| `RoughlyEnoughItems-26.1.819.jar` | The full REI jar from the same download. Optional integration. |
| `Jade-mc26.1-Fabric-26.1.11.jar` | [Jade on Modrinth](https://modrinth.com/mod/jade). Optional integration. |
| `architectury-fabric-20.1.16.jar` | [Architectury API on Modrinth](https://modrinth.com/mod/architectury-api). Needed by REI at runtime. |

Version numbers in the file names only need to match loosely — `build.gradle`
picks up whatever `*.jar` files it finds in those directories. If you use
different versions, prefer 26.1-compatible builds.

## Running without the optional integrations

Farmer's Delight is the only hard dependency. If you only want the mod itself,
you can build with just the Farmer's Delight jar present; the REI and Jade code
is guarded and the integrations are soft dependencies.

`runClient` additionally needs Architectury's math library, which the vendored
`flatDir` jars do not pull in transitively. If `runClient` fails with
`NoClassDefFoundError: me/shedaniel/math/Point`, move
`libs/runtime/RoughlyEnoughItems-*.jar` and `libs/runtime/architectury-*.jar`
aside — REI is build-time-only for this mod, so the shipped jar is unaffected.
