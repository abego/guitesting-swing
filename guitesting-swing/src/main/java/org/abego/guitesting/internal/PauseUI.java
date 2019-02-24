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

package org.abego.guitesting.internal;


import javax.annotation.Nullable;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JWindow;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.text.MessageFormat;

import static org.abego.guitesting.internal.SwingUtil.iconFromResource;
import static org.abego.guitesting.internal.SwingUtil.screenBounds;


public final class PauseUI {

    public static final String PAUSE_WINDOW_NAME = "PauseWindow"; //NON-NLS
    public static final String CONTINUE_BUTTON_NAME = "continueButton"; //NON-NLS
    public static final String MESSAGE_FIELD_NAME = "messageField"; //NON-NLS

    private static final PauseUI instance = new PauseUI();
    private volatile boolean waitingForUser;
    private PauseUIWindow pauseUIWindow;
    private String message;

    private PauseUI() {
    }

    static PauseUI pauseUI() {
        return instance;
    }

    void showPauseWindowAndWaitForUser(@Nullable String message) {
        this.message = message;

        setAndShowPauseWindow(new PauseUIWindow(message));

        waitForUser();

        pauseUIWindow.dispose();
        pauseUIWindow = null;
    }

    private void setAndShowPauseWindow(PauseUIWindow window) {
        Window oldWaitingWindow;

        synchronized (pauseUI()) {
            oldWaitingWindow = pauseUIWindow;
            pauseUIWindow = window;
            pauseUIWindow.setVisible(true);
        }

        if (oldWaitingWindow != null) {
            oldWaitingWindow.dispose();
        }
    }

    private void waitForUser() {
        waitingForUser = true;

        // check the state of the window every 1/10 second.
        do {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e1) {
                Thread.currentThread().interrupt();
                waitingForUser = false;
            }
        } while (waitingForUser);
    }

    private class PauseUIWindow extends JWindow {
        private final JButton button = new JButton();
        private final JTextField textField = new JTextField();
        private final JPanel panel = new JPanel();


        PauseUIWindow(@Nullable String message) {
            setName(PAUSE_WINDOW_NAME);
            setAlwaysOnTop(true);

            initComponents();
            styleComponents();
            layoutComponents();

            // when no message is given just display the button,
            // otherwise the panel with button and text(field)
            boolean noMessage = message == null || message.isEmpty();
            getContentPane().add(noMessage ? button : panel);

            pack();

            Point pos = locationOnScreen();
            setLocation(pos.x, pos.y);
        }

        private PauseUIWindow getWaitWindow() {
            return this;
        }

        private void initComponents() {
            button.setName(CONTINUE_BUTTON_NAME);
            button.setIcon(iconFromResource(PauseUI.class, "continue.png")); //NON-NLS
            button.setToolTipText("Click here to continue"); //NON-NLS
            button.addActionListener(e -> {
                getWaitWindow().setVisible(false);
                waitingForUser = false;
            });

            textField.setName(MESSAGE_FIELD_NAME);
            textField.setEditable(false);
            textField.setText(MessageFormat.format("      {0}        ", message != null ? message : ""));
            textField.setBackground(Color.white);
        }

        private void styleComponents() {
            button.setMargin(new Insets(0, 0, 0, 0));
            button.setBorder(BorderFactory.createEmptyBorder());
        }

        private void layoutComponents() {
            panel.setLayout(new BorderLayout());
            //noinspection AbsoluteAlignmentInUserInterface
            panel.add(button, BorderLayout.WEST);
            panel.add(textField, BorderLayout.CENTER);
        }

        private Point locationOnScreen() {
            Rectangle bounds = screenBounds();
            return new Point(bounds.width / 2 - 100, bounds.y);
        }
    }
}
