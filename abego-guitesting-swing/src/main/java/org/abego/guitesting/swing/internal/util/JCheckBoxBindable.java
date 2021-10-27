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

package org.abego.guitesting.swing.internal.util;

import javax.swing.Action;
import javax.swing.JCheckBox;

import static java.lang.Boolean.FALSE;
import static javax.swing.SwingUtilities.invokeLater;
import static org.abego.guitesting.swing.internal.util.PropBindable.newPropBindable;

public final class JCheckBoxBindable extends JCheckBox {

    private PropBindable<Boolean> selectedProp =
            newPropBindable(FALSE, this, "selected", f -> updateSelectedUI());

    private JCheckBoxBindable() {
        addItemListener(i -> updateSelectedProp());

        updateSelectedProp();
    }

    public static JCheckBoxBindable checkBoxUpdateable() {
        return new JCheckBoxBindable();
    }

    @Override
    public void setAction(Action action) {
        super.setAction(action);
        SwingUtil.handleAccelerator(this, action);
    }

    public void bindSelectedTo(Prop<Boolean> prop) {
        selectedProp.bindTo(prop);
    }

    private void updateSelectedUI() {
        invokeLater(() -> {
            setSelected(selectedProp.get());
        });
    }

    private void updateSelectedProp() {
        boolean isSelected = isSelected();
        if (selectedProp.get() != isSelected) {
            selectedProp.set(isSelected);
        }
    }

}
