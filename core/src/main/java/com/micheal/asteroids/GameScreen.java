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

    private MPU6050Reader mpuReader;
    private SpriteBatch batch;
    private Texture asteroidTexture;
    private List<Asteroid> asteroids;
    private Texture playerTexture;
    private float playerX, playerY;
    private float angle;

    private float drag = 0.98f;
    private float playerRadius;
    private int score = 0;
    private int lives = 3;
    private BitmapFont font;

    private Texture bulletTexture;
    private List<Bullet> bullets;
    private float shootCooldown = 0.3f;
    private float shootTimer = 0f;

    private float velocityX = 0;
    private float velocityY = 0;

    private float rotationSpeed = 180f;
    private float thrustPower = 1000f;

    // Wave system
    private int wave = 1;
    private int startingAsteroids = 5;
    private boolean waveCleared = false;

    private AsteroidsGame game;

    public GameScreen(AsteroidsGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        mpuReader = new MPU6050Reader("COM6", 115200);
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

        spawnAsteroids(startingAsteroids);
    }

    private void spawnAsteroids(int count) {
        for (int i = 0; i < count; i++) {
            float x = MathUtils.random(0, Gdx.graphics.getWidth());
            float y = MathUtils.random(0, Gdx.graphics.getHeight());
            asteroids.add(new Asteroid(asteroidTexture));
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
        bullets.removeIf(bullet -> !bullet.active);

        // Update player position
        playerX += velocityX * delta;
        playerY += velocityY * delta;
        velocityX *= drag;
        velocityY *= drag;

        // Update asteroids
        for (Asteroid a : asteroids) {
            a.update(delta);
        }

        // Player-asteroid collision
        List<Asteroid> asteroidsToRemove = new ArrayList<>();
        for (Asteroid asteroid : asteroids) {
            if (asteroid.collidesWith(playerX + playerTexture.getWidth() / 2f, playerY + playerTexture.getHeight() / 2f, playerRadius)) {
                System.out.println("Player hit an asteroid!");
                playerX = Gdx.graphics.getWidth() / 2f - playerTexture.getWidth() / 2f;
                playerY = Gdx.graphics.getHeight() / 2f - playerTexture.getHeight() / 2f;
                velocityX = 0;
                velocityY = 0;
                lives--;
                if (lives <= 0) {
                    ((Game) Gdx.app.getApplicationListener()).setScreen(new GameOverScreen((AsteroidsGame) Gdx.app.getApplicationListener(), score));
                    return; // <---- immediately exit render() to avoid crashing
                }
                asteroidsToRemove.add(asteroid); // Remove asteroid after collision
            }
        }

        // Bullet-asteroid collision
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

        asteroids.removeAll(asteroidsToRemove);
        bullets.removeAll(bulletsToRemove);

        // Wave progression
        if (asteroids.isEmpty() && !waveCleared) {
            wave++;
            startingAsteroids += 2; // Increase difficulty
            spawnAsteroids(startingAsteroids);
            waveCleared = true;
        }

        if (!asteroids.isEmpty()) {
            waveCleared = false;
        }

        // Screen wrapping
        if (playerX < -playerTexture.getWidth()) playerX = Gdx.graphics.getWidth();
        if (playerX > Gdx.graphics.getWidth()) playerX = -playerTexture.getWidth();
        if (playerY < -playerTexture.getHeight()) playerY = Gdx.graphics.getHeight();
        if (playerY > Gdx.graphics.getHeight()) playerY = -playerTexture.getHeight();

        // Rendering
        Gdx.gl.glClearColor(0, 0, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        for (Asteroid a : asteroids) {
            a.render(batch);
        }
        for (Bullet bullet : bullets) {
            bullet.render(batch);
        }

        font.draw(batch, "Score: " + score, 20, Gdx.graphics.getHeight() - 20);
        font.draw(batch, "Lives: " + lives, 20, Gdx.graphics.getHeight() - 50);
        font.draw(batch, "Wave: " + wave, 20, Gdx.graphics.getHeight() - 80);

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
        float roll = mpuReader.getRoll();
        float pitch = mpuReader.getPitch();

        // Left/Right rotation based on Roll
        float rollThreshold = 15f;
        if (roll > rollThreshold) {
            angle -= -rotationSpeed * delta;
        } else if (roll < -rollThreshold) {
            angle += -rotationSpeed * delta;
        }

        // Forward thrust based on Pitch
        float pitchThreshold = 10f;
        if (pitch < -pitchThreshold) {
            float radians = (angle + 90) * MathUtils.degreesToRadians;
            velocityX += MathUtils.cos(radians) * thrustPower * delta;
            velocityY += MathUtils.sin(radians) * thrustPower * delta;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && shootTimer <= 0f) {
            float bulletX = playerX + playerTexture.getWidth() / 2f - bulletTexture.getWidth() / 2f;
            float bulletY = playerY + playerTexture.getHeight() / 2f - bulletTexture.getHeight() / 2f;
            bullets.add(new Bullet(bulletTexture, bulletX, bulletY, angle));
            shootTimer = shootCooldown;
        }
    }

    @Override
    public void resize(int width, int height) {}
    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void hide() {}
    @Override
    public void dispose() {
        batch.dispose();
        playerTexture.dispose();
        bulletTexture.dispose();
        font.dispose();
        if (mpuReader != null) mpuReader.close();
    }

}

