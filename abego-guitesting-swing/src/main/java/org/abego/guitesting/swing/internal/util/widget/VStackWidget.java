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

package org.abego.guitesting.swing.internal.util.widget;

import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import static java.awt.GridBagConstraints.REMAINDER;

public class VStackWidget extends HVStackWidget {
    private VStackWidget(int spacing) {
        super(spacing);
    }

    public static VStackWidget vStackWidget(int spacing, Widget... widgets) {
        VStackWidget newWidget = new VStackWidget(spacing);
        newWidget.setItems(widgets);
        return newWidget;
    }

    public static VStackWidget vStackWidget(Widget... widgets) {
        return vStackWidget(DEFAULT_SPACING,widgets);
    }

    protected GridBagConstraints getItemConstraints() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(0, 0, getSpacing(), 0);
        constraints.gridwidth = REMAINDER;
        return constraints;
    }

    @Override
    protected GridBagConstraints getLastItemConstraints() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = REMAINDER;
        return constraints;
    }

    protected void addFillers() {
        // (adding a JLabel is fine as it takes no space when text is empty)
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weighty = 1;
        content.add(new JLabel(), constraints);
    }

}
