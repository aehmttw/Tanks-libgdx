package theopalgames.tanks;

import basewindow.BaseVibrationPlayer;
import org.robovm.apple.uikit.UIImpactFeedbackGenerator;
import org.robovm.apple.uikit.UIImpactFeedbackStyle;
import org.robovm.apple.uikit.UISelectionFeedbackGenerator;

public class IOSVibrationPlayer extends BaseVibrationPlayer
{
    UISelectionFeedbackGenerator select = new UISelectionFeedbackGenerator();
    UIImpactFeedbackGenerator click = new UIImpactFeedbackGenerator(UIImpactFeedbackStyle.Light);
    UIImpactFeedbackGenerator heavyClick = new UIImpactFeedbackGenerator(UIImpactFeedbackStyle.Heavy);

    @Override
    public void selectionChanged()
    {
        select.selectionChanged();
    }

    @Override
    public void click()
    {
        click.impactOccurred();
    }

    @Override
    public void heavyClick()
    {
        heavyClick.impactOccurred();
    }
}
