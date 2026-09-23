# Command Actions Reference

Complete guide to all command executor types and how to use them in scoring rules.

## Table of Contents
- [Basic Commands](#basic-commands)
- [Player Messages](#player-messages)
- [Visual Effects](#visual-effects)
- [Item Management](#item-management)
- [Player State](#player-state)
- [Advanced Commands](#advanced-commands)
- [Variables](#variables)
- [Examples](#examples)

---

## Basic Commands

### CONSOLE
**Description:** Execute command as console  
**Example:**
```yaml
on_success:
  - executor: CONSOLE
    command: "give {player} diamond 1"
```

**Common Uses:**
- Give items: `give {player} diamond 10`
- Teleport: `tp {player} 0 100 0`
- Change gamemode: `gamemode creative {player}`
- Run vanilla commands

---

### PLAYER
**Description:** Execute command as the player  
**Example:**
```yaml
on_success:
  - executor: PLAYER
    command: "say I earned {points} points!"
```

**Common Uses:**
- Player chat: `say Hello!`
- Player commands: `spawn`
- Any command player has permission for

---

### OP
**Description:** Execute command as player with temporary OP  
**Warning:** Use carefully! Grants temporary admin privileges  
**Example:**
```yaml
on_success:
  - executor: OP
    command: "gamemode creative"
```

---

## Player Messages

### PLAYER_MESSAGE
**Description:** Send chat message to player only  
**Example:**
```yaml
on_success:
  - executor: PLAYER_MESSAGE
    command: "&aYou earned &e{points} &apoints!"
```

**Color Codes:**
- &0-9, a-f: Colors
- &l: Bold
- &n: Underline
- &o: Italic
- &r: Reset

---

### BROADCAST
**Description:** Broadcast message to all players  
**Example:**
```yaml
on_success:
  - executor: BROADCAST
    command: "&6{player} &ejust earned &c{points} &epoints!"
```

---

### TITLE
**Description:** Send title to player  
**Format:** `Title~Subtitle~FadeIn~Stay~FadeOut` (times in ticks)  
**Example:**
```yaml
on_success:
  - executor: TITLE
    command: "&6+{points} Points!~&eNice work!~10~70~20"
```

**Parameters:**
- Title text (use ~ separator)
- Subtitle text
- Fade in time (ticks)
- Stay time (ticks)
- Fade out time (ticks)

---

### SUBTITLE
**Description:** Send subtitle only  
**Example:**
```yaml
on_success:
  - executor: SUBTITLE
    command: "&eYou earned points!"
```

---

### ACTIONBAR
**Description:** Send action bar message  
**Example:**
```yaml
on_success:
  - executor: ACTIONBAR
    command: "&eScore: &6{score} &7| &ePosition: &6#{position}"
```

---

## Visual Effects

### SOUND
**Description:** Play sound to player  
**Format:** `SOUND_NAME~Volume~Pitch`  
**Example:**
```yaml
on_success:
  - executor: SOUND
    command: "ENTITY_PLAYER_LEVELUP~1.0~1.0"
```

**Common Sounds:**
- ENTITY_PLAYER_LEVELUP
- ENTITY_EXPERIENCE_ORB_PICKUP
- BLOCK_NOTE_BLOCK_PLING
- ENTITY_FIREWORK_ROCKET_LAUNCH
- UI_TOAST_CHALLENGE_COMPLETE

**Volume:** 0.0-2.0 (1.0 = normal)  
**Pitch:** 0.5-2.0 (1.0 = normal, higher = higher pitch)

---

### PARTICLE
**Description:** Spawn particle effect at player location  
**Format:** `PARTICLE_TYPE~Count~Spread`  
**Example:**
```yaml
on_success:
  - executor: PARTICLE
    command: "FIREWORKS_SPARK~10~0.5"
```

**Common Particles:**
- FIREWORKS_SPARK
- HEART
- VILLAGER_HAPPY
- FLAME
- ENCHANTMENT_TABLE
- EXPLOSION_NORMAL
- SPELL_WITCH

---

## Item Management

### GIVE_ITEM
**Description:** Give item to player  
**Format:** `MATERIAL~Amount~Display Name~Lore1|Lore2`  
**Examples:**
```yaml
# Simple item
- executor: GIVE_ITEM
  command: "DIAMOND~5"

# Named item
- executor: GIVE_ITEM
  command: "DIAMOND_SWORD~1~&bLegendary Sword"

# Named item with lore
- executor: GIVE_ITEM
  command: "DIAMOND_SWORD~1~&bLegendary Sword~&7A powerful weapon|&7Forged in fire"
```

---

### TAKE_ITEM
**Description:** Take item from player  
**Format:** `MATERIAL~Amount`  
**Example:**
```yaml
on_success:
  - executor: TAKE_ITEM
    command: "DIRT~64"
```

---

## Player State

### HEAL
**Description:** Heal player  
**Format:** Amount (or "FULL")  
**Examples:**
```yaml
- executor: HEAL
  command: "10"

- executor: HEAL
  command: "FULL"
```

---

### FEED
**Description:** Feed player  
**Format:** Amount (or "FULL")  
**Example:**
```yaml
- executor: FEED
  command: "FULL"
```

---

### DAMAGE
**Description:** Damage player  
**Format:** Amount  
**Example:**
```yaml
- executor: DAMAGE
  command: "5"
```

---

### SET_HEALTH
**Description:** Set player health to exact value  
**Format:** Amount (max 20)  
**Example:**
```yaml
- executor: SET_HEALTH
  command: "10"
```

---

### SET_HUNGER
**Description:** Set player hunger to exact value  
**Format:** Amount (max 20)  
**Example:**
```yaml
- executor: SET_HUNGER
  command: "20"
```

---

### EFFECT
**Description:** Apply potion effect to player  
**Format:** `EFFECT_TYPE~Duration(seconds)~Amplifier`  
**Examples:**
```yaml
# Speed 2 for 60 seconds
- executor: EFFECT
  command: "SPEED~60~1"

# Regeneration 1 for 30 seconds
- executor: EFFECT
  command: "REGENERATION~30~0"
```

**Common Effects:**
- SPEED, SLOW
- JUMP, SLOW_FALLING
- STRENGTH, WEAKNESS
- REGENERATION, POISON
- NIGHT_VISION, BLINDNESS
- INVISIBILITY, GLOWING
- ABSORPTION, HEALTH_BOOST
- FIRE_RESISTANCE, WATER_BREATHING

**Amplifier:** 0 = Level 1, 1 = Level 2, etc.

---

### CLEAR_EFFECTS
**Description:** Remove all potion effects from player  
**Example:**
```yaml
- executor: CLEAR_EFFECTS
  command: ""
```

---

### SET_LEVEL
**Description:** Set player level  
**Format:** Level  
**Example:**
```yaml
- executor: SET_LEVEL
  command: "30"
```

---

### GIVE_EXP
**Description:** Give player experience points  
**Format:** Amount  
**Example:**
```yaml
- executor: GIVE_EXP
  command: "100"
```

---

### TAKE_EXP
**Description:** Take player experience points  
**Format:** Amount  
**Example:**
```yaml
- executor: TAKE_EXP
  command: "50"
```

---

## Advanced Commands

### TELEPORT
**Description:** Teleport player to location  
**Format:** `World~X~Y~Z` or `X~Y~Z` (current world)  
**Examples:**
```yaml
# Specific world
- executor: TELEPORT
  command: "world~0~100~0"

# Current world
- executor: TELEPORT
  command: "0~100~0"
```

---

### LAUNCH
**Description:** Launch player into air  
**Format:** Power (1-10)  
**Example:**
```yaml
- executor: LAUNCH
  command: "5"
```

---

### STRIKE_LIGHTNING
**Description:** Strike lightning at player  
**Format:** Damage (true/false)  
**Examples:**
```yaml
# Visual only
- executor: STRIKE_LIGHTNING
  command: "false"

# With damage
- executor: STRIKE_LIGHTNING
  command: "true"
```

---

### CREATE_EXPLOSION
**Description:** Create explosion at player  
**Format:** `Power~SetFire~BreakBlocks`  
**Example:**
```yaml
# Visual explosion, no damage
- executor: CREATE_EXPLOSION
  command: "2~false~false"
```

---

### SET_FIRE
**Description:** Set player on fire  
**Format:** Duration (seconds)  
**Example:**
```yaml
- executor: SET_FIRE
  command: "5"
```

---

### SET_GAMEMODE
**Description:** Set player gamemode  
**Format:** SURVIVAL/CREATIVE/ADVENTURE/SPECTATOR  
**Example:**
```yaml
- executor: SET_GAMEMODE
  command: "CREATIVE"
```

---

### CLOSE_INVENTORY
**Description:** Close player's open inventory  
**Example:**
```yaml
- executor: CLOSE_INVENTORY
  command: ""
```

---

### SET_METADATA
**Description:** Set custom metadata on player  
**Format:** `Key~Value`  
**Example:**
```yaml
- executor: SET_METADATA
  command: "contest_winner~true"
```

---

### REMOVE_METADATA
**Description:** Remove metadata from player  
**Format:** Key  
**Example:**
```yaml
- executor: REMOVE_METADATA
  command: "contest_winner"
```

---

## Special Executors

### DELAYED
**Description:** Execute command after delay  
**Format:** `Ticks~Command`  
**Example:**
```yaml
# Wait 3 seconds (60 ticks), then heal
- executor: DELAYED
  command: "60~HEAL:FULL"
```

---

### CHANCE
**Description:** Execute command with probability  
**Format:** `Chance(0.0-1.0)~Command`  
**Examples:**
```yaml
# 50% chance
- executor: CHANCE
  command: "0.5~GIVE_ITEM:DIAMOND~1"

# 10% chance
- executor: CHANCE
  command: "0.1~BROADCAST:&6{player} got lucky!"
```

---

### CONDITIONAL
**Description:** Execute command only if condition met  
**Format:** `if:{condition}~then:{command}`  
**Examples:**
```yaml
# Give diamond if level 50+
- executor: CONDITIONAL
  command: "if:{level}>=50~then:GIVE_ITEM:DIAMOND~1"

# Broadcast if score > 1000
- executor: CONDITIONAL
  command: "if:{score}>1000~then:BROADCAST:{player} is dominating!"
```

**Supported Operators:** <, >, <=, >=, ==, !=

---

### EXECUTE_SERIES
**Description:** Execute multiple commands in sequence  
**Format:** `Command1|Command2|Command3`  
**Example:**
```yaml
- executor: EXECUTE_SERIES
  command: "PLAYER_MESSAGE:Great job!|SOUND:ENTITY_PLAYER_LEVELUP~1~1|GIVE_ITEM:DIAMOND~1"
```

---

### RANDOM
**Description:** Execute random command from list  
**Format:** `Command1|Command2|Command3`  
**Example:**
```yaml
- executor: RANDOM
  command: "GIVE_ITEM:DIAMOND~1|GIVE_ITEM:EMERALD~5|GIVE_ITEM:GOLD_INGOT~10"
```

---

## Variables

All commands support variable substitution using `{variable_name}` syntax.

### Player Variables
- `{player}` - Player name
- `{player_uuid}` - Player UUID
- `{player_displayname}` - Player display name
- `{health}` - Current health
- `{max_health}` - Max health
- `{hunger}` - Current hunger
- `{level}` - Player level
- `{exp}` - Player experience points
- `{gamemode}` - Current gamemode

### Contest Variables
- `{score}` - Current contest score
- `{points}` - Points awarded this action
- `{total_points}` - Total points in contest
- `{position}` - Leaderboard position
- `{contest}` - Contest name
- `{contest_time_left}` - Time remaining (seconds)
- `{contest_time_left_formatted}` - Formatted time (MM:SS)

### Location Variables
- `{world}` - Current world
- `{x}` - X coordinate (rounded)
- `{y}` - Y coordinate
- `{z}` - Z coordinate
- `{yaw}` - Yaw rotation
- `{pitch}` - Pitch rotation
- `{biome}` - Current biome

### Environment Variables
- `{time}` - World time
- `{time_formatted}` - Formatted time (Day/Night)
- `{weather}` - Current weather
- `{moon_phase}` - Moon phase

### Event Variables
- `{material}` - Block/item material (if applicable)
- `{entity}` - Entity type (if applicable)
- `{entity_name}` - Entity custom name (if applicable)
- `{damage}` - Damage dealt/received (if applicable)
- `{amount}` - Item amount (if applicable)

### Chain/Streak Variables
- `{chain}` - Current chain count
- `{streak}` - Current streak count
- `{combo}` - Current combo count

---

## Complete Examples

### Welcome Message on Join
```yaml
scoring_rules:
  - event: CONTEST_JOIN
    points: 0
    on_success:
      - executor: TITLE
        command: "&6Contest Started!~&eGood luck, {player}!~10~70~20"
      - executor: SOUND
        command: "ENTITY_PLAYER_LEVELUP~1.0~1.0"
      - executor: PLAYER_MESSAGE
        command: "&7Time remaining: &e{contest_time_left_formatted}"
```

### Reward with Multiple Effects
```yaml
scoring_rules:
  - event: ENTITY_KILL
    conditions:
      entity_type: ENDER_DRAGON
    points: 1000
    on_success:
      - executor: BROADCAST
        command: "&5&l{player} DEFEATED THE ENDER DRAGON!"
      - executor: TITLE
        command: "&5&lDRAGON SLAYER!~&e+1000 Points~10~100~20"
      - executor: EFFECT
        command: "REGENERATION~60~2"
      - executor: EFFECT
        command: "STRENGTH~60~1"
      - executor: EFFECT
        command: "ABSORPTION~120~2"
      - executor: GIVE_ITEM
        command: "DRAGON_HEAD~1~&5Dragon Trophy~&7Proof of victory"
      - executor: SOUND
        command: "UI_TOAST_CHALLENGE_COMPLETE~2.0~1.0"
      - executor: PARTICLE
        command: "FIREWORKS_SPARK~50~2.0"
```

### Penalty System
```yaml
scoring_rules:
  - event: PLAYER_DEATH
    points: -50
    on_success:
      - executor: PLAYER_MESSAGE
        command: "&c&lDEATH PENALTY: &7-50 points"
      - executor: ACTIONBAR
        command: "&cNew score: &e{score}"
      - executor: EFFECT
        command: "SLOW~30~0"
      - executor: SOUND
        command: "ENTITY_VILLAGER_NO~1.0~0.8"
```

### Combo Reward System
```yaml
scoring_rules:
  - event: BLOCK_BREAK
    conditions:
      material: STONE
      chain_multiplier: true
    points: 1
    on_success:
      - executor: CONDITIONAL
        command: "if:{chain}>10~then:PLAYER_MESSAGE:&6&lCOMBO x{chain}!"
      - executor: CONDITIONAL
        command: "if:{chain}>20~then:EFFECT:SPEED~10~1"
      - executor: CONDITIONAL
        command: "if:{chain}>50~then:BROADCAST:&6{player} has a &lx{chain} combo!"
      - executor: ACTIONBAR
        command: "&eCombo: &6x{chain} &7| &ePoints: &6+{points}"
```

### Random Reward
```yaml
scoring_rules:
  - event: PLAYER_FISH_SUCCESS
    points: 10
    on_success:
      - executor: PLAYER_MESSAGE
        command: "&bYou caught a fish! +10 points"
      - executor: CHANCE
        command: "0.1~EXECUTE_SERIES:PLAYER_MESSAGE:&6&lBONUS CATCH!|GIVE_ITEM:DIAMOND~1|SOUND:ENTITY_PLAYER_LEVELUP~1~2"
```

### Milestone Rewards
```yaml
scoring_rules:
  - event: PERIODIC
    cooldown_seconds: 60
    points: 0
    on_success:
      - executor: CONDITIONAL
        command: "if:{score}>=100~then:PLAYER_MESSAGE:&aMilestone: 100 points!"
      - executor: CONDITIONAL
        command: "if:{score}>=500~then:GIVE_ITEM:GOLDEN_APPLE~1"
      - executor: CONDITIONAL
        command: "if:{score}>=1000~then:BROADCAST:&6{player} reached 1000 points!"
```

### Punishment on Rule Break
```yaml
scoring_rules:
  - event: LEAVE_AREA
    points: -25
    on_success:
      - executor: PLAYER_MESSAGE
        command: "&c&lYou left the contest area! -25 points"
      - executor: DAMAGE
        command: "5"
      - executor: SET_FIRE
        command: "3"
      - executor: SOUND
        command: "ENTITY_GENERIC_EXPLODE~1.0~1.0"
```

---

## Best Practices

1. **Always test commands** in creative mode first
2. **Use PLAYER_MESSAGE** for individual feedback
3. **Use BROADCAST** sparingly (spam prevention)
4. **Combine visual effects** (title + sound + particle)
5. **Use CONDITIONAL** for dynamic rewards
6. **Use DELAYED** for timed sequences
7. **Use CHANCE** to add randomness
8. **Keep messages concise** and clear
9. **Use color codes** for emphasis
10. **Test variable substitution** thoroughly

---

## Common Patterns

### Basic Point Notification
```yaml
on_success:
  - executor: ACTIONBAR
    command: "&a+{points} &7| Score: &e{score}"
```

### Rich Feedback
```yaml
on_success:
  - executor: TITLE
    command: "&a+{points}~&eNice!~10~30~10"
  - executor: SOUND
    command: "ENTITY_EXPERIENCE_ORB_PICKUP~1.0~1.2"
```

### Progressive Rewards
```yaml
on_success:
  - executor: CONDITIONAL
        command: "if:{score}%100==0~then:GIVE_ITEM:GOLDEN_APPLE~1"
```

### Team Announcement
```yaml
on_success:
  - executor: BROADCAST
    command: "&e{player} &7earned &a+{points} &7for their team!"
```

---

See EVENT_TYPES.md for event reference.
See CONDITION_TYPES.md for condition reference.
See CONFIG_EXAMPLES.md for complete contest examples.
