package libgdxwindow;

import basewindow.BaseFile;
import basewindow.BaseFileManager;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.ArrayList;
import java.util.Arrays;

public class LibGDXFileManager extends BaseFileManager
{
    @Override
    public BaseFile getFile(String file)
    {
        return new LibGDXFile(file);
    }

    @Override
    public ArrayList<String> getInternalFileContents(String file)
    {
        if (file.startsWith("/"))
            file = file.substring(1);

        FileHandle f = Gdx.files.internal(file);

        if (!f.exists())
            return null;

        return new ArrayList<>(Arrays.asList(f.readString().replace("\r", "").split("\n")));
    }

    @Override
    public void openFileManager(String path)
    {
        String f = new LibGDXFile(path).file.toString();
        if (f.startsWith("/"))
            f = f.substring(1);

        if (Gdx.app.getType() == Application.ApplicationType.iOS)
            Gdx.net.openURI("shareddocuments://" + Gdx.files.getExternalStoragePath().toString() + f);
        else
            Gdx.net.openURI("file://" + Gdx.files.getExternalStoragePath().toString() + f);
    }
}
