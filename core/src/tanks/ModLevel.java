package tanks;

import tanks.Game;
import tanks.Level;
import tanks.gui.screen.ScreenGame;
import tanks.gui.screen.ScreenInterlevel;
import tanks.tank.Tank;
import tanks.tank.TankPlayer;
import tanks.tank.TankPlayerRemote;

public abstract class ModLevel extends Level
{
    /**
     * The amount of coins one gets from killing players.
     */
    public int playerKillCoins = 0;

    /**
     * Forcibly disable the minimap. Useful for games like hide and seek.
     */
    public boolean forceDisableMinimap = false;

    public boolean enableKillMessages = false;

    /**
     * If you used getLevelString(), make sure to switch it with the actual level string before publishing it!
     */
    public ModLevel(String levelString)
    {
        super(levelString);
    }

    @Override
    public void loadLevel()
    {
        ModAPI.menuGroup.clear();
        ScreenInterlevel.fromModdedLevels = true;

        super.loadLevel();
        setUp();

        Game.screen = new ScreenGame();
    }

    /**
     * Add custom scoreboards, text, etc.
     */
    public void setUp()
    {

    }

    /**
     * Update the custom items here
     */
    public void update()
    {

    }

    /**
     * Override this method to do something when the level ends
     */
    public void onLevelEnd(boolean levelWon)
    {

    }

    public String generateKillMessage(Tank killed, Tank killer, boolean isBullet)
    {
        StringBuilder message = new StringBuilder();

        String killedR;
        String killedG;
        String killedB;

        String killR;
        String killG;
        String killB;

        if (killed.team != null && killed.team.enableColor)
        {
            killedR = String.format("%03d", (int) killed.team.teamColorR);
            killedG = String.format("%03d", (int) killed.team.teamColorG);
            killedB = String.format("%03d", (int) killed.team.teamColorB);
        }
        else
        {
            killedR = String.format("%03d", (int) killed.colorR);
            killedG = String.format("%03d", (int) killed.colorG);
            killedB = String.format("%03d", (int) killed.colorB);
        }

        if (killer.team != null && killer.team.enableColor)
        {
            killR = String.format("%03d", (int) killer.team.teamColorR);
            killB = String.format("%03d", (int) killer.team.teamColorG);
            killG = String.format("%03d", (int) killer.team.teamColorB);
        }
        else
        {
            killR = String.format("%03d", (int) killer.colorR);
            killG = String.format("%03d", (int) killer.colorG);
            killB = String.format("%03d", (int) killer.colorB);
        }

        message.append("\u00a7").append(killedR).append(killedG).append(killedB).append("255");

        if (killed instanceof TankPlayer)
            message.append(((TankPlayer) killed).player.username);
        else if (killed instanceof TankPlayerRemote)
            message.append(((TankPlayerRemote) killed).player.username);
        else
        {
            String name = killed.getClass().getSimpleName();
            StringBuilder outputName = new StringBuilder();
            int prevBeginIndex = 0;

            for (int i = 1; i < name.length(); i++)
            {
                if (65 <= name.charAt(i) && name.charAt(i) <= 90)
                {
                    if (prevBeginIndex > 0)
                        outputName.append(name, prevBeginIndex, i).append(" ");
                    prevBeginIndex = i;
                }
            }
            outputName.append(name.substring(prevBeginIndex)).append(" Tank");
            message.append(outputName.toString());
        }
        message.append("\u00a7000000000255 was ").append(isBullet ? "shot" : "blown up").append(" by ").append("\u00a7").append(killR).append(killG).append(killB).append("255");

        if (killer instanceof TankPlayer)
            message.append(((TankPlayer) killer).player.username);

        else if (killer instanceof TankPlayerRemote)
            message.append(((TankPlayerRemote) killer).player.username);

        else
        {
            String name = killer.getClass().getSimpleName();
            StringBuilder outputName = new StringBuilder();
            int prevBeginIndex = 0;

            for (int i = 1; i < name.length(); i++)
            {
                if (65 <= name.charAt(i) && name.charAt(i) <= 90)
                {
                    if (prevBeginIndex > 0)
                        outputName.append(name, prevBeginIndex, i).append(" ");
                    prevBeginIndex = i;
                }
            }
            outputName.append(name.substring(prevBeginIndex)).append(" Tank");
            message.append(outputName.toString());
        }

        return message.toString();
    }

    public String generateDrownMessage(Tank killed)
    {
        StringBuilder message = new StringBuilder();

        String killedR;
        String killedG;
        String killedB;

        if (killed.team != null && killed.team.enableColor)
        {
            killedR = String.format("%03d", (int) killed.team.teamColorR);
            killedG = String.format("%03d", (int) killed.team.teamColorG);
            killedB = String.format("%03d", (int) killed.team.teamColorB);
        }
        else
        {
            killedR = String.format("%03d", (int) killed.colorR);
            killedG = String.format("%03d", (int) killed.colorG);
            killedB = String.format("%03d", (int) killed.colorB);
        }

        message.append("\u00a7").append(killedR).append(killedG).append(killedB).append("255");

        if (killed instanceof TankPlayer)
            message.append(((TankPlayer) killed).player.username);

        else if (killed instanceof TankPlayerRemote)
            message.append(((TankPlayerRemote) killed).player.username);

        else
        {
            String name = killed.getClass().getSimpleName();
            StringBuilder outputName = new StringBuilder();
            int prevBeginIndex = 0;

            for (int i = 1; i < name.length(); i++)
            {
                if (65 <= name.charAt(i) && name.charAt(i) <= 90)
                {
                    if (prevBeginIndex > 0)
                        outputName.append(name, prevBeginIndex, i).append(" ");
                    prevBeginIndex = i;
                }
            }
            outputName.append(name.substring(prevBeginIndex)).append(" Tank");
            message.append(outputName.toString());
        }

        message.append("\u00a7000000000255 drowned");
        return message.toString();
    }
}