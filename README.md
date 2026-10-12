# ☁️ Cloudy v1

A Meteor Client addon. **Made by Mostly Cloudy.**

Runs alongside Meteor Client and Glazed — drop all three jars in `mods/`.

- Minecraft **26.2** · Fabric Loader **0.19.3+** · Java **25**
- Meteor Client **26.2** build

---

## Building the jar

### Option A — GitHub (no setup on your PC)
1. Create a new GitHub repo and upload this folder (keep the `.github` folder).
2. GitHub Actions builds it automatically on every push.
3. Go to **Releases → Dev Build** and download `cloudy-1.0.0.jar`.

### Option B — on your PC
1. Install **Java 25** (Temurin: adoptium.net).
2. Open a terminal in this folder and run:
   - Windows: `gradlew.bat build`
   - Mac/Linux: `./gradlew build`
3. The jar is in `build/libs/cloudy-1.0.0.jar`.

Then copy it into `.minecraft/mods/` next to Meteor and Glazed.

---

## First-time setup
1. Open the click GUI (**Right Shift**) — Cloudy has its own **Cloudy** panel.
2. Turn on **Cloudy Theme** to switch the menus to blue.
3. Add the watermark: Meteor → **HUD** → right-click empty space → **Cloudy → watermark**.
4. Right-side module list: in the HUD editor add **Active Modules**, drag it to the top-right,
   set alignment to Right and sort by Biggest. Set its colour mode to match the theme.

**Binds:** every module can be bound — right-click the module in the click GUI to open it,
click **Bind** and press a key. Render Toggle also has its own Low / High / Swap key settings.

---

## Modules

| Module | What it does | Key settings |
|---|---|---|
| **Weather Notifier** | Alerts when rain/thunder start and stop | each alert on/off, thunder-only, chat/toast/both, sound, alert on join |
| **Kill Switch** | On = everything off and remembered. Off = same modules back on | never-disable list, hide HUD, chat feedback |
| **Render Toggle** | Keys to drop render distance and restore it | low (2), high (32) or restore previous, low/high/swap keys |
| **Auto Relog** | Relogs when you pass a Y level, rejoins the same server | Y level (0), below/above, delay, once or every time, server filter |
| **Auto Log+** | Logs out on low HP, low totems, a big hit or a nearby non-friend | every trigger on/off with its own threshold, toggle off after, stop reconnect |
| **Netherite Finder** | Highlights ancient debris, alerts with coords | nether-only, Y range, alerts + cluster grouping, colours, tracers, range |
| **Cloudy Theme** | Blue re-skin of the Meteor GUI | every colour editable live; turning off restores Meteor defaults |
| **Watermark (HUD)** | "Cloudy v1 / made by Mostly Cloudy" | text, scale, shadow, gradient colours |

### Notes
- **Render Toggle**: the module must be enabled for its keys to work. The server's view distance
  still caps what you receive — 32 on your end won't load more than the server sends.
- **Auto Relog** rejoins using the last server you joined. If Meteor's own Auto Reconnect is on,
  Cloudy lets that handle the rejoin so you don't double-connect.
- **Kill Switch** remembers its list even if you close the game while it's on.
- **Netherite Finder** only sees what the server sends; with anti-xray, buried debris shows up
  once a face is exposed.
