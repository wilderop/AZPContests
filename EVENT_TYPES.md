# Event Types Reference

Complete list of all supported event types for contest scoring rules.

## Block Events

### BLOCK_PLACE
**Description:** Triggers when a player places a block

**Example:**
```yaml
scoring_rules:
  - event: BLOCK_PLACE
    conditions:
      material: STONE
      min_y: 30
      max_y: 60
    points: 1
    message: "&a+1 point! Stone placed"
```

**Common Conditions:**
- material, materials, material_tag
- min_y, max_y, exact_y
- in_area, biome, world
- player_gamemode, player_has_item

---

### BLOCK_BREAK
**Description:** Triggers when a player breaks a block

**Example:**
```yaml
scoring_rules:
  - event: BLOCK_BREAK
    conditions:
      material: DIAMOND_ORE
      require_tool_type: PICKAXE
      require_enchant: FORTUNE
    points: 50
    message: "&6Diamond ore mined! +50 points"
```

---

### BLOCK_INTERACT
**Description:** Triggers when a player right-clicks a block

**Example:**
```yaml
scoring_rules:
  - event: BLOCK_INTERACT
    conditions:
      material: BUTTON
      biome: DESERT
    points: 5
```

---

## Entity Events

### ENTITY_KILL
**Description:** Triggers when a player kills an entity

**Example:**
```yaml
scoring_rules:
  - event: ENTITY_KILL
    conditions:
      entity_type: ZOMBIE
      time: NIGHT
      player_health_below: 10
    points: 15
    message: "&cNight Survivor Bonus! +15 points"
```

---

### ENTITY_DAMAGE
**Description:** Triggers when a player damages an entity

**Example:**
```yaml
scoring_rules:
  - event: ENTITY_DAMAGE
    conditions:
      entity_type: ENDER_DRAGON
      damage_dealt: ">50"
      damage_type: PROJECTILE
    points: 100
    message: "&5Critical hit on dragon! +100"
```

---

### ENTITY_TAME
**Description:** Triggers when a player tames an animal

**Example:**
```yaml
scoring_rules:
  - event: ENTITY_TAME
    conditions:
      entity_type: WOLF
    points: 25
```

---

### ENTITY_BREED
**Description:** Triggers when a player breeds two entities

**Example:**
```yaml
scoring_rules:
  - event: ENTITY_BREED
    conditions:
      entity_type: COW
      biome: PLAINS
    points: 10
```

---

## Player Events

### PLAYER_FISH
**Description:** Triggers when a player catches something while fishing

**Example:**
```yaml
scoring_rules:
  - event: PLAYER_FISH
    conditions:
      fish_type: SALMON
      weather: RAIN
    points: 20
    message: "&bRainy day salmon! +20"
```

---

### PLAYER_CRAFT
**Description:** Triggers when a player crafts an item

**Example:**
```yaml
scoring_rules:
  - event: PLAYER_CRAFT
    conditions:
      material: DIAMOND_SWORD
      player_level_above: 30
    points: 100
    message: "&6Expert crafter! +100"
```

---

### PLAYER_ENCHANT
**Description:** Triggers when a player enchants an item

**Example:**
```yaml
scoring_rules:
  - event: PLAYER_ENCHANT
    conditions:
      item_enchant: SHARPNESS
      item_enchant_level: "SHARPNESS:5"
    points: 50
```

---

### PLAYER_BREW
**Description:** Triggers when a player brews a potion

**Example:**
```yaml
scoring_rules:
  - event: PLAYER_BREW
    conditions:
      material: POTION
    points: 30
```

---

### PLAYER_LEVEL_UP
**Description:** Triggers when a player gains a level

**Example:**
```yaml
scoring_rules:
  - event: PLAYER_LEVEL_UP
    points: 10
    formula: "base * {level}"
    message: "&aLevel up! +{points} points"
```

---

### PLAYER_TRADE
**Description:** Triggers when a player trades with a villager

**Example:**
```yaml
scoring_rules:
  - event: PLAYER_TRADE
    conditions:
      material: EMERALD
      trade_type: BUY
    points: 5
```

---

## Combat Events

### PVP_KILL
**Description:** Triggers when a player kills another player

**Example:**
```yaml
scoring_rules:
  - event: PVP_KILL
    conditions:
      friendly_fire: false
      player_health_above: 15
    points: 100
    message: "&4PvP Kill! +100 points"
```

---

### PVP_DAMAGE
**Description:** Triggers when a player damages another player

**Example:**
```yaml
scoring_rules:
  - event: PVP_DAMAGE
    conditions:
      damage_dealt: ">10"
      damage_type: MELEE
    points: 5
```

---

### MOB_KILL
**Description:** Triggers when a player kills a mob (non-player entity)

**Example:**
```yaml
scoring_rules:
  - event: MOB_KILL
    conditions:
      entity_types: [ZOMBIE, SKELETON, CREEPER]
      time: NIGHT
    points: 10
```

---

### BOSS_KILL
**Description:** Triggers when a player kills a boss mob

**Example:**
```yaml
scoring_rules:
  - event: BOSS_KILL
    conditions:
      entity_types: [ENDER_DRAGON, WITHER]
    points: 1000
    message: "&5&lBOSS DEFEATED! +1000 points"
```

