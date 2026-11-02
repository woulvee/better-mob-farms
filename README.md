# BetterMobFarms

A Minecraft mod with blocks and items for building better mob farms: spikes that hurt mobs, fans that push them, traps, soap, and a way to grow spawn eggs from mob DNA.

Built for **Minecraft 26.1.2** with **NeoForge 26.1.2.x**.

## Features

- **Spikes:** Iron, Diamond Tipped, Netherite Coated, Hot Netherite Coated and Ice Spikes, plus Punji Sticks. Ice Spikes slowly freeze whatever walks on them.
- **Fan:** A redstone-powered fan that pushes mobs. Upgrade items change its width, height, length and power.
- **Bear Trap:** Holds mobs and players in place for 3 seconds. Bosses are immune.
- **Soap:** Soap bars make entities slippery and stop spiders from climbing. Use them directly on an entity or spread a layer on floors and walls. To make one, add bone meal to a water cauldron to get lye, then throw in rotten flesh and leather.
- **Syringes:** Take DNA from mobs and players, fill them with potions and inject those into mobs, or use them on yourself while sneaking.
- **Incubator:** Turns a DNA syringe, an egg and liquid nutrients into the spawn egg of that mob.
- **Feeding Trough:** Lures livestock and feeds them from its inventory so they breed on their own.
- **Scarecrow:** Keeps pests and phantoms away. Give it a player head and it lures hostile mobs instead.

Most ranges and timings can be changed in the mod config. Modpack authors can adjust which mobs are affected through entity tags, such as `bettermobfarms:bear_trap_immune`, `bettermobfarms:livestock`, `bettermobfarms:scarecrow_pests` and `bettermobfarms:scarecrow_lure_immune`.

## Building from source

The build needs a Java 25 toolchain, which Gradle downloads automatically.

```bash
./gradlew build
```

The finished jar is written to `build/libs/`. Use `./gradlew runClient` to start a development client.

## License

BetterMobFarms is licensed under the [MIT License](LICENSE). The files that come from the NeoForged MDK template keep their own MIT license in [NEOFORGE_MDK_LICENSE.txt](NEOFORGE_MDK_LICENSE.txt).
