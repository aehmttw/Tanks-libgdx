package libgdxwindow;

import basewindow.BaseShapeRenderer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;

public class LibGDXShapeRenderer extends BaseShapeRenderer
{
    public LibGDXWindow window;

    public LibGDXShapeRenderer(LibGDXWindow window)
    {
        this.window = window;
    }
    
    @Override
    public void fillOval(double x, double y, double sX, double sY)
    {
        x += sX / 2;
        y += sY / 2;

        int sides = Math.min((int) (sX + sY) / 4 + 5, 100000);

        this.window.setDrawMode(GL20.GL_TRIANGLES, false, true, sides * 3);
        double step = Math.PI * 2 / sides;

        float pX =  (float) (x + Math.cos(0) * sX / 2);
        float pY =  (float) (y + Math.sin(0) * sY / 2);
        double d = 0;
        for (int n = 0; n < sides; n++)
        {
            d += step;

            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex(pX, pY, 0);
            pX = (float) (x + Math.cos(d) * sX / 2);
            pY = (float) (y + Math.sin(d) * sY / 2);
            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex(pX, pY, 0);
            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex((float) x, (float) y, 0);
        }
    }

    @Override
    public void fillOval(double x, double y, double z, double sX, double sY, boolean depthTest)
    {
        x += sX / 2;
        y += sY / 2;

        int sides = (int) Math.min((sX + sY + Math.max(z / 20, 0)) / 4 + 5, 100000);

        this.window.setDrawMode(GL20.GL_TRIANGLES, depthTest, this.window.colorA >= 1, sides * 3);
        double step = Math.PI * 2 / sides;

        float pX =  (float) (x + Math.cos(0) * sX / 2);
        float pY =  (float) (y + Math.sin(0) * sY / 2);
        double d = 0;
        for (int n = 0; n < sides; n++)
        {
            d += step;

            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex((float) x, (float) y, (float) z);
            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex(pX, pY, (float) z);
            pX = (float) (x + Math.cos(d) * sX / 2);
            pY = (float) (y + Math.sin(d) * sY / 2);
            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex(pX, pY, (float) z);
        }
    }

    @Override
    public void fillPartialOval(double x, double y, double sX, double sY, double start, double end)
    {

    }

    @Override
    public void fillFacingOval(double x, double y, double z, double sX, double sY, boolean depthTest)
    {

    }

    @Override
    public void fillGlow(double x, double y, double sX, double sY, boolean shade)
    {
        this.fillGlow(x, y, sX, sY, shade, false);
    }

    @Override
    public void fillGlow(double x, double y, double sX, double sY, boolean shade, boolean light)
    {
        x += sX / 2;
        y += sY / 2;

        int sides = Math.min((int) (sX + sY) / 16 + 5, 100000);

        if (!shade)
            this.window.color = Color.toFloatBits((float)(this.window.colorR * this.window.colorA), (float)(this.window.colorG * this.window.colorA), (float) (this.window.colorB * this.window.colorA), 1);
        else
            this.window.color = Color.toFloatBits((float) this.window.colorR, (float) this.window.colorG, (float) this.window.colorB, (float) this.window.colorA);

        float transparent = this.window.transparent;

        if (shade)
            transparent = Color.toFloatBits((float) this.window.colorR, (float) this.window.colorG, (float) this.window.colorB, 0);

        this.window.setDrawMode(GL20.GL_TRIANGLES, false, false, !shade, light,sides * 3);
        double step = Math.PI * 2 / sides;

        float pX =  (float) (x + Math.cos(0) * sX / 2);
        float pY =  (float) (y + Math.sin(0) * sY / 2);
        double d = 0;
        for (int n = 0; n < sides; n++)
        {
            d += step;

            this.window.renderer.color(transparent);
            this.window.renderer.vertex(pX, pY, 0);
            pX = (float) (x + Math.cos(d) * sX / 2);
            pY = (float) (y + Math.sin(d) * sY / 2);
            this.window.renderer.color(transparent);
            this.window.renderer.vertex(pX, pY, 0);
            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex((float) x, (float) y, 0);
        }
    }

