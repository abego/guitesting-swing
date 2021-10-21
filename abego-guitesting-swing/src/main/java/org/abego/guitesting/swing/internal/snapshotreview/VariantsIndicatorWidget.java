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

package org.abego.guitesting.swing.internal.snapshotreview;

import org.abego.guitesting.swing.ScreenCaptureSupport;
import org.eclipse.jdt.annotation.Nullable;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import static org.abego.guitesting.swing.internal.util.SwingUtil.DEFAULT_FLOW_GAP;
import static org.abego.guitesting.swing.internal.util.SwingUtil.flowLeft;
import static org.abego.guitesting.swing.internal.util.SwingUtil.label;

class VariantsIndicatorWidget<T extends ScreenCaptureSupport.SnapshotIssue> implements Widget {
    private static final int BULLET_SIZE = 24;
    private static final Font BULLET_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, BULLET_SIZE);

    @Nullable
    private VariantsInfo<T> variantsInfo;
    private final JComponent content;

    VariantsIndicatorWidget() {
        content = flowLeft(DEFAULT_FLOW_GAP, 0);
        // make the panel so small only one bullet fits into the row,
        // so the bullets are stacked vertically
        content.setPreferredSize(new Dimension(BULLET_SIZE, Integer.MAX_VALUE));
        content.setOpaque(true);
        content.setBackground(Color.white);
    }

    @Override
    public JComponent getComponent() {
        return content;
    }

    @Nullable
    public VariantsInfo<T> getVariantsInfo() {
        return variantsInfo;
    }

    public void setVariantsInfo(@Nullable VariantsInfo<T> info) {
        variantsInfo = info;

        updateContent();
    }

    private void updateContent() {
        content.removeAll();

        @Nullable VariantsInfo<T> info = getVariantsInfo();
        if (info != null) {
            int n = info.getVariantsCount();
            int sel = info.getVariantsIndex();
            for (int i = 0; n > 1 && i < n; i++) {
                content.add(
                        label(i == sel ? "●" : "○", c -> c.setFont(BULLET_FONT)));
            }
            content.repaint();
            content.revalidate();
        }
    }
}
