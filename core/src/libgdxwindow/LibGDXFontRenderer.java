package libgdxwindow;

import basewindow.BaseFontRenderer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class LibGDXFontRenderer extends BaseFontRenderer
{
    String chars;
    int[] charSizes;
    String image;

    //how many characters fit per horizontal line
    public float size = 16;

    //spacing between rows, increase this to 2 for antialiasing to prevent weird artifacts
    public int hSpace = 2;

    public LibGDXFontRenderer(LibGDXWindow h, String fontFile)
    {
        super(h);
        this.chars = " !\"#$%&'()*+,-./" +
                "0123456789:;<=>?" +
                "@ABCDEFGHIJKLMNO" +
                "PQRSTUVWXYZ[\\]^_" +
                "'abcdefghijklmno" +
                "pqrstuvwxyz{|}~`" +
                "âăîşţàçæèéêëïôœù" +
                "úûüÿáíóñ¡¿äöå";
        this.charSizes = new int[]
                {
                        3, 2, 4, 5, 5, 6, 5, 2, 3, 3, 4, 5, 1, 5, 1, 5,
                        5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 1, 1, 5, 5, 5, 5,
                        7, 5, 5, 5, 5, 5, 5, 5, 5, 3, 5, 5, 5, 5, 5, 5,
                        5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 3, 5, 3, 5, 5,
                        2, 5, 5, 5, 5, 5, 4, 5, 5, 1, 5, 4, 2, 5, 5, 5,
                        5, 5, 5, 5, 3, 5, 5, 5, 5, 5, 5, 4, 1, 4, 6, 2,
                        5, 5, 5, 5, 3, 5, 5, 7, 5, 5, 5, 5, 3, 5, 7, 5,
                        5, 5, 5, 5, 5, 3, 5, 5, 3, 5, 5, 5, 5
                };

        this.image = fontFile;
    }

    protected int drawChar(double x, double y, double z, double sX, double sY, char c, boolean depthtest)
    {
        int i = this.chars.indexOf(c);

        if (i == -1)
            i = 31;

        int col = i % 16;
        int row = i / 16;
        int width = charSizes[i];
        ((LibGDXWindow) this.window).drawLinkedImage(x, y - sY * 16, z, sX * 32 * size, sY * 32 * size,
                col / size, (row * hSpace) / size,
                (col + width / 8f) / size, (row * hSpace + 2) / size);

        return width;
    }

    @Override
    public boolean supportsChar(char c)
    {
        return this.chars.contains(c + "");
    }

    public void drawString(double x, double y, double z, double sX, double sY, String s)
    {
        if (window.drawingShadow)
            return;

        //((LibGDXWindow)this.window).setDrawMode(7, true, true, 6 * s.length());

//        Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);
//        spriteBatch.setProjectionMatrix(((LibGDXWindow) this.window).projectionMatrix);
//        spriteBatch.setTransformMatrix(((LibGDXWindow) this.window).modelviewMatrix);
//        spriteBatch.getProjectionMatrix().translate(0, 0, (float) (z));
        //spriteBatch.begin();

        double curX = x;
        char[] c = s.toCharArray();

        ((LibGDXWindow) this.window).beginLinkedImages(image, false, true);

        for (int i = 0; i < c.length; i++)
        {
            if (c[i] == '\u00C2')
                continue;
            else if (c[i] == '\u00A7')
            {
                int r = Integer.parseInt(c[i + 1] + "" + c[i + 2] + "" + c[i + 3]);
                int g = Integer.parseInt(c[i + 4] + "" + c[i + 5] + "" + c[i + 6]);
                int b = Integer.parseInt(c[i + 7] + "" + c[i + 8] + "" + c[i + 9]);
                int a = Integer.parseInt(c[i + 10] + "" + c[i + 11] + "" + c[i + 12]);
                this.window.setColor(r, g, b, a);

                i += 12;
            }
            else
                curX += (drawChar(curX, y, z, sX, sY, c[i], true) + 1) * sX * 4;
        }

        ((LibGDXWindow) this.window).endLinkedImages();

        //spriteBatch.end();
        //spriteBatch.getProjectionMatrix().translate(0, 0, (float) (-z));

//        Gdx.gl20.glDisable(GL20.GL_DEPTH_TEST);
//        Gdx.gl.glEnable(GL20.GL_BLEND);
//        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void drawString(double x, double y, double sX, double sY, String s)
    {
        ((LibGDXWindow)this.window).setDrawMode(-1, false, false, 6 * s.length());

        if (window.drawingShadow)
            return;

        Gdx.gl20.glDisable(GL20.GL_DEPTH_TEST);
        double curX = x;
        char[] c = s.toCharArray();

        ((LibGDXWindow) this.window).beginLinkedImages(image, false, false);

        for (int i = 0; i < c.length; i++)
        {
            if (c[i] == '\u00C2')
                continue;
            else if (c[i] == '\u00A7')
            {
                int r = Integer.parseInt(c[i + 1] + "" + c[i + 2] + "" + c[i + 3]);
                int g = Integer.parseInt(c[i + 4] + "" + c[i + 5] + "" + c[i + 6]);
                int b = Integer.parseInt(c[i + 7] + "" + c[i + 8] + "" + c[i + 9]);
                int a = Integer.parseInt(c[i + 10] + "" + c[i + 11] + "" + c[i + 12]);
                this.window.setColor(r, g, b, a);

                i += 12;
            }
            else
                curX += (drawChar(curX, y, 0, sX, sY, c[i], false) + 1) * sX * 4;
        }

        ((LibGDXWindow) this.window).endLinkedImages();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    public double getStringSizeX(double sX, String s)
    {
        double w = 0;
        char[] c = s.toCharArray();

        for (int i = 0; i < c.length; i++)
        {
            if (c[i] == '\u00C2')
                continue;
            else if (c[i] == '\u00A7')
                i += 12;
            else if (this.chars.indexOf(c[i]) == -1)
                c[i] = '?';
            else
                w += (charSizes[this.chars.indexOf(c[i])] + 1) * sX * 4;
        }

        return w - sX * 4;
    }

    public double getStringSizeY(double sY, String s)
    {
        return (sY * 32);
    }
}