    @Override
    public void fillGlow(double x, double y, double z, double sX, double sY, boolean depthTest, boolean shade)
    {
        this.fillGlow(x, y, z, sX, sY, depthTest, shade, false);
    }

    @Override
    public void fillGlow(double x, double y, double z, double sX, double sY, boolean depthTest, boolean shade, boolean light)
    {
        x += sX / 2;
        y += sY / 2;

        int sides = Math.min((int) (sX + sY + Math.max(z / 20, 0)) / 16 + 5, 100000);

        if (!shade)
            this.window.color = Color.toFloatBits((float) (this.window.colorR * this.window.colorA), (float) (this.window.colorG * this.window.colorA), (float) (this.window.colorB * this.window.colorA), 1);
        else
            this.window.color = Color.toFloatBits((float) this.window.colorR, (float) this.window.colorG, (float) this.window.colorB, (float) this.window.colorA);

        float transparent = this.window.transparent;

        if (shade)
            transparent = Color.toFloatBits((float) this.window.colorR, (float) this.window.colorG, (float) this.window.colorB, 0);

        this.window.setDrawMode(GL20.GL_TRIANGLES, depthTest, false, !shade, light, sides * 3);
        double step = Math.PI * 2 / sides;

        float pX =  (float) (x + Math.cos(0) * sX / 2);
        float pY =  (float) (y + Math.sin(0) * sY / 2);
        double d = 0;
        for (int n = 0; n < sides; n++)
        {
            d += step;

            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex((float) x, (float) y, (float) z);
            this.window.renderer.color(transparent);
            this.window.renderer.vertex(pX, pY, (float) z);
            pX = (float) (x + Math.cos(d) * sX / 2);
            pY = (float) (y + Math.sin(d) * sY / 2);
            this.window.renderer.color(transparent);
            this.window.renderer.vertex(pX, pY, (float) z);
        }
    }

    @Override
    public void fillFacingGlow(double x, double y, double z, double sX, double sY, boolean depthTest, boolean shade)
    {

    }

    @Override
    public void fillFacingGlow(double x, double y, double z, double sX, double sY, boolean depthTest, boolean shade, boolean light)
    {

    }

    @Override
    public void fillGlow(double x, double y, double sX, double sY)
    {
        this.fillGlow(x, y, sX, sY, false);
    }

    @Override
    public void fillGlow(double x, double y, double z, double sX, double sY, boolean depthTest)
    {
        this.fillGlow(x, y, z, sX, sY, depthTest, false);
    }

    @Override
    public void fillFacingGlow(double x, double y, double z, double sX, double sY, boolean depthTest)
    {

    }

    @Override
    public void drawOval(double x, double y, double sX, double sY)
    {
        drawOval(x, y, 0, sX, sY);
    }

    @Override
    public void drawOval(double x, double y, double z, double sX, double sY)
    {
        x += sX / 2;
        y += sY / 2;

        int sides = Math.min((int) (sX + sY + 5), 100000);

        this.window.setDrawMode(GL20.GL_LINES, false, true, sides * 2);

        for (double i = 0; i < Math.PI * 2; i += Math.PI * 2 / sides)
        {
            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex((float) (x + Math.cos(i) * sX / 2), (float) (y + Math.sin(i) * sY / 2), (float) z);
            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex((float) (x + Math.cos(i + Math.PI * 2 / sides) * sX / 2), (float) (y + Math.sin(i + Math.PI * 2 / sides) * sY / 2), (float) z);
        }
    }

    @Override
    public void fillRect(double x, double y, double width, double height)
    {
        this.window.setDrawMode(GL20.GL_TRIANGLES, false, true, 6);
        this.window.renderer.color(this.window.color);
        this.window.renderer.vertex((float) x, (float) y, 0);
        this.window.renderer.color(this.window.color);
        this.window.renderer.vertex((float) (x + width), (float) y, 0);
        this.window.renderer.color(this.window.color);
        this.window.renderer.vertex((float) (x + width), (float) (y + height), 0);

        this.window.renderer.color(this.window.color);
        this.window.renderer.vertex((float) x, (float) y, 0);
        this.window.renderer.color(this.window.color);
        this.window.renderer.vertex((float) x, (float) (y + height), 0);
        this.window.renderer.color(this.window.color);
        this.window.renderer.vertex((float) (x + width), (float) (y + height), 0);
    }

