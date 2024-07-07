package theopalgames.tanks;

import basewindow.BasePlatformHandler;
import basewindow.BaseVibrationPlayer;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import libgdxwindow.LibGDXFileManager;
import libgdxwindow.LibGDXWindow;
import tanks.Game;
import tanks.GameDrawer;
import tanks.GameUpdater;
import tanks.GameWindowHandler;
import tanks.gui.screen.ScreenExit;

public class Tanks extends ApplicationAdapter
{
	public static Tanks instance;
	public static LibGDXWindow window;

	public static BaseVibrationPlayer vibrationPlayer;
	public static BasePlatformHandler platformHandler;
	public static IKeyboardHeightListener keyboardHeightListener;

	public static double pointWidth = -1;
	public static double pointHeight = -1;

	public static Application.ApplicationType appType;

	public static void initialize()
	{
		window = new LibGDXWindow("Tanks", 1400, 900, 1000, new GameUpdater(), new GameDrawer(), new GameWindowHandler(), false, true);

		window.appType = appType;
		Game.game.fileManager = new LibGDXFileManager();
		Game.framework = Game.Framework.libgdx;
		Game.initScript();
		Game.game.window = window;
	}

	@Override
	public void create()
	{
		instance = this;
		Game.debug = true;

		window.absoluteWidth = Gdx.graphics.getWidth();
		window.absoluteHeight = Gdx.graphics.getHeight();

		window.absoluteDepth = 1000;

		window.initialize();

		Game.game.window.vibrationPlayer = vibrationPlayer;
		Game.game.window.vibrationsEnabled = vibrationPlayer != null;

		Game.game.window.platformHandler = platformHandler;

		window.touchscreen = true;

		window.pointWidth = pointWidth;
		window.pointHeight = pointHeight;

		if (Gdx.app.getType() == Application.ApplicationType.Android)
			keyboardHeightListener.init();
	}

	@Override
	public void resize(int width, int height)
	{
		window.absoluteWidth = width;
		window.absoluteHeight = height;

		window.loadPerspective();
	}

	@Override
	public void render()
	{
		window.render();
	}

	@Override
	public void dispose()
	{
		Game.game.window.windowHandler.onWindowClose();
	}

	@Override
	public void pause()
	{
		if (Game.screen instanceof ScreenExit)
		{
			window.windowHandler.onWindowClose();
			System.exit(0);
		}
	}

	@Override
	public void resume()
	{
		window.lastFrame = System.currentTimeMillis();
	}
}
