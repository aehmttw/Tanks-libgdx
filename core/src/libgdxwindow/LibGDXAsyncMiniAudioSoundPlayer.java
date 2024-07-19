package libgdxwindow;

import basewindow.BaseSoundPlayer;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import games.rednblack.miniaudio.MASound;
import games.rednblack.miniaudio.MiniAudio;
import tanks.Game;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class LibGDXAsyncMiniAudioSoundPlayer extends BaseSoundPlayer
{
    public LibGDXWindow window;

    public HashMap<String, Long> lastPlayed = new HashMap<>();

    public HashMap<String, MASound> syncedTracks = new HashMap<>();
    public HashMap<String, MASound> stoppingSyncedTracks = new HashMap<>();
    public HashMap<String, Float> syncedTrackCurrentVolumes = new HashMap<>();
    public HashMap<String, Float> syncedTrackMaxVolumes = new HashMap<>();
    public HashMap<String, Float> syncedTrackFadeRate = new HashMap<>();
    protected ArrayList<String> removeTracks = new ArrayList<>();

    public HashMap<String, MASound> musicMap = new HashMap<>();

    public MASound currentMusic = null;
    public MASound prevMusic = null;

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

    public Queue<MASound> oldSounds = new LinkedList<>();

    public static MiniAudio miniAudio = new MiniAudio();

    public LibGDXAsyncMiniAudioSoundPlayer(LibGDXWindow window)
    {
        this.window = window;

        FileHandle[] musics = Gdx.files.internal("music").list();
        for (FileHandle f : musics)
        {
            String path = f.path();
            if (path.startsWith("/"))
                path = path.substring(1);

            if (f.path().endsWith(".ogg"))
            {
                try
                {
                    MASound s = miniAudio.createSound(f.path());
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
        if (last != null && System.currentTimeMillis() - last < 20)
            return;

        lastPlayed.put(path, System.currentTimeMillis());

        MASound s = miniAudio.createSound(path);
        s.setPitch(pitch);
        s.setVolume(volume);
        s.play();
        oldSounds.add(s);

        if (oldSounds.size() > 150)
            oldSounds.remove().dispose();
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
            MASound m = getMusic(path);

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
                float s = prevMusic.getCursorPosition();
                m.seekTo(s / 2);
                m.setVolume(0);

                fadeEnd = System.currentTimeMillis() + fadeTime;
            }
            else
            {
                for (MASound s: syncedTracks.values())
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

    public MASound getMusic(String path)
    {
        if (path.startsWith("/"))
            path = path.substring(1);

        MASound s = musicMap.get(path);
        if (s == null)
        {
            s = miniAudio.createSound(path);
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

            for (MASound m: syncedTracks.values())
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

            for (MASound s: syncedTracks.values())
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
            MASound s = stoppingSyncedTracks.remove(path);
            if (s != null)
                s.stop();

            if (currentMusic == null)
                return;

            MASound m = getMusic(path);
            m.play();

            m.setLooping(looped);
            m.setVolume(0);
            m.seekTo(currentMusic.getCursorPosition() / 2);
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
                float p = this.prevMusic.getCursorPosition();
                float c = this.currentMusic.getCursorPosition();
                //this.latency = l * 0.25f + this.latency * 0.75f;
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
            MASound m = this.syncedTracks.get(s);
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
