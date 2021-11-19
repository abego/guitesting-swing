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

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Collection;

import static java.awt.GridBagConstraints.REMAINDER;

public class VStackWidget implements Widget {
    protected final JComponent content = new JPanel(new GridBagLayout());

    protected VStackWidget() {
        styleComponents();
    }

    private void styleComponents() {
        content.setOpaque(false);
        content.setBorder(null);
    }

    public static VStackWidget vStackWidget(Widget... widgets) {
        VStackWidget newWidget = new VStackWidget();
        newWidget.setItems(widgets);
        return newWidget;
    }

    @Override
    public JComponent getContent() {
        return content;
    }

    @Override
    public void close() {
    }

    public void setItems(Collection<Widget> widgets) {
        setItems(widgets.toArray(new Widget[0]));
    }

    public void setItems(Widget... widgets) {
        content.removeAll();
        int n = widgets.length;

        // add all but the last item (use the same constraints))
        GridBagConstraints constraints = getItemConstraints();
        for (int i = 0; i < n - 1; i++) {
            content.add(widgets[i].getContent(), constraints);
        }
        // add the last item (may need special constraints)
        if (n > 0) {
            content.add(widgets[n - 1].getContent(), getLastItemConstraints());
        }

        // add extra elements as a filler taking all remaining extra space
        addFillers();

        // We need to do an explicit repaint as in certain settings areas are
        // not atomatically repainted when the widgets "shrinks", i.e. occupies
        // less space as before.
        content.repaint();
        content.revalidate();
    }

    protected GridBagConstraints getItemConstraints() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = REMAINDER;
        return constraints;
    }

    protected GridBagConstraints getLastItemConstraints() {
        return getItemConstraints();
    }

    protected void addFillers() {
        // (adding a JLabel is fine as it takes no space when text is empty)
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weighty = 1;
        content.add(new JLabel(), constraints);
    }

}
