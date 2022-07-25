package libgdxwindow;

import basewindow.*;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.math.Matrix4;
import tanks.Game;
import tanks.Team;
import tanks.gui.TextBox;
import theopalgames.tanks.Tanks;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import static com.badlogic.gdx.Input.Keys.*;

public class LibGDXWindow extends BaseWindow
{
    public Application.ApplicationType appType;

    public boolean previousKeyboard = false;

    public ImmediateModeRenderer20 renderer;
    public SpriteBatch spriteBatch;

    public ImmediateModeRenderer20 notexRenderer;
    public ImmediateModeRenderer20 texRenderer;

    public static final HashMap<Integer, Integer> key_translations = new HashMap<>();

    public HashMap<String, Texture> textures = new HashMap<>();

    public float color;
    public float transparent = Color.toFloatBits(0, 0, 0, 0);

    public Matrix4 perspective = new Matrix4();

    public ArrayList<Integer> rawTextInput = new ArrayList<Integer>();

    protected int currentDrawMode = -1;
    protected boolean depthTest = false;
    protected boolean depthMask = true;
    protected boolean glow;
    protected boolean light;
    protected int currentVertices = 0;
    protected int maxVertices = 1000000;

    public boolean quadMode = false;
    public int quadNum = 0;

    public float col1;
    public float qx1;
    public float qy1;
    public float qz1;

    public float col3;
    public float qx3;
    public float qy3;
    public float qz3;

    public float[] matrix = new float[16];
    public Matrix4 matrix2 = new Matrix4();

    protected boolean batchMode = false;

    public LibGDXWindow(String name, int x, int y, int z, IUpdater u, IDrawer d, IWindowHandler w, boolean vsync, boolean showMouse)
    {
        super(name, x, y, z, u, d, w, vsync, showMouse);
    }

    public void initialize()
    {
        setupKeyMap();

        this.shapeRenderer = new LibGDXShapeRenderer(this);
        this.shapeDrawer = new ImmediateModeModelPart.ImmediateModeShapeDrawer(this);

        perspective.idt().setToProjection(
                (float)(-absoluteWidth / (absoluteDepth * 2.0)),
                (float)(absoluteWidth / (absoluteDepth * 2.0)),
                (float) (absoluteHeight / (absoluteDepth * 2.0)),
                (float)(-absoluteHeight / (absoluteDepth * 2.0)),
                1, (float) (absoluteDepth * 2));
        perspective.translate((float) -(absoluteWidth / 2), (float) (-absoluteHeight / 2), (float) -absoluteDepth);

        notexRenderer = new ImmediateModeRenderer20(maxVertices, false, true, 0);
        texRenderer = new ImmediateModeRenderer20(maxVertices, false, true, 1);

        renderer = notexRenderer;

        spriteBatch = new SpriteBatch();
        spriteBatch.setProjectionMatrix(this.perspective);
        fontRenderer = new LibGDXFontRenderer(this, "font.png");

        this.soundsEnabled = true;
        this.soundPlayer = new LibGDXSoundPlayer();

        this.antialiasingSupported = true;

        Gdx.input.setInputProcessor(new InputAdapter()
        {
            @Override
            public boolean touchDown(int x, int y, int pointer, int button)
            {
                touchPoints.put(pointer, new InputPoint(x, y + absoluteHeight * keyboardOffset));
                absoluteMouseX = x;
                absoluteMouseY = y + absoluteHeight * keyboardOffset;
                pressedButtons.add(button);
                validPressedButtons.add(button);
                return true;
            }

            @Override
            public boolean touchDragged(int x, int y, int pointer)
            {
                validPressedButtons.remove((Integer) 0);
                InputPoint i = touchPoints.get(pointer);
                i.x = x;
                i.y = y + absoluteHeight * keyboardOffset;

                if (Math.abs(i.x - i.startX) >= 10 || Math.abs(i.y - i.startY) >= 10)
                    i.valid = false;

                absoluteMouseX = x;
                absoluteMouseY = y + absoluteHeight * keyboardOffset;
                return true;
            }

            @Override
            public boolean touchUp(int x, int y, int pointer, int button)
            {
                absoluteMouseX = -1;
                absoluteMouseY = -1;
                touchPoints.remove(pointer);
                pressedButtons.remove((Integer)button);
                validPressedButtons.remove((Integer)button);
                return true;
            }

            @Override
            public boolean keyDown(int keyCode)
            {
                //rawTextInput.add(keyCode);

                int key = translateKey(keyCode);
                pressedKeys.add(key);
                validPressedKeys.add(key);

                textPressedKeys.add(key);
                textValidPressedKeys.add(key);
                return true;
            }

            @Override
            public boolean keyTyped(char keyCode)
            {
                rawTextInput.add((int) keyCode);
                return true;
            }

            @Override
            public boolean keyUp(int keyCode)
            {
                if (Gdx.app.getType() == Application.ApplicationType.Android)
                    return true;

                rawTextInput.remove((Integer) keyCode);

                int key = translateKey(keyCode);
                pressedKeys.remove((Integer) key);
                validPressedKeys.remove((Integer) key);

                textPressedKeys.remove((Integer) key);
                textValidPressedKeys.remove((Integer) key);
                return true;
            }
        });
    }

