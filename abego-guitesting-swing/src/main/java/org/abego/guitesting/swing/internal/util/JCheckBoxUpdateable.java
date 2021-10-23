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
import java.util.function.Supplier;

import static javax.swing.SwingUtilities.invokeLater;

public final class JCheckBoxUpdateable extends JCheckBox implements Updateable {
    private Supplier<Boolean> selectedCondition;

    private JCheckBoxUpdateable() {
        this.selectedCondition = () -> false;
        update();
    }

    public static JCheckBoxUpdateable checkBoxUpdateable() {
        return new JCheckBoxUpdateable();
    }

    public Supplier<Boolean> getSelectedCondition() {
        return selectedCondition;
    }

    public void setSelectedCondition(Supplier<Boolean> selectedCondition) {
        this.selectedCondition = selectedCondition;
        update();
    }

    public void setSelected(boolean value) {
        throw new UnsupportedOperationException(
                "Cannot explicitly set selected value (is defined by `selectedCondition`)"); //NON-NLS
    }

    @Override
    public void setAction(Action action) {
        super.setAction(action);
        SwingUtil.handleAccelerator(this, action);
    }

    public void update() {
        invokeLater(() -> {
            boolean selectedValue = getSelectedCondition().get();
            if (isSelected() != selectedValue) {
                super.setSelected(selectedValue);
            }
        });
    }
}