---

## Special Contest Events

### CONTEST_JOIN
**Description:** Triggers when a player joins the contest

**Example:**
```yaml
scoring_rules:
  - event: CONTEST_JOIN
    points: 0
    on_success:
      - executor: PLAYER_MESSAGE
        command: "Welcome to the contest! Good luck!"
      - executor: SOUND
        command: "ENTITY_PLAYER_LEVELUP~1.0~1.0"
```

---

### LEAVE_AREA
**Description:** Triggers when a player leaves the contest area

**Example:**
```yaml
scoring_rules:
  - event: LEAVE_AREA
    points: -10
    message: "&cYou left the contest area! -10 points"
```

---

### RETURN_AREA
**Description:** Triggers when a player returns to the contest area

**Example:**
```yaml
scoring_rules:
  - event: RETURN_AREA
    points: 5
    message: "&aWelcome back! +5 points"
```

---

## Time-Based Events

### PERIODIC
**Description:** Triggers at regular intervals while player is in contest

**Example:**
```yaml
scoring_rules:
  - event: PERIODIC
    cooldown_seconds: 60
    conditions:
      player_gamemode: SURVIVAL
      player_health_above: 10
    points: 5
    message: "&aSurvival bonus! +5 points per minute"
```

---

### EVERY_SECOND
**Description:** Triggers every second the player is in the contest

**Example:**
```yaml
scoring_rules:
  - event: EVERY_SECOND
    conditions:
      biome: NETHER_WASTES
      player_on_fire: false
    points: 1
    message: "&6Nether exploration +1/sec"
```

---

### EVERY_MINUTE
**Description:** Triggers every minute the player is in the contest

**Example:**
```yaml
scoring_rules:
  - event: EVERY_MINUTE
    conditions:
      time: NIGHT
      weather: THUNDER
    points: 10
    message: "&5Brave soul! +10 for surviving the storm"
```

---

## Advanced Examples

### Combo/Chain System
```yaml
scoring_rules:
  - event: BLOCK_BREAK
    conditions:
      material: STONE
      streak_material: true
    points: 1
    chain_multiplier: true
    chain_timeout_seconds: 3
    message: "&aStone x{chain}! +{points} points"
```

### Multi-Condition Complex Rule
```yaml
scoring_rules:
  - event: ENTITY_KILL
    conditions:
      entity_type: CREEPER
      time: NIGHT
      biome: FOREST
      player_sneaking: true
      player_has_item: DIAMOND_SWORD
      player_health_above: 15
      weather: RAIN
    points: 100
    message: "&6&lNINJA CREEPER HUNTER! +100 points"
```

### Dynamic Points with Formula
```yaml
scoring_rules:
  - event: BLOCK_PLACE
    conditions:
      material: DIAMOND_BLOCK
      min_y: 100
    formula: "base * ({y} / 10)"
    message: "&bSky builder! +{points} points"
```

### Conditional Commands
```yaml
scoring_rules:
  - event: PLAYER_LEVEL_UP
    points: 0
    on_success:
      - executor: CONDITIONAL
        command: "if:{level}>=50~then:give {player} diamond 1"
      - executor: PLAYER_MESSAGE
        command: "Level {level} reached!"
```

### Punishment Rules
```yaml
scoring_rules:
  - event: PLAYER_DEATH
    points: -50
    message: "&cYou died! -50 points"
    on_success:
      - executor: EFFECT
        command: "SLOW~60~1"
      - executor: PLAYER_MESSAGE
        command: "&7Respawn penalty: Slowness for 60s"
```

---

## Event Categories Quick Reference

**Block Events:** BLOCK_PLACE, BLOCK_BREAK, BLOCK_INTERACT, BLOCK_FERTILIZE, BLOCK_IGNITE

**Entity Events:** ENTITY_KILL, ENTITY_DAMAGE, ENTITY_TAME, ENTITY_BREED, ENTITY_SHEAR

**Player Events:** PLAYER_FISH, PLAYER_CRAFT, PLAYER_ENCHANT, PLAYER_BREW, PLAYER_LEVEL_UP, PLAYER_TRADE

**Combat Events:** PVP_KILL, PVP_DAMAGE, MOB_KILL, BOSS_KILL

**Time-Based:** PERIODIC, EVERY_SECOND, EVERY_MINUTE

**Special:** CONTEST_JOIN, LEAVE_AREA, RETURN_AREA

---

## Tips

1. **Use conditions to be specific** - More conditions = more precise scoring
2. **Combine events** - Use multiple rules for complex contests
3. **Test rules incrementally** - Start simple, add complexity
4. **Use formulas for dynamic scoring** - Scale points based on variables
5. **Add messages for feedback** - Players love knowing why they scored
6. **Use cooldowns to prevent spam** - Especially for time-based events
7. **Chain/combo systems** - Make contests more engaging
8. **Negative points** - Create risk/reward scenarios

---

See CONDITION_TYPES.md for complete condition reference.
See COMMAND_ACTIONS.md for complete command reference.
See CONFIG_EXAMPLES.md for full contest examples.