    public void setupKeyMap()
    {
        key_translations.put(ESCAPE, InputCodes.KEY_ESCAPE);
        key_translations.put(F1, InputCodes.KEY_F1);
        key_translations.put(F2, InputCodes.KEY_F2);
        key_translations.put(F3, InputCodes.KEY_F3);
        key_translations.put(F4, InputCodes.KEY_F4);
        key_translations.put(F5, InputCodes.KEY_F5);
        key_translations.put(F6, InputCodes.KEY_F6);
        key_translations.put(F7, InputCodes.KEY_F7);
        key_translations.put(F8, InputCodes.KEY_F8);
        key_translations.put(F9, InputCodes.KEY_F9);
        key_translations.put(F10, InputCodes.KEY_F10);
        key_translations.put(F11, InputCodes.KEY_F11);
        key_translations.put(F12, InputCodes.KEY_F12);

        key_translations.put(GRAVE, InputCodes.KEY_GRAVE_ACCENT);
        key_translations.put(NUM_1, InputCodes.KEY_1);
        key_translations.put(NUM_2, InputCodes.KEY_2);
        key_translations.put(NUM_3, InputCodes.KEY_3);
        key_translations.put(NUM_4, InputCodes.KEY_4);
        key_translations.put(NUM_5, InputCodes.KEY_5);
        key_translations.put(NUM_6, InputCodes.KEY_6);
        key_translations.put(NUM_7, InputCodes.KEY_7);
        key_translations.put(NUM_8, InputCodes.KEY_8);
        key_translations.put(NUM_9, InputCodes.KEY_9);
        key_translations.put(NUM_0, InputCodes.KEY_0);
        key_translations.put(MINUS, InputCodes.KEY_MINUS);
        key_translations.put(EQUALS, InputCodes.KEY_EQUAL);
        key_translations.put(BACKSPACE, InputCodes.KEY_BACKSPACE);

        key_translations.put(TAB, InputCodes.KEY_TAB);
        key_translations.put(LEFT_BRACKET, InputCodes.KEY_LEFT_BRACKET);
        key_translations.put(RIGHT_BRACKET, InputCodes.KEY_RIGHT_BRACKET);
        key_translations.put(BACKSLASH, InputCodes.KEY_BACKSLASH);
        key_translations.put(SEMICOLON, InputCodes.KEY_SEMICOLON);
        key_translations.put(APOSTROPHE, InputCodes.KEY_APOSTROPHE);
        key_translations.put(ENTER, InputCodes.KEY_ENTER);

        key_translations.put(SHIFT_LEFT, InputCodes.KEY_LEFT_SHIFT);
        key_translations.put(COMMA, InputCodes.KEY_COMMA);
        key_translations.put(PERIOD, InputCodes.KEY_PERIOD);
        key_translations.put(SLASH, InputCodes.KEY_SLASH);
        key_translations.put(SHIFT_RIGHT, InputCodes.KEY_RIGHT_SHIFT);

        key_translations.put(CONTROL_LEFT, InputCodes.KEY_LEFT_CONTROL);
        key_translations.put(CONTROL_RIGHT, InputCodes.KEY_RIGHT_CONTROL);
        key_translations.put(ALT_LEFT, InputCodes.KEY_LEFT_ALT);
        key_translations.put(ALT_RIGHT, InputCodes.KEY_RIGHT_ALT);
        key_translations.put(SPACE, InputCodes.KEY_SPACE);
        key_translations.put(UP, InputCodes.KEY_UP);
        key_translations.put(DOWN, InputCodes.KEY_DOWN);
        key_translations.put(LEFT, InputCodes.KEY_LEFT);
        key_translations.put(RIGHT, InputCodes.KEY_RIGHT);

        key_translations.put(Q, InputCodes.KEY_Q);
        key_translations.put(W, InputCodes.KEY_W);
        key_translations.put(E, InputCodes.KEY_E);
        key_translations.put(R, InputCodes.KEY_R);
        key_translations.put(T, InputCodes.KEY_T);
        key_translations.put(Y, InputCodes.KEY_Y);
        key_translations.put(U, InputCodes.KEY_U);
        key_translations.put(I, InputCodes.KEY_I);
        key_translations.put(O, InputCodes.KEY_O);
        key_translations.put(P, InputCodes.KEY_P);
        key_translations.put(A, InputCodes.KEY_A);
        key_translations.put(S, InputCodes.KEY_S);
        key_translations.put(D, InputCodes.KEY_D);
        key_translations.put(F, InputCodes.KEY_F);
        key_translations.put(G, InputCodes.KEY_G);
        key_translations.put(H, InputCodes.KEY_H);
        key_translations.put(J, InputCodes.KEY_J);
        key_translations.put(K, InputCodes.KEY_K);
        key_translations.put(L, InputCodes.KEY_L);
        key_translations.put(Z, InputCodes.KEY_Z);
        key_translations.put(X, InputCodes.KEY_X);
        key_translations.put(C, InputCodes.KEY_C);
        key_translations.put(V, InputCodes.KEY_V);
        key_translations.put(B, InputCodes.KEY_B);
        key_translations.put(N, InputCodes.KEY_N);
        key_translations.put(M, InputCodes.KEY_M);
    }

