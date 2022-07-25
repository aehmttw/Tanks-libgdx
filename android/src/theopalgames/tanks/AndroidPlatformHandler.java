package theopalgames.tanks;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import basewindow.BasePlatformHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplication;

public class AndroidPlatformHandler extends BasePlatformHandler
{
    @Override
    public void quit()
    {
        System.exit(0);
    }

    @Override
    public void openLink(String url)
    {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        PackageManager pm = ((AndroidApplication) Gdx.app).getContext().getPackageManager();
        pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        ((AndroidApplication)Gdx.app).runOnUiThread(new Runnable() {
            @Override
            public void run () {
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                // LiveWallpaper and Daydream applications need this flag
                if (!(((AndroidApplication)Gdx.app).getContext() instanceof Activity))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ((AndroidApplication)Gdx.app).startActivity(intent);
            }
        });
    }
}
