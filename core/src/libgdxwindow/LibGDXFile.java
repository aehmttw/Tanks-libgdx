package libgdxwindow;

import basewindow.BaseFile;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import theopalgames.tanks.Tanks;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class LibGDXFile extends BaseFile
{
    public FileHandle file;
    public String[] contents;
    public int readingLine;

    public String write;

    public LibGDXFile(String path)
    {
        super(path);

        if (Tanks.window.appType == Application.ApplicationType.iOS)
        {
            path = path.replace("/.tanks/", "/");
            this.file = Gdx.files.external(path);
        }
        else
            this.file = Gdx.files.local(path);
    }

    @Override
    public boolean exists()
    {
        return this.file.exists();
    }

    @Override
    public boolean create() throws IOException
    {
        file.writeString("", false);
        return true;
    }

    @Override
    public void renameTo(String name)
    {
        String text = file.readString();
        file.delete();
        file = Gdx.files.local(name);
        file.writeString(text, false);
    }

    @Override
    public void delete()
    {
        this.file.delete();
    }

    @Override
    public ArrayList<String> getSubfiles() throws IOException
    {
        FileHandle[] list = this.file.list();

        ArrayList<String> files = new ArrayList<String>();

        for (FileHandle f: list)
        {
            files.add(f.path());
        }

        Collections.sort(files, new Comparator<String>()
        {
            @Override
            public int compare(String o1, String o2)
            {
                return o1.compareToIgnoreCase(o2);
            }
        });

        return files;
    }

    @Override
    public void startReading() throws FileNotFoundException
    {
        contents = file.readString().replace("\r\n", "\n").split("\n");
        readingLine = 0;
    }

    @Override
    public boolean hasNextLine()
    {
        return readingLine < contents.length;
    }

    @Override
    public String nextLine()
    {
        String s = contents[readingLine];
        readingLine++;

        return s;
    }

    @Override
    public void stopReading()
    {
        readingLine = 0;
        contents = null;
    }

    @Override
    public void startWriting() throws FileNotFoundException
    {
        this.write = "";
    }

    @Override
    public void println(String s)
    {
        write += s + "\n";
    }

    @Override
    public void stopWriting()
    {
        file.writeString(write, false);
    }

    @Override
    public void mkdirs()
    {
        file.mkdirs();
    }

    @Override
    public long lastModified()
    {
        return file.lastModified();
    }
}
