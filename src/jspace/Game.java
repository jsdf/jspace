package jspace;

import java.awt.*;
import java.awt.image.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

public class Game {
	public Graphics graphics;
	public int screenWidth = 800;
	public int screenHeight = 600;
	public HashSet<String> keysDown;
	private double lastEnemySpawnTime = 0;
	private double time = 0;
	private HashMap<GameObject.Type, BufferedImage> images;
	private Vector<GameObject> worldObjects;
	private GameObject player;
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
		// load all the images
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
		this.worldObjects = new Vector<GameObject>();
		this.spawnPlayer();
	}

	public void update(double dt) {
		this.time += dt;
		this.removeOutOfBoundsObjects();
		this.updateEnemySpawning(dt);
		this.updatePlayer(dt);
		this.updateWorldObjects(dt);
		this.updateCollisionDetection();
	}

	private void updatePlayer(double dt) {
		Vector2d playerInput= new Vector2d(0, 0);
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

		this.player.position = this.player.position.add(playerInput.normalize().multiplyScalar(this.player.getSpeed() * dt));

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
		this.player = new GameObject(
			GameObject.Category.player,
			GameObject.Type.player
		);
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
		GameObject enemy = new GameObject(GameObject.Category.enemy, type);
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
		// modifying a vector we're iterating is not allowed
		Vector<GameObject> copyOfWorldObjects = (Vector)this.worldObjects.clone();
		for (GameObject obj: copyOfWorldObjects) {
			if (obj.category == GameObject.Category.enemy) {
				this.updateEnemy(obj, dt);
			}
			if (obj.category == GameObject.Category.projectile) {
				this.updateProjectile(obj, dt);
			}
		}
	}

	private void updateEnemy(GameObject enemy, double dt) {
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

	private void updateProjectile(GameObject projectile, double dt) {
		// player projectiles go up, enemy projectiles go down
		int direction = projectile.type == GameObject.Type.player_projectile ? -1 : 1;
		projectile.position.y += projectile.getSpeed() * dt * direction;
	}

	private void fireProjectile(GameObject source) {
		GameObject.Type projectileType = source.category == GameObject.Category.player
			? GameObject.Type.player_projectile
			: GameObject.Type.enemy_projectile;

		GameObject projectile = new GameObject(
			GameObject.Category.projectile,
			projectileType
		);
		projectile.position = source.position.copy();
		this.worldObjects.add(projectile);

		source.lastShot = this.time;
	}

	private void removeOutOfBoundsObjects() {
		Vector<GameObject> objectsToRemove = new Vector<GameObject>();
		for (int i = 0; i < this.worldObjects.size(); i++) {
			GameObject obj = this.worldObjects.get(i);
			if (
				obj.position.x < 0 - OFFSCREEN_SPACE ||
				obj.position.x > this.screenWidth + OFFSCREEN_SPACE ||
				obj.position.y < 0 - OFFSCREEN_SPACE ||
				obj.position.y > this.screenHeight + OFFSCREEN_SPACE
			) {
				System.out.println("removing "+obj.toString());
				objectsToRemove.add(obj);
			}
		}
		this.worldObjects.removeAll(objectsToRemove);
	}

	private void destroyPlayer() {
		this.reset();
	}

	private void destroyEnemy(GameObject enemy) {
		this.worldObjects.remove(enemy);
	}

	private void destroyProjectile(GameObject projectile) {
		this.worldObjects.remove(projectile);
	}

	private void updateCollisionDetection() {
		Vector<GameObject> copyOfWorldObjects = (Vector)this.worldObjects.clone();
		for (GameObject obj: copyOfWorldObjects) {
			for (GameObject otherObj: copyOfWorldObjects) {
				if (obj != otherObj) {
					if (this.collision(obj, otherObj)) {
						if (
							obj.category == GameObject.Category.player
							&& otherObj.category == GameObject.Category.enemy
						) {
							this.destroyPlayer();
						} else if (obj.category == GameObject.Category.projectile) {
							if (
								obj.type == GameObject.Type.player_projectile
								&& otherObj.category == GameObject.Category.enemy
							) {
								this.destroyEnemy(otherObj);
								this.destroyProjectile(obj);
							} else if (
								obj.type == GameObject.Type.enemy_projectile
								&& otherObj.category == GameObject.Category.player
							) {
								this.destroyPlayer();
							}
						}
					}
				}
			}
		}

	}

	public void draw() {
		this.drawBackground();

		// first draw all projectiles
		for (GameObject obj : this.worldObjects) {
			if (obj.category == GameObject.Category.projectile) {
				this.drawObject(obj);
			}
		}
		// then draw everything else (ships) on top
		for (GameObject obj : this.worldObjects) {
			if (obj.category != GameObject.Category.projectile) {
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
}