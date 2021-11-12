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

import org.abego.guitesting.swing.internal.util.prop.Bindings;
import org.abego.guitesting.swing.internal.util.prop.Prop;
import org.abego.guitesting.swing.internal.util.prop.PropService;
import org.abego.guitesting.swing.internal.util.prop.PropServices;

import javax.swing.JComponent;
import javax.swing.JLabel;

public final class LabelWidget implements Widget {

    //region State/Model
    private final PropService propService = PropServices.getDefault();
    //region @Prop public String text = ""
    private final Prop<String> textProp =
            propService.newProp("", this, "text"); //NON-NLS

    public String getText() {
        return getTextProp().get();
    }

    public void setText(String value) {
        textProp.set(value);
    }

    public Prop<String> getTextProp() {
        return textProp;
    }

    //endregion
    //endregion
    //region Components
    private final JLabel label = new JLabel();

    //endregion
    //region Construction/Closing
    private LabelWidget() {
        initBindings();
    }

    public static LabelWidget labelWidget() {
        return new LabelWidget();
    }

    public void close() {
        bindings.close();
    }

    //endregion
    //region Widget related
    @Override
    public JComponent getContent() {
        return label;
    }

    //endregion
    //region Binding related
    private Bindings bindings = propService.newBindings();

    private void initBindings() {
        bindings.bindSwingCode(textProp, () -> label.setText(textProp.get()));
        label.addPropertyChangeListener("text", e -> updateTextProp());
    }


    private void updateTextProp() {
        String text = label.getText();
        //TODO: do we need the equals check, or should this do the setter?
        //noinspection CallToSuspiciousStringMethod
        if (!(textProp.get().equals(text))) {
            textProp.set(text);
        }
    }

    //endregion
}
