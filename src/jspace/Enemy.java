package jspace;

public class Enemy extends ShootingObject {
    Enemy(Type type) {
        super(type);
    }


    public int getSpeed() {
        switch (this.type) {
            case ship1: return 30;
            case ship2: return 50;
            case ship3: return 60;
            case ship4: return 40;
        }
        throw new RuntimeException("invalid enemy type");
    }

    // get the cooldown time before this thing can shoot again, based on what type it is
    public double getCooldownTime() {
        switch (this.type) {
            case ship1: return 1;
            case ship2: return 1;
            case ship3: return 1;
            case ship4: return 1;
        }
        throw new RuntimeException("invalid enemy type");
    }
}
