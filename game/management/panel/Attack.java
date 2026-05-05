package game.management.panel;

import game.base.Character;
import game.base.Enemy;
import game.base.Skill;
import game.base.Boss;
import game.enemies.bosses.Boneclaw;
import game.enemies.bosses.IronMaw;
import game.enemies.bosses.Venomshade;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Attack {


    // Reference to the Graphical User Interface for updating the battle log
    private final GameVisuals gui;


    // Constructor to initialize the Attack handler with GUI and Battle manager references
    public Attack(GameVisuals gui, Battle manager) {
        this.gui = gui;
    }


    // Handles the logic when a player uses a specific skill on an enemy
    public boolean resolvePlayerAction(Character character, Skill skill, Enemy currentEnemy) {
        try {
            // Check for null pointers to prevent crashes during action resolution
            if (character == null || skill == null || currentEnemy == null) {
                gui.battleLogArea.append("[ERROR] Invalid action: Character, skill, or enemy is missing.\n");
                return currentEnemy != null && currentEnemy.isAlive();
            }


            // Verify if the player character is conscious
            if (character.currentHP <= 0) {
                gui.battleLogArea.append(character.name + " is knocked out and cannot act!\n");
                character.currentResource = 0;
                return true;
            }


            // Check if the player has enough Mana/Stamina to perform the skill
            if (character.currentResource < skill.cost) {
                gui.battleLogArea.append("[ERROR] " + character.name + " attempted to use " + skill.name + " but lacked the resources!\n");
                return true;
            }


            // Deduct resource cost and apply damage to the enemy
            character.currentResource -= skill.cost;
            int damageDealt = skill.damage;
            currentEnemy.takeDamage(damageDealt);


            // Log the outcome of the player's attack to the GUI
            gui.battleLogArea.append(character.name + " channels their energy and unleashes " + skill.name + "!\n");
            gui.battleLogArea.append("The attack strikes " + currentEnemy.name + " dealing "+ skill.damage+" damage.\n\n");


            return currentEnemy.isAlive();
        } catch (Exception e) {
            // General error handling for unexpected issues during combat calculations
            System.err.println("Error resolving player action: " + e.getMessage());
            e.printStackTrace();
            gui.battleLogArea.append("[ERROR] An unexpected error stopped the player's action.\n");
            return currentEnemy != null && currentEnemy.isAlive();
        }
    }




    // Logic for the enemy's turn, choosing a target and executing an attack
    public void resolveEnemyAction(Enemy currentEnemy, List<Character> playerParty) {
        // Filter out party members who are already defeated
        List<Character> aliveCharacters = getAliveCharacters(playerParty);


        if (aliveCharacters.isEmpty()) {
            return;
        }


        // Randomly select a target from the surviving party members
        Random rand = new Random();
        Character target = aliveCharacters.get(rand.nextInt(aliveCharacters.size()));


        // Determine if the enemy is a standard unit or a Boss with special moves
        if (currentEnemy instanceof Boss) {
            performBossMove((Boss)currentEnemy, target);
        } else {
            performNormalEnemyMove(currentEnemy, target);
        }
    }


    // Handles standard attack behavior for non-boss enemies
    private void performNormalEnemyMove(Enemy enemy, Character target) {
        int damage = enemy.damage;
        enemy.attack(target);


        gui.battleLogArea.append("The " + enemy.name + " lunges forward violently to "+ target.name + "!\n");
        gui.battleLogArea.append("                                       >>> " + target.name + " takes "
                + damage + " damage!\n");
    }


    // Handles complex logic for Bosses, including resource checking and skill probabilities
    private void performBossMove(Boss boss, Character target) {
        Random rand = new Random();


        // Check if boss has enough mana and passes a 40% chance roll to use a special skill
        boolean canUseSkill = boss.currentMana >= 20;
        boolean wantsToUseSkill = rand.nextInt(100) < 40; // 40% chance


        // Executes the boss's special attack
        if (canUseSkill && wantsToUseSkill) {


            boss.currentMana -= 20; // 20 mana cost per skill
            boss.useSkill(target);


            gui.battleLogArea.append(boss.name + " unleashes a DEVASTATING attack on " + target.name + "!\n");


            // Logic for specific Boss sub-type behaviors (lifesteal or healing)
            if(boss instanceof Boneclaw){
                gui.battleLogArea.append("Boneclaw drains 10 HP from " + target.name + " and absorbs it!\n");
            } else if(boss instanceof IronMaw || boss instanceof Venomshade){
                gui.battleLogArea.append(boss.name + " heals himself by 20 HP!");
            } else {
                gui.battleLogArea.append("DR. ALCAZAR USES HIS SKILL: HIT " + target.name + " by "+ boss.damage +"!\n" );
            }
            gui.battleLogArea.append("(Boss Mana Remaining: " + boss.currentMana + ")\n\n");


        } else {
            // Executes the boss's basic attack if skill conditions aren't met
            int damage = boss.damage;
            boss.attack(target);


            gui.battleLogArea.append("\nBOSS: " + boss.name + " strikes " + target.name + ".\n");
            gui.battleLogArea.append("                                          -> " + target.name + " took " + damage +
                    " damage.\n");
        }
    }


    // Utility method to scan a list of characters and return only those with HP > 0
    private List<Character> getAliveCharacters(List<Character> party) {
        List<Character> alive = new ArrayList<>();
        try {
            for (Character c : party) {
                if (c.currentHP > 0) {
                    alive.add(c);
                }
            }
        } catch (Exception e) {
            System.err.println("Error while filtering alive characters: " + e.getMessage());
            e.printStackTrace();
        }
        return alive;
    }
}
