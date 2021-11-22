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

import org.abego.commons.lang.exception.MustNotInstantiateException;
import org.abego.commons.swing.event.ComponentListenerAdapter;

import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.abego.commons.lang.IntUtil.limit;
import static org.abego.guitesting.swing.internal.util.ListCellRendererForTextProvider.newListCellRendererForTextProvider;

public final class SwingUtil {
    public static final int DEFAULT_FLOW_GAP = 5;
    public final static Color LIGHTER_GRAY = new Color(0xd0d0d0);

    SwingUtil() {
        throw new MustNotInstantiateException();
    }

    //region Component related
    public static void onComponentResized(
            Component component, Consumer<ComponentEvent> callback) {

        component.addComponentListener(new ComponentListenerAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                callback.accept(e);
            }
        });
    }
    //endregion
    //region Container related
    public static void addAll(Container container, Component... components) {
        for (Component c : components) {
            container.add(c);
        }
    }
    //endregion
    //region JComponent related
    public static void onJComponentBecomesVisible(JComponent component, Runnable code) {
        component.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                code.run();
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {

            }

            @Override
            public void ancestorMoved(AncestorEvent event) {

            }
        });
    }

    public static void handleAccelerator(JComponent component, Action action) {
        Object accelerator = action.getValue(Action.ACCELERATOR_KEY);
        if (accelerator instanceof KeyStroke) {
            Object key = new Object();
            component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    (KeyStroke) accelerator, key);
            component.getActionMap().put(key, action);
        }
    }
    //endregion
    //region JLabel related
    public static JLabel label(String text, Consumer<JLabel> initCode) {
        JLabel label = new JLabel(text);
        initCode.accept(label);
        return label;
    }

    //endregion
    //region JButton related
    public static JButton toolbarButton() {
        JToolbarButton jToolbarButton = JToolbarButton.newJToolbarButton();
        jToolbarButton.setOpaque(false);
        return jToolbarButton;
    }
    //endregion
    //region JList related
    public static <T> DefaultListModel<T> newDefaultListModel(Iterable<T> items) {
        DefaultListModel<T> listModel = new DefaultListModel<>();
        for (T i : items) {
            listModel.addElement(i);
        }
        return listModel;
    }

    public static <T> void removeIf(DefaultListModel<T> listModel, Predicate<T> predicate) {
        for (int i = listModel.size() - 1; i >= 0; i--) {
            if (predicate.test(listModel.get(i))) {
                listModel.removeElementAt(i);
            }
        }
    }

    public static <T> ListCellRenderer<? super T> newListCellRenderer(
            Class<T> valueType, Function<T, String> textProvider) {
        return newListCellRendererForTextProvider(valueType, textProvider);
    }

    public static <T> ListCellRenderer<? super T> newListCellRenderer(
            Function<T, String> textProvider) {
        return newListCellRendererForTextProvider(textProvider);
    }

    public static void changeSelectedIndex(JList<?> list, int diff) {
        int size = list.getModel().getSize();
        if (size > 0) {
            int newIndex = limit(list.getSelectedIndex() + diff, size - 1);
            list.setSelectedIndex(newIndex);
        }
    }

    public static void ensureSelectionIsVisible(JList<?> list) {
        int selectedIndex = list.getSelectedIndex();
        if (selectedIndex >= 0) {
            list.ensureIndexIsVisible(selectedIndex);
        }
    }
    //endregion
    //region JScrollPane related
    public static JScrollPane scrolling(JComponent component, Consumer<JScrollPane> initCode) {
        JScrollPane jScrollPane = new JScrollPane(component);
        initCode.accept(jScrollPane);
        return jScrollPane;
    }

    //TODO: provide similar stuff as Widget
    public static JScrollPane scrollingNoBorder(JComponent component) {
        return scrolling(component, c -> c.setBorder(null));
    }

    //endregion
}
