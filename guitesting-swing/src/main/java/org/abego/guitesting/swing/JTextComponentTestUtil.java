package org.abego.guitesting.swing;

import org.abego.guitesting.GuiTesting;
import org.abego.guitesting.GuiTestingException;

import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.Point;
import java.awt.Rectangle;

import static org.abego.commons.lang.StringUtil.substringSafe;

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
        return substringSafe(text,0, s)
                + startMarker + substringSafe(text,s, e) + endMarker +
                substringSafe(text, e);

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
            throw new GuiTestingException("clickAtOffset with invalid offset", e); //NON-NLS
        }
    }

    public static void clickAtStartOfSubstring(JTextComponent textComponent, String substring,
                                      GuiTesting guiTesting) {
        int i = textComponent.getText().indexOf(substring);
        if (i >= 0) {
            clickAtOffset(textComponent, i, guiTesting);
        }
    }
}
