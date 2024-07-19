package theopalgames.tanks;

import android.content.Context;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidAudio;
import com.badlogic.gdx.backends.android.AndroidFiles;
import com.badlogic.gdx.backends.android.DefaultAndroidFiles;

import libgdxwindow.LibGDXAsyncMiniAudioSoundPlayer;
import tanks.Game;

import java.net.Inet4Address;
import java.net.InetAddress;

public class AndroidLauncher extends AndroidApplication
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.getFilesDir();
		Gdx.files = new DefaultAndroidFiles(this.getAssets(), this, true);
		Tanks.appType = ApplicationType.Android;
		Tanks.initialize();

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.depth = 24;
		config.useImmersiveMode = true;
		config.useAccelerometer = false;
		config.useCompass = false;
		config.maxSimultaneousSounds = 64;

		if (Game.antialiasing)
		{
			Tanks.window.antialiasingEnabled = true;
			config.numSamples = 4;
		}

		Tanks.keyboardHeightListener = new AndroidKeyboardHeightListener(this);
		Tanks.vibrationPlayer = new AndroidVibrationPlayer();

		DisplayMetrics displayMetrics = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

		if (Build.VERSION.SDK_INT >= 30)
			this.getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;

		Tanks.pointWidth = displayMetrics.widthPixels / displayMetrics.density;
		Tanks.pointHeight = displayMetrics.heightPixels / displayMetrics.density;

		Tanks.platformHandler = new AndroidPlatformHandler();

		initialize(new Tanks(), config);
	}

	@Override
	public AndroidAudio createAudio (Context context, AndroidApplicationConfiguration config)
	{
		LibGDXAsyncMiniAudioSoundPlayer.miniAudio.setupAndroid(context.getAssets());
		return super.createAudio(context, config);
	}
}