    public void updatePerspective()
    {
        perspective.idt().setToProjection(
                (float)(-absoluteWidth / (absoluteDepth * 2.0)),
                (float)(absoluteWidth / (absoluteDepth * 2.0)),
                (float) (absoluteHeight / (absoluteDepth * 2.0)),
                (float)(-absoluteHeight / (absoluteDepth * 2.0)),
                1, (float) (absoluteDepth * 2));
        perspective.translate((float) -(absoluteWidth / 2), (float) (-absoluteHeight / 2), (float) -absoluteDepth);
        perspective.translate(0, (float) -(keyboardOffset * absoluteHeight), 0);

        if (!this.showKeyboard && this.keyboardOffset > 0)
            this.keyboardOffset = Math.max(0, this.keyboardOffset * Math.pow(0.98, frameFrequency) - 0.015 * frameFrequency);

        spriteBatch.setProjectionMatrix(this.perspective);
    }

    @Override
    public void setIcon(String icon)
    {

    }

    public void setDrawMode(int mode, boolean depthTest, boolean depthMask, int vertices)
    {
        this.setDrawMode(mode, depthTest, depthMask, false, vertices);
    }

    public void setDrawMode(int mode, boolean depthTest, boolean depthMask, boolean glow, int vertices)
    {
        this.setDrawMode(mode, depthTest, depthMask, glow, false, vertices);
    }

    public void setDrawMode(int mode, boolean depthTest, boolean depthMask, boolean glow, boolean light, int vertices)
    {
        if (this.currentVertices + vertices > maxVertices || this.currentDrawMode != mode || this.depthTest != depthTest || this.depthMask != depthMask || this.glow != glow || this.light != light)
        {
            this.currentVertices = 0;

            if (this.currentDrawMode != 7)
                this.renderer.end();

            this.light = light;
            this.glow = glow;
            this.currentDrawMode = mode;
            this.depthTest = depthTest;
            this.depthMask = depthMask;

            if (mode == -1)
                return;

            if (depthTest)
            {
                Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
                //Gdx.gl.glDepthMask(false);
                //Gdx.gl.glDepthFunc(depthFunc);
                Gdx.gl.glDepthFunc(GL20.GL_LESS);
            }
            else
            {
                Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
                //Gdx.gl.glDepthMask(true);
                Gdx.gl.glDepthFunc(GL20.GL_ALWAYS);
            }

            Gdx.gl.glEnable(GL20.GL_BLEND);

            if (light)
                Gdx.gl.glBlendFunc(GL20.GL_DST_COLOR, GL20.GL_ONE);
            else if (!glow)
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            else
                Gdx.gl.glBlendFunc(GL20.GL_SRC_COLOR, GL20.GL_ONE);

            Gdx.gl.glDepthMask(depthMask);

            if (mode != 7)
                this.renderer.begin(this.perspective, mode);
        }

        this.currentVertices += vertices;
    }

