package tanks;

import basewindow.IDrawer;
import com.badlogic.gdx.Gdx;
import libgdxwindow.LibGDXWindow;
import tanks.extension.Extension;
import tanks.tank.Tank;
import tanks.tank.TankDummyLoadingScreen;
import tanks.tank.TankModels;

public class GameDrawer implements IDrawer
{
	@Override
	public void draw()
	{
//		Game.game.window.shaderDefault.set();
//		Drawing.drawing.setColor(0, 255, 127 * (1 + Math.sin(System.currentTimeMillis() / 1000.0)), 128, 1);
//		Drawing.drawing.drawModel(TankModels.tank.turretBase, 900 * (1 + Math.sin(System.currentTimeMillis() / 1000.0)), 600, 0, 1000, 1000, 100, System.currentTimeMillis() / 1000.0);
//		Drawing.drawing.setColor(0, 255, 127 * (1 + Math.sin(System.currentTimeMillis() / 1000.0)), 128, 1);
//		Drawing.drawing.drawModel(TankModels.tank.turretBase, 900 * (1 + Math.sin(System.currentTimeMillis() / 1000.0 + 0.5)), 600, 0, 1000, 1000, 100, System.currentTimeMillis() / 1000.0);
//		Drawing.drawing.setColor(255, 0, 127 * (1 + Math.sin(System.currentTimeMillis() / 1000.0)), 255, 1);
//		Drawing.drawing.drawModel(TankModels.tank.turret, 900 * (1 - Math.sin(System.currentTimeMillis() / 1000.0)), 600, 0, 1000, 1000, 100, System.currentTimeMillis() / 1000.0);

//		for (int i = 0; i < 28; i++)
//		{
//			for (int j = 0; j < 18; j++)
//			{
//				Drawing.drawing.setColor(i / 28.0 * 255, j / 18.0 * 255, 127);
//				Drawing.drawing.drawRect(i * 50 + 25, j * 50 + 25, 50, 50);
//				Drawing.drawing.setColor(i / 28.0 * 255, j / 18.0 * 255, 255);
//				Drawing.drawing.drawModel(TankModels.tank.turretBase, i * 50 + 25, j * 50 + 25, 50, 50, System.currentTimeMillis() * (i + j) / 10000.0);
//			}
//		}

//		Drawing.drawing.setColor(0, 255, 127 * (1 + Math.sin(System.currentTimeMillis() / 1000.0)), 128, 1);
//		Drawing.drawing.drawModel(TankModels.tank.turretBase, 900 * (1 + Math.sin(System.currentTimeMillis() / 1000.0)), 600, 0, 1000, 1000, 100, System.currentTimeMillis() / 1000.0);
//
//		//Drawing.drawing.setColor(255, 0, 0);
//		Drawing.drawing.fillBox(100, 100, 100, 500, 500, 500);
////		Game.game.window.shapeRenderer.setBatchMode(true, true, false, false, false);
////		Drawing.drawing.setColor(255, 0, 0);
////		Drawing.drawing.addVertex(100, 100, 0);
////		Drawing.drawing.addVertex(100, 500, 0);
////		Drawing.drawing.addVertex(500, 500, 0);
////		Drawing.drawing.addVertex(500, 100, 0);
////		Game.game.window.shapeRenderer.setBatchMode(false, true, false, false, false);
//
//		//Drawing.drawing.setColor(255, 0, 127 * (1 + Math.sin(System.currentTimeMillis() / 1000.0)), 128, 1);
//		Drawing.drawing.drawModel(TankModels.tank.turretBase, 900 * (1 + Math.sin(System.currentTimeMillis() / 1000.0)), 600, 0, 1000, 1000, 100, System.currentTimeMillis() / 1000.0 + 0.5);
//
//		Game.game.window.shapeRenderer.setBatchMode(false, false, false, false, false);
//		Drawing.drawing.addVertex(600, 100, 0);
//		Drawing.drawing.addVertex(600, 500, 0);
//		Drawing.drawing.addVertex(1100, 500, 0);
//		Drawing.drawing.addVertex(1100, 100, 0);
//		Game.game.window.shapeRenderer.setBatchMode(false, false, false, false, false);
//
//		Game.game.window.shapeRenderer.setBatchMode(true, true, false, false, false);
//		Drawing.drawing.addVertex(600, 600, 0);
//		Drawing.drawing.addVertex(600, 1100, 0);
//		Drawing.drawing.addVertex(1100, 1100, 0);
//		Drawing.drawing.addVertex(1100, 600, 0);
//
//		Game.game.window.shapeRenderer.setBatchMode(false, true, false, false, false);

		try
		{
			if (Game.enableExtensions)
			{
				for (int i = 0; i < Game.extensionRegistry.extensions.size(); i++)
				{
					Extension e = Game.extensionRegistry.extensions.get(i);

					e.preDraw();
				}
			}

			Panel.panel.draw();

			if (Game.enableExtensions)
			{
				for (int i = 0; i < Game.extensionRegistry.extensions.size(); i++)
				{
					Extension e = Game.extensionRegistry.extensions.get(i);

					e.draw();
				}
			}
		}
		catch (Throwable e)
		{
			Game.exitToCrash(e);
		}
	}
}
