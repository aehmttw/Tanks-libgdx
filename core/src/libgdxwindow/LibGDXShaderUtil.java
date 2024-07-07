package libgdxwindow;

import basewindow.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import static com.badlogic.gdx.graphics.GL20.*;

import java.lang.reflect.Field;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

public class LibGDXShaderUtil extends BaseShaderUtil
{
    public LibGDXWindow window;
    public ShaderProgram program;
    public int programID;

    public ShaderProgram.Attribute position = getAttribute();
    public ShaderProgram.Attribute color = getAttribute();
    public ShaderProgram.Attribute textureCoord = getAttribute();
    public ShaderProgram.Attribute normal = getAttribute();

    public ShaderProgram.UniformMatrix4 modelViewMatrix;
    public ShaderProgram.UniformMatrix4 projectionMatrix;
    public ShaderProgram.UniformMatrix4 modelViewProjectionMatrix;

    public ArrayList<Integer> enabledAttributes = new ArrayList<>();
    public ArrayList<ShaderProgram.Attribute> defaultAttributes = new ArrayList<>();

    public LibGDXShaderUtil(LibGDXWindow w, ShaderProgram s)
    {
        this.window = w;
        this.program = s;
    }

    @Override
    public void setUp(String vert, String[] vertHeaders, String frag, String[] fragHeaders) throws Exception
    {
        this.createProgram(vert, vertHeaders, null, null, frag, fragHeaders);
    }

    @Override
    public void setUp(String vert, String[] vertHeaders, String geom, String[] geomHeaders, String frag, String[] fragHeaders) throws Exception
    {
        this.createProgram(vert, vertHeaders, geom, geomHeaders, frag, fragHeaders);
    }

    protected void bindDefaultAttribute(ShaderProgram.Attribute a, String name)
    {
        a.name = name;
        a.bind();
        a.name = name;
        this.defaultAttributes.add(a);
    }

    public void createProgram(String vert, String[] vertHeaders, String geom, String[] geomHeaders, String frag, String[] fragHeaders) throws Exception
    {
        this.programID = Gdx.gl.glCreateProgram();

        //new Exception("Shader id = " + this.programID).printStackTrace();

        int vshader = this.createShader(vert, vertHeaders, GL_VERTEX_SHADER);
        int fshader = this.createShader(frag, fragHeaders, GL_FRAGMENT_SHADER);

        Gdx.gl.glAttachShader(this.programID, vshader);
        Gdx.gl.glAttachShader(this.programID, fshader);

        if (geom != null)
        {
            throw new RuntimeException("LibGDX does not (yet?) support geometry shaders");
        }

        Gdx.gl.glLinkProgram(this.programID);
        this.bindDefaultAttribute(position, "glVertex");
        this.bindDefaultAttribute(color, "glColor");
        this.bindDefaultAttribute(textureCoord, "glMultiTexCoord");
        this.bindDefaultAttribute(normal, "glNormal");

        this.program.bindAttributes();
        this.program.initializeAttributeParameters();

        String programLog = Gdx.gl.glGetProgramInfoLog(this.programID);

        if (programLog.trim().length() > 0)
            System.err.println(programLog);

        this.setUpUniforms();
    }

    protected int createShader(String filename, int shaderType) throws Exception
    {
        return createShader(filename, null, shaderType);
    }

