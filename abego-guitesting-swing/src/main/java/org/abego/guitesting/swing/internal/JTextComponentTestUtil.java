/*
 * MIT License
 *
 * Copyright (c) 2019 Udo Borkowski, (ub@abego.org)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.abego.guitesting.swing.internal;

import org.abego.guitesting.swing.GT;
import org.abego.guitesting.swing.GuiTestingException;

import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.Point;
import java.awt.Rectangle;

import static org.abego.commons.lang.StringUtil.substringSafe;

public class JTextComponentTestUtil {

    static final String CLICK_AT_OFFSET_WITH_INVALID_OFFSET_MESSAGE = "clickAtOffset with invalid offset"; //NON-NLS

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
        return substringSafe(text, 0, s)
                + startMarker + substringSafe(text, s, e) + endMarker +
                substringSafe(text, e);

    }


    public static Point screenLocationOfOffset(JTextComponent textComponent, int offset)
            throws BadLocationException {
        Rectangle r = textComponent.modelToView(offset);
        Point topLeft = textComponent.getLocationOnScreen();
        return new Point(r.x + topLeft.x, r.y + topLeft.y + r.height / 2);
    }

    public static void clickAtOffset(JTextComponent textComponent, int offset,
                                     GT gt) {
        try {
            Point pt = screenLocationOfOffset(textComponent, offset);
            gt.clickLeft(pt);
        } catch (BadLocationException e) {
            throw new GuiTestingException(CLICK_AT_OFFSET_WITH_INVALID_OFFSET_MESSAGE, e); //NON-NLS
        }
    }

    public static void clickAtStartOfSubstring(JTextComponent textComponent, String substring,
                                               GT gt) {
        int i = textComponent.getText().indexOf(substring);
        if (i >= 0) {
            clickAtOffset(textComponent, i, gt);
        }
    }
}