    @Override
    public void fillBox(double x, double y, double z, double sX, double sY, double sZ)
    {
        fillBox(x, y, z, sX, sY, sZ, (byte) 0);
    }

    /**
     * Options byte:
     *
     * 0: default
     *
     * +1 hide behind face
     * +2 hide front face
     * +4 hide bottom face
     * +8 hide top face
     * +16 hide left face
     * +32 hide right face
     *
     * +64 draw on top
     * */
    public void fillBox(double posX, double posY, double posZ, double sX, double sY, double sZ, byte options)
    {
        float x = (float) posX;
        float y = (float) posY;
        float z = (float) posZ;
        float width = (float) sX;
        float height = (float) sY;
        float depth = (float) sZ;

        float color2 = Color.toFloatBits((float) this.window.colorR * 0.8f, (float) this.window.colorG * 0.8f, (float) this.window.colorB * 0.8f, (float) this.window.colorA);
        float color3 = Color.toFloatBits((float) this.window.colorR * 0.6f, (float) this.window.colorG * 0.6f, (float) this.window.colorB * 0.6f, (float) this.window.colorA);

        boolean depthMask = true;
        boolean glow = false;

        if (this.window.batchMode)
        {
            depthMask = this.window.depthMask;
            glow = this.window.glow;
        }

        if ((options >> 6) % 2 == 0)
            this.window.setDrawMode(GL20.GL_TRIANGLES, true, depthMask, glow, 36);
        else
            this.window.setDrawMode(GL20.GL_TRIANGLES, false, depthMask, glow, 36);

        if (options % 2 == 0)
        {
            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex(x, y, z);
            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex(x + width, y, z);
            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex(x + width, y + height, z);

            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex(x, y, z);
            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex(x + width, y + height, z);
            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex(x, y + height, z);
        }

        if ((options >> 2) % 2 == 0)
        {
            this.window.renderer.color(color2);
            this.window.renderer.vertex(x, y + height, z);
            this.window.renderer.color(color2);
            this.window.renderer.vertex(x + width, y + height, z);
            this.window.renderer.color(color2);
            this.window.renderer.vertex(x + width, y + height, z + depth);

            this.window.renderer.color(color2);
            this.window.renderer.vertex(x, y + height, z);
            this.window.renderer.color(color2);
            this.window.renderer.vertex(x + width, y + height, z + depth);
            this.window.renderer.color(color2);
            this.window.renderer.vertex(x, y + height, z + depth);
        }

        if ((options >> 3) % 2 == 0)
        {
            this.window.renderer.color(color2);
            this.window.renderer.vertex(x, y, z + depth);
            this.window.renderer.color(color2);
            this.window.renderer.vertex(x + width, y, z + depth);
            this.window.renderer.color(color2);
            this.window.renderer.vertex(x + width, y, z);

            this.window.renderer.color(color2);
            this.window.renderer.vertex(x, y, z + depth);
            this.window.renderer.color(color2);
            this.window.renderer.vertex(x + width, y, z);
            this.window.renderer.color(color2);
            this.window.renderer.vertex(x, y, z);
        }

        if ((options >> 4) % 2 == 0)
        {
            this.window.renderer.color(color3);
            this.window.renderer.vertex(x, y, z + depth);
            this.window.renderer.color(color3);
            this.window.renderer.vertex(x, y, z);
            this.window.renderer.color(color3);
            this.window.renderer.vertex(x, y + height, z);

            this.window.renderer.color(color3);
            this.window.renderer.vertex(x, y, z + depth);
            this.window.renderer.color(color3);
            this.window.renderer.vertex(x, y + height, z);
            this.window.renderer.color(color3);
            this.window.renderer.vertex(x, y + height, z + depth);
        }

        if ((options >> 5) % 2 == 0)
        {
            this.window.renderer.color(color3);
            this.window.renderer.vertex(x + width, y, z);
            this.window.renderer.color(color3);
            this.window.renderer.vertex(x + width, y, z + depth);
            this.window.renderer.color(color3);
            this.window.renderer.vertex(x + width, y + height, z + depth);

            this.window.renderer.color(color3);
            this.window.renderer.vertex(x + width, y, z);
            this.window.renderer.color(color3);
            this.window.renderer.vertex(x + width, y + height, z + depth);
            this.window.renderer.color(color3);
            this.window.renderer.vertex(x + width, y + height, z);
        }

        if ((options >> 1) % 2 == 0)
        {
            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex(x + width, y, z + depth);
            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex(x, y, z + depth);
            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex(x + width, y + height, z + depth);

            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex(x + width, y + height, z + depth);
            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex(x, y, z + depth);
            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex(x, y + height, z + depth);
        }
    }

