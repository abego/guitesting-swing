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

package org.abego.guitesting.swing.internal.util.boxstyle;

import org.abego.guitesting.swing.GT;
import org.abego.guitesting.swing.GuiTesting;
import org.junit.jupiter.api.Test;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;

import static java.awt.Color.BLACK;
import static java.awt.Color.GREEN;
import static java.awt.Color.ORANGE;
import static java.awt.Color.PINK;
import static java.awt.Color.WHITE;
import static org.abego.guitesting.swing.internal.util.SwingUtil.LIGHTER_GRAY;
import static org.abego.guitesting.swing.internal.util.boxstyle.BoxStyle.Style.SOLID;
import static org.abego.guitesting.swing.internal.util.boxstyle.BoxStyle.style;

class BoxStyleTest {
    private GT gt = GuiTesting.newGT();

    @Test
    void smoketest() {
        JPanel content = new JPanel();
        content.setBackground(WHITE);
        content.setPreferredSize(new Dimension(69, 33));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(LIGHTER_GRAY);
        panel.add(content);

        style()
                .borderTop(1, SOLID, BLACK)
                .borderLeft(5, SOLID, PINK)
                .borderBottom(3, SOLID, ORANGE)
                .borderRight(8, SOLID, GREEN)
                .margin(3, 5, 2, 1)
                .padding(2, 4, 6, 8)

                .applyTo(panel);

        JFrame f = gt.showInFrameTitled("BoxStyle", panel);
        gt.waitUntilScreenshotMatchesSnapshot(panel);
    }
}