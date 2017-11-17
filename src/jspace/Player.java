package jspace;

public class Player extends ShootingObject {
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
