package com.zoozve;

//import com.badlogic.gdx.backends.lwjgl3.HdpiMode;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class SpaceGame {

    public static void main(String[] args) {

        Lwjgl3ApplicationConfiguration config =
                new Lwjgl3ApplicationConfiguration();

        // Janela
        config.setTitle("Zoozve: Last Farm of Earth");
        config.setWindowedMode(1350, 900);
        config.setResizable(true);

        // Sincronização com o monitor
        config.useVsync(true);
        config.setForegroundFPS(0);

        // Qualidade do backbuffer
        config.setBackBufferConfig(
                8,  // red
                8,  // green
                8,  // blue
                8,  // alpha
                24, // depth
                8,  // stencil
                8   // MSAA 8x
        );

        // HiDPI (4K / Retina)
       // config.setHdpiMode(HdpiMode.Logical);

        // Ícone (opcional)
        // config.setWindowIcon("icons/icon128.png");

        new Lwjgl3Application(new GamePanel(), config);
    }
}