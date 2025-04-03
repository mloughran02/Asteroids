package com.micheal.asteroids;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class GameOverScreen implements Screen {

    private final AsteroidsGame game;
    private SpriteBatch batch;
    private BitmapFont font;

    private final int finalScore;

    public GameOverScreen(AsteroidsGame game, int score) {
        this.game = game;
        this.finalScore = score;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        font = new BitmapFont(); // You can set a custom font here
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        font.draw(batch, "Game Over", Gdx.graphics.getWidth() * 0.4f, Gdx.graphics.getHeight() * 0.6f);
        font.draw(batch, "Your Score: " + finalScore, Gdx.graphics.getWidth() * 0.4f, Gdx.graphics.getHeight() * 0.5f);
        font.draw(batch, "Press SPACE to restart", Gdx.graphics.getWidth() * 0.35f, Gdx.graphics.getHeight() * 0.4f);
        batch.end();

        // Handle input to restart or exit
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.setScreen(new GameScreen(game));
            dispose(); // Dispose of resources before transitioning
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
        font.dispose();
    }
}
