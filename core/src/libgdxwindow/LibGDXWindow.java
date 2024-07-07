package libgdxwindow;

import basewindow.*;
import basewindow.transformation.*;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.BufferUtils;
import tanks.Game;
import theopalgames.tanks.Tanks;

import java.io.InputStream;
import java.net.URL;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import static com.badlogic.gdx.Input.Keys.*;
import static com.badlogic.gdx.graphics.GL20.*;

public class LibGDXWindow extends BaseWindow
{
    public Application.ApplicationType appType;

    public boolean previousKeyboard = false;

    public LibGDXImmediateModeRenderer renderer;

    public LibGDXImmediateModeRenderer notexRenderer;
    public LibGDXImmediateModeRenderer texRenderer;

    public static final HashMap<Integer, Integer> key_translations = new HashMap<>();

    public HashMap<String, Texture> textures = new HashMap<>();

    public Color color = new Color();
    public Color transparent = new Color(0, 0, 0, 0);
    public double colorGlow = 0;

    public Stack<Matrix4> projectionHistory = new Stack<Matrix4>();
    public Matrix4 projectionMatrix = new Matrix4();

    public Stack<Matrix4> modelviewHistory = new Stack<Matrix4>();
    public Matrix4 modelviewMatrix = new Matrix4();

    public boolean modelviewMode;

    public ArrayList<Character> rawTextInput = new ArrayList<>();

    protected int currentDrawMode = -1;
    protected boolean depthTest = false;
    protected boolean depthMask = true;
    protected boolean glow;
    protected boolean light;
    protected int currentVertices = 0;
    protected int maxVertices = 1000000;

    public boolean quadMode = false;
    public int quadNum = 0;

    public LibGDXShaderHandler shaderHandler;

    public Color col1;
    public float qx1;
    public float qy1;
    public float qz1;

    public Color col3;
    public float qx3;
    public float qy3;
    public float qz3;

    public float[] matrix = new float[16];
    public Matrix4 matrix2 = new Matrix4();

    double bbx1 = 1;
    double bby1 = 0;
    double bbz1 = 0;
    double bbx2 = 0;
    double bby2 = 1;
    double bbz2 = 0;
    double bbx3 = 0;
    double bby3 = 0;
    double bbz3 = 1;

    protected float[] transformedMouse = new float[2];

    public boolean shadowsEnabled = false;

    protected boolean batchMode = false;

    public LibGDXWindow(String name, int x, int y, int z, IUpdater u, IDrawer d, IWindowHandler w, boolean vsync, boolean showMouse)
    {
        super(name, x, y, z, u, d, w, vsync, showMouse);
    }

