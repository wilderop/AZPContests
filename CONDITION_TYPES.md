# Condition Types Reference

Complete list of all supported condition types for scoring rules. Conditions determine when points should be awarded.

## Material Conditions

### material
**Description:** Block/item must be this exact material  
**Type:** String  
**Example:** `material: STONE`

### materials
**Description:** Block/item must be one of these materials  
**Type:** List or comma-separated string  
**Examples:**
```yaml
materials: [STONE, DIRT, COBBLESTONE]
# OR
materials: "STONE,DIRT,COBBLESTONE"
```

### material_tag
**Description:** Block/item must match this Minecraft tag  
**Type:** String  
**Examples:**
```yaml
material_tag: "minecraft:logs"
material_tag: "minecraft:planks"
material_tag: "minecraft:wool"
```

**Common Tags:**
- minecraft:logs
- minecraft:planks
- minecraft:wool
- minecraft:stone_bricks
- minecraft:flowers
- minecraft:saplings

### not_material
**Description:** Block/item must NOT be this material  
**Type:** String  
**Example:** `not_material: BEDROCK`

---

## Location Conditions

### min_y
**Description:** Y-coordinate must be >= this value  
**Type:** Integer  
**Example:** `min_y: 30`

### max_y
**Description:** Y-coordinate must be <= this value  
**Type:** Integer  
**Example:** `max_y: 60`

### exact_y
**Description:** Y-coordinate must be exactly this value  
**Type:** Integer  
**Example:** `exact_y: 64`

### in_area
**Description:** Must be inside the contest area  
**Type:** Boolean  
**Example:** `in_area: true`

### world
**Description:** Must be in this world  
**Type:** String  
**Example:** `world: world_nether`

### worlds
**Description:** Must be in one of these worlds  
**Type:** List  
**Example:** `worlds: [world, world_nether, world_the_end]`

### biome
**Description:** Must be in this biome  
**Type:** String  
**Examples:**
```yaml
biome: PLAINS
biome: DESERT
biome: JUNGLE
biome: NETHER_WASTES
```

**Common Biomes:**
- PLAINS, FOREST, TAIGA, DESERT, SAVANNA
- JUNGLE, SWAMP, MUSHROOM_FIELDS
- MOUNTAINS, SNOWY_TUNDRA
- NETHER_WASTES, CRIMSON_FOREST, WARPED_FOREST
- THE_END, END_HIGHLANDS

### biomes
**Description:** Must be in one of these biomes  
**Type:** List  
**Example:** `biomes: [PLAINS, FOREST, JUNGLE]`

### distance_from_spawn
**Description:** Distance from world spawn (supports <, >, <=, >=)  
**Type:** String  
**Examples:**
```yaml
distance_from_spawn: "<1000"   # Within 1000 blocks
distance_from_spawn: ">500"    # Beyond 500 blocks
```

---

## Time & Weather Conditions

### time
**Description:** Time of day must match  
**Type:** String  
**Values:** DAY, NIGHT, DAWN, DUSK  
**Example:** `time: NIGHT`

**Time Ranges:**
- DAY: 0-12000 ticks
- NIGHT: 12000-24000 ticks
- DAWN: 23000-1000 ticks
- DUSK: 11000-13000 ticks

### weather
**Description:** Weather must match  
**Type:** String  
**Values:** CLEAR, RAIN, THUNDER  
**Example:** `weather: THUNDER`

### moon_phase
**Description:** Moon phase must match  
**Type:** String  
**Values:** FULL, NEW, HALF, WAXING, WANING  
**Example:** `moon_phase: FULL`

### minecraft_day
**Description:** Minecraft day number (supports <, >, <=, >=)  
**Type:** String  
**Example:** `minecraft_day: ">100"`

---

## Entity Conditions

### entity_type
**Description:** Entity must be this type  
**Type:** String  
**Examples:**
```yaml
entity_type: ZOMBIE
entity_type: SKELETON
entity_type: ENDER_DRAGON
```

**Common Types:**
- Hostile: ZOMBIE, SKELETON, CREEPER, SPIDER, ENDERMAN
- Passive: COW, PIG, SHEEP, CHICKEN, HORSE
- Bosses: ENDER_DRAGON, WITHER
- Nether: PIGLIN, HOGLIN, GHAST, BLAZE

