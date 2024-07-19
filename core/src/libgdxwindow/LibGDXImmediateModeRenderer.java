
// This file is adapted from the LibGDX ImmediateModeRenderer20 to work with our stuff

/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package libgdxwindow;

import basewindow.ShaderBase;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;

import static com.badlogic.gdx.graphics.GL20.*;

/** Immediate mode rendering class for GLES 2.0. The renderer will allow you to specify vertices on the fly and provides a default
 * shader for (unlit) rendering.
 * @author mzechner */
public class LibGDXImmediateModeRenderer implements ImmediateModeRenderer
{
    private int primitiveType;
    private int vertexIdx;
    private int numSetTexCoords;
    private final int maxVertices;
    public int numVertices;
    private final Mesh mesh;
    private final int numTexCoords;
    private final int vertexSize;
    private final int normalOffset;
    private final int colorOffset;
    private final int texCoordOffset;
    private final Matrix4 projModelView = new Matrix4();
    private final float[] vertices;

    public DummyShader dummyShader = new DummyShader();
    public VertexAttribute[] attributes;
    public LibGDXWindow window;

    public boolean normals;
    public boolean colors;
    public boolean texCoords;
    protected boolean running;

    public LibGDXImmediateModeRenderer(LibGDXWindow window, int maxVertices, boolean hasNormals, boolean hasColors, int numTexCoords)
    {
        this.window = window;
        this.maxVertices = maxVertices;
        this.numTexCoords = numTexCoords;

        VertexAttribute[] attribs = buildVertexAttributes(hasNormals, hasColors, numTexCoords);
        this.attributes = attribs;
        mesh = new Mesh(false, maxVertices, 0, attribs);
        mesh.setAutoBind(false);

        this.normals = hasNormals;
        this.colors = hasColors;
        this.texCoords = numTexCoords > 0;

        vertices = new float[maxVertices * (mesh.getVertexAttributes().vertexSize / 4)];
        vertexSize = mesh.getVertexAttributes().vertexSize / 4;
        normalOffset = mesh.getVertexAttribute(Usage.Normal) != null ? mesh.getVertexAttribute(Usage.Normal).offset / 4 : 0;
        colorOffset = mesh.getVertexAttribute(Usage.ColorUnpacked) != null ? mesh.getVertexAttribute(Usage.ColorUnpacked).offset / 4 : 0;
        texCoordOffset = mesh.getVertexAttribute(Usage.TextureCoordinates) != null ? mesh.getVertexAttribute(Usage.TextureCoordinates).offset / 4 : 0;
    }

    private VertexAttribute[] buildVertexAttributes(boolean hasNormals, boolean hasColor, int numTexCoords)
    {
        Array<VertexAttribute> attribs = new Array<VertexAttribute>();
        attribs.add(new VertexAttribute(Usage.Position, 3, "glPosition"));
        if (hasNormals) attribs.add(new VertexAttribute(Usage.Normal, 3, "glNormal"));
        if (hasColor) attribs.add(new VertexAttribute(Usage.ColorUnpacked, 4, "glColor"));
        for (int i = 0; i < numTexCoords; i++)
        {
            attribs.add(new VertexAttribute(Usage.TextureCoordinates, 2, "glMultiTexCoord"));
        }
        VertexAttribute[] array = new VertexAttribute[attribs.size];
        for (int i = 0; i < attribs.size; i++)
            array[i] = attribs.get(i);
        return array;
    }

    public void begin(Matrix4 projModelView, int primitiveType)
    {
        this.projModelView.set(projModelView);
        this.primitiveType = primitiveType;
        this.running = true;
    }

    public void color(Color color)
    {
        color(color.r, color.g, color.b, color.a);
    }

    public void color(float r, float g, float b, float a)
    {
        if (!running)
            return;

        vertices[vertexIdx + colorOffset] = r;
        vertices[vertexIdx + colorOffset + 1] = g;
        vertices[vertexIdx + colorOffset + 2] = b;
        vertices[vertexIdx + colorOffset + 3] = a;
    }

    @Override
    public void color(float colorBits)
    {
        throw new RuntimeException("use a real color please");
    }

