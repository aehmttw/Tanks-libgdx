package theopalgames.tanks;

import basewindow.BasePlatformHandler;
import com.badlogic.gdx.Gdx;
import org.robovm.apple.foundation.NSURL;
import org.robovm.apple.glkit.GLKViewDrawableMultisample;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.objc.Selector;

public class IOSPlatformHandler extends BasePlatformHandler
{
    @Override
    public void quit()
    {
        try
        {
            UIApplication.getSharedApplication().performSelector(Selector.register("suspend"));
            Thread.sleep(1000);
            System.exit(0);
        }
        catch (Exception e)
        {
            System.exit(0);
        }
    }

    @Override
    public void openLink(String uri)
    {
        NSURL url = new NSURL(uri);
        UIApplication.getSharedApplication().openURL(url, null, null);
    }
}
