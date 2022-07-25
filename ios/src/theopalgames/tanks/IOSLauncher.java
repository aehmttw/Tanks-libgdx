package theopalgames.tanks;

import basewindow.InputCodes;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;
import com.badlogic.gdx.backends.iosrobovm.IOSFiles;
import org.robovm.apple.avfoundation.AVAudioSession;
import org.robovm.apple.coreanimation.CADisplayLink;
import org.robovm.apple.foundation.*;
import org.robovm.apple.glkit.GLKViewDrawableMultisample;
import org.robovm.apple.uikit.*;
import org.robovm.objc.Selector;
import org.robovm.objc.annotation.Method;
import tanks.Game;

public class IOSLauncher extends IOSApplication.Delegate
{
    public static IOSApplicationConfiguration config;

    @Override
    protected IOSApplication createApplication()
    {
        Gdx.files = new IOSFiles();
        Tanks.appType = Application.ApplicationType.iOS;
        Tanks.initialize();

        config = new IOSApplicationConfiguration();
        config.hideHomeIndicator = false;
        config.screenEdgesDeferringSystemGestures = UIRectEdge.All;
        config.useAccelerometer = false;
        config.useCompass = false;
        config.allowIpod = true;
        config.preferredFramesPerSecond = 120;

        if (Game.antialiasing)
        {
            config.multisample = GLKViewDrawableMultisample._4X;
            Tanks.window.antialiasingEnabled = true;
        }

        try
        {
            Tanks.vibrationPlayer = new IOSVibrationPlayer();
        }
        catch (Exception ignored) {}

        Tanks.platformHandler = new IOSPlatformHandler();

        Tanks.pointWidth = UIScreen.getMainScreen().getBounds().getWidth();
        Tanks.pointHeight = UIScreen.getMainScreen().getBounds().getHeight();

        NSNotificationCenter.getDefaultCenter().addObserver(this, Selector.register("keyboardWillShow"), "UIKeyboardWillChangeFrameNotification", null);
        NSNotificationCenter.getDefaultCenter().addObserver(this, Selector.register("keyboardWillHide"), "UIKeyboardWillHideNotification", null);

        return new IOSApplication(new Tanks(), config);
    }

    @Method(selector = "keyboardWillShow")
    public void keyboardWillShow(NSNotification n)
    {
        NSDictionary dict = n.getUserInfo();
        double keyboardFrame = ((NSValue) dict.get("UIKeyboardFrameEndUserInfoKey")).rectValue().getHeight();

        Tanks.window.keyboardFraction = 1 - (keyboardFrame / UIScreen.getMainScreen().getBounds().getHeight());
    }

    @Method(selector = "keyboardWillHide")
    public void keyboardWillHide(NSNotification n)
    {
        if (Tanks.window.showKeyboard)
            Tanks.window.validPressedKeys.add(InputCodes.KEY_ESCAPE);
    }

    public static void main(String[] argv)
    {
        NSAutoreleasePool pool = new NSAutoreleasePool();
        UIApplication.main(argv, null, IOSLauncher.class);

        pool.close();
    }
}