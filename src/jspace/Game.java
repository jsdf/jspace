package jspace;

import java.awt.*;
import java.awt.image.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;

public class Game {
    // the AWT graphics object which we can use to draw stuff. this is set in the Main class
	public Graphics graphics;
    // the screen width. this is set in the Main class
	public int screenWidth = 800;
    // the screen height. this is set in the Main class
	public int screenHeight = 600;
	// the keyboard keys currently pressed. this is set in the Main class
	public HashSet<String> keysDown;
	// to keep track of when the last enemy spawned, so we know if its time to spawn another
	private double lastEnemySpawnTime = 0;
	// time since the game started running
	private double time = 0;
	// a mapping of the types of game object to their images
	private HashMap<GameObject.Type, BufferedImage> images;
	// a list of all the game objects currently in the game world
	private ArrayList<GameObject> worldObjects;
	// the player
	private Player player;
	// a list of the enemy game object types, which we'll use when randomly selecting one
	private static final GameObject.Type[] ENEMY_TYPES = {
        GameObject.Type.ship1,
        GameObject.Type.ship2,
        GameObject.Type.ship3,
        GameObject.Type.ship4,
	};
	// how often enemies spawn (in seconds)
	private static final int ENEMY_SPAWN_INTERVAL = 3;
	// distance objects can go out of bounds before being destroyed
	private static final int OFFSCREEN_SPACE = 200;


	Game() {
		// load all the images and put them into a map of gameobject type to image
        // then later we can look up the image for a type of gameobject by calling this.images.get(type)
		this.images = new HashMap<GameObject.Type, BufferedImage>();
		this.images.put(GameObject.Type.player, Utils.loadImage("images/ship5.png"));
		this.images.put(GameObject.Type.ship1, Utils.loadImage("images/ship1.png"));
		this.images.put(GameObject.Type.ship2, Utils.loadImage("images/ship2.png"));
		this.images.put(GameObject.Type.ship3, Utils.loadImage("images/ship3.png"));
		this.images.put(GameObject.Type.ship4, Utils.loadImage("images/ship4.png"));
		this.images.put(GameObject.Type.player_projectile, Utils.loadImage("images/projectile2.png"));
		this.images.put(GameObject.Type.enemy_projectile, Utils.loadImage("images/projectile3.png"));

		this.reset();
	}

	private void reset() {
		// init world
		this.worldObjects = new ArrayList<GameObject>();
		this.spawnPlayer();
	}

    // this gets called 60 times a second, before this.draw()
	public void update(double dt) {
		this.time += dt;

        // to prevent the world from getting filled up with junk which is far off the screen,
        // we'll go through and check if each object is out of bounds, and then remove it if it is.
		this.removeOutOfBoundsObjects();
		// spawn enemies if it is time to do so
		this.updateEnemySpawning(dt);
		// move player or fire a shot based on keys pressed, make sure they don't go out of bounds
		this.updatePlayer(dt);
		// move all of the enemies and projectiles based on the rules they follow
		this.updateWorldObjects(dt);
		// check if any objects are colliding with any others and take appropriate action
		this.updateCollisionDetection();
	}

	private void updatePlayer(double dt) {
	    // figure out what movement should be applied to the player based on the keys pressed
		Vector2d playerInput = new Vector2d(0, 0);
		if (this.keysDown.contains("W")) {
			playerInput = playerInput.add(new Vector2d(0, -1));
		}
		if (this.keysDown.contains("S")) {
			playerInput = playerInput.add(new Vector2d(0, 1));
		}
		if (this.keysDown.contains("A")) {
			playerInput = playerInput.add(new Vector2d(-1, 0));
		}
		if (this.keysDown.contains("D")) {
			playerInput = playerInput.add(new Vector2d(1, 0));
		}

		// multiply speed by delta time (time since last frame) so we know how far to move
		double howFarToMove = this.player.getSpeed() * dt;
		// the normalizing the vector of directional input (WASD keys) ensures that the player
        // doesn't move faster when pressing two directions at once.
		Vector2d direction = playerInput.normalize();

		// apply change to player position
		this.player.position = this.player.position.add(direction.multiplyScalar(howFarToMove));

		BufferedImage playerImage = this.images.get(GameObject.Type.player);
		double minX = 0 + playerImage.getWidth() / 2; // left boundary
		double maxX = this.screenWidth + playerImage.getWidth() / 2; // right boundary
		double minY = 0 + playerImage.getHeight() / 2; // top boundary
		double maxY = this.screenHeight + playerImage.getHeight() / 2; // bottom boundary
		this.player.position.x = Utils.clamp(this.player.position.x, minX, maxX);
		this.player.position.y = Utils.clamp(this.player.position.y, minY, maxY);

		if (this.keysDown.contains("␣")) { // that's the spacebar key
			if (this.time > this.player.lastShot + this.player.getCooldownTime()) {
				this.fireProjectile(this.player);
			}
		}
	}

