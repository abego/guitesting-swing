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

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import java.awt.Component;
import java.util.function.Function;

final class ListCellRendererForTextProvider<T> extends DefaultListCellRenderer {
    private final Class<T> valueType;
    private final Function<T, String> textProvider;

    private ListCellRendererForTextProvider(Class<T> valueType, Function<T, String> textProvider) {
        this.valueType = valueType;
        this.textProvider = textProvider;
    }
    public static <T, L extends ListCellRenderer<T>> L newListCellRendererForTextProvider(Class<T> valueType, Function<T, String> textProvider) {
        //noinspection unchecked
        return (L) new ListCellRendererForTextProvider<>(valueType, textProvider);
    }

    @Override
    public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

        JLabel listCellRendererComponent =
                (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (valueType.isInstance(value)) {
            //noinspection unchecked
            listCellRendererComponent.setText(textProvider.apply((T) value));
        }
        return listCellRendererComponent;
    }
}
