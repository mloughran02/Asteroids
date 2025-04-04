package com.micheal.asteroids;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import java.util.ArrayList;
import java.util.List;


public class GameScreen implements Screen {

    private SpriteBatch batch;
    private Texture asteroidTexture;
    private List<Asteroid> asteroids;
    private Texture playerTexture;
    private float playerX, playerY;
    private float angle; // in degrees

    private float playerRadius;
    private int score = 0;
    private int lives = 3;
    private BitmapFont font;

    private Texture bulletTexture;
    private List<Bullet> bullets;
    private float shootTimer = 0f;


    private float velocityX = 0;
    private float velocityY = 0;

    @Override
    public void show() {
        font = new BitmapFont();
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
        // 1 = no drag, closer to 0 = more friction
        float drag = 0.98f;
        velocityX *= drag;
        velocityY *= drag;

        // Update asteroids
        for (Asteroid a : asteroids) {
            a.update(delta);
        }
        //Asteroid collision detections
        for (Asteroid asteroid : asteroids) {
            if (asteroid.collidesWith(playerX + playerTexture.getWidth() / 2f, playerY + playerTexture.getHeight() / 2f, playerRadius)) {
                System.out.println("Player hit an asteroid!");
                // Temporary response: reset position
                playerX = Gdx.graphics.getWidth() / 2f - playerTexture.getWidth() / 2f;
                playerY = Gdx.graphics.getHeight() / 2f - playerTexture.getHeight() / 2f;
                velocityX = 0;
                velocityY = 0;
                lives--;
                break;
            }
        }
        if (lives <= 0) {
            // Transition to the GameOverScreen
            ((Game) Gdx.app.getApplicationListener()).setScreen(new GameOverScreen((AsteroidsGame) Gdx.app.getApplicationListener(), score));
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
                    score++;
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

        font.draw(batch, "Score:"+score, 100, 100);
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
        // degrees per second
        float rotationSpeed = 180f;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            angle += rotationSpeed * delta;
        }

        // Rotate right
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            angle -= rotationSpeed * delta;
        }

        // Thrust forward
        float thrustPower = 1000f;
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
            // Seconds between shots
            shootTimer = 0.3f;
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