	private void spawnPlayer() {
		this.player = new Player();
		// start in the middle of the screen
		this.player.position.x = this.screenWidth / 2;
		this.player.position.y = this.screenHeight / 2;
		this.player.lastShot = this.time;

		this.worldObjects.add(player);
	}

	private void updateEnemySpawning(double dt) {
		// is it time to spawn an enemy?
		if (this.time > this.lastEnemySpawnTime + ENEMY_SPAWN_INTERVAL) {
			// randomly pick an enemy type
			int randomEnemyTypeIndex = (int) Math.floor(Math.random() * ENEMY_TYPES.length);
			GameObject.Type enemyTypeToSpawn = ENEMY_TYPES[randomEnemyTypeIndex];
			this.spawnEnemy(enemyTypeToSpawn);
			this.lastEnemySpawnTime = this.time;
		}
	}

	private void spawnEnemy(GameObject.Type type) {
		Enemy enemy = new Enemy(type);
		// give enemy random position at top of screen
		enemy.position.x = Math.random() * this.screenWidth;
		enemy.position.y = -100; // off top of screen
		enemy.lastShot = this.time;
        System.out.println("spawnEnemy "+enemy.toString());
		this.worldObjects.add(enemy);
	}

	private void updateWorldObjects(double dt) {
		// copy the list of world objects before updating them, because we might
		// add new objects to the world while iterating it (eg. projectiles) and
		// modifying a arraylist we're iterating is not allowed
		ArrayList<GameObject> copyOfWorldObjects = new ArrayList<>(this.worldObjects);
		for (GameObject obj: copyOfWorldObjects) {
			if (obj instanceof Enemy) {
				this.updateEnemy((Enemy)obj, dt);
			}
			if (obj instanceof Projectile) {
				this.updateProjectile((Projectile)obj, dt);
			}
		}
	}

	private void updateEnemy(Enemy enemy, double dt) {
		// enemies move downward
		double enemySpeed = enemy.getSpeed();
		enemy.position.y += enemySpeed * dt;
		// some enemy types also move toward player (but only in the x dimension)
		if (enemy.type == GameObject.Type.ship2 || enemy.type == GameObject.Type.ship3) {
			Vector2d vectorTowardPlayer = player.position.subtract(enemy.position).normalize();
			enemy.position.x += vectorTowardPlayer.x;
		}

		if (this.time > enemy.lastShot + enemy.getCooldownTime()) {
			this.fireProjectile(enemy);
		}
	}

	private void updateProjectile(Projectile projectile, double dt) {
		// player projectiles go up, enemy projectiles go down
		int direction = projectile.type == GameObject.Type.player_projectile ? -1 : 1;
		projectile.position.y += projectile.getSpeed() * dt * direction;
	}

	private void fireProjectile(GameObject source) {
		GameObject.Type projectileType = source instanceof Player
			? GameObject.Type.player_projectile
			: GameObject.Type.enemy_projectile;

		Projectile projectile = new Projectile(
			projectileType
		);
		projectile.position = source.position.copy();
		this.worldObjects.add(projectile);

        (source instanceof Player ? (Player)source : (Enemy)source).lastShot = this.time;
	}

