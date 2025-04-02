package com.micheal.asteroids;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;


public class Bullet {
    private Texture texture;
    public float x, y;
    private float velocityX, velocityY;
    private float speed = 500f;
    private float angle;
    public float radius;
    public boolean active = true;

    public Bullet(Texture texture, float startX, float startY, float angle) {
        this.texture = texture;
        this.x = startX;
        this.y = startY;
        this.angle = angle;
        this.radius = texture.getWidth() / 2f;
        float radians = (angle + 90) * MathUtils.degreesToRadians;
        velocityX = MathUtils.cos(radians) * speed;
        velocityY = MathUtils.sin(radians) * speed;
    }

    public void update(float delta) {
        x += velocityX * delta;
        y += velocityY * delta;

        // Deactivate bullet if it goes off screen
        if (x < -texture.getWidth() || x > Gdx.graphics.getWidth() ||
            y < -texture.getHeight() || y > Gdx.graphics.getHeight()) {
            active = false;
        }
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, x, y);
    }
}