### entity_types
**Description:** Entity must be one of these types  
**Type:** List  
**Example:** `entity_types: [ZOMBIE, SKELETON, CREEPER]`

### entity_name
**Description:** Entity's custom name must exactly match  
**Type:** String  
**Example:** `entity_name: "Boss Zombie"`

### entity_name_contains
**Description:** Entity's custom name must contain this text  
**Type:** String  
**Example:** `entity_name_contains: "Boss"`

### entity_health_above
**Description:** Entity health must be > this value  
**Type:** Number  
**Example:** `entity_health_above: 50`

### entity_health_below
**Description:** Entity health must be < this value  
**Type:** Number  
**Example:** `entity_health_below: 10`

### is_baby
**Description:** Entity must be a baby  
**Type:** Boolean  
**Example:** `is_baby: true`

### is_adult
**Description:** Entity must be an adult  
**Type:** Boolean  
**Example:** `is_adult: true`

### is_boss
**Description:** Entity must be a boss mob  
**Type:** Boolean  
**Example:** `is_boss: true`

**Recognized Boss Mobs:**
- ENDER_DRAGON
- WITHER
- ELDER_GUARDIAN
- WARDEN

---

## Player Conditions

### player_gamemode
**Description:** Player's gamemode must match  
**Type:** String  
**Values:** SURVIVAL, CREATIVE, ADVENTURE, SPECTATOR  
**Example:** `player_gamemode: SURVIVAL`

### player_health_above
**Description:** Player health must be > this value  
**Type:** Number (max 20)  
**Example:** `player_health_above: 15`

### player_health_below
**Description:** Player health must be < this value  
**Type:** Number  
**Example:** `player_health_below: 5`

### player_hunger_above
**Description:** Player hunger must be > this value  
**Type:** Number (max 20)  
**Example:** `player_hunger_above: 15`

### player_hunger_below
**Description:** Player hunger must be < this value  
**Type:** Number  
**Example:** `player_hunger_below: 5`

### player_level_above
**Description:** Player level must be > this value  
**Type:** Integer  
**Example:** `player_level_above: 30`

### player_level_below
**Description:** Player level must be < this value  
**Type:** Integer  
**Example:** `player_level_below: 10`

### player_has_item
**Description:** Player must have this item in inventory  
**Type:** String (material name)  
**Example:** `player_has_item: DIAMOND_SWORD`

### player_has_items
**Description:** Player must have ALL of these items  
**Type:** List  
**Example:** `player_has_items: [DIAMOND_SWORD, DIAMOND_PICKAXE, GOLDEN_APPLE]`

### player_has_enchant
**Description:** Player must have item with this enchantment equipped/in hand  
**Type:** String  
**Example:** `player_has_enchant: FORTUNE`

### player_wearing_helmet
**Description:** Player must be wearing this helmet  
**Type:** String  
**Example:** `player_wearing_helmet: DIAMOND_HELMET`

### player_wearing_chestplate
**Description:** Player must be wearing this chestplate  
**Type:** String  
**Example:** `player_wearing_chestplate: DIAMOND_CHESTPLATE`

### player_wearing_leggings
**Description:** Player must be wearing these leggings  
**Type:** String  
**Example:** `player_wearing_leggings: DIAMOND_LEGGINGS`

### player_wearing_boots
**Description:** Player must be wearing these boots  
**Type:** String  
**Example:** `player_wearing_boots: DIAMOND_BOOTS`

### player_sneaking
**Description:** Player must be sneaking  
**Type:** Boolean  
**Example:** `player_sneaking: true`

### player_sprinting
**Description:** Player must be sprinting  
**Type:** Boolean  
**Example:** `player_sprinting: true`

### player_swimming
**Description:** Player must be swimming  
**Type:** Boolean  
**Example:** `player_swimming: true`

### player_flying
**Description:** Player must be flying  
**Type:** Boolean  
**Example:** `player_flying: true`

### player_on_fire
**Description:** Player must be on fire  
**Type:** Boolean  
**Example:** `player_on_fire: true`

