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

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

import static org.abego.guitesting.swing.internal.util.BorderedPanel.newBorderedPanel;

//TODO: move methods to commons
public class Util {

    public static void copyFile(File source, File destination) {
        try {
            Files.copy(
                    source.toPath(),
                    destination.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Action newAction(String text, KeyStroke accelerator, Consumer<ActionEvent> action) {
        return MyAction.newAction(text, accelerator, action);
    }

    public static JLabel labelWithBorder(String title, Color color, int borderSize) {
        JLabel label = new JLabel(title);
        label.setBorder(BorderFactory.createLineBorder(color, borderSize));
        return label;
    }

    public static JLabel label() {
        return new JLabel();
    }

    public static JButton button(Action action) {
        JButton button = new JButton(action);
        Object accelearator = action.getValue(Action.ACCELERATOR_KEY);
        if (accelearator instanceof KeyStroke) {
            Object key = new Object();
            button.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    (KeyStroke) accelearator, key);
            button.getActionMap().put(key, action);
        }
        return button;
    }

    public static JComponent scrolling(JComponent component) {
        return new JScrollPane(component);
    }


    public static BorderedPanel bordered() {
        return newBorderedPanel();
    }

    public static JComponent flowLeft(JComponent... components) {
        JPanel result = new JPanel(new FlowLayout(FlowLayout.LEADING));
        for (JComponent component : components) {
            result.add(component);
        }
        return result;
    }

    public static Border lineBorder(Color borderColor, int thickness) {
        return BorderFactory.createLineBorder(borderColor, thickness);
    }

    public static ImageIcon loadIcon(File file) {
        try {
            return new ImageIcon(file.toPath().toUri().toURL());
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("serial")
    private static class MyAction extends AbstractAction {
        private final Consumer<ActionEvent> action;

        private MyAction(String text, KeyStroke accelerator, Consumer<ActionEvent> eventHandler) {
            super(text, null);
            this.action = eventHandler;
            putValue(ACCELERATOR_KEY, accelerator);
        }

        public static Action newAction(String text, KeyStroke accelerator, Consumer<ActionEvent> action) {
            return new MyAction(text, accelerator, action);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            action.accept(e);
        }
    }
}
