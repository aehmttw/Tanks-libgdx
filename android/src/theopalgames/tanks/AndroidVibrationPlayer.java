package theopalgames.tanks;

import basewindow.BaseVibrationPlayer;
import com.badlogic.gdx.Gdx;

public class AndroidVibrationPlayer extends BaseVibrationPlayer
{
    @Override
    public void selectionChanged()
    {
        Gdx.input.vibrate(5);
    }

    @Override
    public void click()
    {
        Gdx.input.vibrate(10);
    }

    @Override
    public void heavyClick()
    {
        Gdx.input.vibrate(50);
    }
}
