package jspace;

public class ShootingObject extends GameObject {
    // time that this thing last fired a projectile. used to out when they can fire another.
    public double lastShot = 0;

    ShootingObject(Type type) {
        super(type);
    }
}
