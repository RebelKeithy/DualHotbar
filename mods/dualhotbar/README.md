# Dual Hotbar

A second hotbar, made from the bottom row of the inventory, drawn at half size above the normal hotbar with its bottom edge tucked behind it. **Shift + mouse wheel** (or a key of your choice) swaps the two: the items trade places in the inventory, and the bars animate into each other's place, turning one way for wheel down and the other way for wheel up. Items in the second bar are used by swapping them into the hotbar, or, with the experimental **Select Second Bar** option, by moving the selection onto the second bar: scroll past the end of a hotbar, hold Ctrl and press a number key, or double-tap a number key.

Client-only, for Fabric (with Fabric API) and NeoForge on Minecraft 26.1, 26.2 and 26.3. Servers don't need it.

## Settings

The settings screen opens from the mod's config button in NeoForge's Mods list or in [Mod Menu](https://modrinth.com/mod/modmenu) on Fabric, or with the **Open Dual Hotbar Settings** key (unbound by default). Changes apply at once and are saved when the screen closes. Its **Key Binds...** button opens Controls > Key Binds.

The same settings live in `config/dualhotbar.properties`, created on first launch. Hand edits apply while the game is running (the file is checked once a second).

| Setting | File key | Default | |
|---|---|---|---|
| Second Hotbar Size | `secondHotbarScale` | `0.5` | Size of the second bar relative to the hotbar: 10% to 100% in the screen, 0.1 to 1.0 in the file |
| Overlap | `secondHotbarOverlap` | `3` | How many of the second bar's own pixels tuck behind the top of the hotbar, 0 to 22. 3 hides its bottom border; 22 (Full) hides the whole bar until you swap and leaves the HUD at vanilla's height |
| Swap Animation | `swapAnimationMillis` | `300` | Length of the swap animation: off to 1000 ms in the screen, up to 5000 in the file |
| Scroll to Swap | `scrollToSwap` | `true` | Whether scrolling while holding the Swap Modifier key swaps the hotbars |
| Select Second Bar | `selectableSecondHotbar` | `false` | Experimental: lets the selection move onto the second bar, in the ways below. See [Selecting the second hotbar](#selecting-the-second-hotbar-experimental) |
| Scroll Past Edge | `scrollPastEdge` | `true` | With Select Second Bar: scrolling past either end of a hotbar moves onto the other, as one 18-slot ring. When off, scrolling with a second-bar slot selected stays on the second bar |
| Double-Tap Keys | `doubleTapNumberKeys` | `true` | With Select Second Bar: pressing a number key twice within 0.3 s selects that slot of the second bar; pressing it once goes back to the hotbar |
| Long Hotbar | `longHotbar` | `false` | With Select Second Bar: draws the hotbar and the second bar side by side as one 18-slot hotbar, hotbar on the left, that works like a plain hotbar: scrolling moves across all 18 slots and the bars never swap. Second Hotbar Size, Overlap, Swap Animation and Scroll to Swap don't apply and are greyed out |

## Keys

Under **Dual Hotbar** in Controls > Key Binds:

| Key | Default | |
|---|---|---|
| Swap Modifier (Hold + Scroll) | Left Shift | Hold while scrolling to swap. Wheel down and wheel up turn the animation opposite ways. Shares Left Shift with Sneak, so you sneak while swapping |
| Second Bar Modifier (Hold + Number) | Left Control | With Select Second Bar: hold while pressing a number key to select that slot of the second bar. Shares Left Control with Sprint, so holding Sprint and pressing a number selects the second bar |
| Swap Hotbars | Not bound | Swaps with one press, animating like wheel down |
| Open Dual Hotbar Settings | Not bound | Opens the settings screen |

## Selecting the second hotbar (experimental)

Off by default. Only hotbar slots can be held, and vanilla servers refuse any other slot, so while a second-bar slot is selected Dual Hotbar swaps its item with the hotbar item below it (the same "swap with hotbar slot" click a number key makes in the inventory) and holds that hotbar slot. The HUD keeps drawing both items where they belong, with the selection frame drawn on top of the hotbar so it isn't cut off. Selecting anything else swaps them back. A full hotbar swap while a second-bar item is held keeps it in hand, now in the hotbar.

The swap is undone while the pause menu or your own inventory is open, so the inventory shows the real layout and quitting saves it, and it's redone when the screen closes if nothing else was selected meanwhile.

**Long Hotbar** puts the two bars side by side instead, as one 18-slot hotbar at full size (shrunk only if the screen is too narrow), with the offhand slot beside its ends and the rest of the HUD at vanilla's height. The 18 slots are built from pieces of the hotbar texture so the join looks like any other divider. It behaves like a plain 18-slot hotbar: the two halves never swap (Shift + scroll just scrolls, and the Swap Hotbars key does nothing), and scrolling moves across all 18 slots whether or not Scroll Past Edge is on.

At high overlap most of the second bar is hidden; at full overlap only its selection frame shows, over the hotbar. With Select Second Bar on, the settings screen shows a warning under the Overlap slider once it passes half the second bar's height (12 or more). In the settings screen, Scroll Past Edge and Double-Tap Keys are greyed out while Select Second Bar is off. The selection frame on the second bar is drawn over the hotbar, but under the selected item and its stack count, as in vanilla.

Earlier versions of Dual Hotbar (up to 1.12) made extra inventory rows real hotbar slots, which needs the mod on the server as well. This version keeps working on any server instead; [Compatibility](#compatibility) lists what that costs.

## Compatibility

Tested with vanilla Minecraft plus Fabric API (and Mod Menu) or NeoForge on their own, on 26.1, 26.2 and 26.3. It hasn't been tested alongside other mods, so the points below come from how it hooks into the game, not from reports.

**Always**

- **The mod draws the hotbar itself.** Mods that change vanilla's hotbar drawing won't show their changes, and other mods that restyle, raise or add hotbars will fight over it. Item decorations (durability bars, cooldowns, NeoForge item decorators) still draw, and Fabric mods that add HUD elements next to the hotbar through Fabric API are unaffected.
- **The HUD above the hotbar moves up** (10 pixels by default, none at full overlap). On Fabric, mods that draw their own bars or overlays at vanilla's fixed positions (thirst or mana bars, AppleSkin-style food overlays) don't move with it and may overlap or sit off-target. On NeoForge, bars that stack on `leftHeight`/`rightHeight` move along; fixed-position ones don't. HUD-editor mods may move things twice.
- **Scrolling.** Mods that also change hotbar scrolling, or use Shift + scroll in the world (such as hotbar features in Tweakeroo or Inventory Profiles Next), may conflict with Scroll to Swap.
- **Swapping sends 9 ordinary inventory clicks** without an inventory open. Vanilla servers accept them, but anti-cheat plugins (Grim, NoCheatPlus, Vulcan, ...) may flag clicks without an open inventory, and some servers' rules forbid inventory automation. Slots a server locks are refused and corrected.
- **Inventory layout.** The second bar is inventory slots 27-35, the bottom row; mods that resize the player inventory would break that.
- **Resource packs** change the hotbar texture as usual. The default overlap and the hiding of the bar behind assume vanilla's 3-pixel opaque border, so packs with a different border may look slightly off.

**With Select Second Bar (experimental)**

- **The inventory really has the column swapped** while a second-bar item is held. Anything that reads it sees the swap: other HUD and inventory mods, auto-refill and sorting mods, creative toolbar saving (C + number), and chest-type screens.
- **Changes made to those two slots by the server or other mods** can leave Dual Hotbar's idea of the swap wrong, so swapping back moves the wrong items. Pick block is the most likely cause, since the server moves the picked item into the hotbar and changes the selection; `/give`, `/clear`, plugins and auto-refill can do it too.
- **Lag.** If the server refuses one of the clicks and corrects the client, the two can disagree until something else is selected.
- **Leaving without the pause menu** (a crash, a kick, closing the window) while a second-bar item is held leaves it swapped into the hotbar. Quitting from the pause menu is safe.
- **Death without keepInventory.** Dual Hotbar still counts the column as swapped, so the next change of selection can move a newly picked-up item into the second bar.
- **More clicks.** Every move between the bars sends one, which means more exposure to anti-cheat.

## How it works

**Swapping.** For each of the 9 columns, the client sends the same "swap with hotbar slot N" click that pressing a number key over an inventory slot sends: inventory slot 27 + N with hotbar slot N. The server applies the clicks like any other inventory click, which is why the mod works on servers without it. Columns where both slots are empty are skipped. The selected column stays selected, so the item in hand becomes the one that was above it.

**Animation.** The bars ride on opposite ends of a diameter of a turning wheel, as in the sketch: the vertical axis is the bar's scale, and the horizontal axis is the height of the bar's centre above the bottom of the screen. At rest the hotbar sits at 135° (upper left: scale 1, lowest) and the second bar at 315° (lower right: scale 0.5, higher). Scrolling down turns the wheel 180° counterclockwise, as in the sketch; scrolling up turns it 180° clockwise, which is the same animation played in reverse:

```
scale ^                      a bar at angle θ:
      |      .-'''-.           scale  = (1 + s)/2  + (1 - s)/2 · √2 · sin θ
  1.0 +- - -H        .         height = (h₁ + h₂)/2 + (h₂ - h₁)/2 · √2 · cos θ
      |   / ↙ \        \
 0.75 +- |      o       |    s  = second bar scale (0.5)
      |   \        \  ↗ /    h₁ = hotbar centre height (11)
  0.5 +- - -'-.___.-S        h₂ = second bar centre height (22 + 8·s)
      +--------------------> height of the bar's centre
```

Scrolling down with the default scale, the hotbar dips about 6 pixels (its bottom edge briefly 3 pixels below the screen) while it shrinks, keeps shrinking to 0.40 as it passes behind the other bar, and rises into the second bar's place. The second bar lifts about 6 pixels while it grows, passes in front at 1.10, and settles into the hotbar's place. Scrolling up, the hotbar grows and rises in front while the second bar shrinks behind it and drops into place. The bigger bar is always drawn in front. The angle follows a sine ease-in-out over `swapAnimationMillis`, so the wheel starts and stops smoothly. A new swap can't start until the current one has finished: scroll steps and key presses during the animation are ignored. The wheel is widened into an ellipse where needed so that the bars' centres separate by at least a full hotbar height mid-swap, each moving half a hotbar height off the middle; at the default overlap that is a little more than the circle would give, and at full overlap the incoming bar rises about 14 pixels over the top of the hotbar and drops in front. The maths is in [`SwapAnimation.java`](src/main/java/io/github/rebelkeithy/dualhotbar/SwapAnimation.java).

**Layout.** At rest the hotbar, offhand slot and attack indicator are exactly where vanilla draws them; the offhand slot and attack indicator don't move during a swap. The second bar sits behind the hotbar with part of it tucked under the hotbar's top edge: by default its bottom border (the 3 texture rows below its slots, 1.5 pixels at half size), up to all of it with Overlap at Full. Whichever bar is behind is clipped where the front bar covers it, so it never shows through the front bar's see-through slots. Everything that normally sits on the hotbar moves up by the visible height of the second bar, `ceil((22 − overlap) × scale)` pixels (10 at the defaults, 0 at full overlap): health, armor, food, air, mount health, the experience/locator/jump bar, the experience level, the held item name, the action bar message and chat. On NeoForge the status bars move by raising `leftHeight`/`rightHeight`, so modded bars that stack on those move too. Nothing moves in spectator mode, which has no hotbar.

## Decisions to review

- **Wheel interpretation of the sketch.** The sketch labels only the scale axis; the other axis is taken to be the bar's height on screen, giving the "rotating wheel" motion above with both overshoot (scale past 1.0 and below 0.5) and a small dip/lift at the start. If the intent was for height to follow the scale instead (bars sliding straight past each other), only `SwapAnimation.pose` needs to change.
- **Easing.** The sketch implies constant rotation speed, which would start and stop the bars abruptly; a sine ease-in-out is applied to the angle. Use `t` instead of `eased` in `SwapAnimation.lag()` to go back to constant speed.
- **The swap modifier is its own key mapping**, defaulting to Left Shift alongside Sneak (both loaders allow two mappings on one key). Scroll steps during a swap animation are dropped rather than queued, so a trackpad flick or a free-spinning wheel swaps once per animation; with Swap Animation off, every step swaps.
- **Second Bar Modifier defaults to Left Control**, shared with Sprint the same way the swap modifier shares Left Shift with Sneak. It works whenever Select Second Bar is on (there's no separate switch like Double-Tap Keys); unbind it to turn it off.
- **Scroll direction.** "Down" is whatever the game treats as down, the direction that moves the hotbar selection to the right, so it follows the system's natural-scrolling setting. Wheel down gets the sketch's counterclockwise turn (the second bar comes over the top and down in front); wheel up gets the reverse.
- **Overlap is measured in the second bar's own pixels**, so the default (its 3-pixel bottom border) looks the same at any size, and 22 is fully hidden at any size.
- **Minimum lift.** `MIN_LIFT` in `HotbarRenderer` is half a hotbar height: each bar moves at least that far off the middle mid-swap. It applies at the default overlap too, where the bars rest closer than that, so the default swap moves about 6 pixels up and down instead of 3.
- **The hotbar is drawn by this mod**, replacing vanilla's item hotbar drawing (vanilla's code is mirrored for the hotbar, selection frame, offhand slot, attack indicator and item pop animation). Other mods that draw by hooking into vanilla's hotbar method won't show their changes; mods that use the loaders' HUD APIs are unaffected.
- **No server component.** Swapping uses vanilla inventory clicks, so it can't be made atomic: on a laggy connection the 9 clicks arrive separately (each is still validated by the server).
- **Chat** moves up with the rest of the HUD, but chat while typing (the chat screen) stays at vanilla's position, so it may overlap the raised health bar while the chat box is open.
- **The settings screen is vanilla's options-screen style** (sliders and toggles), shared by both loaders. On Fabric it is reached through Mod Menu, which the mod is compiled against but doesn't require; without Mod Menu, use the Open Dual Hotbar Settings key. Mod Menu's version for each Minecraft release is `modmenu_version` in `minecraft/<version>.properties`.
- **New keys are unbound** (Swap Hotbars, Open Dual Hotbar Settings) to avoid clashing with other mods.
- **Package, license, version:** `io.github.rebelkeithy.dualhotbar`, All Rights Reserved (the template default), 1.1.0.
