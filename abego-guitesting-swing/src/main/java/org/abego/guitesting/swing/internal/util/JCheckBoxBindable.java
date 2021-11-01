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

import org.abego.guitesting.swing.internal.util.prop.Prop;
import org.abego.guitesting.swing.internal.util.prop.PropService;
import org.abego.guitesting.swing.internal.util.prop.PropServices;

import javax.swing.JCheckBox;

import static java.lang.Boolean.FALSE;
import static javax.swing.SwingUtilities.invokeLater;

public final class JCheckBoxBindable extends JCheckBox {

    //region State/Model
    private final PropService propService = PropServices.getDefault();
    //region @Prop @InheritsGetSet public Boolean selected = FALSE
    private final Prop<Boolean> selectedProp =
            propService.newProp(FALSE, this, "selected"); //NON-NLS

    public void bindSelectedTo(Prop<Boolean> prop) {
        selectedProp.bindTo(prop);
    }

    //endregion
    //endregion
    //region Construction
    private JCheckBoxBindable() {
        initBindings();
    }

    public static JCheckBoxBindable checkBoxBindable() {
        return new JCheckBoxBindable();
    }
    //endregion
    //region Binding related
    private void initBindings() {
        selectedProp.runDependingCode(this::updateSelectedUI);
        addItemListener(i -> updateSelectedProp());
    }

    private void updateSelectedUI() {
        invokeLater(() -> setSelected(selectedProp.get()));
    }

    private void updateSelectedProp() {
        boolean isSelected = isSelected();
        if (selectedProp.get() != isSelected) {
            selectedProp.set(isSelected);
        }
    }
    //endregion
}
