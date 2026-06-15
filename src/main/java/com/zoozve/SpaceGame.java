package com.zoozve;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class SpaceGame {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Zoozve: Last Farm of Earth");
        config.setWindowedMode(900, 600);
        config.setResizable(false);
        config.setForegroundFPS(60);
        new Lwjgl3Application(new GamePanel(), config);
    }
}