    protected void setUpShaders()
    {
        try
        {
            this.shaderDefault = new ShaderGroup(this, "default");
            this.shaderDefault.initialize();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public void initialize()
    {
        setUpShaders();
        setupKeyMap();

        this.shapeRenderer = new LibGDXShapeRenderer(this);
        this.shapeDrawer = new ImmediateModeModelPart.ImmediateModeShapeDrawer(this);
        this.shaderHandler = new LibGDXShaderHandler(this);

        this.loadPerspective();

        modelviewMatrix.idt();

        notexRenderer = new LibGDXImmediateModeRenderer(this, maxVertices, false, true, 0);
        texRenderer = new LibGDXImmediateModeRenderer(this, maxVertices, false, true, 1);

        renderer = notexRenderer;

        fontRenderer = new LibGDXFontRenderer(this, "font.png");

        this.soundsEnabled = true;
        this.soundPlayer = new LibGDXSoundPlayer(this);

        this.antialiasingSupported = true;

        Gdx.input.setInputProcessor(new InputAdapter()
        {
            @Override
            public boolean touchDown(int x, int y, int pointer, int button)
            {
                InputPoint ip = new InputPoint(x, y + absoluteHeight * keyboardOffset);
                touchPoints.put(pointer, ip);
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
                rawTextInput.add(keyCode);
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

    public float[] getTransformedMouse()
    {
        return getTransformedMouse(absoluteMouseX, absoluteMouseY);
    }

    public float[] getTransformedMouse(double x, double y)
    {
        x -= absoluteWidth / 2;
        y -= absoluteHeight / 2;
        float z = (float) -absoluteDepth;

        Matrix4 m = new Matrix4().idt();
        for (int i = 0; i < this.transformations.size(); i++)
        {
            Transformation t = this.transformations.get(i);
            if (t instanceof Rotation)
            {
                Rotation r = (Rotation) t;
                m.rotateRad(new Vector3(0, 1, 0), (float) r.yaw);
                m.rotateRad(new Vector3(1, 0, 0), (float) r.pitch);
                m.rotateRad(new Vector3(0, 0, 1), (float) r.roll);
            }
            else if (t instanceof RotationAboutPoint)
            {
                RotationAboutPoint r = (RotationAboutPoint) t;
                m.translate((float) (r.x * absoluteWidth), (float) (r.y * absoluteHeight), (float) (r.z * absoluteDepth));
                m.rotateRad(new Vector3(0, 1, 0), (float) r.yaw);
                m.rotateRad(new Vector3(1, 0, 0), (float) r.pitch);
                m.rotateRad(new Vector3(0, 0, 1), (float) r.roll);
                m.translate((float) (-r.x * absoluteWidth), (float) (-r.y * absoluteHeight), (float) (-r.z * absoluteDepth));
            }
            else if (t instanceof Scale)
            {
                Scale s = (Scale) t;
                m.scale((float) (1 / s.x), (float) (1 / s.y), (float) (1 / s.z));
            }
            else if (t instanceof Translation)
            {
                Translation r = (Translation) t;
                m.translate((float) (-r.x * absoluteWidth), (float) (-r.y * absoluteHeight), (float) (-r.z * absoluteDepth));
            }
        }

        Matrix4 v = new Matrix4(new float[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, (float) x, (float) y, z, 1});
        m.mul(v);
        transformedMouse[0] = (float) ((m.val[12] / (-m.val[14] / absoluteDepth) + absoluteWidth / 2));
        transformedMouse[1] = (float) ((m.val[13] / (-m.val[14] / absoluteDepth) + absoluteHeight / 2));

        return transformedMouse;
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
        double m = clipMultiplier;

        if (drawingShadow)
            projectionMatrix.setToOrtho(
                    (float) 0,
                    (float) absoluteWidth,
                    (float) absoluteHeight,
                    (float) 0,
                    (float) -absoluteDepth,
                    (float) absoluteDepth);
        else
            projectionMatrix.idt().setToProjection(
                    (float)(-absoluteWidth / (absoluteDepth * 2.0) * m),
                    (float)(absoluteWidth / (absoluteDepth * 2.0) * m),
                    (float) (absoluteHeight / (absoluteDepth * 2.0) * m),
                    (float)(-absoluteHeight / (absoluteDepth * 2.0) * m),
                    (float) m, (float) (absoluteDepth * m * clipDistMultiplier));

        if (!this.showKeyboard && this.keyboardOffset > 0)
            this.keyboardOffset = Math.max(0, this.keyboardOffset * Math.pow(0.98, frameFrequency) - 0.015 * frameFrequency);
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
        this.setDrawMode(mode, depthTest, depthMask, glow, light, vertices, this.colorGlow);
    }

    public void setDrawMode(int mode, boolean depthTest, boolean depthMask, boolean glow, boolean light, int vertices, double colorGlow)
    {
        if (this.currentVertices + vertices > maxVertices || this.currentDrawMode != mode || this.depthTest != depthTest || this.depthMask != depthMask || this.glow != glow || this.light != light || this.colorGlow != colorGlow)
        {
            this.currentVertices = 0;

            if (this.currentDrawMode >= 0)
                this.renderer.end();

            this.light = light;
            this.glow = glow;
            this.currentDrawMode = mode;
            this.depthTest = depthTest;
            this.depthMask = depthMask;

            this.colorGlow = colorGlow;

            if (drawingShadow && (!depthMask || !depthTest || glow || light))
                return;

            if (this.currentShader != null)
            {
                if (!drawingShadow)
                    this.currentShader.group.shaderBase.glow.set((float) colorGlow);

                if (depthTest)
                {
                    enableDepthtest();
                    Gdx.gl.glDepthFunc(GL_LEQUAL);
                }
                else
                {
                    disableDepthtest();
                    Gdx.gl.glDepthFunc(GL20.GL_ALWAYS);
                }
            }

            Gdx.gl.glEnable(GL20.GL_BLEND);

            if (light)
                Gdx.gl.glBlendFunc(GL20.GL_DST_COLOR, GL20.GL_ONE);
            else if (!glow)
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            else
                Gdx.gl.glBlendFunc(GL20.GL_SRC_COLOR, GL20.GL_ONE);

            Gdx.gl.glDepthMask(depthMask);

            if (mode >= 0)
                this.renderer.begin(this.projectionMatrix, mode);
        }

        this.currentVertices += vertices;
    }

    public void render()
    {
        this.startTiming();

        this.soundPlayer.update();

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

        if (shadowsEnabled)
            this.shaderHandler.renderShadowMap();

        this.shaderHandler.renderNormal();

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

        this.bbx1 = 1;
        this.bby1 = 0;
        this.bbz1 = 0;
        this.bbx2 = 0;
        this.bby2 = 1;
        this.bbz2 = 0;
        this.bbx3 = 0;
        this.bby3 = 0;
        this.bbz3 = 1;

        this.updatePerspective();
    }

    @Override
    public void applyTransformations()
    {
        for (int i = this.transformations.size() - 1; i >= 0; i--)
        {
            this.transformations.get(i).apply();
        }
    }

    public void applyShadowTransformations()
    {
        for (int i = this.transformations.size() - 1; i >= 0; i--)
        {
            Transformation t = this.transformations.get(i);

            if (t.applyAsShadow)
                t.apply();
            else
                t.applyToWindow();
        }
    }

    @Override
    public void loadPerspective()
    {
        this.setDrawMode(-1, false, false, 0);
        setUpPerspective();

        if (this.drawingShadow)
        {
            applyShadowTransformations();

            for (Transformation t: this.lightBaseTransformation)
                t.apply();
        }
        else
        {
            applyTransformations();

            for (Transformation t: this.baseTransformations)
                t.apply();

            projectionMatrix.translate(0, (float) -(keyboardOffset * absoluteHeight), 0);
        }
    }

    @Override
    public void clearDepth()
    {
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
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
    public ArrayList<Character> getRawTextKeys()
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
        setDrawMode(-1, this.depthTest, this.depthMask, this.glow, 0);

        for (int i = 0; i < matrix.length; i++)
        {
            this.matrix[i] = (float) matrix[i];
        }

        this.matrix2.set(this.matrix);

        if (this.modelviewMode)
            modelviewMatrix.mul(this.matrix2);
        else
            projectionMatrix.mul(this.matrix2);
    }

    @Override
    public void calculateBillboard()
    {
        angled = !(yaw == 0 && pitch == 0 && roll == 0);

        double a = Math.cos(-roll);
        double b = Math.sin(-roll);
        double c = Math.cos(-pitch);
        double d = Math.sin(-pitch);
        double e = Math.cos(-yaw);
        double f = Math.sin(-yaw);

        bbx1 = e * a - b * d * f;
        bby1 = -a * d * f - e * b;
        bbz1 = -c * f;
        bbx2 = b * c;
        bby2 = a * c;
        bbz2 = -d;
        bbx3 = a * f + e * b * d;
        bby3 = e * a * d - b * f;
        bbz3 = e * c;
    }

    @Override
    public double getEdgeBounds()
    {
        return Math.max(absoluteWidth - absoluteHeight * 18 / 9, 0) / 2;
    }

    public int createVBO()
    {
        return Gdx.gl.glGenBuffer();
    }

    public void freeVBO(int i)
    {
        Gdx.gl.glDeleteBuffer(i);
    }

    public void vertexBufferData(int id, Buffer buffer)
    {
        Gdx.gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, id);
        int size = buffer.remaining() << 2;

        Gdx.gl.glBufferData(GL20.GL_ARRAY_BUFFER, size, buffer, GL_STATIC_DRAW);
    }

    public void vertexBufferDataDynamic(int id, Buffer buffer)
    {
        Gdx.gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, id);
        int size = buffer.remaining() << 2;

        Gdx.gl.glBufferData(GL20.GL_ARRAY_BUFFER, size, buffer, GL_DYNAMIC_DRAW);
    }

    public void vertexBufferSubData(int id, int off, Buffer b)
    {
        Gdx.gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, id);
        int size = b.remaining() << 2;

        Gdx.gl.glBufferSubData(GL_ARRAY_BUFFER, off, size, b);
    }

    public void vertexBufferSubData(int id, int off, int size)
    {
        Buffer b = BufferUtils.newFloatBuffer(size);
        Gdx.gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, id);
        int s = b.remaining() << 2;
        Gdx.gl.glBufferSubData(GL_ARRAY_BUFFER, off, s, b);
    }


    @Override
    public void createImage(String image, InputStream in)
    {

    }

    @Override
    public void setUpscaleImages(boolean upscaleImages)
    {

    }

    public void setBatchMode(boolean enabled, boolean quads, boolean depth)
    {
        if (quadNum != 0)
            Game.exitToCrash(new RuntimeException("quad num invalid " + quadNum));

        this.batchMode = enabled;
        this.setDrawMode(enabled ? GL20.GL_TRIANGLES : -1, depth, this.colorA >= 1, 1000);
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
//        if (quadNum != 0)
//            Game.exitToCrash(new RuntimeException("quad num invalid " + quadNum));

        this.batchMode = enabled;
        this.setDrawMode(enabled ? GL20.GL_TRIANGLES : -1, depth, this.colorA >= 1 && !glow, glow,1000);
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
//        if (quadNum != 0)
//            Game.exitToCrash(new RuntimeException("quad num invalid " + quadNum));

        this.batchMode = enabled;
        this.setDrawMode(enabled ? GL20.GL_TRIANGLES : -1, depth, depthMask, glow,1000);
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
            this.enableTexture();
            this.renderer.begin(this.projectionMatrix, GL20.GL_TRIANGLES);
            t.bind();
        }
    }

    @Override
    public void stopTexture()
    {
        Gdx.gl.glDisable(GL_TEXTURE_2D);
        this.renderer = notexRenderer;
        this.disableTexture();
    }

    @Override
    public void addVertex(double x, double y, double z)
    {
//        if (!batchMode)
//            Game.exitToCrash(new RuntimeException("not batching!"));

        if (quadMode)
        {
            if (quadNum == 0)
            {
                qx1 = (float) x;
                qy1 = (float) y;
                qz1 = (float) z;
                //renderer.color(new Color(1, 0, 0, 1));
                col1 = color.cpy(); //new Color(0, 1, 1, 1).toFloatBits();
            }
            /*else if (quadNum == 1)
            {
                renderer.color(new Color(1, 1, 0, 1));
            }*/
            else if (quadNum == 2)
            {
                qx3 = (float) x;
                qy3 = (float) y;
                qz3 = (float) z;
                //renderer.color(new Color(0, 1, 0, 1));
                //col3 = new Color(0, 0, 1, 1).toFloatBits();//color;
                col3 = color.cpy();
            }
            else if (quadNum == 3)
            {
                renderer.color(col1);
                renderer.vertex(qx1, qy1, qz1);
                renderer.color(col3);
                renderer.vertex(qx3, qy3, qz3);
                //renderer.color(new Color(1, 0, 1, 1));
            }
            quadNum = (quadNum + 1) % 4;
        }
        /*else
        {
            quadNum = (quadNum + 1) % 3;
        }*/

        renderer.color(color);
        renderer.vertex((float) x, (float) y,  (float) z);
    }

    @Override
    public void addVertex(double x, double y)
    {
//        if (!batchMode)
//            Game.exitToCrash(new RuntimeException("not batching!"));

        if (quadMode)
        {
            if (quadNum == 0)
            {
                qx1 = (float) x;
                qy1 = (float) y;
                qz1 = (float) 0;
                //renderer.color(new Color(1, 0, 0, 1));
                col1 = color.cpy(); //new Color(0, 1, 1, 1).toFloatBits();
            }
            /*else if (quadNum == 1)
            {
                renderer.color(new Color(1, 1, 0, 1));
            }*/
            else if (quadNum == 2)
            {
                qx3 = (float) x;
                qy3 = (float) y;
                qz3 = (float) 0;
                //renderer.color(new Color(0, 1, 0, 1));
                //col3 = new Color(0, 0, 1, 1).toFloatBits();//color;
                col3 = color.cpy();
            }
            else if (quadNum == 3)
            {
                renderer.color(col1);
                renderer.vertex(qx1, qy1, qz1);
                renderer.color(col3);
                renderer.vertex(qx3, qy3, qz3);
                //renderer.color(new Color(1, 0, 1, 1));
            }
            quadNum = (quadNum + 1) % 4;
        }
        /*else
        {
            quadNum = (quadNum + 1) % 3;
        }*/

        renderer.color(color);
        renderer.vertex((float) x, (float) y,  (float) 0);
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
        if (quality <= 0)
        {
            this.shaderHandler.quality = 1;
            this.shadowsEnabled = false;
        }
        else
        {
            this.shaderHandler.quality = quality;
            this.shadowsEnabled = true;
        }
    }

    @Override
    public double getShadowQuality()
    {
        if (!this.shadowsEnabled)
            return 0;
        else
            return this.shaderHandler.quality;
    }

    @Override
    public void setLighting(double light, double glowLight, double shadow, double glowShadow)
    {
        setDrawMode(-1, this.depthTest, this.depthMask, this.glow, 0);

        this.currentShaderGroup.shaderBase.light.set((float) light);
        this.currentShaderGroup.shaderBase.glowLight.set((float) glowLight);
        this.currentShaderGroup.shaderBase.shade.set((float) shadow);
        this.currentShaderGroup.shaderBase.glowShade.set((float) glowShadow);
    }

    @Override
    public void setMaterialLights(float[] ambient, float[] diffuse, float[] specular, double shininess)
    {

    }

    @Override
    public void setMaterialLights(float[] ambient, float[] diffuse, float[] specular, double shininess, double minBound, double maxBound, boolean enableNegative)
    {

    }

    @Override
    public void disableMaterialLights()
    {

    }

    @Override
    public void setCelShadingSections(float sections)
    {

    }

    @Override
    public void createLights(ArrayList<double[]> lights, double scale)
    {

    }

    @Override
    public void addMatrix()
    {
        setDrawMode(-1, this.depthTest, this.depthMask, this.glow, 0);

        if (this.modelviewMode)
            modelviewHistory.add(modelviewMatrix.cpy());
        else
            projectionHistory.add(projectionMatrix.cpy());
    }

    @Override
    public void removeMatrix()
    {
        setDrawMode(-1, this.depthTest, this.depthMask, this.glow, 0);

        if (this.modelviewMode)
            modelviewMatrix.set(modelviewHistory.pop());
        else
            projectionMatrix.set(projectionHistory.pop());
    }

    @Override
    public void setMatrixProjection()
    {
        setDrawMode(-1, this.depthTest, this.depthMask, this.glow, 0);

        this.modelviewMode = false;
    }

    @Override
    public void setMatrixModelview()
    {
        setDrawMode(-1, this.depthTest, this.depthMask, this.glow, 0);

        this.modelviewMode = true;
    }

    @Override
    public ModelPart createModelPart()
    {
        return new ImmediateModeModelPart(this);
    }

    @Override
    public ModelPart createModelPart(Model model, ArrayList<ModelPart.Shape> shapes, Model.Material material)
    {
        return new VBOModelPart(this, model, shapes, material);
    }

    @Override
    public PosedModel createPosedModel(Model m)
    {
        return null;
    }

    @Override
    public BaseShapeBatchRenderer createStaticBatchRenderer(ShaderGroup shader, boolean color, String texture, boolean normal, int vertices)
    {
        return new LibGDXStaticBatchRenderer(this, shader);
    }

    @Override
    public BaseShapeBatchRenderer createShapeBatchRenderer()
    {
        return new LibGDXShapeBatchRenderer(this);
    }

    @Override
    public BaseShapeBatchRenderer createShapeBatchRenderer(ShaderGroup shader)
    {
        return new LibGDXShapeBatchRenderer(this, shader);
    }

    @Override
    public BaseShaderUtil getShaderUtil(ShaderProgram p)
    {
        return new LibGDXShaderUtil(this, p);
    }

    public void enableTexture()
    {
        if (!drawingShadow)
            this.currentShaderGroup.shaderBase.texture.set(true);

        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
    }

    public void disableTexture()
    {
        if (!drawingShadow && this.currentShaderGroup != null)
            this.currentShaderGroup.shaderBase.texture.set(false);

        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE1);
        this.shaderHandler.frameBuffer.getColorBufferTexture().bind(1);
    }

