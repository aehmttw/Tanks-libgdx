package libgdxwindow;

import basewindow.*;
import basewindow.transformation.Rotation;
import basewindow.transformation.Scale;
import basewindow.transformation.Translation;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.BufferUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;


public class VBOModelPart extends ModelPart
{
    public LibGDXWindow window;

    protected int colorVBO;

    protected int vertexVBO;
    protected int texVBO;
    protected int normalVBO;

    @Override
    public void draw(double posX, double posY, double posZ, double sX, double sY, double sZ, double yaw, double pitch, double roll, boolean depthTest)
    {
        IBaseShader shader = (IBaseShader) this.window.currentShader;
        boolean depthMask = this.material.useDefaultDepthMask ? this.window.colorA >= 1.0 : this.material.depthMask;

        window.setDrawMode(-1, depthTest, depthMask, 0);
        if (window.drawingShadow && (!depthTest || !depthMask))
            return;

        if (this.material.glow)
            window.setGlowBlendFunc();
        else
            window.setTransparentBlendFunc();

        if (depthTest)
            window.enableDepthtest();
        else
            window.disableDepthtest();

        if (depthMask)
            window.enableDepthmask();
        else
            window.disableDepthmask();

        if (this.material.customLight)
            window.setMaterialLights(this.material.ambient, this.material.diffuse, this.material.specular, this.material.shininess);

        if (depthTest)
            window.enableDepthtest();
        else
            window.disableDepthtest();

        window.setCelShadingSections(this.material.celSections);

        this.window.setMatrixModelview();
        this.window.addMatrix();
        Translation.transform(window, posX / window.absoluteWidth, posY / window.absoluteHeight, posZ / window.absoluteDepth);
        Rotation.transform(window, -pitch, -roll, -yaw);
        Scale.transform(window, sX, sY, sZ);

        if (this.material.texture != null)
            window.setTexture(this.material.texture);

        shader.renderVBO(this.vertexVBO, this.colorVBO, this.texVBO, this.normalVBO, this.shapes.length * 3);
        window.disableTexture();

        this.window.removeMatrix();
        this.window.setMatrixProjection();

        window.disableDepthtest();

        if (this.material.customLight)
            window.disableMaterialLights();

        window.stopTexture();
    }

    @Override
    public void draw2D(double posX, double posY, double posZ, double sX, double sY, double sZ)
    {
        window.setDrawMode(-1, false, false, 0);

        if (window.drawingShadow)
            return;

        IBaseShader shader = (IBaseShader) this.window.currentShader;

        if (this.material.glow)
            window.setGlowBlendFunc();
        else
            window.setTransparentBlendFunc();

        window.disableDepthtest();

        if (this.material.useDefaultDepthMask || this.material.depthMask)
            window.enableDepthmask();
        else
            window.disableDepthmask();

        this.window.setMatrixModelview();
        this.window.addMatrix();
        Translation.transform(window, posX / window.absoluteWidth, posY / window.absoluteHeight, posZ / window.absoluteDepth);
        Scale.transform(window, 1, 1, 0);
        Rotation.transform(window, 0, Math.PI * -3 / 4, 0);
        Rotation.transform(window, 0, 0, Math.PI / 4);
        Scale.transform(window, sX, sY, sZ);

        if (this.material.texture != null)
            window.setTexture(this.material.texture);

        shader.renderVBO(this.vertexVBO, this.colorVBO, this.texVBO, this.normalVBO, this.shapes.length * 3);
        window.disableTexture();

        this.window.removeMatrix();
        window.stopTexture();
    }

    @Override
    public void draw(double posX, double posY, double sX, double sY, double angle)
    {
        window.setDrawMode(-1, false, false, 0);

        if (window.drawingShadow)
            return;

        IBaseShader shader = (IBaseShader) this.window.currentShader;

        if (this.material.glow)
            window.setGlowBlendFunc();
        else
            window.setTransparentBlendFunc();

        window.disableDepthtest();

        if (this.material.useDefaultDepthMask || this.material.depthMask)
            window.enableDepthmask();
        else
            window.disableDepthmask();

        if (this.material.customLight)
            window.setMaterialLights(this.material.ambient, this.material.diffuse, this.material.specular, this.material.shininess);

        window.setCelShadingSections(this.material.celSections);

        this.window.setMatrixModelview();
        this.window.addMatrix();

        Translation.transform(window, posX / window.absoluteWidth, posY / window.absoluteHeight, 0);
        Rotation.transform(window, 0, 0, -angle);
        Scale.transform(window, sX, sY, 0);

        if (this.material.texture != null)
            window.setTexture(this.material.texture);

        shader.renderVBO(this.vertexVBO, this.colorVBO, this.texVBO, this.normalVBO, this.shapes.length * 3);
        window.disableTexture();

        this.window.removeMatrix();
        this.window.setMatrixProjection();

        if (this.material.customLight)
            window.disableMaterialLights();

        window.stopTexture();
    }

    @Override
    public void processShapes()
    {
        FloatBuffer vert = BufferUtils.newFloatBuffer(this.shapes.length * 9);
        FloatBuffer color = BufferUtils.newFloatBuffer(this.shapes.length * 12);
        FloatBuffer tex = BufferUtils.newFloatBuffer(this.shapes.length * 6);
        FloatBuffer normals = BufferUtils.newFloatBuffer(this.shapes.length * 9);

        for (Shape s : this.shapes)
        {
            for (Point p : s.points)
            {
                vert.put((float) p.x);
                vert.put((float) p.y);
                vert.put((float) p.z);
            }

            for (Point p : s.texCoords)
            {
                tex.put((float) p.x);
                tex.put((float) p.y);
            }

            for (Point p : s.normals)
            {
                normals.put((float) p.x);
                normals.put((float) p.y);
                normals.put((float) p.z);
            }

            for (double[] p : s.colors)
            {
                color.put((float) (p[0] * this.material.colorR));
                color.put((float) (p[1] * this.material.colorG));
                color.put((float) (p[2] * this.material.colorB));
                color.put((float) (p[3] * this.material.colorA));
            }
        }

        vert.flip();
        color.flip();
        normals.flip();
        tex.flip();

        this.vertexVBO = window.createVBO();
        this.colorVBO = window.createVBO();

        window.vertexBufferData(this.vertexVBO, vert);
        window.vertexBufferData(this.colorVBO, color);

        if (this.material.texture != null)
        {
            this.texVBO = window.createVBO();
            window.vertexBufferData(this.texVBO, tex);
        }

        if (this.material.useNormals)
        {
            this.normalVBO = window.createVBO();
            window.vertexBufferData(this.normalVBO, normals);
        }

        window.stopTexture();
    }

    public VBOModelPart(LibGDXWindow window)
    {
        super(window);
    }

    public VBOModelPart(BaseWindow window, Model model, ArrayList<Shape> shapes, Model.Material material)
    {
        super(window, model, shapes, material);
    }

    @Override
    public void setWindow(BaseWindow w)
    {
        this.window = (LibGDXWindow) w;
    }
}