    @Override
    public void fillQuad(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4)
    {
        this.window.setDrawMode(GL20.GL_TRIANGLES, false, true, 6);

        this.window.renderer.color(this.window.color);
        this.window.renderer.vertex((float) x1, (float) y1, 0);
        this.window.renderer.color(this.window.color);
        this.window.renderer.vertex((float) x2, (float) y2, 0);
        this.window.renderer.color(this.window.color);
        this.window.renderer.vertex((float) x3, (float) y3, 0);

        this.window.renderer.color(this.window.color);
        this.window.renderer.vertex((float) x1, (float) y1, 0);
        this.window.renderer.color(this.window.color);
        this.window.renderer.vertex((float) x4, (float) y4, 0);
        this.window.renderer.color(this.window.color);
        this.window.renderer.vertex((float) x3, (float) y3, 0);
    }

    /**
     * Options byte:
     *
     * 0: default
     *
     * +1 hide behind face
     * +2 hide front face
     * +4 hide bottom face
     * +8 hide top face
     * +16 hide left face
     * +32 hide right face
     *
     * +64 draw on top
     * */
    @Override
    public void fillQuadBox(double posx1, double posy1,
                            double posx2, double posy2,
                            double posx3, double posy3,
                            double posx4, double posy4,
                            double posz, double sizeZ,
                            byte options)
    {
        float x1 = (float) posx1;
        float x2 = (float) posx2;
        float x3 = (float) posx3;
        float x4 = (float) posx4;

        float y1 = (float) posy1;
        float y2 = (float) posy2;
        float y3 = (float) posy3;
        float y4 = (float) posy4;

        float z = (float) posz;
        float sZ = (float) sizeZ;

        float color2 = Color.toFloatBits((float) this.window.colorR * 0.8f, (float) this.window.colorG * 0.8f, (float) this.window.colorB * 0.8f, (float) this.window.colorA);
        float color3 = Color.toFloatBits((float) this.window.colorR * 0.6f, (float) this.window.colorG * 0.6f, (float) this.window.colorB * 0.6f, (float) this.window.colorA);

        if ((options >> 6) % 2 == 0)
            this.window.setDrawMode(GL20.GL_TRIANGLES, true, true, 36);
        else
            this.window.setDrawMode(GL20.GL_TRIANGLES, false, true, 36);

        if (options % 2 == 0)
        {
            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex(x1, y1, z);
            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex(x2, y2, z);
            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex(x3, y3, z);

            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex(x1, y1, z);
            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex(x4, y4, z);
            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex(x3, y3, z);
        }

        if ((options >> 2) % 2 == 0)
        {
            this.window.renderer.color(color3);
            this.window.renderer.vertex(x1, y1, z + sZ);
            this.window.renderer.color(color3);
            this.window.renderer.vertex(x2, y2, z + sZ);
            this.window.renderer.color(color3);
            this.window.renderer.vertex(x2, y2, z);

            this.window.renderer.color(color3);
            this.window.renderer.vertex(x1, y1, z + sZ);
            this.window.renderer.color(color3);
            this.window.renderer.vertex(x1, y1, z);
            this.window.renderer.color(color3);
            this.window.renderer.vertex(x2, y2, z);
        }

        if ((options >> 3) % 2 == 0)
        {
            this.window.renderer.color(color3);
            this.window.renderer.vertex(x3, y3, z + sZ);
            this.window.renderer.color(color3);
            this.window.renderer.vertex(x4, y4, z + sZ);
            this.window.renderer.color(color3);
            this.window.renderer.vertex(x4, y4, z);

            this.window.renderer.color(color3);
            this.window.renderer.vertex(x3, y3, z + sZ);
            this.window.renderer.color(color3);
            this.window.renderer.vertex(x3, y3, z);
            this.window.renderer.color(color3);
            this.window.renderer.vertex(x4, y4, z);
        }

        if ((options >> 4) % 2 == 0)
        {
            this.window.renderer.color(color2);
            this.window.renderer.vertex(x1, y1, z + sZ);
            this.window.renderer.color(color2);
            this.window.renderer.vertex(x4, y4, z + sZ);
            this.window.renderer.color(color2);
            this.window.renderer.vertex(x4, y4, z);

            this.window.renderer.color(color2);
            this.window.renderer.vertex(x1, y1, z + sZ);
            this.window.renderer.color(color2);
            this.window.renderer.vertex(x1, y1, z);
            this.window.renderer.color(color2);
            this.window.renderer.vertex(x4, y4, z);
        }

        if ((options >> 5) % 2 == 0)
        {
            this.window.renderer.color(color2);
            this.window.renderer.vertex(x3, y3, z + sZ);
            this.window.renderer.color(color2);
            this.window.renderer.vertex(x2, y2, z + sZ);
            this.window.renderer.color(color2);
            this.window.renderer.vertex(x2, y2, z);

            this.window.renderer.color(color2);
            this.window.renderer.vertex(x3, y3, z + sZ);
            this.window.renderer.color(color2);
            this.window.renderer.vertex(x3, y3, z);
            this.window.renderer.color(color2);
            this.window.renderer.vertex(x2, y2, z);
        }

        if ((options >> 1) % 2 == 0)
        {
            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex(x1, y1, z + sZ);
            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex(x2, y2, z + sZ);
            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex(x3, y3, z + sZ);

            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex(x1, y1, z + sZ);
            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex(x4, y4, z + sZ);
            this.window.renderer.color(this.window.color);
            this.window.renderer.vertex(x3, y3, z + sZ);
        }
    }