    protected int createShader(String filename, String[] headers, int shaderType) throws Exception
    {
        int shader = 0;
        try
        {
            shader = Gdx.gl.glCreateShader(shaderType);

            if (shader == 0)
                return 0;

            StringBuilder header = new StringBuilder();
            if (headers != null)
            {
                for (String h: headers)
                {
                    header.append(this.window.readFileAsString(h));
                }
            }

            String source = header + this.window.readFileAsString(filename);
            source = source.replace("#version 120\n", "");
            source = source.replace("#extension GL_EXT_gpu_shader4 : enable\n", "");
            //source = source.replace("gl_Vertex.w", "1.0");

            for (int i = 31; i >= 0; i--)
            {
                source = source.replace(">> " + i, "/ " + (int) Math.pow(2, i));
                source = source.replace("<< " + i, "* " + (int) Math.pow(2, i));
            }

            source = source.replace("gl_TexCoord[0]", "gl_TexCoord");

            String attribs = "";
            if (shaderType == GL_VERTEX_SHADER)
            {
                attribs = "attribute vec4 glVertex;\n" +
                        "attribute vec4 glColor;\n" +
                        "attribute vec2 glMultiTexCoord;\n" +
                        "attribute vec3 glNormal;\n";
            }

            source = "#define gl_Vertex glVertex\n" +
                    "#define gl_Color glColor\n" +
                    "#define gl_MultiTexCoord0 glMultiTexCoord\n" +
                    "#define gl_Normal glNormal\n" +
                    "#define gl_ModelViewProjectionMatrix modelViewProjectionMatrix\n" +
                    "#define gl_ModelViewMatrix modelViewMatrix\n" +
                    "#define gl_ProjectionMatrix projectionMatrix\n" +
                    "#define gl_TexCoord glTexCoord\n" +
                    "#define GLES\n" +
                    attribs +
                    "varying vec2 glTexCoord;\n" +
                    "uniform mat4 modelViewProjectionMatrix;\n" +
                    "uniform mat4 modelViewMatrix;\n" +
                    "uniform mat4 projectionMatrix;\n" + source;

            source = source.replace("vec2 ", "highp vec2 ");
            source = source.replace("vec3 ", "highp vec3 ");
            source = source.replace("vec4 ", "highp vec4 ");
            source = source.replace("mat2 ", "highp mat2 ");
            source = source.replace("mat3 ", "highp mat3 ");
            source = source.replace("mat4 ", "highp mat4 ");
            source = source.replace("float ", "highp float ");

            Gdx.gl.glShaderSource(shader, source);
            Gdx.gl.glCompileShader(shader);

            String log = getLogInfo(shader);
            if (log.length() > 0)
            {
                String[] lines = source.split("\n");
                System.err.println("Shader source:");
                for (int i = 0; i < lines.length; i++)
                {
                    System.err.println(String.format("%3d ", (i + 1)) + lines[i]);
                }
                throw new RuntimeException("Error creating shader " + filename + ": " + getLogInfo(shader));
            }

            return shader;
        }
        catch (Exception exc)
        {
            Gdx.gl.glDeleteShader(shader);
            throw exc;
        }
    }

