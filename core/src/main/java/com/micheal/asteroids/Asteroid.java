package com.micheal.asteroids;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public class Asteroid {

    private Texture texture;
    public float x, y;
    private float speedX, speedY;

    private float screenWidth, screenHeight;

    public Asteroid(Texture texture, float screenWidth, float screenHeight) {
        this.texture = texture;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        // Random position
        x = MathUtils.random(screenWidth);
        y = MathUtils.random(screenHeight);

        // Random direction
        speedX = MathUtils.random(-100, 100);
        speedY = MathUtils.random(-100, 100);
    }
    public boolean collidesWith(float otherX, float otherY, float radius) {
        float dx = x + texture.getWidth() / 2f - otherX;
        float dy = y + texture.getHeight() / 2f - otherY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float thisRadius = texture.getWidth() / 2f;
        return distance < (thisRadius + radius);
    }


    public void update(float delta) {
        x += speedX * delta;
        y += speedY * delta;

        // Wrap around screen edges
        if (x < -texture.getWidth()) x = screenWidth;
        if (x > screenWidth) x = -texture.getWidth();
        if (y < -texture.getHeight()) y = screenHeight;
        if (y > screenHeight) y = -texture.getHeight();
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, x, y);
    }
}
