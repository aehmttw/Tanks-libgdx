package libgdxwindow;

import basewindow.BaseSoundPlayer;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import tanks.Game;
import tanks.network.SynchronizedList;

import java.io.InputStream;
import java.util.*;

public class LibGDXAsyncSoundPlayer extends BaseSoundPlayer
{
    public LibGDXWindow window;

    public HashMap<String, Sound> soundsMap = new HashMap<>();
    public HashMap<String, Long> lastPlayed = new HashMap<>();

    public HashMap<String, Music> syncedTracks = new HashMap<>();
    public HashMap<String, Music> stoppingSyncedTracks = new HashMap<>();
    public HashMap<String, Float> syncedTrackCurrentVolumes = new HashMap<>();
    public HashMap<String, Float> syncedTrackMaxVolumes = new HashMap<>();
    public HashMap<String, Float> syncedTrackFadeRate = new HashMap<>();
    protected ArrayList<String> removeTracks = new ArrayList<>();

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

    public final Queue<MusicCommand> musicCommands = new LinkedList<>();
    ArrayList<MusicCommand> tempMusicCommands = new ArrayList<>();

    public float latency = 0.086f;

    public LibGDXAsyncSoundPlayer(LibGDXWindow window)
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

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (true)
                {
                    try
                    {
                        updateAsync();
                    }
                    catch (Exception e)
                    {
                        Game.exitToCrash(e);
                    }
                }
            }
        }).start();
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
        if (last != null && System.currentTimeMillis() - last < 100 && Gdx.app.getType() == Application.ApplicationType.iOS)
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

    public abstract class MusicCommand
    {
        public abstract void execute();
    }

    public class PlayMusicCommand extends MusicCommand
    {
        String path;
        float volume;
        boolean looped;
        String continueID;
        long fadeTime;
        boolean stoppable;

        public PlayMusicCommand(String path, float volume, boolean looped, String continueID, long fadeTime, boolean stoppable)
        {
            this.path = path;
            this.volume = volume;
            this.looped = looped;
            this.continueID = continueID;
            this.fadeTime = fadeTime;
            this.stoppable = stoppable;
        }

        public void execute()
        {
            Music m = getMusic(path);

            if (currentMusic == m)
            {
                m.setVolume(volume);
                return;
            }

            if (prevMusic != null)
                prevMusic.stop();

            prevMusic = currentMusic;
            currentMusic = m;
            currentVolume = volume;

            m.play();
            m.setLooping(looped);
            m.setVolume(volume);

            fadeBegin = System.currentTimeMillis();
            if (continueID != null && continueID.equals(musicID))
            {
                float p = prevMusic.getPosition();
                m.setPosition(p + latency);
                m.setVolume(0);

                fadeEnd = System.currentTimeMillis() + fadeTime;
            }
            else
            {
                for (Music s: syncedTracks.values())
                    s.stop();

                syncedTracks.clear();
                stoppingSyncedTracks.clear();
                syncedTrackMaxVolumes.clear();
                syncedTrackCurrentVolumes.clear();
                syncedTrackFadeRate.clear();

                fadeEnd = System.currentTimeMillis();
            }

            prevVolume = volume;
            musicID = continueID;
        }
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
        synchronized (this.musicCommands)
        {
            this.musicCommands.add(new PlayMusicCommand(path, volume, looped, continueID, fadeTime, stoppable));
        }
    }

    public class SetMusicVolumeCommand extends MusicCommand
    {
        float volume;
        public SetMusicVolumeCommand(float volume)
        {
            this.volume = volume;
        }

        @Override
        public void execute()
        {
            currentVolume = volume;
            currentMusic.setVolume(volume);

            for (Music m: syncedTracks.values())
                m.setVolume(volume);
        }
    }

    @Override
    public void setMusicVolume(float volume)
    {
        synchronized (this.musicCommands)
        {
            this.musicCommands.add(new SetMusicVolumeCommand(volume));
        }
    }

    @Override
    public void setMusicSpeed(float speed)
    {

    }

    public class StopMusicCommand extends MusicCommand
    {
        public StopMusicCommand()
        {

        }

        @Override
        public void execute()
        {
            if (currentMusic != null)
                currentMusic.stop();

            if (prevMusic != null)
                prevMusic.stop();

            currentMusic = null;
            prevMusic = null;
            musicID = null;

            for (Music s: syncedTracks.values())
                s.stop();

            syncedTracks.clear();
            stoppingSyncedTracks.clear();
            syncedTrackMaxVolumes.clear();
            syncedTrackCurrentVolumes.clear();
            syncedTrackFadeRate.clear();
        }
    }

    @Override
    public void stopMusic()
    {
        synchronized (this.musicCommands)
        {
            this.musicCommands.add(new StopMusicCommand());
        }
    }

    public class AddSyncedMusicCommand extends MusicCommand
    {
        String path;
        float volume;
        boolean looped;
        long fadeTime;

        public AddSyncedMusicCommand(String path, float volume, boolean looped, long fadeTime)
        {
            this.path = path;
            this.volume = volume;
            this.looped = looped;
            this.fadeTime = fadeTime;
        }

        @Override
        public void execute()
        {
            Music s = stoppingSyncedTracks.remove(path);
            if (s != null)
                s.stop();

            if (currentMusic == null)
                return;

            Music m = getMusic(path);
            m.play();

            m.setLooping(looped);
            m.setVolume(0);
            m.setPosition(currentMusic.getPosition() + latency);
            syncedTracks.put(path, m);
            syncedTrackCurrentVolumes.put(path, 0f);
            syncedTrackMaxVolumes.put(path, volume);
            syncedTrackFadeRate.put(path, volume / fadeTime * 10);
        }
    }


    @Override
    public void addSyncedMusic(String path, float volume, boolean looped, long fadeTime)
    {
        synchronized (this.musicCommands)
        {
            this.musicCommands.add(new AddSyncedMusicCommand(path, volume, looped, fadeTime));
        }
    }

    public class RemoveSyncedMusicCommand extends MusicCommand
    {
        String path;
        long fadeTime;

        public RemoveSyncedMusicCommand(String path, long fadeTime)
        {
            this.path = path;
            this.fadeTime = fadeTime;
        }

        @Override
        public void execute()
        {
            if (syncedTracks.containsKey(path))
            {
                stoppingSyncedTracks.put(path, syncedTracks.get(path));
                syncedTrackFadeRate.put(path, syncedTrackMaxVolumes.get(path) / fadeTime * 10);
            }
        }
    }

    @Override
    public void removeSyncedMusic(String path, long fadeTime)
    {
        synchronized (this.musicCommands)
        {
            this.musicCommands.add(new RemoveSyncedMusicCommand(path, fadeTime));
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

    }

    public void updateAsync() throws InterruptedException
    {
        tempMusicCommands.clear();
        synchronized (musicCommands)
        {
            while (!musicCommands.isEmpty())
            {
                tempMusicCommands.add(musicCommands.remove());
            }
        }

        for (MusicCommand m: tempMusicCommands)
        {
            m.execute();
        }

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

        for (String s: this.syncedTracks.keySet())
        {
            Music m = this.syncedTracks.get(s);
            float vol = this.syncedTrackCurrentVolumes.get(s);

            if (this.stoppingSyncedTracks.containsKey(s))
            {
                vol = (float) (vol - window.frameFrequency * this.syncedTrackFadeRate.get(s));
                m.setVolume(Math.max(0, vol));
                this.syncedTrackCurrentVolumes.put(s, vol);

                if (vol <= 0)
                {
                    m.stop();
                    this.stoppingSyncedTracks.remove(s);
                    this.syncedTrackFadeRate.remove(s);
                    this.syncedTrackMaxVolumes.remove(s);
                    this.syncedTrackCurrentVolumes.remove(s);
                    removeTracks.add(s);
                }
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

        for (String r: removeTracks)
        {
            this.syncedTracks.remove(r);
        }

        this.removeTracks.clear();

        Thread.sleep(10);
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
