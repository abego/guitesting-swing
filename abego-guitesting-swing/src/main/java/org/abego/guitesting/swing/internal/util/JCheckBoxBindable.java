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

import org.abego.event.EventObserver;
import org.abego.event.EventService;
import org.abego.event.EventServices;
import org.abego.event.PropertyChanged;
import org.eclipse.jdt.annotation.Nullable;

import javax.swing.Action;
import javax.swing.JCheckBox;

import static java.lang.Boolean.FALSE;
import static javax.swing.SwingUtilities.invokeLater;

public final class JCheckBoxBindable extends JCheckBox {
    private Prop<Boolean> selected = Prop.newProp(FALSE);
    private @Nullable EventObserver<PropertyChanged> observer;

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

    public void setSelectedBinding(Prop<Boolean> binding) {
        EventService eventService = EventServices.getDefault();
        if (observer != null) {
            eventService.removeObserver(observer);
        }
        selected = binding;
        observer = eventService.addPropertyObserver(binding,
                c -> updateSelectedUI());
        updateSelectedUI();
    }

    private void updateSelectedUI() {
        setSelected(selected.get());
    }

    private void updateSelectedProp() {
        invokeLater(() -> {
            boolean isSelected = isSelected();
            if (selected.get() != isSelected) {
                selected.set(isSelected);
            }
        });
    }

}
