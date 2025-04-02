package com.micheal.asteroids;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import java.util.ArrayList;
import java.util.List;


public class GameScreen implements Screen {

    private final AsteroidsGame game;
    private SpriteBatch batch;
    private Texture asteroidTexture;
    private List<Asteroid> asteroids;
    private Texture playerTexture;
    private float playerX, playerY;
    private float angle; // in degrees

    private float drag = 0.98f; // 1 = no drag, closer to 0 = more friction

    private float playerRadius;

    private Texture bulletTexture;
    private List<Bullet> bullets;
    private float shootCooldown = 0.2f; // Seconds between shots
    private float shootTimer = 0f;


    private float velocityX = 0;
    private float velocityY = 0;

    private float rotationSpeed = 180f; // degrees per second
    private float thrustPower = 1000f;

    public GameScreen(AsteroidsGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        playerTexture = new Texture(Gdx.files.internal("player.png"));

        playerX = Gdx.graphics.getWidth() / 2f - playerTexture.getWidth() / 2f;
        playerY = Gdx.graphics.getHeight() / 2f - playerTexture.getHeight() / 2f;

        asteroidTexture = new Texture(Gdx.files.internal("asteroid.png"));
        asteroids = new ArrayList<>();

        playerRadius = playerTexture.getWidth() / 2f;

        bulletTexture = new Texture(Gdx.files.internal("bullet.png"));
        bullets = new ArrayList<>();


        // Spawn asteroids
        for (int i = 0; i < 5; i++) {
            asteroids.add(new Asteroid(asteroidTexture, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
        }

    }

    @Override
    public void render(float delta) {
        handleInput(delta);

        shootTimer -= delta;

        // Update bullets
        for (Bullet bullet : bullets) {
            bullet.update(delta);
        }

        // Remove inactive bullets
        bullets.removeIf(bullet -> !bullet.active);


        // Update position based on velocity
        playerX += velocityX * delta;
        playerY += velocityY * delta;

        // Apply drag to slow down over time
        velocityX *= drag;
        velocityY *= drag;

        // Update asteroids
        for (Asteroid a : asteroids) {
            a.update(delta);
        }
        //Asteroid collision deteciotns
        for (Asteroid asteroid : asteroids) {
            if (asteroid.collidesWith(playerX + playerTexture.getWidth() / 2f, playerY + playerTexture.getHeight() / 2f, playerRadius)) {
                System.out.println("BOOM! Player hit an asteroid!");
                // Temporary response: reset position
                playerX = Gdx.graphics.getWidth() / 2f - playerTexture.getWidth() / 2f;
                playerY = Gdx.graphics.getHeight() / 2f - playerTexture.getHeight() / 2f;
                velocityX = 0;
                velocityY = 0;
                break;
            }
        }
        //Bullet Hit Detection
        List<Asteroid> asteroidsToRemove = new ArrayList<>();
        List<Bullet> bulletsToRemove = new ArrayList<>();

        for (Asteroid asteroid : asteroids) {
            for (Bullet bullet : bullets) {
                float bulletCenterX = bullet.x + bulletTexture.getWidth() / 2f;
                float bulletCenterY = bullet.y + bulletTexture.getHeight() / 2f;
                if (asteroid.collidesWith(bulletCenterX, bulletCenterY, bullet.radius)) {
                    asteroidsToRemove.add(asteroid);
                    bulletsToRemove.add(bullet);
                    System.out.println("Asteroid destroyed!");
                }
            }
        }

// Remove them outside the loop
        asteroids.removeAll(asteroidsToRemove);
        bullets.removeAll(bulletsToRemove);



        // Screen wrap
        if (playerX < -playerTexture.getWidth()) playerX = Gdx.graphics.getWidth();
        if (playerX > Gdx.graphics.getWidth()) playerX = -playerTexture.getWidth();
        if (playerY < -playerTexture.getHeight()) playerY = Gdx.graphics.getHeight();
        if (playerY > Gdx.graphics.getHeight()) playerY = -playerTexture.getHeight();

        Gdx.gl.glClearColor(0, 0, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        for (Asteroid a : asteroids) {
            a.render(batch);
        }
        // Draw bullets
        for (Bullet bullet : bullets) {
            bullet.render(batch);
        }


        batch.draw(
            playerTexture,
            playerX, playerY,
            playerTexture.getWidth() / 2f,
            playerTexture.getHeight() / 2f,
            playerTexture.getWidth(),
            playerTexture.getHeight(),
            1f, 1f,
            angle,
            0, 0,
            playerTexture.getWidth(),
            playerTexture.getHeight(),
            false, false
        );
        batch.end();
    }

    private void handleInput(float delta) {
        // Rotate left
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            angle += rotationSpeed * delta;
        }

        // Rotate right
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            angle -= rotationSpeed * delta;
        }

        // Thrust forward
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            float radians = (angle + 90) * MathUtils.degreesToRadians;
            velocityX += MathUtils.cos(radians) * thrustPower * delta;
            velocityY += MathUtils.sin(radians) * thrustPower * delta;
        }

        // Reverse thrust
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            float radians = (angle + 90) * MathUtils.degreesToRadians;
            velocityX -= MathUtils.cos(radians) * thrustPower * delta;
            velocityY -= MathUtils.sin(radians) * thrustPower * delta;
        }

        //shoot bullet
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && shootTimer <= 0f) {
            float bulletX = playerX + playerTexture.getWidth() / 2f - bulletTexture.getWidth() / 2f;
            float bulletY = playerY + playerTexture.getHeight() / 2f - bulletTexture.getHeight() / 2f;
            bullets.add(new Bullet(bulletTexture, bulletX, bulletY, angle));
            shootTimer = shootCooldown;
        }
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        batch.dispose();
        playerTexture.dispose();
        asteroidTexture.dispose();
        bulletTexture.dispose();


    }
}

