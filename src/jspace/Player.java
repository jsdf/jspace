package jspace;

public class Player extends GameObject {
    // time that player last fired a projectile. used to figure out when they can fire another.
    public double lastShot = 0;

    Player() {
        super(Type.player);
    }


    public int getSpeed() {
        return 150;
    }

    // get the cooldown time before the player can shoot again
    public double getCooldownTime() {
        return 0.2;
    }
}
