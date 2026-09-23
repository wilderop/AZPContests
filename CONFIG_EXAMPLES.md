# Complete Contest Configuration Examples

Real-world contest examples using the flexible rule-based system.

## Table of Contents
- [Simple Contests](#simple-contests)
- [Building Contests](#building-contests)
- [Combat Contests](#combat-contests)
- [Survival Challenges](#survival-challenges)
- [Hybrid Contests](#hybrid-contests)
- [Advanced Features](#advanced-features)

---

## Simple Contests

### Basic Mining Contest
```yaml
contests:
  cobble_mining:
    duration_minutes: 20
    warning_minutes: 5
    
    area:
      world: world
      min_x: -300
      max_x: 300
      min_y: 0
      max_y: 256
      min_z: -300
      max_z: 300
    
    scoring_rules:
      - event: BLOCK_BREAK
        conditions:
          material: COBBLESTONE
          in_area: true
        points: 1
        message: "&7+1 cobblestone"
    
    messages:
      start_broadcast: "&6Cobble Mining Contest Started!"
      end_broadcast: "&6Contest Ended! Winner: {winner_name}"
    
    prizes:
      winner_item:
        material: DIAMOND_PICKAXE
        name: "&bMiner's Champion Pickaxe"
        enchants:
          EFFICIENCY: 5
          FORTUNE: 3
          UNBREAKING: 3
      winner_prize_amount: 1
```

---

### Fishing Contest
```yaml
contests:
  fishing_tournament:
    duration_minutes: 15
    
    area:
      world: world
      min_x: -500
      max_x: 500
      min_y: 0
      max_y: 256
      min_z: -500
      max_z: 500
    
    scoring_rules:
      # Regular fish
      - event: PLAYER_FISH_SUCCESS
        conditions:
          fish_type: COD
        points: 5
      
      # Salmon worth more
      - event: PLAYER_FISH_SUCCESS
        conditions:
          fish_type: SALMON
        points: 10
      
      # Rare fish
      - event: PLAYER_FISH_SUCCESS
        conditions:
          fish_type: PUFFERFISH
        points: 25
        message: "&6Rare catch! +25 points"
      
      # Rain bonus
      - event: PLAYER_FISH_SUCCESS
        conditions:
          weather: RAIN
        points: 5
        message: "&bRainy day bonus! +5"
    
    prizes:
      winner_item:
        material: FISHING_ROD
        name: "&bMaster Angler's Rod"
        enchants:
          LUCK_OF_THE_SEA: 3
          LURE: 3
          UNBREAKING: 3
          MENDING: 1
```

---

## Building Contests

### Y-Level Building Challenge
```yaml
contests:
  spawn_building:
    duration_minutes: 20
    
    area:
      world: world
      min_x: -300
      max_x: 300
      min_y: 0
      max_y: 256
      min_z: -300
      max_z: 300
    
    scoring_rules:
      # Stone at Y30-60
      - event: BLOCK_PLACE
        conditions:
          material: STONE
          min_y: 30
          max_y: 60
          in_area: true
        points: 1
      
      # Dirt at Y60-80
      - event: BLOCK_PLACE
        conditions:
          material: DIRT
          min_y: 60
          max_y: 80
          in_area: true
        points: 1
      
      # Diamond blocks at high altitude (bonus)
      - event: BLOCK_PLACE
        conditions:
          material: DIAMOND_BLOCK
          min_y: 100
          in_area: true
        formula: "base * ({y} / 10)"
        message: "&6Sky builder! +{points} points"
      
      # Breaking own blocks penalty
      - event: BLOCK_BREAK
        conditions:
          materials: [STONE, DIRT]
          only_if_not_self_placed: true
          in_area: true
        points: -1
        message: "&cDon't break others' work! -1"
    
    prizes:
      winner_item:
        material: DIAMOND_PICKAXE
        name: "&bMaster Builder's Tool"
        lore:
          - "&7Awarded to the greatest builder"
        enchants:
          EFFICIENCY: 5
          UNBREAKING: 5
```

---

## Combat Contests

### Mob Slayer Challenge
```yaml
contests:
  mob_slayer:
    duration_minutes: 30
    
    area:
      world: world
      min_x: -500
      max_x: 500
      min_y: 0
      max_y: 256
      min_z: -500
      max_z: 500
    
    scoring_rules:
      # Regular hostile mobs
      - event: ENTITY_KILL
        conditions:
          entity_types: [ZOMBIE, SKELETON, SPIDER, CREEPER]
          in_area: true
        points: 10
      
      # Night bonus
      - event: ENTITY_KILL
        conditions:
          entity_types: [ZOMBIE, SKELETON]
          time: NIGHT
          in_area: true
        points: 5
        message: "&eNight hunter bonus! +5"
      
      # Headshot bonus (projectile kill)
      - event: ENTITY_KILL
        conditions:
          damage_type: PROJECTILE
          in_area: true
        points: 10
        message: "&6Headshot! +10"
      
      # Enderman (harder)
      - event: ENTITY_KILL
        conditions:
          entity_type: ENDERMAN
          in_area: true
        points: 25
      
      # Boss mobs
      - event: BOSS_KILL
        points: 500
        on_success:
          - executor: BROADCAST
            command: "&5&l{player} DEFEATED A BOSS!"
          - executor: TITLE
            command: "&5BOSS KILL!~&e+500 Points~10~70~20"
      
      # Death penalty
      - event: PLAYER_DEATH
        points: -50
        on_success:
          - executor: PLAYER_MESSAGE
            command: "&cDeath penalty: -50 points"
          - executor: EFFECT
            command: "SLOW~30~0"
    
    prizes:
      winner_item:
        material: NETHERITE_SWORD
        name: "&4Demon Slayer"
        lore:
          - "&7Forged in the heat of battle"
          - "&7Kills: &cInfinite"
        enchants:
          SHARPNESS: 5
          LOOTING: 3
          UNBREAKING: 3
          SWEEPING: 3
```

---

### PvP Arena
```yaml
contests:
  pvp_arena:
    duration_minutes: 15
    
    area:
      world: world
      min_x: -100
      max_x: 100
      min_y: 60
      max_y: 100
      min_z: -100
      max_z: 100
    
    scoring_rules:
      # Kill = points
      - event: PVP_KILL
        conditions:
          friendly_fire: false
          in_area: true
        points: 100
        on_success:
          - executor: BROADCAST
            command: "&c{player} &7eliminated an opponent!"
          - executor: HEAL
            command: "FULL"
          - executor: SOUND
            command: "ENTITY_PLAYER_LEVELUP~1.0~1.0"
      
      # Assist damage
      - event: PVP_DAMAGE
        conditions:
          damage_dealt: ">5"
          in_area: true
        points: 5
      
      # Kill streak bonuses
      - event: PVP_KILL
        conditions:
          in_area: true
        points: 0
        on_success:
          - executor: CONDITIONAL
            command: "if:{streak}==3~then:PLAYER_MESSAGE:&6&lKILLING SPREE!"
          - executor: CONDITIONAL
            command: "if:{streak}==5~then:BROADCAST:&c{player} is on a RAMPAGE!"
          - executor: CONDITIONAL
            command: "if:{streak}==10~then:BROADCAST:&4&l{player} is UNSTOPPABLE!"
      
      # Death penalty
      - event: PVP_DEATH
        points: -25
        on_success:
          - executor: PLAYER_MESSAGE
            command: "&7You were eliminated! -25 points"
```

---

## Survival Challenges

### Nether Survival
```yaml
contests:
  nether_survival:
    duration_minutes: 30
    
    area:
      world: world_nether
      min_x: -1000
      max_x: 1000
      min_y: 0
      max_y: 128
      min_z: -1000
      max_z: 1000
    
    scoring_rules:
      # Survival points per minute
      - event: EVERY_MINUTE
        conditions:
          world: world_nether
          player_gamemode: SURVIVAL
          in_area: true
        points: 10
        message: "&6Survival bonus! +10 points"
      
      # Bonus for low health survival
      - event: EVERY_MINUTE
        conditions:
          player_health_below: 5
          world: world_nether
        points: 20
        message: "&c&lDanger Zone Bonus! +20"
      
      # Ancient debris mining
      - event: BLOCK_BREAK
        conditions:
          material: ANCIENT_DEBRIS
          in_area: true
        points: 100
        on_success:
          - executor: BROADCAST
            command: "&6{player} found Ancient Debris!"
          - executor: SOUND
            command: "UI_TOAST_CHALLENGE_COMPLETE~1.0~1.0"
      
      # Nether mob kills
      - event: ENTITY_KILL
        conditions:
          entity_types: [PIGLIN, HOGLIN, GHAST, BLAZE]
          in_area: true
        points: 15
      
      # Wither skeleton (harder)
      - event: ENTITY_KILL
        conditions:
          entity_type: WITHER_SKELETON
          in_area: true
        points: 50
        message: "&5Wither Skeleton defeated! +50"
      
      # Death = heavy penalty
      - event: PLAYER_DEATH
        points: -100
        on_success:
          - executor: PLAYER_MESSAGE
            command: "&4&lDEATH IN NETHER: -100 POINTS"
```

---

## Hybrid Contests

### Ultimate Survival Challenge
```yaml
contests:
  ultimate_survival:
    duration_minutes: 60
    
    area:
      world: world
      min_x: -1000
      max_x: 1000
      min_y: 0
      max_y: 256
      min_z: -1000
      max_z: 1000
    
    scoring_rules:
      # Mining valuable ores
      - event: BLOCK_BREAK
        conditions:
          material: DIAMOND_ORE
          require_tool_type: PICKAXE
        points: 50
      
      - event: BLOCK_BREAK
        conditions:
          material: EMERALD_ORE
        points: 75
      
      # Mob hunting
      - event: ENTITY_KILL
        conditions:
          is_boss: false
        points: 10
      
      - event: BOSS_KILL
        points: 1000
      
      # Crafting achievements
      - event: PLAYER_CRAFT
        conditions:
          material: DIAMOND_SWORD
        points: 100
      
      - event: PLAYER_CRAFT
        conditions:
          material: ENCHANTING_TABLE
        points: 150
      
      # Building
      - event: BLOCK_PLACE
        conditions:
          material: BEACON
        points: 500
        message: "&5&lBEACON PLACED! +500 points"
      
      # Enchanting
      - event: PLAYER_ENCHANT
        conditions:
          item_enchant_level: "SHARPNESS:5"
        points: 200
      
      # Trading
      - event: PLAYER_TRADE
        points: 20
      
      # Farming
      - event: ENTITY_BREED
        points: 15
      
      # Exploration bonus
      - event: PERIODIC
        cooldown_seconds: 300
        conditions:
          distance_from_spawn: ">1000"
        points: 50
        message: "&eExplorer bonus! +50"
    
    prizes:
      winner_item:
        material: NETHERITE_PICKAXE
        name: "&5&lULTIMATE SURVIVOR'S TOOL"
        lore:
          - "&7Forged through trials"
          - "&7Nothing can stop you now"
        enchants:
          EFFICIENCY: 5
          FORTUNE: 3
          UNBREAKING: 3
          MENDING: 1
      winner_prize_amount: 1
```

---

## Advanced Features

### Combo System Contest
```yaml
contests:
  stone_breaker:
    duration_minutes: 15
    
    area:
      world: world
      min_x: -300
      max_x: 300
      min_y: 0
      max_y: 256
      min_z: -300
      max_z: 300
    
    scoring_rules:
      - event: BLOCK_BREAK
        conditions:
          material: STONE
          in_area: true
          streak_material: true
        points: 1
        chain_multiplier: true
        chain_timeout_seconds: 3
        streak_bonus: 2
        message: "&aStone x{chain}! +{points} points"
        on_success:
          - executor: CONDITIONAL
            command: "if:{chain}>=10~then:ACTIONBAR:&6&lx{chain} COMBO!"
          - executor: CONDITIONAL
            command: "if:{chain}>=20~then:EFFECT:SPEED~10~1"
          - executor: CONDITIONAL
            command: "if:{chain}>=50~then:BROADCAST:&6{player} has a &l&nx{chain} MEGA COMBO!"
    
    prizes:
      winner_item:
        material: DIAMOND_PICKAXE
        name: "&6Combo Master's Pickaxe"
```

---

### Progressive Rewards
```yaml
contests:
  progressive_mining:
    duration_minutes: 30
    
    scoring_rules:
      - event: BLOCK_BREAK
        conditions:
          material: STONE
        points: 1
        on_success:
          # Milestone messages
          - executor: CONDITIONAL
            command: "if:{score}==100~then:PLAYER_MESSAGE:&aMilestone: 100 stones!"
          
          # Progressive rewards
          - executor: CONDITIONAL
            command: "if:{score}==100~then:GIVE_ITEM:GOLDEN_APPLE~1"
          
          - executor: CONDITIONAL
            command: "if:{score}==500~then:EXECUTE_SERIES:GIVE_ITEM:DIAMOND~5|PLAYER_MESSAGE:&6500 milestone! +5 diamonds"
          
          - executor: CONDITIONAL
            command: "if:{score}==1000~then:BROADCAST:&6&l{player} REACHED 1000 POINTS!"
          
          # Speed boost every 100
          - executor: CONDITIONAL
            command: "if:{score}%100==0~then:EFFECT:SPEED~30~1"
```

---

### Random Events
```yaml
contests:
  lucky_miner:
    duration_minutes: 20
    
    scoring_rules:
      - event: BLOCK_BREAK
        conditions:
          material: STONE
        points: 1
        on_success:
          # 10% chance for bonus
          - executor: CHANCE
            command: "0.1~PLAYER_MESSAGE:&6&lLUCKY BREAK! Bonus diamond!"
          - executor: CHANCE
            command: "0.1~GIVE_ITEM:DIAMOND~1"
          - executor: CHANCE
            command: "0.1~SOUND:ENTITY_PLAYER_LEVELUP~1~2"
          
          # 1% chance for jackpot
          - executor: CHANCE
            command: "0.01~EXECUTE_SERIES:BROADCAST:&6&l{player} HIT THE JACKPOT!|GIVE_ITEM:DIAMOND_BLOCK~1|TITLE:&6&lJACKPOT!~&eDiamond Block!~10~70~20"
```

---

### Anti-Camping System
```yaml
contests:
  no_camping:
    duration_minutes: 20
    
    scoring_rules:
      - event: BLOCK_BREAK
        conditions:
          material: DIAMOND_ORE
        points: 50
        cooldown_seconds: 30
        message: "&bDiamond ore! +50 (30s cooldown)"
      
      # Penalty for staying still
      - event: PERIODIC
        cooldown_seconds: 60
        conditions:
          # Custom metadata check (would need to track movement)
          player_has_metadata: "has_moved_recently"
          NOT: true
        points: -10
        on_success:
          - executor: PLAYER_MESSAGE
            command: "&cKeep moving! -10 camping penalty"
          - executor: DAMAGE
            command: "2"
```

---

### Team-Based Contest
```yaml
contests:
  team_battle:
    duration_minutes: 30
    
    scoring_rules:
      # Team kills
      - event: ENTITY_KILL
        conditions:
          friendly_fire: false
        points: 10
        on_success:
          - executor: BROADCAST
            command: "&e{player}'s team scored +10!"
      
      # Team death penalty
      - event: PLAYER_DEATH
        points: -5
        on_success:
          - executor: BROADCAST
            command: "&c{player}'s team lost 5 points"
      
      # Teamwork bonus (assist)
      - event: ENTITY_DAMAGE
        conditions:
          damage_dealt: ">5"
          same_team: true
        points: 2
        message: "&aTeamwork! +2"
```

---

## Tips for Creating Contests

1. **Start Simple** - Begin with basic rules, add complexity
2. **Test Thoroughly** - Try all edge cases
3. **Balance Points** - Common actions = low points, rare = high points
4. **Use Cooldowns** - Prevent spam and grinding
5. **Add Feedback** - Players love knowing why they scored
6. **Mix Events** - Combine multiple event types
7. **Progressive Rewards** - Keep players engaged throughout
8. **Anti-Cheese** - Prevent exploits with smart conditions
9. **Visual Feedback** - Titles, sounds, particles make it fun
10. **Clear Rules** - Document what players should do

---

See EVENT_TYPES.md for all available events.
See CONDITION_TYPES.md for all available conditions.
See COMMAND_ACTIONS.md for all available commands.