### player_in_water
**Description:** Player must be in water  
**Type:** Boolean  
**Example:** `player_in_water: true`

### player_in_lava
**Description:** Player must be in lava  
**Type:** Boolean  
**Example:** `player_in_lava: true`

### player_on_ground
**Description:** Player must be on ground (not airborne)  
**Type:** Boolean  
**Example:** `player_on_ground: true`

---

## Item Conditions

### item_name
**Description:** Item's display name must exactly match  
**Type:** String  
**Example:** `item_name: "Legendary Sword"`

### item_name_contains
**Description:** Item's display name must contain this text  
**Type:** String  
**Example:** `item_name_contains: "Legendary"`

### item_lore_contains
**Description:** Item's lore must contain this text  
**Type:** String  
**Example:** `item_lore_contains: "Powerful"`

### item_enchant
**Description:** Item must have this enchantment  
**Type:** String  
**Example:** `item_enchant: SHARPNESS`

### item_enchants
**Description:** Item must have ALL of these enchantments  
**Type:** List  
**Example:** `item_enchants: [SHARPNESS, LOOTING, UNBREAKING]`

### item_enchant_level
**Description:** Enchantment must be at minimum level  
**Type:** String (format: ENCHANT:LEVEL)  
**Example:** `item_enchant_level: "SHARPNESS:5"`

### item_durability_above
**Description:** Item durability must be > this value  
**Type:** Integer  
**Example:** `item_durability_above: 100`

### item_amount_above
**Description:** Item stack size must be > this value  
**Type:** Integer  
**Example:** `item_amount_above: 32`

---

## Tool/Weapon Conditions

### require_tool_type
**Description:** Requires specific tool type  
**Type:** String  
**Values:** PICKAXE, AXE, SHOVEL, HOE, SWORD, NONE  
**Example:** `require_tool_type: PICKAXE`

### require_tool_material
**Description:** Requires specific tool material  
**Type:** String  
**Values:** WOOD, STONE, IRON, GOLD, DIAMOND, NETHERITE  
**Example:** `require_tool_material: DIAMOND`

### require_enchant
**Description:** Requires tool to have this enchantment  
**Type:** String  
**Example:** `require_enchant: FORTUNE`

### require_enchant_level
**Description:** Requires enchant at minimum level  
**Type:** String (format: ENCHANT:LEVEL)  
**Example:** `require_enchant_level: "FORTUNE:3"`

---

## Special Conditions

### only_if_not_self_placed
**Description:** Only score if player didn't place this block  
**Type:** Boolean  
**Example:** `only_if_not_self_placed: true`

**Use Case:** Prevent players from placing and breaking same blocks for points

### friendly_fire
**Description:** Allow scoring on same-team players  
**Type:** Boolean  
**Example:** `friendly_fire: false`

**Use Case:** Disable PvP scoring within teams

### chance
**Description:** Probability of this rule triggering (0.0-1.0)  
**Type:** Number  
**Examples:**
```yaml
chance: 0.5   # 50% chance
chance: 0.1   # 10% chance
chance: 0.01  # 1% chance
```

### cooldown_seconds
**Description:** Seconds before this rule can trigger again for same player  
**Type:** Integer  
**Example:** `cooldown_seconds: 10`

### require_line_of_sight
**Description:** Requires clear line of sight to target  
**Type:** Boolean  
**Example:** `require_line_of_sight: true`

### block_light_level
**Description:** Block light level must match (supports <, >, <=, >=)  
**Type:** String  
**Examples:**
```yaml
block_light_level: ">10"   # Well lit
block_light_level: "<5"    # Dark
```

### sky_light_level
**Description:** Sky light level must match  
**Type:** String  
**Example:** `sky_light_level: ">10"`

---

## Chain/Combo Conditions

### chain_multiplier
**Description:** Multiply points by chain count  
**Type:** Boolean  
**Example:** `chain_multiplier: true`

**How it works:** Each consecutive trigger increases multiplier
- 1st: x1 points
- 2nd: x2 points
- 3rd: x3 points
- etc.

### chain_timeout_seconds
**Description:** Seconds before chain resets  
**Type:** Integer  
**Default:** 5  
**Example:** `chain_timeout_seconds: 3`

