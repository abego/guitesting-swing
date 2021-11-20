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

public class HStackWidget extends HVStackWidget {

    private HStackWidget(int spacing) {
        super(spacing);
    }

    public static HStackWidget hStackWidget(int spacing, Widget... widgets) {
        HStackWidget newWidget = new HStackWidget(spacing);
        newWidget.setItems(widgets);
        return newWidget;
    }

    public static HStackWidget hStackWidget(Widget... widgets) {
        return hStackWidget(DEFAULT_SPACING, widgets);
    }

    @Override
    protected GridBagConstraints getItemConstraints() {
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.insets = new Insets(0, 0, 0, getSpacing());
        return gridBagConstraints;
    }

    protected void addFillers() {
        GridBagConstraints constraints = new GridBagConstraints();

        // add a filler to fill all space right to the last item
        // (adding a JLabel is fine as it takes no space when text is empty)
        constraints.weightx = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        content.add(new JLabel(), constraints);

        // add a filler to fill all space belowo the items
        constraints.weighty = 1;
        content.add(new JLabel(), constraints);
    }


}
