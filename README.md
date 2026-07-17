# ⚛️ AtomCore - The Ultimate SMP Essential

**AtomCore** is a lightweight, all-in-one core plugin designed specifically for Survival Multiplayer (SMP) servers. It provides everything your players need—from a fully functional GUI economy to homes, teleports, and an elegant scoreboard—without bloated features slowing down your server.

### ✨ Key Features

*   🌍 **Dual Language Support (EN/TR):** Players can instantly toggle their personal language using `/lang english` or `/lang turkce`. All menus, shop items, and messages adapt in real-time!
*   🛒 **Advanced GUI Shop:** A fully interactive, 6-category shop menu (Ores, Blocks, Food, Mob Drops, Armor, Tools). Features Left-Click to Buy, Right-Click to Sell, and a Shift-Right-Click **Confirmation Menu** to safely sell all items of a type.
*   💰 **Built-in Economy:** No need for Vault or external economy plugins. AtomCore handles player balances, customizable starting money, maximum balance limits, and currency symbols directly via `config.yml`.
*   📊 **Elegant Scoreboard:** A beautiful, flicker-free sidebar scoreboard displaying the player's ping, K/D ratio, and balance. Players can easily toggle it off/on with `/sb`.
*   🏠 **TPA & Home System:** Essential survival commands built right in. Set homes, teleport to them, and send teleport requests to other players with ease.

### 📜 Commands

*   `/help` - Displays the AtomCore command list.
*   `/lang <english/turkce>` - Changes the player's personal language.
*   `/shop` - Opens the main Shop GUI.
*   `/tpa <player>` - Sends a teleport request.
*   `/tpaccept` / `/tpdeny` - Accepts or denies a pending teleport request.
*   `/sethome` - Saves your current location as your home.
*   `/home` - Teleports you to your saved home.
*   `/sb` - Toggles the visibility of the scoreboard.

### ⚙️ Configuration (`config.yml`)
AtomCore is highly customizable. Server owners can easily change the scoreboard title, starting balance, max money limit, and the currency symbol (e.g., $, €, ⛃, ₺) without touching a single line of code!

```yaml
scoreboard-title: "&6&lAtomCore SMP"
currency-symbol: "$"
starting-balance: 100.0
max-money: 1000000.0
