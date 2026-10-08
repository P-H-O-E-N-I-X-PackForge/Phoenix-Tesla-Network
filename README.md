# Phoenix: Tesla Network
### A GregTech Modern Capstone Addon | 1.20.1

> "The present is theirs; the future, for which I really worked, is mine." Nikola Tesla

**Phoenix: Tesla Network** is the endgame reward for the GregTech Modern engineer who is tired of hiding their base behind a wall of identical machines. We’re introducing **Capstone Multiblocks**: massive, high-impact structures that act as the centerpiece of your factory. Mainly the Tesla Tower.
A multiblock sitting at quite the nice size of 19x53x19 capable of harnessing the power of your entire base in an interesting way.

---

##  What is a "Capstone"?
We’re over the "spamming singleblocks" meta. If you’ve conquered the tech tree, you deserve a monument, not another row of boxes. Capstone Multiblocks are:

* **The Heart of the Base:** You don't mass-produce these. You build one or two as your primary infrastructure hubs.
* **Built to be Seen:** Massive scale, custom particle effects, and sometimes even wild shaders. These are designed to look incredible in a base tour.
* **The Ultimate Flex:** Seeing a Tesla Tower on the horizon is the universal sign that you’ve actually finished the grind.

---

##  The Tesla Network
Forget the cable lag. The **Tesla Tower** is your new wireless hub for power and data.

* **Wireless Power:** Stable connections with all singleblocks, multiblocks, and our unique tesla hatches through useage of the Tesla Binder.
* **Rock Solid Tech:** We love saved data here, everything runs through TeamEnergySavedData.
* **Think Big:** If it fits in one block, we don't want it. Every machine in this addon is a true multiblock project (apart from the tesla energy hatches and wireless chargers, but shh).

---

##  Three Towers, One Network
There are three independent Tesla Tower multiblocks. They are separate machines, but they all feed and drain the **same team network**, so a team can run any combination of them at once.

| Tower | Default hatches | Default batteries | Default reach |
|---|---|---|---|
| **Basic Tesla Tower** | LV - IV | LV - IV | 128 block radius, same dimension |
| **Advanced Tesla Tower** | LV - UHV | LV - UHV | Infinite range, same dimension |
| **Tesla Tower** | ULV - MAX | LV - MAX | Infinite range, cross-dimensional |

Anything on the network - wireless hatches, linked machines, wireless chargers, and the Phoenix Tech Suite - has to be inside the network's coverage to use it. Coverage is the union of every working tower plus every working **Tesla Range Extender**.

**Tesla Range Extender:** a small multiblock that adds a radius of coverage around itself (128 blocks by default). It only works while it is itself inside coverage from a tower or another working extender, so extenders can be chained outward from a tower. A stranded extender does nothing.

### Configuration (`config/phoenix_tesla_network.yaml`)
Pack devs can tune every tower under `towers`:

* `tier1Basic`, `tier2Advanced`, `tier3Ultimate` each have: `enabled`, `minHatchTier`, `maxHatchTier`, `minBatteryTier`, `maxBatteryTier`, `rangeBlocks`, `infiniteRange`, `crossDimension`. Tiers are GT tier numbers (`0`=ULV, `1`=LV ... `5`=IV ... `9`=UHV ... `14`=MAX).
* `crossDimension` makes range ignore which dimension something is in, so an infinite-range cross-dimensional tower covers everything everywhere.
* `relay.enabled` and `relay.rangeBlocks` control the Range Extender.
* `suitRespectsRange` decides whether the Phoenix Tech Suite needs to be inside coverage.

### Transmission Loss (`towers.loss`)
Energy moving between the network and a wireless hatch, linked machine, wireless charger or the suit's tool charging loses a share on the way, based on how far the receiving end is from the nearest tower or range extender covering it (so extenders cut loss):

`loss % = min(maxLossPercent, baseLossPercent + lossPercentPerUnit * (distance / distanceUnitBlocks) ^ exponent)`

Defaults: 0% base, 5% per 128 blocks, linear, 10% flat when only a cross-dimensional tower covers it, capped at 50%. Set `loss.enabled` to `false` for lossless transfer, or `loss.applyToSuit` to `false` to exempt the suit. A machine receives (100 - loss)% of what the network spends, and the network receives (100 - loss)% of what a generator sends.

### Tesla Batteries
Tesla Batteries exist for every tier from LV to MAX. The LV - UV ones scale off GregTech's own values (`teslaBatteries` in the config), each tier with its own multiplier:

| Tier | Reference | Default multiplier | Capacity |
|---|---|---|---|
| LV | highest LV battery (lithium, 120k) | x8 | 960,000 EU |
| MV | highest MV battery (lithium, 420k) | x16 | 6,720,000 EU |
| HV | highest HV battery (lithium, 1.8M) | x32 | 57,600,000 EU |
| EV | EV Lapotronic capacitor (150M) | x0.25 | 37,500,000 EU |
| IV | IV Lapotronic capacitor (1.5B) | x0.5 | 750,000,000 EU |
| LuV | LuV Lapotronic capacitor (6B) | x1 | 6,000,000,000 EU |
| ZPM | ZPM Lapotronic capacitor (24B) | x2 | 48,000,000,000 EU |
| UV | UV Lapotronic capacitor (96B) | x4 | 384,000,000,000 EU |

`globalMultiplier` scales all of them at once. The UHV - MAX batteries keep their fixed capacities. For now the LV - UV batteries all reuse GT's ZPM Lapotronic battery texture as a placeholder.

Changing `enabled` or any tier limit needs a restart, since they shape what gets registered and what the multiblock patterns accept. The `/tesla_debug` command lists each network's towers and extenders.

---

##  Development & Formatting
We use **[Spotless](https://github.com/diffplug/spotless)** to keep the backend as organized as a perfect GregTech production line. It’s basically an automated "tidy up" tool for the source code.

**How to keep the code clean:**
1.  **The Easy Way:** Run the `spotlessApply` task in your Gradle tab in IntelliJ.
2.  **The Pro Way:** Install the [Spotless Gradle plugin](https://plugins.jetbrains.com/plugin/18321-spotless-gradle) for IntelliJ.
3.  **The Manual Way:** Use the terminal: `gradlew.bat :spotlessApply` (Windows) or `./gradlew spotlessApply` (Linux/Mac).

---

##  Installation & Resources
* **Requirements:** GregTech Modern (1.20.1).
* **Support:** If you’re stuck or want to show off your build, hop into the [Phoenix Forge Technologies Discord](https://discord.gg/KBNst7hZ4C).

*Built with ⚡ by the Phoenix Team.*