    public void render()
    {
        this.startTiming();

        this.updatePerspective();

        LibGDXSoundPlayer soundPlayer = (LibGDXSoundPlayer) this.soundPlayer;

        soundPlayer.musicPlaying = soundPlayer.currentMusic != null && soundPlayer.currentMusic.isPlaying();

        if (soundPlayer.prevMusic != null && soundPlayer.fadeEnd < System.currentTimeMillis())
        {
            if (soundPlayer.prevMusicStoppable && (soundPlayer.musicID == null || !soundPlayer.musicID.equals(soundPlayer.prevMusicID)))
                soundPlayer.prevMusic.stop();
            else if (soundPlayer.prevMusicStoppable)
                soundPlayer.prevMusic.setVolume(0);

            soundPlayer.prevMusic = null;

            if (soundPlayer.currentMusic != null)
                soundPlayer.currentMusic.setVolume(soundPlayer.currentVolume);
        }

        if (soundPlayer.prevMusic != null && soundPlayer.currentMusic != null)
        {
            double frac = (System.currentTimeMillis() - soundPlayer.fadeBegin) * 1.0 / (soundPlayer.fadeEnd - soundPlayer.fadeBegin);

            if (soundPlayer.prevMusicStoppable)
                soundPlayer.prevMusic.setVolume((float) (soundPlayer.prevVolume * (1 - frac)));

            soundPlayer.currentMusic.setVolume((float) (soundPlayer.currentVolume * frac));
        }

		/*Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);*/

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling?GL20.GL_COVERAGE_BUFFER_BIT_NV:0));

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        if (this.previousKeyboard != this.showKeyboard)
        {
            Gdx.input.setOnscreenKeyboardVisible(Game.game.window.showKeyboard);
            this.previousKeyboard = this.showKeyboard;
        }

        if (Gdx.app.getType() == Application.ApplicationType.Android)
            this.keyboardFraction = Tanks.keyboardHeightListener.getUsableWindowHeight();

        this.updater.update();

        this.drawer.draw();

        this.setDrawMode(-1, false, true, 0);

        if (Gdx.app.getType() == Application.ApplicationType.Android)
        {
            this.pressedKeys.clear();
            this.validPressedKeys.clear();

            this.textPressedKeys.clear();
            this.textValidPressedKeys.clear();
        }

        this.stopTiming();
    }

    @Override
    public void run()
    {

    }

    @Override
    public void setShowCursor(boolean show)
    {

    }

    @Override
    public void setCursorLocked(boolean locked)
    {

    }

    @Override
    public void setCursorPos(double x, double y)
    {

    }

    @Override
    public void setFullscreen(boolean enabled)
    {

    }

    @Override
    public void setOverrideLocations(ArrayList<String> loc, BaseFileManager fileManager)
    {

    }

    @Override
    public void setUpPerspective()
    {
        this.angled = false;

        this.yaw = 0;
        this.pitch = 0;
        this.roll = 0;
        this.xOffset = 0;
        this.yOffset = 0;
        this.zOffset = 0;

        this.updatePerspective();
    }

    @Override
    public void applyTransformations()
    {
        for (int i = this.transformations.size() - 1; i >= 0; i--)
        {
            this.transformations.get(i).apply();
        }

        spriteBatch.setProjectionMatrix(this.perspective);
    }

    @Override
    public void loadPerspective()
    {
        setUpPerspective();
        applyTransformations();
        //this.baseTransformation.apply();
    }

    @Override
    public void clearDepth()
    {
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
    }

    public void rotate(double x, double y, double rotation)
    {
        spriteBatch.getProjectionMatrix().translate((float) x, (float) y, 0);
        spriteBatch.getProjectionMatrix().rotateRad((float) 0, (float) 0, 1, (float) rotation);
        spriteBatch.getProjectionMatrix().translate((float) -x, (float) -y, 0);
    }

    @Override
    public String getClipboard()
    {
        String s = Gdx.app.getClipboard().getContents();

        if (s != null)
            return s;
        else
            return "";
    }

    @Override
    public void setClipboard(String s)
    {
        Gdx.app.getClipboard().setContents(s);
    }

    @Override
    public void setVsync(boolean enable)
    {
        Gdx.graphics.setVSync(enable);
    }

    @Override
    public ArrayList<Integer> getRawTextKeys()
    {
        return rawTextInput;
    }

    @Override
    public String getKeyText(int key)
    {
        return (char) key + "";
    }

    @Override
    public String getTextKeyText(int key)
    {
        return (char) key + "";
    }

    @Override
    public int translateKey(int key)
    {
        Integer k = key_translations.get(key);

        if (k == null)
            return key;

        return k;
    }

    @Override
    public int translateTextKey(int key)
    {
        return key;
    }

    @Override
    public void transform(double[] matrix)
    {
        for (int i = 0; i < matrix.length; i++)
        {
            this.matrix[i] = (float) matrix[i];
        }

        this.matrix2.set(this.matrix);
        perspective.mul(this.matrix2);
    }

    @Override
    public void calculateBillboard()
    {

    }

    @Override
    public double getEdgeBounds()
    {
        return Math.max(absoluteWidth - absoluteHeight * 18 / 9, 0) / 2;
    }

    @Override
    public void createImage(String image, InputStream in)
    {

    }

    public void setBatchMode(boolean enabled, boolean quads, boolean depth)
    {
        this.batchMode = enabled;
        this.setDrawMode(GL20.GL_TRIANGLES, depth, this.colorA >= 1, 1000);
        if (quads)
        {
            quadMode = true;
            quadNum = 0;
        }
        else
            quadMode = false;
    }

    public void setBatchMode(boolean enabled, boolean quads, boolean depth, boolean glow)
    {
        this.batchMode = enabled;
        this.setDrawMode(GL20.GL_TRIANGLES, depth, this.colorA >= 1 && !glow, glow,1000);
        if (quads)
        {
            quadMode = true;
            quadNum = 0;
        }
        else
            quadMode = false;
    }

    public void setBatchMode(boolean enabled, boolean quads, boolean depth, boolean glow, boolean depthMask)
    {
        this.batchMode = enabled;
        this.setDrawMode(GL20.GL_TRIANGLES, depth, depthMask, glow,1000);
        if (quads)
        {
            quadMode = true;
            quadNum = 0;
        }
        else
            quadMode = false;
    }

    @Override
    public void setTextureCoords(double u, double v)
    {
        renderer.texCoord((float) u, (float) v);
    }

    @Override
    public void setTexture(String image)
    {
        if (image != null && image.startsWith("/"))
            image = image.substring(1);

        Texture t = this.textures.get(image);

        if (t == null && image != null)
        {
            t = new Texture(Gdx.files.internal(image));
            this.textures.put(image, t);
        }

        if (t != null)
        {
            this.renderer = texRenderer;
            this.renderer.begin(this.perspective, GL20.GL_TRIANGLES);
            t.bind();
        }
    }

    @Override
    public void stopTexture()
    {
        this.renderer.end();
        Gdx.gl.glDisable(GL20.GL_TEXTURE_2D);
        this.renderer = notexRenderer;
    }

    @Override
    public void addVertex(double x, double y, double z)
    {
        if (quadMode)
        {
            if (quadNum == 0)
            {
                qx1 = (float) x;
                qy1 = (float) y;
                qz1 = (float) z;
                col1 = color;
            }
            else if (quadNum == 2)
            {
                qx3 = (float) x;
                qy3 = (float) y;
                qz3 = (float) z;
                col3 = color;
            }
            else if (quadNum == 3)
            {
                renderer.color(col1);
                renderer.vertex(qx1, qy1, qz1);
                renderer.color(col3);
                renderer.vertex(qx3, qy3, qz3);
            }
            quadNum = (quadNum + 1) % 4;
        }

        renderer.color(color);
        renderer.vertex((float) x, (float) y, (float) z);
    }

    @Override
    public void addVertex(double x, double y)
    {
        if (quadMode)
        {
            if (quadNum == 0)
            {
                qx1 = (float) x;
                qy1 = (float) y;
                qz1 = (float) 0;
                col1 = color;
            }
            else if (quadNum == 2)
            {
                qx3 = (float) x;
                qy3 = (float) y;
                qz3 = (float) 0;
                col3 = color;
            }
            else if (quadNum == 3)
            {
                renderer.color(col1);
                renderer.vertex(qx1, qy1, qz1);
                renderer.color(col3);
                renderer.vertex(qx3, qy3, qz3);
            }
            quadNum = (quadNum + 1) % 4;
        }

        renderer.color(color);
        renderer.vertex((float) x, (float) y, 0);
    }

    @Override
    public void openLink(URL url) throws Exception
    {
        if (this.platformHandler != null)
            this.platformHandler.openLink(url.toString());
        else
            Gdx.net.openURI(url.toString());
    }

    @Override
    public void setResolution(int x, int y)
    {

    }

    @Override
    public void setShadowQuality(double quality)
    {

    }

    @Override
    public double getShadowQuality()
    {
        return 0;
    }

    @Override
    public void setLighting(double light, double glowLight, double shadow, double glowShadow)
    {

    }

    @Override
    public void addMatrix()
    {

    }

    @Override
    public void removeMatrix()
    {

    }

    @Override
    public void setMatrixProjection()
    {

    }

    @Override
    public void setMatrixModelview()
    {

    }

    @Override
    public ModelPart createModelPart()
    {
        return new ImmediateModeModelPart(this);
    }

    @Override
    public ModelPart createModelPart(Model model, ArrayList<ModelPart.Shape> shapes, Model.Material material)
    {
        return new ImmediateModeModelPart(this, model, shapes, material);
    }

    @Override
    public PosedModel createPosedModel(Model m)
    {
        return null;
    }

    @Override
    public BaseShapeBatchRenderer createShapeBatchRenderer()
    {
        return null;
    }

    public void drawLinkedImage(double x, double y, double z, double sX, double sY, double u1, double v1, double u2, double v2, String image, boolean scaled, boolean depthtest)
    {
        if (image.startsWith("/"))
            image = image.substring(1);

        Texture texture = textures.get(image);

        if (texture == null)
        {
            texture = new Texture(Gdx.files.internal(image));
            textures.put(image, texture);
        }

        double width = sX * (u2 - u1);
        double height = sY * (v2 - v1);

        if (scaled)
        {
            width *= texture.getWidth();
            height *= texture.getHeight();
        }

        spriteBatch.setColor((float) this.colorR, (float) this.colorG, (float) this.colorB, (float) this.colorA);
        spriteBatch.draw(texture, (float) x, (float) y, (float) width, (float) height, (float) u1, (float) v1, (float) u2, (float) v2);
    }

    @Override
    public void setColor(double r, double g, double b, double a, double glow)
    {
        this.setColor(r, g, b, a);
    }

    @Override
    public void setColor(double r, double g, double b, double a)
    {
        this.colorR = (float) Math.min(1, Math.max(0, r / 255.0));
        this.colorG = (float) Math.min(1, Math.max(0, g / 255.0));
        this.colorB = (float) Math.min(1, Math.max(0, b / 255.0));
        this.colorA = (float) Math.min(1, Math.max(0, a / 255.0));
        this.color = Color.toFloatBits((float) colorR, (float) colorG, (float) colorB, (float) colorA);
    }

    @Override
    public void setColor(double r, double g, double b)
    {
        this.colorR = (float) Math.min(1, Math.max(0, r / 255.0));
        this.colorG = (float) Math.min(1, Math.max(0, g / 255.0));
        this.colorB = (float) Math.min(1, Math.max(0, b / 255.0));
        this.colorA = 1;
        this.color = Color.toFloatBits((float) colorR, (float) colorG, (float) colorB, 1);
    }
}
