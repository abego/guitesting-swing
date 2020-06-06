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
import org.abego.guitesting.swing.GuiTesting;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.Dimension;
import java.awt.Point;

import static org.abego.guitesting.swing.internal.JTextComponentTestUtil.CLICK_AT_OFFSET_WITH_INVALID_OFFSET_MESSAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JTextComponentTestUtilTest {
    private final static GT gt = GuiTesting.newGT();

    @AfterEach
    void teardown() {
        gt.cleanup();
    }

    @Test
    void clickAtOffset_ok() {
        JTextArea textArea = new JTextArea();
        textArea.setText("foo bar \nbaz qux");
        gt.showInFrame(new JScrollPane(textArea), new Point(50, 50), new Dimension(200, 100));

        JTextComponentTestUtil.clickAtOffset(textArea, 5, gt);

        assertEquals(5, textArea.getSelectionStart());
        assertEquals(5, textArea.getSelectionEnd());
    }

    @Test
    void clickAtOffset_outOfRange() {
        JTextArea textArea = new JTextArea();
        textArea.setText("foo bar \nbaz qux");
        gt.showInFrame(new JScrollPane(textArea), new Point(50, 50), new Dimension(200, 100));

        Exception e = assertThrows(Exception.class,
                () -> JTextComponentTestUtil.clickAtOffset(textArea, 50, gt));

        assertEquals(CLICK_AT_OFFSET_WITH_INVALID_OFFSET_MESSAGE, e.getMessage());
    }

    @Test
    void clickAtStartOfSubstring_ok() {
        JTextArea textArea = new JTextArea();
        textArea.setText("foo bar \nbaz qux");
        gt.showInFrame(new JScrollPane(textArea), new Point(50, 50), new Dimension(200, 100));

        JTextComponentTestUtil.clickAtStartOfSubstring(textArea, "bar", gt);

        assertEquals(4, textArea.getSelectionStart());
        assertEquals(4, textArea.getSelectionEnd());
    }

    @Test
    void getTextAndHighlights_noHighlights() {
        JTextArea textArea = new JTextArea();
        textArea.setText("foo bar \nbaz qux");
        gt.showInFrame(new JScrollPane(textArea), new Point(50, 50), new Dimension(200, 100));

        String textAndHighlights = JTextComponentTestUtil.getTextAndHighlights(textArea);

        assertEquals("foo bar \nbaz qux", textAndHighlights);
    }

    @Test
    void getTextAndHighlights_withHighlights() throws BadLocationException {
        JTextArea textArea = new JTextArea();
        textArea.setText("foo bar \nbaz qux");
        Highlighter highlighter = textArea.getHighlighter();
        highlighter.addHighlight(4, 7, DefaultHighlighter.DefaultPainter);
        gt.showInFrame(new JScrollPane(textArea), new Point(50, 50), new Dimension(200, 100));

        String textAndHighlights = JTextComponentTestUtil.getTextAndHighlights(textArea);

        assertEquals("foo «bar» \nbaz qux", textAndHighlights);
    }

}