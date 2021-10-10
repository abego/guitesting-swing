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

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.abego.commons.lang.IntUtil.limit;
import static org.abego.guitesting.swing.internal.util.BorderedPanel.newBorderedPanel;
import static org.abego.guitesting.swing.internal.util.ListCellRendererForTextProvider.newListCellRendererForTextProvider;

public final class SwingUtil {
    public static final int DEFAULT_FLOW_GAP = 5;
    public final static Color LIGHTER_GRAY = new Color(0xd0d0d0);

    SwingUtil() {
        throw new MustNotInstantiateException();
    }

    //region Action related
    public static Action newAction(
            String text,
            KeyStroke accelerator,
            String description,
            ImageIcon smallIcon,
            Consumer<ActionEvent> action) {
        return ActionWithEventHandler.newAction(
                text, accelerator, description, smallIcon, action);
    }

    public static Action newAction(
            String text, KeyStroke accelerator, Consumer<ActionEvent> action) {
        return ActionWithEventHandler.newAction(
                text, accelerator, text, null, action);
    }

    public static Action newAction(
            String text,
            KeyStroke accelerator,
            ImageIcon smallIcon,
            Consumer<ActionEvent> action) {
        return ActionWithEventHandler.newAction(
                text, accelerator, text, smallIcon, action);
    }

    public static Action newAction(
            String text,
            KeyStroke accelerator,
            String description,
            Consumer<ActionEvent> action) {
        return ActionWithEventHandler.newAction(
                text, accelerator, description, null, action);
    }
    //endregion

    //region Icon related
    public static ImageIcon icon(File file) {
        try {
            return icon(file.toPath().toUri().toURL());
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static ImageIcon icon(URL url) {
        return new ImageIcon(url);
    }
    //endregion

    //region Border related
    public static Border lineBorder(Color borderColor, int thickness) {
        return BorderFactory.createLineBorder(borderColor, thickness);
    }
    //endregion

    //region Component related
    public static void setVisible(boolean value, Component... components) {
        for (Component component : components) {
            component.setVisible(value);
        }
    }

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

    public static JLabel label(String text) {
        return label(text, l -> {});
    }

    public static JLabel label() {
        return new JLabel();
    }
    //endregion

    //region JButton related
    public static JButton button(Action action, Consumer<JButton> initCode) {
        JButton button = new JButton(action);
        handleAccelerator(button, action);
        initCode.accept(button);
        return button;
    }

    public static JButton iconButton(Action action) {
        return button(action, b -> {
            b.setText(null);
            b.setBorder(new EmptyBorder(3, 3, 3, 3));
            b.setBackground(LIGHTER_GRAY);
            b.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    b.setOpaque(true);
                    b.repaint();
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    b.setOpaque(false);
                    b.repaint();
                }
            });
        });
    }
    //endregion

    //region JList related
    public static <T> JList<T> vlist(ListModel<T> listModel, Consumer<JList<T>> initCode) {
        JList<T> list = new JList<>(listModel);
        initCode.accept(list);
        return list;
    }

    public static <T> DefaultListModel<T> newDefaultListModel(Iterable<T> items) {
        DefaultListModel<T> listModel = new DefaultListModel<>();
        for (T i : items) {
            listModel.addElement(i);
        }
        return listModel;
    }

    public static <T> ListCellRenderer<T> newListCellRenderer(
            Class<T> valueType, Function<T, String> textProvider) {
        return newListCellRendererForTextProvider(valueType, textProvider);
    }

    public static void changeSelectedIndex(JList<?> list, int diff) {
        int size = list.getModel().getSize();
        if (size > 0) {
            int newIndex = limit(list.getSelectedIndex() + diff, size - 1);
            list.setSelectedIndex(newIndex);
        }
    }
    //endregion

    //region JScollPane related
    public static JScrollPane scrolling(JComponent component) {
        return new JScrollPane(component);
    }
    //endregion

    //region Special JComponents
    public static JComponent separatorBar() {
        JPanel jPanel = new JPanel(null);
        jPanel.setPreferredSize(new Dimension(1, 22));
        jPanel.setBackground(LIGHTER_GRAY);
        jPanel.setOpaque(true);

        return jPanel;
    }
    //endregion

    //region Layout related
    public static BorderedPanel bordered() {
        return newBorderedPanel();
    }

    public static JComponent flow(int align, int hgap, int vgap, Consumer<JPanel> initCode, JComponent... components) {
        JPanel result = new JPanel(new FlowLayout(align, hgap, vgap));
        initCode.accept(result);
        addAll(result, components);
        return result;
    }

    public static JComponent flowLeft(int hgap, int vgap, Consumer<JPanel> initCode, JComponent... components) {
        return flow(FlowLayout.LEADING, hgap, vgap, initCode, components);
    }

    public static JComponent flowLeft(int hgap, int vgap, JComponent... components) {
        return flowLeft(hgap, vgap, p -> {}, components);
    }

    public static JComponent flowLeft(Consumer<JPanel> initCode, JComponent... components) {
        return flowLeft(DEFAULT_FLOW_GAP, DEFAULT_FLOW_GAP, initCode, components);
    }

    public static JComponent flowLeft(JComponent... components) {
        return flowLeft(DEFAULT_FLOW_GAP, DEFAULT_FLOW_GAP, components);
    }

    public static JComponent flowLeftWithBottomLine(JComponent... components) {
        return flowLeft(DEFAULT_FLOW_GAP, DEFAULT_FLOW_GAP,
                l -> l.setBorder(new MatteBorder(0, 0, 1, 0, LIGHTER_GRAY)),
                components);
    }
    //endregion

}
