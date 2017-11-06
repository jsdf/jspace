package jspace;

/*
 * This is an object in the game world, like an enemy, a projectile, or the player
 */
public class GameObject {
    public Vector2d position;
    public Category category;
    public Type type;
    public double lastShot = 0;

    public enum Category {
        player,
        enemy,
        projectile,
    }

    public enum Type {
        player,
        ship1,
        ship2,
        ship3,
        ship4,
        player_projectile,
        enemy_projectile,
    }

    GameObject(Category category, Type type) {
        this.position = new Vector2d(0, 0);
        this.category = category;
        this.type = type;
    }


    public int getSpeed() {
        switch (this.type) {
            case player: return 100;
            case ship1: return 30;
            case ship2: return 50;
            case ship3: return 60;
            case ship4: return 40;
            case enemy_projectile: return 200;
            case player_projectile: return 200;
        }
        return 0;
    }

    public double getCooldownTime() {
        switch (this.type) {
            case player: return 0.2;
            case ship1: return 1;
            case ship2: return 1;
            case ship3: return 1;
            case ship4: return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return super.toString() + " at " + this.position.toString();
    }
}
