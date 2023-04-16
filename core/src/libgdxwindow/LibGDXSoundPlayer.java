package libgdxwindow;

import basewindow.BaseSoundPlayer;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class LibGDXSoundPlayer extends BaseSoundPlayer
{
    //public Sound[] sounds = new Sound[50];
    //public int soundIndex = 0;

    public HashMap<String, Sound> soundsMap = new HashMap<>();
    public HashMap<String, Long> lastPlayed = new HashMap<>();

    public HashMap<String, Music> musicMap = new HashMap<>();
    public HashMap<String, ArrayList<Music>> combinedMusicMap = new HashMap<>();

    public Music currentMusic = null;
    public Music prevMusic = null;

    public String musicID = null;
    public String prevMusicID = null;

    public long fadeBegin = 0;
    public long fadeEnd = 0;

    public float prevVolume;
    public float currentVolume;

    public boolean currentMusicStoppable = true;
    public boolean prevMusicStoppable = true;

    //public float delay = 0.00f;

    public LibGDXSoundPlayer()
    {
        if (Gdx.app.getType() == Application.ApplicationType.Android)
        {
            FileHandle[] sounds = Gdx.files.internal("sounds").list();
            for (FileHandle f : sounds)
            {
                String path = f.path();
                if (path.startsWith("/"))
                    path = path.substring(1);

                if (f.path().endsWith(".ogg"))
                {
                    Sound s = Gdx.audio.newSound(f);
                    soundsMap.put(path, s);
                }
            }

            FileHandle[] musics = Gdx.files.internal("music").list();
            for (FileHandle f : musics)
            {
                String path = f.path();
                if (path.startsWith("/"))
                    path = path.substring(1);

                if (f.path().endsWith("intro.ogg"))
                {
                    Sound s = Gdx.audio.newSound(f);
                    soundsMap.put(path, s);
                }
            }
        }

        FileHandle[] musics = Gdx.files.internal("music").list();
        for (FileHandle f : musics)
        {
            String path = f.path();
            if (path.startsWith("/"))
                path = path.substring(1);

            if ((Gdx.app.getType() == Application.ApplicationType.iOS && f.path().endsWith(".wav")) || (Gdx.app.getType() != Application.ApplicationType.iOS && f.path().endsWith(".ogg")))
            {
                try
                {
                    Music s = Gdx.audio.newMusic(f);
                    musicMap.put(path, s);
                }
                catch (Exception e)
                {

                }
            }
        }
    }

    @Override
    public void loadMusic(String path)
    {

    }

    @Override
    public void playSound(String path)
    {
        this.playSound(path, 1, 1);
    }

    @Override
    public void playSound(String path, float pitch)
    {
        this.playSound(path, pitch, 1);
    }

    @Override
    public void playSound(String path, float pitch, float volume)
    {
        if (path.startsWith("/"))
            path = path.substring(1);

        Long last = lastPlayed.get(path);
        if (last != null && System.currentTimeMillis() - last < 100)
            return;

        lastPlayed.put(path, System.currentTimeMillis());

        Sound s = soundsMap.get(path);
        if (s == null)
        {
            if (Gdx.app.getType() == Application.ApplicationType.iOS)
                path = path.replace(".ogg", ".wav");

            s = Gdx.audio.newSound(Gdx.files.internal(path));
            soundsMap.put(path, s);
        }

        //Sound s = Gdx.audio.newSound(Gdx.files.internal(path.replace(".ogg", ".wav")));
        s.play(volume, pitch, 0);

        /*if (sounds[soundIndex] != null)
            sounds[soundIndex].dispose();

        sounds[soundIndex] = s;
        soundIndex = (soundIndex + 1) % sounds.length;*/
    }

    @Override
    public void playMusic(String path, float volume, boolean looped, String continueID, long fadeTime)
    {
        this.playMusic(path, volume, looped, continueID, fadeTime, true);
    }

    @Override
    public void playMusic(String path, float volume, boolean looped, String continueID, long fadeTime, boolean stoppable)
    {
        if (musicID != null && musicID.equals(continueID) && prevMusic != null)
            prevMusic.setVolume(0);
        else if (prevMusic != null)
            prevMusic.stop();

        if (path.startsWith("/"))
            path = path.substring(1);

        if (Gdx.app.getType() == Application.ApplicationType.iOS)
            path = path.replace(".ogg", ".wav");

        if (combinedMusicMap.get(continueID) != null && !continueID.equals(musicID))
        {
            long time = System.currentTimeMillis();
            for (Music m : combinedMusicMap.get(continueID))
            {
                m.setPosition((System.currentTimeMillis() - time) / 1000f);
                m.setVolume(0);
                m.play();
                m.setLooping(looped);
            }
        }

        Music s = musicMap.get(path);
        if (s == null)
        {
            s = Gdx.audio.newMusic(Gdx.files.internal(path));
            musicMap.put(path, s);
        }

       // float pos = 0;

        if (currentMusic != null)
        {
            if (currentMusic.equals(s))
                currentMusic.setVolume(volume);

            if (currentMusic.equals(s) && continueID != null && continueID.equals(musicID))
                return;

            prevVolume = currentVolume;

            if (prevMusic != null && prevMusicStoppable && (continueID == null || !continueID.equals(musicID)))
            {
                if (combinedMusicMap.get(musicID) != null)
                {
                    for (Music m: combinedMusicMap.get(musicID))
                        m.stop();
                }
                else
                    prevMusic.stop();
            }

            prevMusicStoppable = currentMusicStoppable;
            prevMusic = currentMusic;
            fadeBegin = System.currentTimeMillis();
            fadeEnd = fadeBegin + fadeTime;
            //pos = currentMusic.getPosition();

            if ((continueID == null || !continueID.equals(musicID)) && currentMusicStoppable)
                currentMusic.stop();
        }

        currentVolume = volume;

        if (prevMusic == null)
            s.setVolume(volume);
        else
            s.setVolume(0);

        s.setLooping(looped);

        if (musicID == null || !musicID.equals(continueID))
        {
            s.play();
        }

        //if (currentMusic != null && musicID != null && musicID.equals(continueID))
        //    s.setPosition(pos + delay);

        currentMusic = s;
        prevMusicID = musicID;
        musicID = continueID;
        musicPlaying = true;
        currentMusicStoppable = stoppable;
    }

    @Override
    public void setMusicVolume(float volume)
    {

    }

    @Override
    public void setMusicSpeed(float speed)
    {

    }

    @Override
    public void addSyncedMusic(String path, float volume, boolean looped, long fadeTime)
    {

    }

    @Override
    public void removeSyncedMusic(String path, long fadeTime)
    {

    }

    @Override
    public void stopMusic()
    {
        if (currentMusic != null)
        {
            if (combinedMusicMap.get(musicID) != null)
            {
                for (Music m: combinedMusicMap.get(musicID))
                {
                    m.stop();
                    m.setPosition(0);
                }
            }
            else
            {
                currentMusic.stop();
                currentMusic.setPosition(0);
            }

            musicID = null;
            currentMusic = null;
            musicPlaying = false;
        }
    }

    @Override
    public void registerCombinedMusic(String path, String id)
    {
        if (path.startsWith("/"))
            path = path.substring(1);

        if (Gdx.app.getType() == Application.ApplicationType.iOS)
            path = path.replace(".ogg", ".wav");

        if (combinedMusicMap.get(id) == null)
            combinedMusicMap.put(id, new ArrayList<>());

        ArrayList<Music> al = combinedMusicMap.get(id);

        Music s = musicMap.get(path);
        if (s == null)
        {
            s = Gdx.audio.newMusic(Gdx.files.internal(path));
            musicMap.put(path, s);
        }

        al.add(musicMap.get(path));
    }

    @Override
    public void exit()
    {

    }

    @Override
    public void update()
    {

    }

    @Override
    public void createSound(String path, InputStream in)
    {

    }

    @Override
    public void createMusic(String path, InputStream in)
    {

    }

    @Override
    public void loadMusic(String path, InputStream in)
    {

    }
}
