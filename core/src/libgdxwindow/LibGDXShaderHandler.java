package libgdxwindow;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.BufferUtils;
import theopalgames.tanks.Tanks;

import java.nio.Buffer;
import java.nio.IntBuffer;

import static com.badlogic.gdx.graphics.GL20.*;

public class LibGDXShaderHandler
{
    public int size = 2048;
    public double quality = 1.25;

    public LibGDXWindow window;

    public FrameBuffer frameBuffer;

    protected IntBuffer mainFbo = BufferUtils.newIntBuffer(1);

    protected static int currentProgram = 0;

    public static void setProgram(int i)
    {
        Gdx.gl.glUseProgram(i);
        currentProgram = i;
    }

    public boolean initialized;

    float[] biasMatrix = new float[]
            {
                    0.5f, 0.0f, 0.0f, 0.0f,
                    0.0f, 0.5f, 0.0f, 0.0f,
                    0.0f, 0.0f, 0.5f, 0.0f,
                    0.5f, 0.5f, 0.5f, 1.0f
            };

    public LibGDXShaderHandler(LibGDXWindow window)
    {
        this.window = window;

        this.createFbo();
    }

    public int createDepthRenderBuffer(int size)
    {
        int i = Gdx.gl.glGenBuffer();
        Gdx.gl.glBindRenderbuffer(GL_RENDERBUFFER, i);
        Gdx.gl.glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT16, size, size);
        return i;
    }

    public int createColorRenderBuffer(int size)
    {
        int i = Gdx.gl.glGenRenderbuffer();
        Gdx.gl.glBindRenderbuffer(GL_RENDERBUFFER, i);
        Gdx.gl.glRenderbufferStorage(GL_RENDERBUFFER, GL_RGBA, size, size);
        return i;
    }

    public void createFbo()
    {
        FrameBuffer fbo = new FrameBuffer(Pixmap.Format.RGBA8888, size, size, true, false);
        this.frameBuffer = fbo;
    }

    public void renderShadowMap()
    {
        this.window.drawingShadow = true;

        int s = (int) (Math.max(this.window.absoluteHeight, this.window.absoluteWidth) * this.quality);
        if (s > 0 && s != this.size)
        {
            this.size = (int) (this.quality * Math.max(this.window.absoluteHeight, this.window.absoluteWidth));
            this.createFbo();
        }

        this.window.setShader(this.window.shaderDefault.shaderShadowMap);

        this.window.loadPerspective();

        this.mainFbo.clear();
        Gdx.gl.glGetIntegerv(GL_FRAMEBUFFER_BINDING, this.mainFbo);
        //Gdx.gl.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, fbo);

        this.frameBuffer.begin();
        //Gdx.gl.glViewport(0, 0, size, size);

        Gdx.gl.glClearColor(1.0f, 0, 0, 1);
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        //Gdx.gl.glClearDepthf(1.0f);

        this.window.setColor(255, 0, 0);
        //this.window.shapeRenderer.fillBox(200, 200, 100, 300, 300, 300, null);
        this.window.drawer.draw();

        this.window.setDrawMode(-2, false, true, 0);
        this.frameBuffer.end();

        Gdx.gl.glBindFramebuffer(GL_FRAMEBUFFER, this.mainFbo.get());
        setProgram(0);
    }

    public void renderNormal()
    {
        //if (true)
        //    return;

        Matrix4 m = this.window.projectionMatrix.cpy();
        this.window.setShader(this.window.shaderDefault.shaderBase);
        this.window.shaderDefault.shaderBase.shadowres.set(this.size);
        //this.window.shaderDefault.shaderBase.lightVec.set((float) this.window.lightVec[0], (float) this.window.lightVec[1], (float) this.window.lightVec[2]);
        this.window.shaderDefault.shaderBase.shadow.set(this.window.shadowsEnabled);
        this.window.shaderDefault.shaderBase.width.set((float) this.window.absoluteWidth);
        this.window.shaderDefault.shaderBase.height.set((float) this.window.absoluteHeight);
        this.window.shaderDefault.shaderBase.depth.set((float) this.window.absoluteDepth);

        if (!this.initialized)
        {
            this.initialized = true;
            this.window.setLighting(1.0, 1.0, 0.5, 1.0);
        }

        this.window.drawingShadow = false;

        this.window.loadPerspective();

        //float[] projMatrix = new float[16];
        //Gdx.gl.glGetFloatv(GL_PROJECTION_MATRIX, projMatrix);

        this.window.shaderDefault.shaderBase.lightViewProjectionMatrix.set(m.getValues(), false);
        this.window.shaderDefault.shaderBase.biasMatrix.set(biasMatrix, false);

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());

        Gdx.gl.glClearColor(0.0f, 1, 0, 1);
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        Gdx.gl.glActiveTexture(GL_TEXTURE1);
        //Gdx.gl.glBindTexture(GL_TEXTURE_2D, this.frameBuffer.getDepthBufferHandle());
        this.frameBuffer.getColorBufferTexture().bind(1);
        Gdx.gl.glActiveTexture(GL_TEXTURE0);
        Gdx.gl.glBindTexture(GL_TEXTURE_2D, 0);

        this.window.drawer.draw();

        Gdx.gl.glActiveTexture(GL_TEXTURE0);
        Gdx.gl.glBindTexture(GL_TEXTURE_2D, 0);

//        this.window.renderer = this.window.texRenderer;
//        this.window.enableTexture();
//        this.frameBuffer.getColorBufferTexture().bind();
////        Gdx.gl.glBindTexture(GL_TEXTURE_2D, this.frameBuffer.getDepthBufferHandle());
//        this.window.renderer.begin(this.window.projectionMatrix, GL20.GL_TRIANGLES);
//        this.window.drawLinkedImage(100, 100, 50, 300, 300, 0, 0, 1, 1);
//        this.window.renderer.end();
//        this.window.disableTexture();
//        this.window.renderer = this.window.notexRenderer;

//        glBindTexture(GL_TEXTURE_2D, depthTexture);
//        this.window.textures.put("depth", depthTexture);
//        this.window.setColor(255, 255, 255);
//        this.window.shapeRenderer.drawImage(100, 200, 500, 500, "depth", false);

        this.window.setDrawMode(-2, false, true, 0);
        setProgram(0);
    }
}
