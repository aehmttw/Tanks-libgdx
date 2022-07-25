package theopalgames.tanks.desktop;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import tanks.Game;
import theopalgames.tanks.Tanks;

public class DesktopLauncher
{
	public static void main(String[] arg)
	{
		Gdx.files = new LwjglFiles();
		Tanks.appType = Application.ApplicationType.Desktop;
		Tanks.initialize();

		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.depth = 24;

		if (Game.antialiasing)
		{
			config.samples = 4;
			Tanks.window.antialiasingEnabled = true;
		}

		new LwjglApplication(new Tanks(), config);
	}
}
