package jspace;

public class Vector2d {
    public double x = 0;
    public double y = 0;

    Vector2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2d copy() {
        return new Vector2d(
            this.x,
            this.y
        );
    }

    public Vector2d add(Vector2d other) {
        return new Vector2d(
                this.x + other.x,
                this.y + other.y
        );
    }

    // useful for finding the direction to one vector from another
    public Vector2d subtract(Vector2d other) {
        return new Vector2d(
                this.x - other.x,
                this.y - other.y
        );
    }

    public Vector2d multiplyScalar(double magnitude) {
        return new Vector2d(
            this.x * magnitude,
            this.y * magnitude
        );
    }

    // when you want to keep the direction of a vector, but discard the magnitude
    // you want the 'unit vector' aka 'normalized vector'
    public Vector2d normalize() {
        if (this.x == 0 && this.y == 0) {
            return new Vector2d(0, 0);
        }
        double magnitude = Math.sqrt(this.x * this.x + this.y * this.y);

        return new Vector2d(
            this.x / magnitude,
            this.y / magnitude
        );
    }

    @Override
    public String toString() {
        return "{x="+String.valueOf(this.x)+", y="+String.valueOf(this.y)+"}";
    }
}
