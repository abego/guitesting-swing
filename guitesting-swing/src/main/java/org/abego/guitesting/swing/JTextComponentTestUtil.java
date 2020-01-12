package org.abego.guitesting.swing;

import org.abego.guitesting.GuiTesting;

import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.Point;
import java.awt.Rectangle;

public class JTextComponentTestUtil {

    public static String getTextAndHighlights(JTextComponent textComponent) {
        return getTextAndHighlights(textComponent, "«", "»");
    }

    public static String getTextAndHighlights(JTextComponent textComponent,
                                              String startMarker, String endMarker) {
        Highlighter highlighter = textComponent.getHighlighter();
        Highlighter.Highlight[] highlights = highlighter.getHighlights();

        // the "no highlights" case
        if (highlights.length == 0) {
            return textComponent.getText();
        }

        // at least one highlight exists

        // for now we just return the first highlight
        Highlighter.Highlight h = highlights[0];
        int s = h.getStartOffset();
        int e = h.getEndOffset();
        String text = textComponent.getText();
        //noinspection StringConcatenation
        return text.substring(0, s)
                + startMarker + text.substring(s, e) + endMarker +
                text.substring(e);

    }


    public static Point screenLocationOfOffset(JTextComponent textComponent, int offset)
            throws BadLocationException {
        Rectangle r = textComponent.modelToView(offset);
        Point topLeft = textComponent.getLocationOnScreen();
        return new Point(r.x + topLeft.x, r.y + topLeft.y + r.height / 2);
    }

    public static void clickAtOffset(JTextComponent textComponent, int offset,
                                     GuiTesting guiTesting) {
        try {
            Point pt = screenLocationOfOffset(textComponent, offset);
            guiTesting.clickLeft(pt);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);//TODO: avoid RuntimeException
        }
    }

    public static void clickAtStartOf(JTextComponent textComponent, String subString,
                                      GuiTesting guiTesting) {
        int i = textComponent.getText().indexOf(subString);
        if (i >= 0) {
            clickAtOffset(textComponent, i, guiTesting);
        }
    }
}