	private void removeOutOfBoundsObjects() {
        ArrayList<GameObject> objectsToRemove = new ArrayList<GameObject>();
		for (int i = 0; i < this.worldObjects.size(); i++) {
			GameObject obj = this.worldObjects.get(i);
			if (
				obj.position.x < 0 - OFFSCREEN_SPACE ||
				obj.position.x > this.screenWidth + OFFSCREEN_SPACE ||
				obj.position.y < 0 - OFFSCREEN_SPACE ||
				obj.position.y > this.screenHeight + OFFSCREEN_SPACE
			) {
				objectsToRemove.add(obj);
			}
		}
		this.worldObjects.removeAll(objectsToRemove);
	}

	private void destroyPlayer() {
	    System.out.println("ded");
	    // we don't have a game over screen, just restart the game
		this.reset();
	}

	private void destroyEnemy(GameObject enemy) {
		this.worldObjects.remove(enemy);
	}

	private void destroyProjectile(GameObject projectile) {
		this.worldObjects.remove(projectile);
	}

	private void updateCollisionDetection() {
	    // iterate over a copy of the world objects arraylist because we might add or remove some items
        // but you can't add or remove items from the list you're currently iterating
        ArrayList<GameObject> copyOfWorldObjects = new ArrayList<>(this.worldObjects);

        // basically, check every world object against every other one using this.collision() to find out
        // if they overlap. if they do, then do something specific to the kinds of objects which are colliding.
		for (GameObject obj: copyOfWorldObjects) {
			for (GameObject otherObj: copyOfWorldObjects) {
				if (obj != otherObj) {
					if (this.collision(obj, otherObj)) {
					    // okay, these two are colliding, what do?
						if (
                            obj instanceof Player
							&& otherObj instanceof Enemy
						) {
							this.destroyPlayer();
						} else if (obj instanceof Projectile) {
							if (
								obj.type == GameObject.Type.player_projectile
								&& otherObj instanceof Enemy
							) {
								this.destroyEnemy(otherObj);
								this.destroyProjectile(obj);
							} else if (
								obj.type == GameObject.Type.enemy_projectile
								&& otherObj instanceof Player
							) {
								this.destroyPlayer();
							}
						}
					}
				}
			}
		}

	}

    // checks for rectangular overlap between two objects
    private boolean collision(GameObject a, GameObject b) {
        // work out the corners (x1,x2,y1,y1) of each rectangle
        double aWidth = this.images.get(a.type).getWidth();
        double aHeight = this.images.get(a.type).getHeight();
        double ax1 = a.position.x - aWidth/2;
        double ax2 = a.position.x + aWidth/2;
        double ay1 = a.position.y - aHeight/2;
        double ay2 = a.position.y + aHeight/2;

        double bWidth = this.images.get(b.type).getWidth();
        double bHeight = this.images.get(b.type).getHeight();
        double bx1 = b.position.x - bWidth/2;
        double bx2 = b.position.x + bWidth/2;
        double by1 = b.position.y - bHeight/2;
        double by2 = b.position.y + bHeight/2;

        return !(
            ax1 > bx2 ||
            bx1 > ax2 ||
            ay1 > by2 ||
            by1 > ay2
        );
    }

    // this gets called 60 times a second, after this.update()
	public void draw() {
	    // the black void of space
		this.drawBackground();

		// first draw all projectiles
		for (GameObject obj : this.worldObjects) {
			if (obj instanceof Projectile) {
				this.drawObject(obj);
			}
		}
		// then draw everything else (ships/player) on top
		for (GameObject obj : this.worldObjects) {
			if (!(obj instanceof Projectile)) {
				this.drawObject(obj);
			}
		}
	}

	private void drawBackground() {
		// overwrite the contents of the viewport with this color
		this.graphics.setColor(Color.BLACK);
		this.graphics.fillRect(0,0, screenWidth, screenHeight);
	}

	private void drawObject(GameObject gameObj) {
		BufferedImage image = this.images.get(gameObj.type);
		this.drawImage(
			image,
			gameObj.position.x - image.getWidth() / 2,
			gameObj.position.y - image.getHeight() / 2
		);
	}

	private void drawImage(BufferedImage image, double x, double y) {
		this.graphics.drawImage(
			image,
			(int)x,
			(int)y,
			null
		);
	}
}