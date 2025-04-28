package com.micheal.asteroids;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

public class Asteroid {

    private Texture texture;
    public float x, y;
    private float speedX, speedY;

    private float screenWidth, screenHeight;

    public Asteroid(Texture texture) {
        this.texture = texture;
        x = MathUtils.random(Gdx.graphics.getWidth());
        y = MathUtils.random(Gdx.graphics.getHeight());

        float angle = MathUtils.random(0f, 360f);
        float speed = MathUtils.random(100f, 200f);
        speedX = MathUtils.cosDeg(angle) * speed;
        speedY = MathUtils.sinDeg(angle) * speed;
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

        // Always use real-time screen dimensions
        if (x < -texture.getWidth()) x = Gdx.graphics.getWidth();
        if (x > Gdx.graphics.getWidth()) x = -texture.getWidth();
        if (y < -texture.getHeight()) y = Gdx.graphics.getHeight();
        if (y > Gdx.graphics.getHeight()) y = -texture.getHeight();
    }


    public void render(SpriteBatch batch) {
        batch.draw(texture, x, y);
    }
}