    public void enableDepthtest()
    {
        Gdx.gl.glEnable(GL_DEPTH_TEST);
        Gdx.gl.glDepthFunc(GL_LEQUAL);

        if (!drawingShadow)
            this.currentShaderGroup.shaderBase.depthtest.set(true);
    }

    public void disableDepthtest()
    {
        Gdx.gl.glDisable(GL_DEPTH_TEST);
        Gdx.gl.glDepthFunc(GL20.GL_ALWAYS);

        if (!drawingShadow)
            this.currentShaderGroup.shaderBase.depthtest.set(false);
    }

    public void enableDepthmask()
    {
        Gdx.gl.glDepthMask(true);
    }

    public void disableDepthmask()
    {
        Gdx.gl.glDepthMask(false);
    }

    public void setGlowBlendFunc()
    {
        Gdx.gl.glBlendFunc(GL_SRC_COLOR, GL_ONE);
    }

    public void setLightBlendFunc()
    {
        Gdx.gl.glBlendFunc(GL_DST_COLOR, GL_ONE);
    }

    public void setTransparentBlendFunc()
    {
        Gdx.gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public void beginLinkedImages(String image, boolean scaled, boolean depthtest)
    {
        this.setDrawMode(-1, depthtest, false, 0);

        if (drawingShadow)
            return;

        if (image.startsWith("/"))
            image = image.substring(1);

        Texture texture = textures.get(image);

        if (texture == null)
        {
            texture = new Texture(Gdx.files.internal(image));
            textures.put(image, texture);
        }

        this.setMatrixProjection();

        //if (depthtest)
        //    this.enableDepthtest();

        //this.setUpPerspective();

        this.setMatrixModelview();

        this.enableTexture();

        //Gdx.gl.glEnable(GL_BLEND);
        //this.setTransparentBlendFunc();
        //Gdx.gl.glDepthMask(false);

        texture.bind();

        if (scaled)
        {
            throw new RuntimeException("not supported");
        }

        this.texRenderer.begin(this.projectionMatrix, Gdx.gl.GL_TRIANGLES);
    }

    public void drawLinkedImage(double x, double y, double z, double sX, double sY, double u1, double v1, double u2, double v2)
    {
        double width = sX * (u2 - u1);
        double height = sY * (v2 - v1);

        this.texRenderer.texCoord((float) u1, (float) v1);
        this.texRenderer.color(this.color);
        this.texRenderer.vertex((float) x, (float) y, (float) z);
        this.texRenderer.texCoord((float) u1, (float) v2);
        this.texRenderer.color(this.color);
        this.texRenderer.vertex((float) x, (float) (y + height), (float) z);
        this.texRenderer.texCoord((float) u2, (float) v2);
        this.texRenderer.color(this.color);
        this.texRenderer.vertex((float) (x + width), (float) (y + height), (float) z);

        this.texRenderer.texCoord((float) u1, (float) v1);
        this.texRenderer.color(this.color);
        this.texRenderer.vertex((float) x, (float) y, (float) z);
        this.texRenderer.texCoord((float) u2, (float) v1);
        this.texRenderer.color(this.color);
        this.texRenderer.vertex((float) (x + width), (float) y, (float) z);
        this.texRenderer.texCoord((float) u2, (float) v2);
        this.texRenderer.color(this.color);
        this.texRenderer.vertex((float) (x + width), (float) (y + height), (float) z);
    }

    public void drawLinkedImage(double x, double y, double z, double sX, double sY, double u1, double v1, double u2, double v2, double rotation)
    {
        double width = sX * (u2 - u1);
        double height = sY * (v2 - v1);

        this.texRenderer.texCoord((float) u1, (float) v1);
        this.texRenderer.color(this.color);
        this.texRenderer.vertex((float) rotateX(-width / 2, -height / 2, x, rotation), (float) rotateY(-width / 2, -height / 2, y, rotation), (float) z);
        this.texRenderer.texCoord((float) u1, (float) v2);
        this.texRenderer.color(this.color);
        this.texRenderer.vertex((float) rotateX(-width / 2, height / 2, x, rotation), (float) rotateY(-width / 2, height / 2, y, rotation), (float) z);
        this.texRenderer.texCoord((float) u2, (float) v2);
        this.texRenderer.color(this.color);
        this.texRenderer.vertex((float) rotateX(width / 2, height / 2, x, rotation), (float) rotateY(width / 2, height / 2, y, rotation), (float) z);

        this.texRenderer.texCoord((float) u1, (float) v1);
        this.texRenderer.color(this.color);
        this.texRenderer.vertex((float) rotateX(-width / 2, -height / 2, x, rotation), (float) rotateY(-width / 2, -height / 2, y, rotation), (float) z);
        this.texRenderer.texCoord((float) u2, (float) v1);
        this.texRenderer.color(this.color);
        this.texRenderer.vertex((float) rotateX(width / 2, -height / 2, x, rotation), (float) rotateY(width / 2, -height / 2, y, rotation), (float) z);
        this.texRenderer.texCoord((float) u2, (float) v2);
        this.texRenderer.color(this.color);
        this.texRenderer.vertex((float) rotateX(width / 2, height / 2, x, rotation), (float) rotateY(width / 2, height / 2, y, rotation), (float) z);
    }

    public double rotateY(double px, double py, double posY, double rotation)
    {
        return (px * Math.cos(rotation) + py * Math.sin(rotation)) + posY;
    }

    public double rotateX(double px, double py, double posX, double rotation)
    {
        return (py * Math.cos(rotation) - px * Math.sin(rotation)) + posX;
    }

    void endLinkedImages()
    {
        this.texRenderer.end();

        this.setMatrixProjection();
        this.disableTexture();

        //Gdx.gl.glDepthMask(true);
        //this.disableDepthtest();
    }

    @Override
    public void setColor(double r, double g, double b, double a, double glow)
    {
        this.colorR = (float) Math.min(1, Math.max(0, r / 255.0));
        this.colorG = (float) Math.min(1, Math.max(0, g / 255.0));
        this.colorB = (float) Math.min(1, Math.max(0, b / 255.0));
        this.colorA = (float) Math.min(1, Math.max(0, a / 255.0));
        this.color.r = (float) this.colorR;
        this.color.g = (float) this.colorG;
        this.color.b = (float) this.colorB;
        this.color.a = (float) this.colorA;

        this.setDrawMode(this.currentDrawMode, this.depthTest, this.depthMask, this.glow, this.light, 0, glow);
    }

    @Override
    public void setColor(double r, double g, double b, double a)
    {
        this.setColor(r, g, b, a, 0);
    }

    @Override
    public void setColor(double r, double g, double b)
    {
        this.setColor(r, g, b, 255, 0);
    }

    public String readFileAsString(String h)
    {
        ArrayList<String> s = Game.game.fileManager.getInternalFileContents(h);
        StringBuilder b = new StringBuilder();

        for (String ss: s)
        {
            b.append(ss).append("\n");
        }

        return b.toString();
    }
}