    public void texCoord(float u, float v)
    {
        if (!running || !texCoords)
            return;

        final int idx = vertexIdx + texCoordOffset;
        vertices[idx + numSetTexCoords] = u;
        vertices[idx + numSetTexCoords + 1] = v;
        numSetTexCoords += 2;
    }

    public void normal(float x, float y, float z)
    {
        if (!running)
            return;

        final int idx = vertexIdx + normalOffset;
        vertices[idx] = x;
        vertices[idx + 1] = y;
        vertices[idx + 2] = z;
    }

    public void vertex(float x, float y, float z)
    {
        if (!running)
            return;

        final int idx = vertexIdx;
        vertices[idx] = x;
        vertices[idx + 1] = y;
        vertices[idx + 2] = z;

        numSetTexCoords = 0;
        vertexIdx += vertexSize;
        numVertices++;
    }

    public void bind()
    {
        // This redirects to VertexBufferObject.class, and effectively just runs:
        // gl.glBindBuffer(34962, this.mesh.bufferHandle);
        // gl.glBufferData(34962, this.buffer.limit(), this.buffer, this.usage);
        // Unfortunately, it is annoying when people make their code private.
        this.mesh.bind(dummyShader);
        // The rest of the code is what bind normally would do. But the dummy shader stops it
        // from doing its stuff so that we can do our stuff.

        int id = ((LibGDXShaderUtil) (this.window.currentShader.util)).position.id;
        Gdx.gl.glEnableVertexAttribArray(id);
        Gdx.gl.glVertexAttribPointer(id, 3, GL_FLOAT, false, mesh.getVertexAttributes().vertexSize, 0);

        if (colors)
        {
            id = ((LibGDXShaderUtil) (this.window.currentShader.util)).color.id;
            Gdx.gl.glEnableVertexAttribArray(id);
            Gdx.gl.glVertexAttribPointer(id, 4, GL_FLOAT, false, mesh.getVertexAttributes().vertexSize, colorOffset * 4);
        }

        if (normals)
        {
            id = ((LibGDXShaderUtil) (this.window.currentShader.util)).normal.id;
            Gdx.gl.glEnableVertexAttribArray(id);
            Gdx.gl.glVertexAttribPointer(id, 3, GL_FLOAT, false, mesh.getVertexAttributes().vertexSize, normalOffset * 4);
        }

        if (texCoords)
        {
            id = ((LibGDXShaderUtil) (this.window.currentShader.util)).textureCoord.id;
            Gdx.gl.glEnableVertexAttribArray(id);
            Gdx.gl.glVertexAttribPointer(id, 2, GL_FLOAT, false, mesh.getVertexAttributes().vertexSize, texCoordOffset * 4);
        }
    }

    public void unbind()
    {
        this.mesh.unbind(this.dummyShader);
        Gdx.gl.glDisableVertexAttribArray(((LibGDXShaderUtil) (this.window.currentShader.util)).position.id);
        Gdx.gl.glDisableVertexAttribArray(((LibGDXShaderUtil) (this.window.currentShader.util)).color.id);
        Gdx.gl.glDisableVertexAttribArray(((LibGDXShaderUtil) (this.window.currentShader.util)).normal.id);
        Gdx.gl.glDisableVertexAttribArray(((LibGDXShaderUtil) (this.window.currentShader.util)).textureCoord.id);

        Gdx.gl.glBindBuffer(34962, 0);
    }

    public void flush()
    {
        if (!running)
            return;

        this.running = false;

        if (numVertices == 0) return;
        ((LibGDXShaderUtil)this.window.currentShader.util).setPerspective();

        if (this.window.currentShader instanceof ShaderBase)
            ((ShaderBase) this.window.currentShader).tex.set(0);

        this.bind();
        mesh.setVertices(vertices, 0, vertexIdx);

        if (texCoords)
            this.window.enableTexture();

        mesh.render(null, primitiveType);
        this.unbind();
        this.window.disableTexture();
        numSetTexCoords = 0;
        vertexIdx = 0;
        numVertices = 0;
    }

    public void end()
    {
        flush();
    }

    public int getNumVertices()
    {
        return numVertices;
    }

    @Override
    public int getMaxVertices()
    {
        return maxVertices;
    }

    public void dispose()
    {
        mesh.dispose();
    }
}