    @Override
    public void drawRect(double x, double y, double sX, double sY)
    {
        this.window.setDrawMode(GL20.GL_LINES, false, true, 8);
        this.window.renderer.color(this.window.color);
        this.window.renderer.vertex((float) x, (float) y, 0);
        this.window.renderer.color(this.window.color);
        this.window.renderer.vertex((float) (x + sX), (float) y, 0);

        this.window.renderer.color(this.window.color);
        this.window.renderer.vertex((float) x, (float) y, 0);
        this.window.renderer.color(this.window.color);
        this.window.renderer.vertex((float) x, (float) (y + sY), 0);

        this.window.renderer.color(this.window.color);
        this.window.renderer.vertex((float) (x + sX), (float) y, 0);
        this.window.renderer.color(this.window.color);
        this.window.renderer.vertex((float) (x + sX), (float) (y + sY), 0);

        this.window.renderer.color(this.window.color);
        this.window.renderer.vertex((float) x, (float) (y + sY), 0);
        this.window.renderer.color(this.window.color);
        this.window.renderer.vertex((float) (x + sX), (float) (y + sY), 0);
    }

    @Override
    public void drawImage(double x, double y, double sX, double sY, String image, boolean scaled)
    {
        drawImage(x, y, 0, sX, sY, 0, 0, 1, 1, image, scaled, false);
    }

    @Override
    public void drawImage(double x, double y, double z, double sX, double sY, String image, boolean scaled)
    {
        drawImage(x, y, z, sX, sY, 0, 0, 1, 1, image, scaled, true);
    }

