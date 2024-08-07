package libgdxwindow;

import basewindow.BaseSoundPlayer;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;

import java.io.InputStream;
import java.util.*;

public class LibGDXSoundPlayer extends BaseSoundPlayer
{
    public LibGDXWindow window;

    public HashMap<String, Sound> soundsMap = new HashMap<>();
    public HashMap<String, Long> lastPlayed = new HashMap<>();

    public HashMap<String, Music> syncedTracks = new HashMap<>();
    public HashMap<String, Music> stoppingSyncedTracks = new HashMap<>();
    public HashMap<String, Float> syncedTrackCurrentVolumes = new HashMap<>();
    public HashMap<String, Float> syncedTrackMaxVolumes = new HashMap<>();
    public HashMap<String, Float> syncedTrackFadeRate = new HashMap<>();

    protected LinkedHashMap<String, SyncedMusicCommand> syncedMusicCommands = new LinkedHashMap<>();

    public HashMap<String, Music> musicMap = new HashMap<>();

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

    public float latency = 0.086f;

    public LibGDXSoundPlayer(LibGDXWindow window)
    {
        this.window = window;
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

                if (f.path().endsWith("intro.ogg") || f.path().endsWith("/win.ogg") || f.path().endsWith("/lose.ogg"))
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

            if ((Gdx.app.getType() == Application.ApplicationType.iOS && f.path().endsWith(".m4a")) || (Gdx.app.getType() != Application.ApplicationType.iOS && f.path().endsWith(".ogg")))
            {
                try
                {
                    Music s = Gdx.audio.newMusic(f);
                    musicMap.put(path, s);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
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
            {
                if (path.contains("music/"))
                    path = path.replace(".ogg", ".m4a");
                else
                    path = path.replace(".ogg", ".wav");
            }

            s = Gdx.audio.newSound(Gdx.files.internal(path));
            soundsMap.put(path, s);
        }

        s.play(volume, pitch, 0);
    }

    @Override
    public void playMusic(String path, float volume, boolean looped, String continueID, long fadeTime)
    {
        this.playMusic(path, volume, looped, continueID, fadeTime, true);
    }

    public Music getMusic(String path)
    {
        if (path.startsWith("/"))
            path = path.substring(1);

        if (Gdx.app.getType() == Application.ApplicationType.iOS)
            path = path.replace(".ogg", ".m4a");

        Music s = musicMap.get(path);
        if (s == null)
        {
            s = Gdx.audio.newMusic(Gdx.files.internal(path));
            musicMap.put(path, s);
        }

        return s;
    }

    @Override
    public void playMusic(String path, float volume, boolean looped, String continueID, long fadeTime, boolean stoppable)
    {
        Music m = getMusic(path);

        if (prevMusic != null)
            prevMusic.stop();

        prevMusic = currentMusic;
        currentMusic = m;
        currentVolume = volume;

        m.play();
        m.setLooping(looped);
        m.setVolume(volume);

        fadeBegin = System.currentTimeMillis();
        if (continueID != null && continueID.equals(this.musicID))
        {
            float p = prevMusic.getPosition();
            m.setPosition(p + latency);
            m.setVolume(0);

            fadeEnd = System.currentTimeMillis() + fadeTime;
        }
        else
        {
            for (String s: this.syncedTracks.keySet())
            {
                this.syncedTrackFadeRate.put(s, -1.0f);
                this.syncedMusicCommands.put(s, SyncedMusicCommand.remove(s));
            }

            if (prevMusic != null)
                prevMusic.stop();

            fadeEnd = System.currentTimeMillis();
        }

        prevVolume = volume;
        this.musicID = continueID;
    }

    @Override
    public void setMusicVolume(float volume)
    {
        this.currentVolume = volume;
        this.currentMusic.setVolume(volume);

        for (Music m: this.syncedTracks.values())
            m.setVolume(volume);
    }

    @Override
    public void setMusicSpeed(float speed)
    {

    }

    protected static class SyncedMusicCommand
    {
        boolean remove;
        String path;
        float volume;
        boolean looped;
        long fadeTime;

        public static SyncedMusicCommand add(String path, float volume, boolean looped, long fadeTime)
        {
            SyncedMusicCommand cmd = new SyncedMusicCommand();
            cmd.remove = false;
            cmd.path = path;
            cmd.volume = volume;
            cmd.looped = looped;
            cmd.fadeTime = fadeTime;
            return cmd;
        }

        public static SyncedMusicCommand remove(String path)
        {
            SyncedMusicCommand cmd = new SyncedMusicCommand();
            cmd.remove = true;
            cmd.path = path;
            return cmd;
        }

        public String toString()
        {
            if (remove)
                return "-" + this.path;
            else
                return "+" + this.path;
        }
    }

    @Override
    public void addSyncedMusic(String path, float volume, boolean looped, long fadeTime)
    {
        this.syncedMusicCommands.put(path, SyncedMusicCommand.add(path, volume, looped, fadeTime));
    }

    @Override
    public void removeSyncedMusic(String path, long fadeTime)
    {
        Music m = this.syncedTracks.get(path);
        this.syncedMusicCommands.remove(path);

        if (m != null)
        {
            this.stoppingSyncedTracks.put(path, m);
            this.syncedTrackFadeRate.put(path, -this.syncedTrackMaxVolumes.get(path) / fadeTime * 10);
        }
    }

    protected void updateSyncedMusic()
    {
        while (true)
        {
            System.out.println(syncedMusicCommands.size());
            Iterator<String> irritator = this.syncedMusicCommands.keySet().iterator();
            if (!irritator.hasNext())
                return;

            SyncedMusicCommand cmd = this.syncedMusicCommands.remove(irritator.next());

            if (cmd.remove)
            {
                if (this.syncedTracks.containsKey(cmd.path) && this.syncedTrackFadeRate.get(cmd.path) < 0)
                {
                    Music m = this.syncedTracks.remove(cmd.path);
                    m.stop();
                    this.stoppingSyncedTracks.remove(cmd.path);
                    this.syncedTrackFadeRate.remove(cmd.path);
                    this.syncedTrackMaxVolumes.remove(cmd.path);
                    this.syncedTrackCurrentVolumes.remove(cmd.path);
                    return;
                }
            }
            else
            {
                Music s = this.stoppingSyncedTracks.remove(cmd.path);
                if (s != null)
                    s.stop();

                if (currentMusic == null)
                    return;

                Music m = this.getMusic(cmd.path);
                m.play();

                m.setLooping(cmd.looped);
                m.setVolume(0);
                m.setPosition(currentMusic.getPosition() + latency);
                this.syncedTracks.put(cmd.path, m);
                this.syncedTrackCurrentVolumes.put(cmd.path, 0f);
                this.syncedTrackMaxVolumes.put(cmd.path, cmd.volume);
                this.syncedTrackFadeRate.put(cmd.path, cmd.volume / cmd.fadeTime * 10);
                return;
            }
        }
    }

    @Override
    public void stopMusic()
    {
        if (this.currentMusic != null)
            this.currentMusic.stop();

        if (this.prevMusic != null)
            this.prevMusic.stop();

        this.currentMusic = null;
        this.prevMusic = null;
        this.musicID = null;

        for (String s: this.syncedTracks.keySet())
        {
            this.syncedTrackFadeRate.put(s, -1.0f);
            this.syncedMusicCommands.put(s, SyncedMusicCommand.remove(s));
        }
    }

    @Override
    public void registerCombinedMusic(String path, String id)
    {

    }

    @Override
    public void exit()
    {

    }

    @Override
    public void update()
    {
        this.musicPlaying = this.currentMusic != null && this.currentMusic.isPlaying();

        if (this.prevMusic != null && this.fadeEnd < System.currentTimeMillis())
        {
            if (this.currentMusic != null)
                this.currentMusic.setVolume(this.currentVolume);

            if (this.fadeEnd != this.fadeBegin)
            {
                float p = this.prevMusic.getPosition();
                float c = this.currentMusic.getPosition();
                float l = this.latency + p - c;
                this.latency = l * 0.25f + this.latency * 0.75f;
            }

            this.prevMusic.stop();
            this.prevMusic = null;
        }

        if (this.prevMusic != null && this.currentMusic != null)
        {
            double frac = (System.currentTimeMillis() - this.fadeBegin) * 1.0 / (this.fadeEnd - this.fadeBegin);

            this.prevMusic.setVolume((float) (this.prevVolume * (1 - frac)));
            this.currentMusic.setVolume((float) (this.currentVolume * frac));
        }

        this.updateSyncedMusic();

        for (String s: this.syncedTracks.keySet())
        {
            Music m = this.syncedTracks.get(s);
            float vol = this.syncedTrackCurrentVolumes.get(s);

            if (this.stoppingSyncedTracks.containsKey(s))
            {
                vol = (float) (vol + window.frameFrequency * this.syncedTrackFadeRate.get(s));
                m.setVolume(Math.max(0, vol));
                this.syncedTrackCurrentVolumes.put(s, vol);

                if (vol <= 0)
                    this.syncedMusicCommands.put(s, SyncedMusicCommand.remove(s));
            }
            else
            {
                if (vol < this.syncedTrackMaxVolumes.get(s))
                {
                    vol = (float) Math.min(vol + window.frameFrequency * this.syncedTrackFadeRate.get(s), this.syncedTrackMaxVolumes.get(s));
                    this.syncedTrackCurrentVolumes.put(s, vol);
                    m.setVolume(vol);
                }
            }
        }
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
