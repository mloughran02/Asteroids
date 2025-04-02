package com.micheal.asteroids;

import com.badlogic.gdx.Game;

public class AsteroidsGame extends Game {

    @Override
    public void create() {
        setScreen(new MainMenuScreen(this));
    }
}
