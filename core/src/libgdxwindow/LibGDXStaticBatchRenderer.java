package libgdxwindow;

import basewindow.ShaderGroup;

public class LibGDXStaticBatchRenderer extends LibGDXShapeBatchRenderer
{
    public LibGDXStaticBatchRenderer(LibGDXWindow window)
    {
        super(window, false);
    }

    public LibGDXStaticBatchRenderer(LibGDXWindow window, ShaderGroup s)
    {
        super(window, s, false);
    }
}
