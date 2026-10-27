# Pin Mod

![Minecraft 1.7.10](https://img.shields.io/badge/Minecraft-1.7.10-green.svg?style=flat)
[![Build](https://github.com/LanxHare-GIT/pin-1.7.10/actions/workflows/build.yml/badge.svg)](https://github.com/LanxHare-GIT/pin-1.7.10/actions/workflows/build.yml)

This repository contains the Minecraft **1.7.10** (Forge 10.13.4.1614) build of the Pin mod.

## About

The pin tells us how beautiful it is!

Press the pin key (default `R`) to mark the block you are looking at. Every player in the
same dimension sees the marker with the sender's name and the distance. Markers expire
automatically, and pressing the key again on the same block removes your own marker.

### Features

- Two display presets: a vertical marker (diamond + line) and a callout leader line
  (diamond -> 45 degree leader -> horizontal line -> label).
- Vector drawing for the diamond, lines and leader, so markers and labels always stay aligned.
- Line options: solid or soft edge, screen-space length or world-space height.
- Label options: gap and horizontal / vertical offset.
- Off-screen indicator: arrow or diamond, coloured like the marker.
- In-game configuration screen with a live preview.

## Installation

Put the jar into `minecraft/mods/`. The mod must be installed on both the client and the
server; it has no additional hard dependencies.

## Downloads

Builds are published as releases tagged `v*`. Every push also produces a downloadable build
artifact from the GitHub Actions workflow.

## Building

Requirements: JDK 8.

```bash
cd 1.7.10
./gradlew setupCIWorkspace
./gradlew build
```

The jar is written to `1.7.10/build/libs/`.

## Configuration

The configuration file `config/pin.cfg` is created on first launch and can also be edited
in game through the Mod Options screen.

| Section | Option | Description |
| --- | --- | --- |
| `display` | `preset` | `0` vertical marker, `1` callout leader line |
| `display` | `outsideShape` | `0` arrow, `1` diamond for off-screen markers |
| `marker` | `showDot` | draw the diamond |
| `marker` | `dotColor` / `dotSize` / `farGrowth` | diamond colour, size and distance scaling |
| `line` | `lineEnabled` / `lineSolid` | show the line; draw it solid |
| `line` | `lineWorldSpace` / `lineHeight` | perpendicular to the ground; height in blocks |
| `line` | `lineLength` / `lineWidth` / `lineColor` | screen-space length, thickness, colour |
| `label` | `labelGap` | gap to the line (callout) or to the diamond (vertical) |
| `label` | `labelOffsetX` / `labelOffsetY` | fine offsets of the label |
| `gameplay` | `lifetimeSeconds` | auto-remove delay |

## Issues

Found a bug or want to suggest something? Open an issue at
[github.com/LanxHare-GIT/pin-1.7.10/issues](https://github.com/LanxHare-GIT/pin-1.7.10/issues).
Please include the Forge version, the mod version and the full log.

## Contributions

Pull requests are welcome. Keep changes focused and follow the style of the file you edit.

## Localization

`en_US` and `zh_CN` are included. Files must be encoded as UTF-8. New languages can be added
by creating a file with the appropriate language code under
`1.7.10/src/main/resources/assets/pinmod/lang/`.

## Credits

* (c) 2020 TeamFruit, original Pin mod
* Notch et al for Minecraft
* Lex et al for MinecraftForge
* Kamesuta, sjcl
* all [contributors](https://github.com/LanxHare-GIT/pin-1.7.10/graphs/contributors)
