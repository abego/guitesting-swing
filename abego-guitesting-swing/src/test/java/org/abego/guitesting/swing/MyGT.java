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

package org.abego.guitesting.swing;

import org.abego.commons.blackboard.Blackboard;
import org.abego.commons.seq.Seq;
import org.eclipse.jdt.annotation.Nullable;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelListener;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.abego.commons.lang.ClassUtil.resource;

/**
 * A collection of methods to test GT.
 *
 * <p>The current implementation uses GT itself in most of the methods.
 * This approach must be revised once "mutation testing" is used. So possibly
 * we have to "re-"implement a subset of GT's functionality.
 */
class MyGT {
    private static final GT gt2 = GuiTesting.newGT();

    static <T> void assertEqualsRetrying(T expected, Supplier<T> actualSupplier) {
        gt2.assertEqualsRetrying(expected, actualSupplier, null);
    }

    static void assertTrueRetrying(BooleanSupplier actualSupplier) {
        gt2.assertTrueRetrying(actualSupplier);
    }

    static Blackboard<Object> blackboard() {
        return gt2.blackboard();
    }

    static void runInEDT(Runnable runnable) {
        gt2.runInEDT(runnable);
    }

    private static JFrame showInFrame(Component component, Point position, @Nullable Dimension size) {
        return gt2.showInFrame(component, position, size);
    }

    private static JFrame showFrameTitled(String title, @Nullable Point position, @Nullable Dimension size) {
        return gt2.showInFrameTitled(title, null, position, size);
    }

    @Nullable
    static Component focusOwner() {
        return gt2.focusOwner();
    }

    static void setFocusOwner(Component component) {
        gt2.setFocusOwner(component);
    }

    static void cleanupMyGT() {
        gt2.cleanup();
    }

    @SuppressWarnings("SameParameterValue")
    static <T extends Component> T componentWith(Class<T> componentClass, Container container, Predicate<T> condition) {
        return gt2.componentWith(componentClass, container, condition);
    }

    static Seq<Window> allWindows() {
        return gt2.allWindows();
    }


    static void clickLeft(Component component) {
        gt2.clickLeft(component);
    }

    static JButton buttonNamed(String name) {
        return gt2.componentWith(JButton.class, c -> name.equals(c.getName()));
    }

    static JTextField textFieldNamed(String name) {
        return gt2.componentWith(JTextField.class, c -> name.equals(c.getName()));
    }

    @SuppressWarnings("SameParameterValue")
    static boolean hasWindowNamed(String name) {
        return gt2.hasWindowNamed(name);
    }

    static <T extends Window> T waitForWindowWith(Class<T> windowClass, Predicate<T> condition) {
        return gt2.waitForWindowWith(windowClass, condition);
    }

    private static void waitForIdle() {
        gt2.waitForIdle();
    }

    static JFrame showFrameWithColors() {
        Icon icon = new ImageIcon(resource(MyGT.class, "colors.png"));

        JButton button = new JButton(icon);

        JFrame result = showInFrame(button, new Point(100, 100), null);

        waitForIdle();

        return result;
    }


    static JTextField showFrameWithTextField() {
        // setup a text field to receive the keyboard input
        JTextField tf = new JTextField();
        tf.setColumns(20);

        showInFrame(tf, new Point(50, 50), null);

        return tf;
    }

    static JFrame showNameInputFrame() {
        JPanel panel = createNameInputPanel();
        JFrame result = showInFrame(panel, new Point(50, 50), null);
        result.setName("nameInput");
        return result;
    }

    private static JPanel createNameInputPanel() {
        JPanel panel = new JPanel();

        panel.add(createFirstnameField());
        panel.add(createLastNameField());
        panel.add(createOKButton());
        return panel;
    }

    static JButton createOKButton() {
        JButton btn = new JButton("OK");
        btn.setName("ok");
        return btn;
    }

    private static JTextField createLastNameField() {
        JTextField tf;
        tf = new JTextField();
        tf.setColumns(20);
        tf.setName("lastname");
        return tf;
    }

    private static JTextField createFirstnameField() {
        JTextField tf = new JTextField();
        tf.setColumns(20);
        tf.setName("firstname");
        return tf;
    }


    static JFrame showFrameForMouseTests() {
        final String frameName = "mouseTestsFrame";
        SwingUtilities.invokeLater(() -> {
            JButton button = new JButton("Click");

            JFrame frame = showInFrame(button,
                    new Point(50, 50),
                    new Dimension(200, 200));
            frame.addMouseListener(logMouseEventsToBlackboardWriter());
            frame.addMouseWheelListener(logMouseWheelEventsToBlackboardWriter());
            frame.setLayout(null);
            button.setLocation(100, 100);
            button.setSize(80, 30);
            button.revalidate();
            button.addMouseListener(logMouseEventsToBlackboardWriter());
            button.addMouseWheelListener(logMouseWheelEventsToBlackboardWriter());
            // set the name after all other initialization so we can synchronize
            // with the non-EDT thread's "waitForWindowWith" call (see below)
            frame.setName(frameName);
        });

        // Wait until the frame is completely setup and visible
        // so no clicks are lost.
        return waitForWindowWith(JFrame.class,
                f -> f.getName().equals(frameName) && f.isVisible());
    }

    static JFrame showFrameForDragTests() {

        JFrame frame = showFrameTitled("A Title",
                new Point(50, 50),
                new Dimension(400, 200));
        frame.addMouseListener(logMouseEventsToBlackboardWriter());

        return frame;
    }

    static void showFramesForWindowsTests() {

        showFrameTitled("TitleA",
                new Point(50, 50),
                new Dimension(200, 200));

        showFrameTitled("TitleAA",
                new Point(300, 50),
                new Dimension(200, 200));

        showFrameTitled("TitleB",
                new Point(550, 50),
                new Dimension(200, 200));

    }

    private static MouseAdapter logMouseEventsToBlackboardWriter() {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                blackboard().add(fixedMouseEventParamString(e));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                blackboard().add(fixedMouseEventParamString(e));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                blackboard().add(fixedMouseEventParamString(e));
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                blackboard().add(fixedMouseEventParamString(e));
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                blackboard().add(fixedMouseEventParamString(e));
            }
        };
    }

    private static MouseWheelListener logMouseWheelEventsToBlackboardWriter() {
        return e -> blackboard().add(fixedMouseEventParamString(e));
    }

    /**
     * For not yet explainable reasons the MouseEvent paramString sometimes
     * contains an ",extModifiers=..." entry and sometimes not, when running the
     * same test.
     * For now remove the "extModifiers" entry to get a reliable assertion.
     */
    private static String fixedMouseEventParamString(MouseEvent e) {
        String s = e.paramString();
        return s.replaceAll(",extModifiers=[^,]+", "");
    }


}