    @Override
    public void setUpUniforms() throws InstantiationException, IllegalAccessException
    {
        LibGDXShaderHandler.setProgram(programID);

        Field[] programFields = program.getClass().getFields();
        Field[] groupFields = new Field[0];
        if (program.group != null)
            groupFields = program.group.getClass().getFields();

        Field[] fields = new Field[programFields.length + groupFields.length];
        System.arraycopy(programFields, 0, fields, 0, programFields.length);
        System.arraycopy(groupFields, 0, fields, programFields.length, groupFields.length);

        Class[] classes = new Class[]{LibGDXUniform1b.class,
                LibGDXUniform1i.class, LibGDXUniform2i.class, LibGDXUniform3i.class, LibGDXUniform4i.class,
                LibGDXUniform1f.class, LibGDXUniform2f.class, LibGDXUniform3f.class, LibGDXUniform4f.class,
                LibGDXUniformMatrix2.class, LibGDXUniformMatrix3.class, LibGDXUniformMatrix4.class,
                LibGDXGroupUniform1b.class,
                LibGDXGroupUniform1i.class, LibGDXGroupUniform2i.class, LibGDXGroupUniform3i.class, LibGDXGroupUniform4i.class,
                LibGDXGroupUniform1f.class, LibGDXGroupUniform2f.class, LibGDXGroupUniform3f.class, LibGDXGroupUniform4f.class,
                LibGDXGroupUniformMatrix2.class, LibGDXGroupUniformMatrix3.class, LibGDXGroupUniformMatrix4.class};

        try
        {
            LibGDXUniformMatrix4 pr = new LibGDXUniformMatrix4();
            pr.name = "projectionMatrix";
            pr.programID = this.programID;
            this.projectionMatrix = pr;
            pr.bindNoFail();

            LibGDXUniformMatrix4 mv = new LibGDXUniformMatrix4();
            mv.name = "modelViewMatrix";
            mv.programID = this.programID;
            this.modelViewMatrix = mv;
            mv.bindNoFail();

            LibGDXUniformMatrix4 mvpr = new LibGDXUniformMatrix4();
            mvpr.name = "modelViewProjectionMatrix";
            mvpr.programID = this.programID;
            this.modelViewProjectionMatrix = mvpr;
            mvpr.bindNoFail();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        for (Field f: fields)
        {
            if (ShaderProgram.IUniform.class.isAssignableFrom(f.getType()))
            {
                for (Class c: classes)
                {
                    if (f.getType().isAssignableFrom(c))
                    {
                        LibGDXUniform u = (LibGDXUniform) c.newInstance();
                        u.name = f.getName();
                        u.programID = this.programID;

                        try
                        {
                            u.bind();
                        }
                        catch (Exception e)
                        {
                            // If you get this, it means one of your shader uniforms doesn't have a corresponding
                            // GLSL uniform. This could happen if the uniform is unused and is thus optimized out by the GLSL compiler.
                            throw new RuntimeException("Failed to bind uniform in " + program + ": " + u.name, e);
                        }

                        f.set(program, u);
                    }
                }
            }
            else if (ShaderGroup.IGroupUniform.class.isAssignableFrom(f.getType()))
            {
                for (Class c: classes)
                {
                    if (f.getType().isAssignableFrom(c))
                    {
                        LibGDXGroupUniform u = (LibGDXGroupUniform) f.get(program.group);
                        if (u == null)
                        {
                            u = (LibGDXGroupUniform) c.newInstance();
                            u.setWindow(window);
                            f.set(program.group, u);
                        }

                        try
                        {
                            u.instantiate(f.getName(), this.programID, this.program instanceof ShaderShadowMap);

                            if (!((f.getAnnotation(OnlyBaseUniform.class) != null && this.program instanceof ShaderShadowMap) ||
                                (f.getAnnotation(OnlyShadowMapUniform.class) != null && this.program instanceof ShaderBase)))
                                u.bind(this.program instanceof ShaderShadowMap);
                        }
                        catch (Exception e)
                        {
                            // If you get this, it means one of your shader uniforms in a ShaderGroup class doesn't have a corresponding
                            // GLSL uniform. This could happen if you only use the uniform in the base or shadow map shader of the group
                            // (in which case you can tag them with @OnlyBaseUniform or @OnlyShadowMapUniform)
                            // or if the uniform is unused and is thus optimized out by the GLSL compiler.
                            throw new RuntimeException("Failed to bind uniform in " + program, e);
                        }
                    }
                }
            }
        }

        this.setPerspective();
        this.program.initializeUniforms();
        LibGDXShaderHandler.setProgram(0);
    }

    @Override
    public void set()
    {
        this.window.setDrawMode(-1, false, false, 0);
        LibGDXShaderHandler.setProgram(this.programID);
    }

    protected void setPerspective()
    {
        this.modelViewProjectionMatrix.set((window.projectionMatrix.cpy().mul(window.modelviewMatrix)).getValues(), false);
        this.modelViewMatrix.set(window.modelviewMatrix.getValues(), false);
        this.projectionMatrix.set(window.projectionMatrix.getValues(), false);
    }

    protected static String getLogInfo(int obj)
    {
        return Gdx.gl.glGetShaderInfoLog(obj);
    }

    @Override
    public ShaderProgram.Attribute getAttribute()
    {
        return new LibGDXAttribute();
    }

    public class LibGDXAttribute extends ShaderProgram.Attribute
    {
        public void bind()
        {
            this.id = Gdx.gl.glGetAttribLocation(programID, name);
            Gdx.gl.glBindAttribLocation(programID, id, name);

            if (id < 0)
            {
                System.out.println("did not bind attribute! " + program.group.name + " " + name);
            }
        }
    }

    public void setVertexBuffer(int id)
    {
        if (id <= 0)
            return;

        setCustomBuffer(position, id, 3);
    }

    public void setColorBuffer(int id)
    {
        if (id <= 0)
            return;

        setCustomBuffer(color, id, 4);
    }

    public void setTexCoordBuffer(int id)
    {
        if (id <= 0)
            return;

        setCustomBuffer(textureCoord, id, 2);
    }

    public void setNormalBuffer(int id)
    {
        if (id <= 0)
            return;

        setCustomBuffer(normal, id, 3);
    }

    public void setCustomBuffer(ShaderProgram.Attribute attribute, int bufferID, int size)
    {
        this.setCustomBuffer(attribute.id, bufferID, size);
    }

    public void setCustomBuffer(int attribute, int bufferID, int size)
    {
        Gdx.gl.glBindBuffer(GL_ARRAY_BUFFER, bufferID);
        Gdx.gl.glEnableVertexAttribArray(attribute);
        Gdx.gl.glVertexAttribPointer(attribute, size, GL_FLOAT, false, 0, 0);
        Gdx.gl.glBindBuffer(Gdx.gl.GL_ARRAY_BUFFER, 0);
        this.enabledAttributes.add(attribute);
    }

    public void drawVBO(int numberIndices)
    {
        if (!(LibGDXShaderHandler.currentProgram == this.programID && LibGDXShaderHandler.currentProgram != 0))
        {
            new RuntimeException("Mismatch between current shader in use and shader of model drawn: got " + LibGDXShaderHandler.currentProgram + " but expected " + this.programID).printStackTrace();
        }
        this.setPerspective();
        Gdx.gl20.glDrawArrays(GL20.GL_TRIANGLES, 0, numberIndices);

        for (int i: this.enabledAttributes)
        {
            Gdx.gl.glDisableVertexAttribArray(i);
        }

        this.enabledAttributes.clear();
    }

    public static abstract class LibGDXUniform implements ShaderProgram.IUniform
    {
        protected int flag;
        protected String name;
        protected int programID;

        public void bind()
        {
            this.bindNoFail();
            if (this.flag < 0)
                throw new RuntimeException("Failed to bind uniform: " + name);
        }

        public void bindNoFail()
        {
            this.flag = Gdx.gl.glGetUniformLocation(programID, name);
        }
    }

    public static class LibGDXUniform1b extends LibGDXUniform implements ShaderProgram.Uniform1b
    {
        private boolean value = false;

        public void set(Boolean b)
        {
            value = b;
            Gdx.gl.glUniform1i(flag, b ? 1 : 0);
        }

        public Boolean get()
        {
            return value;
        }
    }

    public static class LibGDXUniform1i extends LibGDXUniform implements ShaderProgram.Uniform1i
    {
        private int value = 0;

        public void set(Integer i)
        {
            value = i;
            Gdx.gl.glUniform1i(flag, i);
        }

        public Integer get()
        {
            return value;
        }
    }

    public static class LibGDXUniform2i extends LibGDXUniform implements ShaderProgram.Uniform2i
    {
        private final int[] value = new int[2];

        public void set(int i1, int i2)
        {
            value[0] = i1;
            value[1] = i2;
            Gdx.gl.glUniform2i(flag, i1, i2);
        }

        public int[] get()
        {
            return value;
        }
    }

    public static class LibGDXUniform3i extends LibGDXUniform implements ShaderProgram.Uniform3i
    {
        private final int[] value = new int[3];

        public void set(int i1, int i2, int i3)
        {
            value[0] = i1;
            value[1] = i2;
            value[2] = i3;
            Gdx.gl.glUniform3i(flag, i1, i2, i3);
        }

        public int[] get()
        {
            return value;
        }
    }

    public static class LibGDXUniform4i extends LibGDXUniform implements ShaderProgram.Uniform4i
    {
        private final int[] value = new int[4];

        public void set(int i1, int i2, int i3, int i4)
        {
            value[0] = i1;
            value[1] = i2;
            value[2] = i3;
            value[3] = i4;
            Gdx.gl.glUniform4i(flag, i1, i2, i3, i4);
        }

        public int[] get()
        {
            return value;
        }
    }

    public static class LibGDXUniform1f extends LibGDXUniform implements ShaderProgram.Uniform1f
    {
        private float value;

        public void set(Float i)
        {
            this.value = i;
            Gdx.gl.glUniform1f(flag, i);
        }

        public Float get()
        {
            return value;
        }
    }

    public static class LibGDXUniform2f extends LibGDXUniform implements ShaderProgram.Uniform2f
    {
        private final float[] value = new float[2];

        public void set(float i1, float i2)
        {
            value[0] = i1;
            value[1] = i2;
            Gdx.gl.glUniform2f(flag, i1, i2);
        }

        public float[] get()
        {
            return value;
        }
    }

    public static class LibGDXUniform3f extends LibGDXUniform implements ShaderProgram.Uniform3f
    {
        private final float[] value = new float[3];

        public void set(float i1, float i2, float i3)
        {
            value[0] = i1;
            value[1] = i2;
            value[2] = i3;
            Gdx.gl.glUniform3f(flag, i1, i2, i3);
        }

        public float[] get()
        {
            return value;
        }
    }

    public static class LibGDXUniform4f extends LibGDXUniform implements ShaderProgram.Uniform4f
    {
        private final float[] value = new float[4];

        public void set(float i1, float i2, float i3, float i4)
        {
            value[0] = i1;
            value[1] = i2;
            value[2] = i3;
            value[3] = i4;
            Gdx.gl.glUniform4f(flag, i1, i2, i3, i4);
        }

        public float[] get()
        {
            return value;
        }
    }

    public static class LibGDXUniformMatrix2 extends LibGDXUniform implements ShaderProgram.UniformMatrix2
    {
        private float[] matrix = new float[0];
        private boolean transpose;

        public void set(float[] floats, boolean transpose)
        {
            this.matrix = floats;
            this.transpose = transpose;
            Gdx.gl.glUniformMatrix2fv(flag, 1, transpose, floats, 0);
        }

        public float[] getMatrix()
        {
            return matrix;
        }

        public boolean getTranspose()
        {
            return this.transpose;
        }
    }

    public static class LibGDXUniformMatrix3 extends LibGDXUniform implements ShaderProgram.UniformMatrix3
    {
        private float[] matrix = new float[0];
        private boolean transpose;

        public void set(float[] floats, boolean transpose)
        {
            this.matrix = floats;
            this.transpose = transpose;
            Gdx.gl.glUniformMatrix3fv(flag, 1, transpose, floats, 0);
        }

        public float[] getMatrix()
        {
            return matrix;
        }

        public boolean getTranspose()
        {
            return this.transpose;
        }
    }

    public static class LibGDXUniformMatrix4 extends LibGDXUniform implements ShaderProgram.UniformMatrix4
    {
        private float[] matrix = new float[0];
        private boolean transpose;

        public void set(float[] floats, boolean transpose)
        {
            this.matrix = floats;
            this.transpose = transpose;
            Gdx.gl.glUniformMatrix4fv(flag, 1, transpose, floats, 0);
        }

        public float[] getMatrix()
        {
            return matrix;
        }

        public boolean getTranspose()
        {
            return this.transpose;
        }
    }

    public interface LibGDXGroupUniform extends ShaderGroup.IGroupUniform
    {
        void instantiate(String name, int programID, boolean shadow);
    }

    public static class LibGDXGroupUniform1b extends ShaderGroup.Uniform1b implements LibGDXGroupUniform
    {
        @Override
        public void instantiate(String name, int programID, boolean shadow)
        {
            if (!shadow)
            {
                this.baseUniform = new LibGDXUniform1b();
                ((LibGDXUniform) this.baseUniform).name = name;
                ((LibGDXUniform) this.baseUniform).programID = programID;
            }
            else
            {
                this.shadowMapUniform = new LibGDXUniform1b();
                ((LibGDXUniform) this.shadowMapUniform).name = name;
                ((LibGDXUniform) this.shadowMapUniform).programID = programID;
            }
        }
    }

    public static class LibGDXGroupUniform1i extends ShaderGroup.Uniform1i implements LibGDXGroupUniform
    {
        @Override
        public void instantiate(String name, int programID, boolean shadow)
        {
            if (!shadow)
            {
                this.baseUniform = new LibGDXUniform1i();
                ((LibGDXUniform) this.baseUniform).name = name;
                ((LibGDXUniform) this.baseUniform).programID = programID;
            }
            else
            {
                this.shadowMapUniform = new LibGDXUniform1i();
                ((LibGDXUniform) this.shadowMapUniform).name = name;
                ((LibGDXUniform) this.shadowMapUniform).programID = programID;
            }
        }
    }

    public static class LibGDXGroupUniform2i extends ShaderGroup.Uniform2i implements LibGDXGroupUniform
    {
        @Override
        public void instantiate(String name, int programID, boolean shadow)
        {
            if (!shadow)
            {
                this.baseUniform = new LibGDXUniform2i();
                ((LibGDXUniform) this.baseUniform).name = name;
                ((LibGDXUniform) this.baseUniform).programID = programID;
            }
            else
            {
                this.shadowMapUniform = new LibGDXUniform2i();
                ((LibGDXUniform) this.shadowMapUniform).name = name;
                ((LibGDXUniform) this.shadowMapUniform).programID = programID;
            }
        }
    }

    public static class LibGDXGroupUniform3i extends ShaderGroup.Uniform3i implements LibGDXGroupUniform
    {
        @Override
        public void instantiate(String name, int programID, boolean shadow)
        {
            if (!shadow)
            {
                this.baseUniform = new LibGDXUniform3i();
                ((LibGDXUniform) this.baseUniform).name = name;
                ((LibGDXUniform) this.baseUniform).programID = programID;
            }
            else
            {
                this.shadowMapUniform = new LibGDXUniform3i();
                ((LibGDXUniform) this.shadowMapUniform).name = name;
                ((LibGDXUniform) this.shadowMapUniform).programID = programID;
            }
        }
    }

    public static class LibGDXGroupUniform4i extends ShaderGroup.Uniform4i implements LibGDXGroupUniform
    {
        @Override
        public void instantiate(String name, int programID, boolean shadow)
        {
            if (!shadow)
            {
                this.baseUniform = new LibGDXUniform4i();
                ((LibGDXUniform) this.baseUniform).name = name;
                ((LibGDXUniform) this.baseUniform).programID = programID;
            }
            else
            {
                this.shadowMapUniform = new LibGDXUniform4i();
                ((LibGDXUniform) this.shadowMapUniform).name = name;
                ((LibGDXUniform) this.shadowMapUniform).programID = programID;
            }
        }
    }

    public static class LibGDXGroupUniform1f extends ShaderGroup.Uniform1f implements LibGDXGroupUniform
    {
        @Override
        public void instantiate(String name, int programID, boolean shadow)
        {
            if (!shadow)
            {
                this.baseUniform = new LibGDXUniform1f();
                ((LibGDXUniform) this.baseUniform).name = name;
                ((LibGDXUniform) this.baseUniform).programID = programID;
            }
            else
            {
                this.shadowMapUniform = new LibGDXUniform1f();
                ((LibGDXUniform) this.shadowMapUniform).name = name;
                ((LibGDXUniform) this.shadowMapUniform).programID = programID;
            }
        }
    }

    public static class LibGDXGroupUniform2f extends ShaderGroup.Uniform2f implements LibGDXGroupUniform
    {
        @Override
        public void instantiate(String name, int programID, boolean shadow)
        {
            if (!shadow)
            {
                this.baseUniform = new LibGDXUniform2f();
                ((LibGDXUniform) this.baseUniform).name = name;
                ((LibGDXUniform) this.baseUniform).programID = programID;
            }
            else
            {
                this.shadowMapUniform = new LibGDXUniform2f();
                ((LibGDXUniform) this.shadowMapUniform).name = name;
                ((LibGDXUniform) this.shadowMapUniform).programID = programID;
            }
        }
    }

    public static class LibGDXGroupUniform3f extends ShaderGroup.Uniform3f implements LibGDXGroupUniform
    {
        @Override
        public void instantiate(String name, int programID, boolean shadow)
        {
            if (!shadow)
            {
                this.baseUniform = new LibGDXUniform3f();
                ((LibGDXUniform) this.baseUniform).name = name;
                ((LibGDXUniform) this.baseUniform).programID = programID;
            }
            else
            {
                this.shadowMapUniform = new LibGDXUniform3f();
                ((LibGDXUniform) this.shadowMapUniform).name = name;
                ((LibGDXUniform) this.shadowMapUniform).programID = programID;
            }
        }
    }

    public static class LibGDXGroupUniform4f extends ShaderGroup.Uniform4f implements LibGDXGroupUniform
    {
        @Override
        public void instantiate(String name, int programID, boolean shadow)
        {
            if (!shadow)
            {
                this.baseUniform = new LibGDXUniform4f();
                ((LibGDXUniform) this.baseUniform).name = name;
                ((LibGDXUniform) this.baseUniform).programID = programID;
            }
            else
            {
                this.shadowMapUniform = new LibGDXUniform4f();
                ((LibGDXUniform) this.shadowMapUniform).name = name;
                ((LibGDXUniform) this.shadowMapUniform).programID = programID;
            }
        }
    }

    public static class LibGDXGroupUniformMatrix2 extends ShaderGroup.UniformMatrix2 implements LibGDXGroupUniform
    {
        @Override
        public void instantiate(String name, int programID, boolean shadow)
        {
            if (!shadow)
            {
                this.baseUniform = new LibGDXUniformMatrix2();
                ((LibGDXUniform) this.baseUniform).name = name;
                ((LibGDXUniform) this.baseUniform).programID = programID;
            }
            else
            {
                this.shadowMapUniform = new LibGDXUniformMatrix2();
                ((LibGDXUniform) this.shadowMapUniform).name = name;
                ((LibGDXUniform) this.shadowMapUniform).programID = programID;
            }
        }
    }

    public static class LibGDXGroupUniformMatrix3 extends ShaderGroup.UniformMatrix3 implements LibGDXGroupUniform
    {
        @Override
        public void instantiate(String name, int programID, boolean shadow)
        {
            if (!shadow)
            {
                this.baseUniform = new LibGDXUniformMatrix3();
                ((LibGDXUniform) this.baseUniform).name = name;
                ((LibGDXUniform) this.baseUniform).programID = programID;
            }
            else
            {
                this.shadowMapUniform = new LibGDXUniformMatrix3();
                ((LibGDXUniform) this.shadowMapUniform).name = name;
                ((LibGDXUniform) this.shadowMapUniform).programID = programID;
            }
        }
    }

    public static class LibGDXGroupUniformMatrix4 extends ShaderGroup.UniformMatrix4 implements LibGDXGroupUniform
    {
        @Override
        public void instantiate(String name, int programID, boolean shadow)
        {
            if (!shadow)
            {
                this.baseUniform = new LibGDXUniformMatrix4();
                ((LibGDXUniform) this.baseUniform).name = name;
                ((LibGDXUniform) this.baseUniform).programID = programID;
            }
            else
            {
                this.shadowMapUniform = new LibGDXUniformMatrix4();
                ((LibGDXUniform) this.shadowMapUniform).name = name;
                ((LibGDXUniform) this.shadowMapUniform).programID = programID;
            }
        }
    }
}