### streak_bonus
**Description:** Bonus points added per streak count  
**Type:** Integer  
**Example:** `streak_bonus: 10`

**How it works:** 
- 5 streak = +50 points (10 * 5)
- 10 streak = +100 points (10 * 10)

### streak_material
**Description:** Must match same material to continue streak  
**Type:** Boolean  
**Example:** `streak_material: true`

---

## Advanced Conditions

### formula
**Description:** Custom formula for point calculation  
**Type:** String  
**Variables:** {base}, {y}, {health}, {level}, {chain}, {streak}, etc.  
**Examples:**
```yaml
# Scale by Y-level
formula: "base * ({y} / 10)"

# Scale by player health
formula: "base * ({health} / 20)"

# Complex formula
formula: "(base + streak * 5) * chain"
```

### damage_dealt
**Description:** Damage dealt must match (supports <, >, <=, >=)  
**Type:** String  
**Example:** `damage_dealt: ">10"`

### damage_type
**Description:** Damage type must match  
**Type:** String  
**Values:** PROJECTILE, MELEE, MAGIC, FIRE, FALL, EXPLOSION  
**Example:** `damage_type: PROJECTILE`

### death_cause
**Description:** Death cause must match  
**Type:** String  
**Values:** FALL, FIRE, DROWNING, LAVA, SUFFOCATION, EXPLOSION, etc.  
**Example:** `death_cause: FALL`

### fish_type
**Description:** Fish caught must match  
**Type:** String  
**Values:** COD, SALMON, TROPICAL_FISH, PUFFERFISH  
**Example:** `fish_type: SALMON`

---

## Logical Operators

### NOT
**Description:** Negates the condition  
**Type:** Boolean  
**Example:**
```yaml
conditions:
  material: STONE
  NOT: true  # NOT stone (anything except stone)
```

### AND
**Description:** All sub-conditions must be true  
**Type:** List of condition maps  
**Example:**
```yaml
AND:
  - material: STONE
  - min_y: 30
  - max_y: 60
```

### OR
**Description:** Any sub-condition must be true  
**Type:** List of condition maps  
**Example:**
```yaml
OR:
  - material: STONE
  - material: DIRT
  - material: COBBLESTONE
```

---

## Complex Condition Examples

### Multi-Layer Conditions
```yaml
conditions:
  # Must be stone OR dirt
  OR:
    - material: STONE
    - material: DIRT
  
  # AND must be between Y30-60
  min_y: 30
  max_y: 60
  
  # AND must be at night
  time: NIGHT
  
  # AND player must be sneaking
  player_sneaking: true
```

### Nested Logic
```yaml
conditions:
  AND:
    - material: DIAMOND_ORE
    - OR:
        - require_enchant: FORTUNE
        - require_enchant_level: "EFFICIENCY:5"
    - min_y: 0
    - max_y: 16
```

### Equipment Requirements
```yaml
conditions:
  player_wearing_helmet: DIAMOND_HELMET
  player_wearing_chestplate: DIAMOND_CHESTPLATE
  player_wearing_leggings: DIAMOND_LEGGINGS
  player_wearing_boots: DIAMOND_BOOTS
  player_has_item: DIAMOND_SWORD
```

### Environmental Combo
```yaml
conditions:
  biome: DESERT
  time: DAY
  weather: CLEAR
  player_health_below: 5
  player_in_lava: false
  # Reward survival in harsh desert during day
```

---

## Tips

1. **Start Simple** - Add conditions incrementally
2. **Test Thoroughly** - Some condition combinations might conflict
3. **Use OR for variants** - E.g., multiple valid materials
4. **Use AND for strict requirements** - All must be true
5. **Cooldowns prevent spam** - Essential for time-based events
6. **Chance adds randomness** - Makes contests less predictable
7. **Formulas enable scaling** - Dynamic points based on variables
8. **Chain/Streak systems** - Reward consecutive actions
9. **Combine environmental conditions** - Time + weather + biome = unique scenarios
10. **NOT operator** - Exclude specific cases

---

See EVENT_TYPES.md for event reference.
See COMMAND_ACTIONS.md for command reference.
See CONFIG_EXAMPLES.md for complete examples.
