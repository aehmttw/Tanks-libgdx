package libgdxwindow;

import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class DummyShader extends ShaderProgram
{
    public DummyShader()
    {
        super("void main() {}", "void main() {}");
    }

    @Override
    public int getAttributeLocation(String s)
    {
        return 0;
    }

    @Override
    public void setVertexAttribute(int location, int size, int type, boolean normalize, int stride, int offset)
    {

    }

    @Override
    public void enableVertexAttribute(int location)
    {

    }
}
