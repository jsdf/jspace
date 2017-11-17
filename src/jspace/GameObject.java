package jspace;

/*
 * This is an object in the game world, like an enemy, a projectile, or the player
 */
public class GameObject {
    // x,y position in the game world
    public Vector2d position;
    // the type of object this is (there are multiple enemy and projectile types)
    public Type type;

    public enum Type {
        player,
        ship1,
        ship2,
        ship3,
        ship4,
        player_projectile,
        enemy_projectile,
    }

    GameObject(Type type) {
        this.position = new Vector2d(0, 0);
        this.type = type;
    }

    // get the speed this thing can move, based on what type it is
    public int getSpeed() {
        switch (this.type) {
            case player: return 150;
            case ship1: return 30;
            case ship2: return 50;
            case ship3: return 60;
            case ship4: return 40;
            case enemy_projectile: return 200;
            case player_projectile: return 200;
        }
        return 0;
    }

    // get the cooldown time before this thing can shoot again, based on what type it is
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

    // useful for logging what thing this is, for debugging
    @Override
    public String toString() {
        return super.toString() + " at " + this.position.toString();
    }
}
