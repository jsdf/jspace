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

    // get the speed this thing can move
    public int getSpeed() {
        return 0;
    }

    // useful for logging what thing this is, for debugging
    @Override
    public String toString() {
        return super.toString() + " at " + this.position.toString();
    }
}