    @Override
    public void drawImage(double x, double y, double sX, double sY, double u1, double v1, double u2, double v2, String image, boolean scaled)
    {
        drawImage(x, y, 0, sX, sY, u1, v1, u2, v2, image, scaled, false);
    }

    @Override
    public void drawImage(double x, double y, double z, double sX, double sY, double u1, double v1, double u2, double v2, String image, boolean scaled)
    {
        drawImage(x, y, z, sX, sY, u1, v1, u2, v2, image, scaled, true);
    }

    @Override
    public void drawImage(double x, double y, double z, double sX, double sY, double u1, double v1, double u2, double v2, String image, boolean scaled, boolean depthtest)
    {
        this.window.setDrawMode(7, depthtest, true, 0);

        if (image.startsWith("/"))
            image = image.substring(1);

        Texture texture = this.window.textures.get(image);

        if (texture == null)
        {
            texture = new Texture(Gdx.files.internal(image));
            this.window.textures.put(image, texture);
        }

        double width = sX * (u2 - u1);
        double height = sY * (v2 - v1);

        if (scaled)
        {
            width *= texture.getWidth();
            height *= texture.getHeight();
        }

        this.window.spriteBatch.getProjectionMatrix().translate(0, 0, (float) (z));
        this.window.spriteBatch.begin();
        this.window.spriteBatch.setColor((float) this.window.colorR, (float) this.window.colorG, (float) this.window.colorB, (float) this.window.colorA);
        this.window.spriteBatch.draw(texture, (float) x, (float) y, (float) width, (float) height, (float) u1, (float) v1, (float) u2, (float) v2);
        this.window.spriteBatch.end();
        this.window.spriteBatch.getProjectionMatrix().translate(0, 0, (float) (-z));

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void drawImage(double x, double y, double sX, double sY, String image, double rotation, boolean scaled)
    {
        this.window.rotate(x, y, rotation);
        this.drawImage(x - sX / 2, y - sY / 2, sX, sY, image, scaled);
        this.window.rotate(x, y, -rotation);
    }

    @Override
    public void drawImage(double x, double y, double z, double sX, double sY, String image, double rotation, boolean scaled)
    {
        this.window.rotate(x, y, rotation);
        this.drawImage(x - sX / 2, y - sY / 2, z, sX, sY, image, scaled);
        this.window.rotate(x, y, -rotation);
    }

    @Override
    public void drawImage(double x, double y, double sX, double sY, double u1, double v1, double u2, double v2, String image, double rotation, boolean scaled)
    {
        this.window.rotate(x, y, rotation);
        this.drawImage(x - sX / 2, y - sY / 2, sX, sY, u1, v1, u2, v2, image, scaled);
        this.window.rotate(x, y, -rotation);
    }

    @Override
    public void drawImage(double x, double y, double z, double sX, double sY, double u1, double v1, double u2, double v2, String image, double rotation, boolean scaled)
    {
        this.window.rotate(x, y, rotation);
        this.drawImage(x - sX / 2, y - sY / 2, z, sX, sY, u1, v1, u2, v2, image, scaled);
        this.window.rotate(x, y, -rotation);
    }

    @Override
    public void drawImage(double x, double y, double z, double sX, double sY, double u1, double v1, double u2, double v2, String image, double rotation, boolean scaled, boolean depthtest)
    {
        this.window.rotate(x, y, rotation);
        this.drawImage(x - sX / 2, y - sY / 2, z, sX, sY, u1, v1, u2, v2, image, scaled, depthtest);
        this.window.rotate(x, y, -rotation);
    }

    @Override
    public void setBatchMode(boolean enabled, boolean quads, boolean depth)
    {
        this.window.setBatchMode(enabled, quads, depth);
    }

    @Override
    public void setBatchMode(boolean enabled, boolean quads, boolean depth, boolean glow)
    {
        this.window.setBatchMode(enabled, quads, depth, glow);
    }

    @Override
    public void setBatchMode(boolean enabled, boolean quads, boolean depth, boolean glow, boolean depthMask)
    {
        this.window.setBatchMode(enabled, quads, depth, glow, depthMask);
    }
}
