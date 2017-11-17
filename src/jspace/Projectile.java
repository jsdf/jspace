package jspace;

public class Projectile extends GameObject {
    Projectile (Type type) {
        super(type);
    }

    public int getSpeed() {
        switch (this.type) {
            case enemy_projectile: return 200;
            case player_projectile: return 200;
        }
        throw new RuntimeException("invalid projectile type");
    }
}
