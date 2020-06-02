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

package org.abego.guitesting;

import static java.lang.Integer.min;
import static javax.swing.SwingUtilities.invokeLater;
import static org.abego.commons.io.PrintStreamToBuffer.newPrintStreamToBuffer;
import static org.abego.commons.lang.SystemUtil.systemOutRedirect;
import static org.abego.commons.lang.ThreadUtil.sleep;
import static org.abego.commons.seq.SeqUtil.newSeq;
import static org.abego.guitesting.MyGT.allWindows;
import static org.abego.guitesting.MyGT.assertEqualsRetrying;
import static org.abego.guitesting.MyGT.assertTrueRetrying;
import static org.abego.guitesting.MyGT.blackboard;
import static org.abego.guitesting.MyGT.buttonNamed;
import static org.abego.guitesting.MyGT.cleanupMyGT;
import static org.abego.guitesting.MyGT.clickLeft;
import static org.abego.guitesting.MyGT.componentWith;
import static org.abego.guitesting.MyGT.createOKButton;
import static org.abego.guitesting.MyGT.focusOwner;
import static org.abego.guitesting.MyGT.hasWindowNamed;
import static org.abego.guitesting.MyGT.runInEDT;
import static org.abego.guitesting.MyGT.setFocusOwner;
import static org.abego.guitesting.MyGT.showFrameForDragTests;
import static org.abego.guitesting.MyGT.showFrameForMouseTests;
import static org.abego.guitesting.MyGT.showFrameWithColors;
import static org.abego.guitesting.MyGT.showFrameWithTextField;
import static org.abego.guitesting.MyGT.showFramesForWindowsTests;
import static org.abego.guitesting.MyGT.showNameInputFrame;
import static org.abego.guitesting.MyGT.textFieldNamed;
import static org.abego.guitesting.MyGT.waitForWindowWith;
import static org.abego.guitesting.internal.SwingUtil.isBlueish;
import static org.abego.guitesting.internal.SwingUtil.isGreenish;
import static org.abego.guitesting.internal.SwingUtil.isRedish;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.abego.commons.blackboard.Blackboard;
import org.abego.commons.io.PrintStreamToBuffer;
import org.abego.commons.lang.RunOnClose;
import org.abego.commons.seq.Seq;
import org.abego.commons.timeout.TimeoutUncheckedException;
import org.abego.guitesting.internal.PauseUI;

/**
 * Test GT
 */
class GTTest {
    private final static GT gt = GuiTesting.newGT();

    private static String head(int count, String text) {
        String[] lines = text.split("\\r?\\n");
        String[] headLines = Arrays.copyOfRange(lines, 0, min(count, lines.length));
        return String.join("\n", headLines);
    }

    private static void async(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.start();
    }

    private Supplier<String> firstTwoLinesOfBlackboard() {
        return () -> head(2, blackboard().text());
    }

    private Supplier<String> firstFiveLinesOfBlackboard() {
        return () -> head(5, blackboard().text());
    }

    @AfterEach
    void tearDown() {
        gt.cleanup();

        cleanupMyGT();
    }

    @Test
    void guiTesting_ok() {

        GT gt = GuiTesting.newGT();

        assertNotNull(gt);
    }

    @Test
    void guiTesting_headless_fails() {

        String key = "java.awt.headless";
        String oldValue = System.getProperty(key);

        try {
            System.setProperty(key, "true");

            GuiTesting.newGT();

        } finally {
            if (oldValue != null)
                System.setProperty(key, oldValue);
            else
                System.clearProperty(key);
        }
    }

    @Test
    void assertEqualsRetrying_noMessage_ok() {

        final String[] s = {""};

        // Change the value to true "later"
        async(() -> {
            sleep(10);
            s[0] = "foo";
        });

        gt.assertEqualsRetrying("foo", () -> s[0]);
    }

    @Test
    void assertEqualsRetrying_noMessage_timeouts() {

        AssertionError ex = assertThrows(AssertionError.class, () -> {
            gt.setTimeout(Duration.ofMillis(1));
            gt.assertEqualsRetrying("foo", () -> "bar");
        });

        assertEquals("[Timeout]  ==> expected: <foo> but was: <bar>", ex.getMessage());
    }

    @Test
    void assertEqualsRetrying_withMessage_timeouts() {

        AssertionError ex = assertThrows(AssertionError.class, () -> {
            gt.setTimeout(Duration.ofMillis(1));
            gt.assertEqualsRetrying("foo", () -> "bar", "MyMessage");
        });

        assertEquals("[Timeout] MyMessage ==> expected: <foo> but was: <bar>", ex.getMessage());
    }

    @Test
    void assertTrueRetrying_noMessage_ok() {

        final boolean[] b = {false};

        // Change the value to true "later"
        async(() -> {
            sleep(10);
            b[0] = true;
        });

        gt.assertTrueRetrying(() -> b[0]);
    }

    @Test
    void assertTrueRetrying_noMessage_timeout() {
        AssertionError ex = assertThrows(AssertionError.class, () -> {
            gt.setTimeout(Duration.ofMillis(1));
            gt.assertTrueRetrying(() -> false);
        });

        assertEquals("[Timeout]  ==> expected: <true> but was: <false>", ex.getMessage());
    }

    @Test
    void assertTrueRetrying_withMessage_timeout() {

        AssertionError ex = assertThrows(AssertionError.class, () -> {
            gt.setTimeout(Duration.ofMillis(1));
            gt.assertTrueRetrying(() -> false, "MyMessage");
        });

        assertEquals("[Timeout] MyMessage ==> expected: <true> but was: <false>", ex.getMessage());
    }

    @Test
    void blackboard_ok() {
        Blackboard<Object> bb = gt.blackboard();

        assertTrue(bb.isEmpty());

        bb.add("SampleA");
        bb.add("SampleB");

        // isEmpty
        assertFalse(bb.isEmpty());

        // text
        assertEquals("SampleA\nSampleB", bb.text());

        // contains
        assertTrue(bb.contains("SampleA"));
        assertTrue(bb.contains("SampleB"));
        assertFalse(bb.contains("SampleC"));

        // containsItemWith
        assertTrue(bb.containsItemWith(i -> i.toString().equals("SampleB"))); // 1
        assertTrue(bb.containsItemWith(i -> i.toString().contains("ample"))); // 2
        assertFalse(bb.containsItemWith(i -> i.toString().contains("Error"))); // 0

        // items
        Seq<Object> items = bb.items();
        assertEquals(2, items.size());
        assertEquals("SampleA", items.item(0));
        assertEquals("SampleB", items.item(1));

        // itemWith
        assertEquals("SampleA", bb.itemWith(i -> i.toString().endsWith("A")));
        assertThrows(NoSuchElementException.class, () -> bb.itemWith(i -> i.toString().endsWith("C")));
        assertEquals("SampleB", bb.itemWith(i -> i.toString().startsWith("Sample")));

        // itemWithOrNull
        assertEquals("SampleA", bb.itemWithOrNull(i -> i.toString().endsWith("A")));
        assertNull(bb.itemWithOrNull(i -> i.toString().endsWith("C")));
        assertEquals("SampleB", bb.itemWithOrNull(i -> i.toString().startsWith("Sample")));

        // clear
        bb.clear();
        assertTrue(bb.isEmpty());

    }

    @Test
    void allComponentsWith_ok() {
        // no components
        assertEquals(0, gt.allComponentsWith(Component.class, c -> true).size());

        // one component (1 textfield)
        showFrameWithTextField();
        assertEquals(1, gt.allComponentsWith(Component.class, c -> c instanceof JTextComponent).size());

        // more components (1 + 2 textfields)
        showNameInputFrame();
        assertEquals(3, gt.allComponentsWith(Component.class, c -> c instanceof JTextComponent).size());
    }

    @Test
    void allComponentsWith_ComponentSeq_ok() {
        JFrame frame = showNameInputFrame();

        Seq<JTextComponent> jTextComponents = gt.allComponentsWith(JTextComponent.class,
                newSeq(frame.getComponents()), c -> true);
        assertEquals(2, jTextComponents.size());
    }

    @Test
    void allComponentsWith_Component_ok() {
        JFrame frame = showNameInputFrame();

        assertEquals(2, gt.allComponentsWith(JTextComponent.class, frame, c -> true).size());
    }

    @Test
    void hasComponentWith_ComponentSeq_ok() {
        JFrame frame = showNameInputFrame();

        boolean found = gt.hasComponentWith(JTextField.class, newSeq(frame.getComponents()), c -> c.getName().equals("firstname"));

        assertTrue(found);
    }

    @Test
    void hasComponentWith_Component_singleComponent_ok() {
        JFrame frame = showNameInputFrame();

        boolean found = gt.hasComponentWith(JTextField.class, frame, c -> c.getName().equals("firstname"));

        assertTrue(found);
    }

    @Test
    void hasComponentWith_singleComponent_ok() {
        showNameInputFrame();

        boolean found = gt.hasComponentWith(JTextField.class, c -> c.getName().equals("firstname"));

        assertTrue(found);
    }

    @Test
    void hasComponentWith_multipleComponents_fails() {
        showNameInputFrame();

        boolean found = gt.hasComponentWith(JTextField.class, c -> true);

        assertTrue(found);
    }

    @Test
    void hasComponentWith_noComponent_fails() {
        boolean found = gt.hasComponentWith(JTextField.class, c -> false);

        assertFalse(found);
    }

    @Test
    void componentWith_Component_ok() {
        JFrame frame = showNameInputFrame();

        JTextField tf = gt.componentWith(JTextField.class, frame, c -> c.getName().equals("firstname"));

        assertEquals("firstname", tf.getName());
    }

    @Test
    void componentWith_ComponentSeq_ok() {
        JFrame frame = showNameInputFrame();

        JTextField tf = gt.componentWith(JTextField.class, newSeq(frame.getComponents()), c -> c.getName().equals("firstname"));

        assertEquals("firstname", tf.getName());
    }

    @Test
    void componentWith_singleComponent_ok() {
        showNameInputFrame();

        JTextField tf = gt.componentWith(JTextField.class, c -> c.getName().equals("firstname"));

        assertEquals("firstname", tf.getName());
    }

    @Test
    void componentNamed_ok() {
        showNameInputFrame();

        JTextField tf = gt.componentNamed(JTextField.class, "firstname");

        assertEquals("firstname", tf.getName());
    }

    @Test
    void waitForComponentNamed_ok() {
        invokeLater(MyGT::showNameInputFrame);

        JTextField tf = gt.waitForComponentNamed(JTextField.class, "firstname");

        assertEquals("firstname", tf.getName());
    }

    @Test
    void waitForComponentNamed_fail() {
        invokeLater(MyGT::showNameInputFrame);

        gt.waitForWindowNamed("nameInput");
        gt.setTimeoutMillis(1); // don't wait too long, the field will never appear...
        AssertionError e = assertThrows(AssertionError.class,
                () -> gt.waitForComponentNamed(JTextField.class, "unknown"));

        assertEquals("Error when looking for javax.swing.JTextField named 'unknown': org.abego.commons.timeout.TimeoutUncheckedException", e.getMessage());
    }

    @Test
    void waitForWindowNamed_ok() {
        invokeLater(() -> {
            JFrame frame = showNameInputFrame();
            frame.setName("MyFrame");
        });

        Window window = gt.waitForWindowNamed("MyFrame");

        assertEquals("MyFrame", window.getName());
    }

    @Test
    void hasComponentNamed_ok() {
        showNameInputFrame();

        assertTrue(gt.hasComponentNamed(JTextField.class, "firstname"));
        assertFalse(gt.hasComponentNamed(JTextField.class, "missingField"));
    }

    @Test
    void componentWith_multipleComponents_fails() {
        showNameInputFrame();

        // There are two textfields in the frame, resulting in an exception
        assertThrows(NoSuchElementException.class, () ->
                gt.componentWith(JTextField.class, c -> true));
    }

    @Test
    void componentWith_noComponent_fails() {
        assertThrows(NoSuchElementException.class, () ->
                gt.componentWith(JTextField.class, c -> false));
    }

    @Test
    void anyComponentWith_Component_ok() {
        JFrame frame = showNameInputFrame();

        JTextField tf = gt.anyComponentWith(JTextField.class, frame, c -> c.getName().equals("firstname"));

        assertEquals("firstname", tf.getName());
    }

    @Test
    void anyComponentWith_ComponentSeq_ok() {
        JFrame frame = showNameInputFrame();

        JTextField tf = gt.anyComponentWith(JTextField.class, newSeq(frame.getComponents()), c -> c.getName().equals("firstname"));

        assertEquals("firstname", tf.getName());
    }

    @Test
    void anyComponentWith_singleComponent_ok() {
        showNameInputFrame();

        JTextField tf = gt.anyComponentWith(JTextField.class, c -> c.getName().equals("firstname"));

        assertEquals("firstname", tf.getName());
    }


    @Test
    void anyComponentWith_multipleComponents_fails() {
        showNameInputFrame();

        JTextField tf = gt.anyComponentWith(JTextField.class, c -> true);

        // There are two textfields in the frame,
        assertTrue(tf.getName().equals("firstname")
                || tf.getName().equals("lastname"));
    }

    @Test
    void anyComponentWith_noComponent_fails() {
        assertThrows(NoSuchElementException.class, () ->
                gt.anyComponentWith(JTextField.class, c -> false));
    }

    @Test
    void showInDialog_ok() {
        JButton btn = createOKButton();

        async(() -> gt.showInDialog(btn));

        JDialog dlg = waitForWindowWith(JDialog.class, w -> true);

        assertEquals(btn, dlg.getContentPane().getComponent(0));
    }


    @Test
    void showInDialog_ESCClosesDialog_ok() {
        JButton btn = createOKButton();

        async(() -> gt.showInDialog(btn));

        waitForWindowWith(JDialog.class, w -> true);

        gt.typeKeycode(KeyEvent.VK_ESCAPE);

        gt.poll(MyGT::allWindows, Seq::isEmpty);
    }

    @Test
    void showInDialogTitled_ok() {
        JButton btn = createOKButton();

        async(() -> gt.showInDialogTitled("Sample", btn));

        JDialog dlg = waitForWindowWith(JDialog.class, w -> true);

        assertEquals("Sample", dlg.getTitle());
        assertEquals(btn, dlg.getContentPane().getComponent(0));
    }

    @Test
    void showInFrame_ok() {
        JButton btn = createOKButton();

        async(() -> gt.showInFrame(btn));

        JFrame frame = waitForWindowWith(JFrame.class, w -> true);

        assertEquals(btn, frame.getContentPane().getComponent(0));
    }

    @Test
    void showInFrame_withPositionSize_ok() {
        JButton btn = createOKButton();

        Point pos = new Point(50, 60);
        Dimension size = new Dimension(200, 100);

        async(() -> gt.showInFrame(btn, pos, size));

        JFrame frame = waitForWindowWith(JFrame.class, w -> true);

        assertEquals(pos, frame.getLocation());
        assertEquals(size, frame.getSize());
        assertEquals(btn, frame.getContentPane().getComponent(0));
    }

    @Test
    void showInFrameTitled_ok() {
        JButton btn = createOKButton();

        async(() -> gt.showInFrameTitled("Sample", btn));

        JFrame frame = waitForWindowWith(JFrame.class, w -> true);

        assertEquals("Sample", frame.getTitle());
        assertEquals(btn, frame.getContentPane().getComponent(0));
    }

    @Test
    void showInFrameTitled_withPositionSize_ok() {
        JButton btn = createOKButton();

        Point pos = new Point(50, 60);
        Dimension size = new Dimension(200, 100);

        async(() -> gt.showInFrameTitled("Sample", btn, pos, size));

        JFrame frame = waitForWindowWith(JFrame.class, w -> true);

        assertEquals("Sample", frame.getTitle());
        assertEquals(pos, frame.getLocation());
        assertEquals(size, frame.getSize());
        assertEquals(btn, frame.getContentPane().getComponent(0));
    }

    @Test
    void runInEDT_withRunnable_ok() {

        gt.runInEDT(() -> {
            if (SwingUtilities.isEventDispatchThread())
                blackboard().add(1);
        });

        assertTrueRetrying(() -> blackboard().contains(1));
    }

    @Test
    void runInEDT_withActionListener_ok() {

        ActionListener a = e -> {
            if (SwingUtilities.isEventDispatchThread() &&
                    e.getID() == ActionEvent.ACTION_PERFORMED &&
                    e.getActionCommand().equals("")) {
                blackboard().add(1);
            }
        };

        gt.runInEDT(a);

        assertTrueRetrying(() -> blackboard().contains(1));
    }

    @Test
    void runInEDT_withActionListener_andActionEvent_ok() {
        final boolean[] b = {false};

        String actionCommand = "foo";
        ActionEvent evt = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, actionCommand);
        ActionListener a = e -> {
            if (SwingUtilities.isEventDispatchThread() &&
                    e.getID() == ActionEvent.ACTION_PERFORMED &&
                    e.getActionCommand().equals(actionCommand)) {
                b[0] = true;
            }
        };

        gt.runInEDT(a, evt);

        assertTrueRetrying(() -> b[0]);
    }

    @Test
    void isEDT_ok() {
        assertFalse(gt.isEDT());

        runInEDT(() -> blackboard().add("isEDT: " + gt.isEDT()));

        assertEquals("isEDT: true", blackboard().text());
    }

    @Test
    void focusOwner_null_ok() {
        assertNull(gt.focusOwner());
    }

    @Test
    void focusOwner_ok() {
        JTextField tf = showFrameWithTextField();

        setFocusOwner(tf);

        assertEquals(tf, gt.focusOwner());
    }

    @Test
    void waitUntilAnyFocus_ok() {
        JTextField tf = showFrameWithTextField();

        async(() -> {
            blackboard().add("pausing");
            gt.waitUntilAnyFocus();
            blackboard().add("pause ended");
        });

        // wait for the pause
        assertEqualsRetrying("pausing", blackboard()::text);

        setFocusOwner(tf);

        assertEqualsRetrying("pausing\npause ended", blackboard()::text);
    }

    @Test
    void waitUntilInFocus_ok() {
        showNameInputFrame();

        JTextField lastName = textFieldNamed("lastname");

        async(() -> {
            blackboard().add("pausing");

            gt.waitUntilInFocus(lastName);

            blackboard().add("pause ended");
        });

        // wait for the pause
        assertEqualsRetrying("pausing", blackboard()::text);

        setFocusOwner(lastName);

        assertEqualsRetrying("pausing\npause ended", blackboard()::text);
    }

    @Test
    void setFocusOwner_ok() {
        JTextField tf = showFrameWithTextField();

        gt.setFocusOwner(tf);

        assertEquals(tf, focusOwner());
    }

    @Test
    void focusNext_ok() {
        showNameInputFrame();

        setFocusOwner(textFieldNamed("firstname"));

        gt.focusNext();

        assertEquals(textFieldNamed("lastname"), focusOwner());

        gt.focusNext();

        assertEquals(buttonNamed("ok"), focusOwner());
    }

    @Test
    void focusPrevious_ok() {
        showNameInputFrame();

        setFocusOwner(buttonNamed("ok"));

        gt.focusPrevious();

        assertEquals(textFieldNamed("lastname"), focusOwner());

        gt.focusPrevious();

        assertEquals(textFieldNamed("firstname"), focusOwner());
    }

    @Test
    void runWithFocusIn_ok() {

        JTextField tf = showFrameWithTextField();

        gt.runWithFocusIn(tf, () -> {
            if (tf.hasFocus())
                blackboard().add("tf has focus");
        });

        assertEquals("tf has focus", blackboard().text());
    }

    @Test
    void keyPress_ok() {

        JTextField tf = showFrameWithTextField();

        // add a listener to check if the key was really pressed
        tf.addKeyListener(logKeyEventsToBlackboardAdapter());

        // move the focus to the textfield
        setFocusOwner(tf);


        // press the "a" key
        gt.keyPress(KeyEvent.VK_A);

        // Check if the correct key event was generated
        assertEqualsRetrying("keyPressed: 65", blackboard()::text);
    }

    @Test
    void keyRelease_ok() {

        JTextField tf = showFrameWithTextField();

        tf.addKeyListener(logKeyEventsToBlackboardAdapter());
        setFocusOwner(tf);

        // (press and) release the "b" key
        // (Just calling keyRelease will not generate any event.
        // You cannot release a button that was not yet pressed.)
        gt.keyPress(KeyEvent.VK_B);
        gt.keyRelease(KeyEvent.VK_B);

        assertEqualsRetrying("keyPressed: 66\n" +
                "keyReleased: 66", blackboard()::text);
    }

    @Test
    void type_ok() {
        JTextField tf = showFrameWithTextField();
        setFocusOwner(tf);

        gt.type("Hello Jörg<3> & Goodbye!");

        assertEqualsRetrying("Hello Jörg<3> & Goodbye!", tf::getText);
    }

    @Test
    void typeKeycode_ok() {

        JTextField tf = showFrameWithTextField();
        setFocusOwner(tf);

        gt.typeKeycode(KeyEvent.VK_A);
        gt.typeKeycode(KeyEvent.VK_B);
        gt.typeKeycode(KeyEvent.VK_C);

        assertEqualsRetrying("abc", tf::getText);
    }

    @Test
    void releaseAllKeys_ok() {
        JTextField tf = showFrameWithTextField();

        // add a listener to check if the key was really pressed
        tf.addKeyListener(logKeyEventsToBlackboardAdapter());

        setFocusOwner(tf);

        gt.keyPress(KeyEvent.VK_H);

        gt.releaseAllKeys();

        assertEqualsRetrying("keyPressed: 72\n" +
                "keyReleased: 72", blackboard()::text);
    }

    @Test
    void mouseMovePressRelease_ok() {
        showFrameForMouseTests();

        int buttonsMask = InputEvent.BUTTON1_DOWN_MASK;

        gt.mouseMove(100, 100);
        gt.mousePress(buttonsMask);
        gt.mouseRelease(buttonsMask);

        assertEqualsRetrying(
                "MOUSE_PRESSED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=1",
                blackboard()::text);
    }

    @Test
    void mouseMovePressReleaseTwice_ok() {
        showFrameForMouseTests();

        int buttonsMask = InputEvent.BUTTON1_DOWN_MASK;

        gt.mouseMove(100, 100);
        gt.mousePress(buttonsMask);
        gt.mouseRelease(buttonsMask);

        gt.mouseMove(110, 120);
        gt.mousePress(buttonsMask);
        gt.mouseRelease(buttonsMask);
        assertEqualsRetrying(
                "MOUSE_PRESSED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_PRESSED,(60,70),absolute(110,120),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(60,70),absolute(110,120),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(60,70),absolute(110,120),button=1,modifiers=Button1,clickCount=1",
                blackboard()::text);
    }

    @Test
    void mouseWheel_ok() {
        showFrameForMouseTests();

        gt.mouseMove(100, 100);
        gt.mouseWheel(100);
        gt.mouseWheel(-200);

        assertEqualsRetrying(
                "MOUSE_WHEEL,(50,50),absolute(0,0),button=0,clickCount=0,scrollType=WHEEL_UNIT_SCROLL,scrollAmount=1,wheelRotation=-100,preciseWheelRotation=-100.0\n" +
                        "MOUSE_WHEEL,(50,50),absolute(0,0),button=0,clickCount=0,scrollType=WHEEL_UNIT_SCROLL,scrollAmount=1,wheelRotation=200,preciseWheelRotation=200.0",
                blackboard()::text);
    }

    @Test
    void click_ok() {
        showFrameForMouseTests();

        int buttonsMask = InputEvent.BUTTON1_DOWN_MASK;

        gt.click(buttonsMask, 100, 100, 1);

        gt.click(buttonsMask, 110, 120, 1);

        assertEqualsRetrying(
                "MOUSE_PRESSED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_PRESSED,(60,70),absolute(110,120),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(60,70),absolute(110,120),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(60,70),absolute(110,120),button=1,modifiers=Button1,clickCount=1",
                blackboard()::text);
    }

    @Test
    void click_Point_ok() {
        showFrameForMouseTests();

        int buttonsMask = InputEvent.BUTTON1_DOWN_MASK;

        gt.click(buttonsMask, new Point(100, 100), 1);

        gt.click(buttonsMask, new Point(110, 120), 1);

        assertEqualsRetrying(
                "MOUSE_PRESSED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_PRESSED,(60,70),absolute(110,120),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(60,70),absolute(110,120),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(60,70),absolute(110,120),button=1,modifiers=Button1,clickCount=1",
                blackboard()::text);
    }

    @Test
    void click_wrongClickcount_fails() {
        assertThrows(IllegalArgumentException.class, () ->
                gt.click(InputEvent.BUTTON1_DOWN_MASK, 100, 100, 0));
    }

    @Test
    void click_separateClicksAreNoDoubleclick_ok() {
        showFrameForMouseTests();

        gt.clickLeft(100, 100);
        gt.clickLeft(100, 100);
        gt.clickLeft(100, 101);
        gt.clickLeft(101, 101);
        gt.clickLeft(102, 102);

        assertEqualsRetrying(
                "MOUSE_PRESSED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_PRESSED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_PRESSED,(50,51),absolute(100,101),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(50,51),absolute(100,101),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(50,51),absolute(100,101),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_PRESSED,(51,51),absolute(101,101),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(51,51),absolute(101,101),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(51,51),absolute(101,101),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_PRESSED,(52,52),absolute(102,102),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(52,52),absolute(102,102),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(52,52),absolute(102,102),button=1,modifiers=Button1,clickCount=1",
                blackboard()::text);
    }

    @Test
    void click_doubleclick_ok() {
        showFrameForMouseTests();

        int buttonsMask = InputEvent.BUTTON1_DOWN_MASK;

        gt.click(buttonsMask, 100, 100, 2);

        assertEqualsRetrying(
                "MOUSE_PRESSED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_PRESSED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=2\n" +
                        "MOUSE_RELEASED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=2\n" +
                        "MOUSE_CLICKED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=2",
                blackboard()::text);
    }

    @Test
    void click_doubleclick_Point_ok() {
        showFrameForMouseTests();

        int buttonsMask = InputEvent.BUTTON1_DOWN_MASK;

        gt.click(buttonsMask, new Point(100, 100), 2);

        assertEqualsRetrying(
                "MOUSE_PRESSED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_PRESSED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=2\n" +
                        "MOUSE_RELEASED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=2\n" +
                        "MOUSE_CLICKED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=2",
                blackboard()::text);
    }

    @Test
    void click_Component_withClickCount_ok() {
        JFrame frame = showFrameForMouseTests();

        Component comp = componentWith(JButton.class, frame, c -> true);

        gt.click(InputEvent.BUTTON1_DOWN_MASK, comp, 10, 10, 1);
        gt.click(InputEvent.BUTTON1_DOWN_MASK, comp, 40, 15, 1);

        assertEqualsRetrying(
                "MOUSE_PRESSED,(10,10),absolute(160,160),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(10,10),absolute(160,160),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(10,10),absolute(160,160),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_PRESSED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1",
                blackboard()::text);
    }

    @Test
    void click_Component_withClickCount_Point_ok() {
        JFrame frame = showFrameForMouseTests();

        Component comp = componentWith(JButton.class, frame, c -> true);

        gt.click(InputEvent.BUTTON1_DOWN_MASK, comp, new Point(10, 10), 1);
        gt.click(InputEvent.BUTTON1_DOWN_MASK, comp, new Point(40, 15), 1);

        assertEqualsRetrying(
                "MOUSE_PRESSED,(10,10),absolute(160,160),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(10,10),absolute(160,160),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(10,10),absolute(160,160),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_PRESSED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1",
                blackboard()::text);
    }

    @Test
    void clickLeft_withClickCount_ok() {
        showFrameForMouseTests();

        gt.clickLeft(100, 100, 1);

        gt.clickLeft(110, 120, 1);

        assertEqualsRetrying(
                "MOUSE_PRESSED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_PRESSED,(60,70),absolute(110,120),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(60,70),absolute(110,120),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(60,70),absolute(110,120),button=1,modifiers=Button1,clickCount=1",
                blackboard()::text);
    }

    @Test
    void clickLeft_withClickCount_Point_ok() {
        showFrameForMouseTests();

        gt.clickLeft(new Point(100, 100), 1);

        gt.clickLeft(new Point(110, 120), 1);

        assertEqualsRetrying(
                "MOUSE_PRESSED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_PRESSED,(60,70),absolute(110,120),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(60,70),absolute(110,120),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(60,70),absolute(110,120),button=1,modifiers=Button1,clickCount=1",
                blackboard()::text);
    }

    @Test
    void clickLeft_ok() {
        showFrameForMouseTests();

        gt.clickLeft(100, 100);

        gt.clickLeft(110, 120);

        assertEqualsRetrying(
                "MOUSE_PRESSED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(50,50),absolute(100,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_PRESSED,(60,70),absolute(110,120),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(60,70),absolute(110,120),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(60,70),absolute(110,120),button=1,modifiers=Button1,clickCount=1",
                blackboard()::text);
    }

    @Test
    void clickLeft_Point_ok() {
        showFrameForMouseTests();

        gt.clickLeft(new Point(200, 100));

        gt.clickLeft(new Point(110, 120));

        assertEqualsRetrying(
                "MOUSE_PRESSED,(150,50),absolute(200,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(150,50),absolute(200,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(150,50),absolute(200,100),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_PRESSED,(60,70),absolute(110,120),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(60,70),absolute(110,120),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(60,70),absolute(110,120),button=1,modifiers=Button1,clickCount=1",
                blackboard()::text);
    }

    @Test
    void clickRight_withClickCount_ok() {
        showFrameForMouseTests();

        gt.clickRight(100, 100, 1);

        gt.clickRight(110, 120, 1);


        assertEqualsRetrying(
                "MOUSE_PRESSED,(50,50),absolute(100,100),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_RELEASED,(50,50),absolute(100,100),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_CLICKED,(50,50),absolute(100,100),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_PRESSED,(60,70),absolute(110,120),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_RELEASED,(60,70),absolute(110,120),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_CLICKED,(60,70),absolute(110,120),button=3,modifiers=⌘+Button3,clickCount=1",
                blackboard()::text);
    }

    @Test
    void clickRight_withClickCount_Point_ok() {
        showFrameForMouseTests();

        gt.clickRight(new Point(100, 100), 1);

        gt.clickRight(new Point(110, 120), 1);


        assertEqualsRetrying(
                "MOUSE_PRESSED,(50,50),absolute(100,100),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_RELEASED,(50,50),absolute(100,100),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_CLICKED,(50,50),absolute(100,100),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_PRESSED,(60,70),absolute(110,120),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_RELEASED,(60,70),absolute(110,120),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_CLICKED,(60,70),absolute(110,120),button=3,modifiers=⌘+Button3,clickCount=1",
                blackboard()::text);
    }

    @Test
    void clickRight_ok() {
        showFrameForMouseTests();

        gt.clickRight(100, 100);

        gt.clickRight(110, 120);


        assertEqualsRetrying(
                "MOUSE_PRESSED,(50,50),absolute(100,100),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_RELEASED,(50,50),absolute(100,100),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_CLICKED,(50,50),absolute(100,100),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_PRESSED,(60,70),absolute(110,120),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_RELEASED,(60,70),absolute(110,120),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_CLICKED,(60,70),absolute(110,120),button=3,modifiers=⌘+Button3,clickCount=1",
                blackboard()::text);
    }

    @Test
    void clickRight_Point_ok() {
        showFrameForMouseTests();

        gt.clickRight(new Point(100, 100));

        gt.clickRight(new Point(110, 120));


        assertEqualsRetrying(
                "MOUSE_PRESSED,(50,50),absolute(100,100),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_RELEASED,(50,50),absolute(100,100),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_CLICKED,(50,50),absolute(100,100),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_PRESSED,(60,70),absolute(110,120),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_RELEASED,(60,70),absolute(110,120),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_CLICKED,(60,70),absolute(110,120),button=3,modifiers=⌘+Button3,clickCount=1",
                blackboard()::text);
    }

    @Test
    void clickLeft_Component_withClickCount_ok() {
        JFrame frame = showFrameForMouseTests();

        Component comp = componentWith(JButton.class, frame, c -> true);

        gt.clickLeft(comp, 10, 10, 1);
        gt.clickLeft(comp, 40, 15, 1);

        assertEqualsRetrying(
                "MOUSE_PRESSED,(10,10),absolute(160,160),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(10,10),absolute(160,160),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(10,10),absolute(160,160),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_PRESSED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1",
                blackboard()::text);
    }

    @Test
    void clickLeft_Component_withClickCount_Point_ok() {
        JFrame frame = showFrameForMouseTests();

        Component comp = componentWith(JButton.class, frame, c -> true);

        gt.clickLeft(comp, new Point(10, 10), 1);
        gt.clickLeft(comp, new Point(40, 15), 1);

        assertEqualsRetrying(
                "MOUSE_PRESSED,(10,10),absolute(160,160),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(10,10),absolute(160,160),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(10,10),absolute(160,160),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_PRESSED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1",
                blackboard()::text);
    }

    @Test
    void clickLeft_Component_ok() {
        JFrame frame = showFrameForMouseTests();

        Component comp = componentWith(JButton.class, frame, c -> true);

        gt.clickLeft(comp, 10, 10);
        gt.clickLeft(comp, 40, 15);

        assertEqualsRetrying(
                "MOUSE_PRESSED,(10,10),absolute(160,160),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(10,10),absolute(160,160),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(10,10),absolute(160,160),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_PRESSED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1",
                blackboard()::text);
    }

    @Test
    void clickLeft_Component_Point_ok() {
        JFrame frame = showFrameForMouseTests();

        Component comp = componentWith(JButton.class, frame, c -> true);

        gt.clickLeft(comp, new Point(10, 10));
        gt.clickLeft(comp, new Point(40, 15));

        assertEqualsRetrying(
                "MOUSE_PRESSED,(10,10),absolute(160,160),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(10,10),absolute(160,160),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(10,10),absolute(160,160),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_PRESSED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1",
                blackboard()::text);
    }

    @Test
    void clickLeft_Component_negativeOffsets_ok() {
        JFrame frame = showFrameForMouseTests();

        Component comp = componentWith(JButton.class, frame, c -> true);

        gt.clickLeft(comp, -10, -10);
        gt.clickLeft(comp, -40, -15);

        assertEqualsRetrying(
                "MOUSE_PRESSED,(70,20),absolute(220,170),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(70,20),absolute(220,170),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(70,20),absolute(220,170),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_PRESSED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1",
                blackboard()::text);
    }

    @Test
    void clickLeft_Component_negativeOffsets_Point_ok() {
        JFrame frame = showFrameForMouseTests();

        Component comp = componentWith(JButton.class, frame, c -> true);

        gt.clickLeft(comp, new Point(-10, -10));
        gt.clickLeft(comp, new Point(-40, -15));

        assertEqualsRetrying(
                "MOUSE_PRESSED,(70,20),absolute(220,170),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(70,20),absolute(220,170),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(70,20),absolute(220,170),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_PRESSED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1",
                blackboard()::text);
    }

    @Test
    void clickLeft_Component_centered_withClickCount_ok() {
        JFrame frame = showFrameForMouseTests();

        Component comp = componentWith(JButton.class, frame, c -> true);

        gt.clickLeft(comp, 1);
        gt.clickLeft(comp, 1);
        assertEqualsRetrying(
                "MOUSE_PRESSED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_PRESSED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1",
                blackboard()::text);
    }

    @Test
    void clickLeft_Component_centered_ok() {
        JFrame frame = showFrameForMouseTests();

        Component comp = componentWith(JButton.class, frame, c -> true);

        gt.clickLeft(comp);
        gt.clickLeft(comp);

        assertEqualsRetrying(
                "MOUSE_PRESSED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_PRESSED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(40,15),absolute(190,165),button=1,modifiers=Button1,clickCount=1",
                blackboard()::text);
    }

    @Test
    void clickCharacterAtIndex_ok() {
        JFrame frame = showNameInputFrame();

        JTextComponent textComponent = gt.waitForComponentNamed(
                JTextComponent.class, "firstname");
        gt._focus().setFocusOwner(textComponent);
        gt.type("foobar");

        // click before the "b" and insert "baz"
        gt.clickCharacterAtIndex(textComponent, 3);
        gt.type("baz");

        // click at the end of the text and append "qux"
        gt.clickCharacterAtIndex(textComponent, -1);
        gt.type("qux");

        assertEqualsRetrying("foobazbarqux",textComponent::getText);
    }


    @Test
    void clickRight_Component_withClickCount_ok() {
        JFrame frame = showFrameForMouseTests();

        JButton comp = componentWith(JButton.class, frame, c -> true);

        gt.clickRight(comp, 10, 10, 1);
        gt.clickRight(comp, 40, 15, 1);

        assertEqualsRetrying(
                "MOUSE_PRESSED,(10,10),absolute(160,160),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_RELEASED,(10,10),absolute(160,160),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_CLICKED,(10,10),absolute(160,160),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_PRESSED,(40,15),absolute(190,165),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_RELEASED,(40,15),absolute(190,165),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_CLICKED,(40,15),absolute(190,165),button=3,modifiers=⌘+Button3,clickCount=1",
                blackboard()::text);
    }

    @Test
    void clickRight_Component_withClickCount_Point_ok() {
        JFrame frame = showFrameForMouseTests();

        JButton comp = componentWith(JButton.class, frame, c -> true);

        gt.clickRight(comp, new Point(10, 10), 1);
        gt.clickRight(comp, new Point(40, 15), 1);

        assertEqualsRetrying(
                "MOUSE_PRESSED,(10,10),absolute(160,160),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_RELEASED,(10,10),absolute(160,160),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_CLICKED,(10,10),absolute(160,160),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_PRESSED,(40,15),absolute(190,165),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_RELEASED,(40,15),absolute(190,165),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_CLICKED,(40,15),absolute(190,165),button=3,modifiers=⌘+Button3,clickCount=1",
                blackboard()::text);
    }

    @Test
    void clickRight_Component_ok() {
        JFrame frame = showFrameForMouseTests();

        JButton comp = componentWith(JButton.class, frame, c -> true);

        gt.clickRight(comp, 10, 10);
        gt.clickRight(comp, 40, 15);

        assertEqualsRetrying(
                "MOUSE_PRESSED,(10,10),absolute(160,160),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_RELEASED,(10,10),absolute(160,160),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_CLICKED,(10,10),absolute(160,160),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_PRESSED,(40,15),absolute(190,165),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_RELEASED,(40,15),absolute(190,165),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_CLICKED,(40,15),absolute(190,165),button=3,modifiers=⌘+Button3,clickCount=1",
                blackboard()::text);
    }

    @Test
    void clickRight_Component_Point_ok() {
        JFrame frame = showFrameForMouseTests();

        JButton comp = componentWith(JButton.class, frame, c -> true);

        gt.clickRight(comp, new Point(10, 10));
        gt.clickRight(comp, new Point(40, 15));

        assertEqualsRetrying(
                "MOUSE_PRESSED,(10,10),absolute(160,160),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_RELEASED,(10,10),absolute(160,160),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_CLICKED,(10,10),absolute(160,160),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_PRESSED,(40,15),absolute(190,165),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_RELEASED,(40,15),absolute(190,165),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_CLICKED,(40,15),absolute(190,165),button=3,modifiers=⌘+Button3,clickCount=1",
                blackboard()::text);
    }

    @Test
    void clickRight_Component_centered_withClickCount_ok() {
        JFrame frame = showFrameForMouseTests();

        JButton comp = componentWith(JButton.class, frame, c -> true);

        gt.clickRight(comp, 1);
        gt.clickRight(comp, 1);

        assertEqualsRetrying(
                "MOUSE_PRESSED,(40,15),absolute(190,165),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_RELEASED,(40,15),absolute(190,165),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_CLICKED,(40,15),absolute(190,165),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_PRESSED,(40,15),absolute(190,165),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_RELEASED,(40,15),absolute(190,165),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_CLICKED,(40,15),absolute(190,165),button=3,modifiers=⌘+Button3,clickCount=1",
                blackboard()::text);
    }

    @Test
    void clickRight_Component_centered_ok() {
        JFrame frame = showFrameForMouseTests();

        JButton comp = componentWith(JButton.class, frame, c -> true);

        gt.clickRight(comp);
        gt.clickRight(comp);

        assertEqualsRetrying(
                "MOUSE_PRESSED,(40,15),absolute(190,165),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_RELEASED,(40,15),absolute(190,165),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_CLICKED,(40,15),absolute(190,165),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_PRESSED,(40,15),absolute(190,165),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_RELEASED,(40,15),absolute(190,165),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                        "MOUSE_CLICKED,(40,15),absolute(190,165),button=3,modifiers=⌘+Button3,clickCount=1",
                blackboard()::text);
    }

    @Test
    void drag_ok() {
        JFrame frame = showFrameForDragTests();

        // move the frame 50 px to the right by dragging its title bar
        gt.drag(InputEvent.BUTTON1_DOWN_MASK, 150, 60, 200, 60);

        assertEqualsRetrying(new Point(100, 50), frame::getLocation);
    }

    @Test
    void drag_Point_ok() {
        JFrame frame = showFrameForDragTests();

        // move the frame 50 px to the right by dragging its title bar
        gt.drag(InputEvent.BUTTON1_DOWN_MASK, new Point(150, 60), new Point(200, 60));

        assertEqualsRetrying(new Point(100, 50), frame::getLocation);
    }

    @Test
    void drag_noDoubleClick_ok() {
        showFrameForDragTests();

        // Special case: when a click occurred close to the start of the drag
        // this must not be interpreted as a double click
        gt.click(InputEvent.BUTTON1_DOWN_MASK, 150, 150, 1);

        // move the frame 1 px to the right by dragging its title bar
        gt.drag(InputEvent.BUTTON1_DOWN_MASK, 150, 150, 151, 151);

        assertEqualsRetrying("MOUSE_PRESSED,(100,100),absolute(150,150),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(100,100),absolute(150,150),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_CLICKED,(100,100),absolute(150,150),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_PRESSED,(100,100),absolute(150,150),button=1,modifiers=Button1,clickCount=1\n" +
                        "MOUSE_RELEASED,(101,101),absolute(151,151),button=1,modifiers=Button1,clickCount=0",
                firstFiveLinesOfBlackboard());
    }

    @Test
    void drag_Component_ok() {
        JFrame frame = showFrameForDragTests();

        gt.drag(InputEvent.BUTTON1_DOWN_MASK, frame, 30, 40, 150, 170);

        assertEqualsRetrying("MOUSE_PRESSED,(30,40),absolute(80,90),button=1,modifiers=Button1,clickCount=1\n" +
                "MOUSE_RELEASED,(150,170),absolute(200,220),button=1,modifiers=Button1,clickCount=0", firstTwoLinesOfBlackboard());
    }

    @Test
    void drag_Component_Point_ok() {
        JFrame frame = showFrameForDragTests();

        gt.drag(InputEvent.BUTTON1_DOWN_MASK, frame, new Point(30, 40), new Point(150, 170));

        assertEqualsRetrying("MOUSE_PRESSED,(30,40),absolute(80,90),button=1,modifiers=Button1,clickCount=1\n" +
                "MOUSE_RELEASED,(150,170),absolute(200,220),button=1,modifiers=Button1,clickCount=0", firstTwoLinesOfBlackboard());
    }

    @Test
    void dragLeft_ok() {
        JFrame frame = showFrameForDragTests();

        // move the frame 50 px to the right by dragging its title bar
        gt.dragLeft(150, 60, 200, 60);

        assertEquals(new Point(100, 50), frame.getLocation());
    }

    @Test
    void dragLeft_Point_ok() {
        JFrame frame = showFrameForDragTests();

        // move the frame 50 px to the right by dragging its title bar
        gt.dragLeft(new Point(150, 60), new Point(200, 60));

        assertEquals(new Point(100, 50), frame.getLocation());
    }

    @Test
    void dragLeft_Component_ok() {
        JFrame frame = showFrameForDragTests();

        gt.dragLeft(frame, 30, 40, 150, 170);

        assertEqualsRetrying("MOUSE_PRESSED,(30,40),absolute(80,90),button=1,modifiers=Button1,clickCount=1\n" +
                "MOUSE_RELEASED,(150,170),absolute(200,220),button=1,modifiers=Button1,clickCount=0", firstTwoLinesOfBlackboard());
    }

    @Test
    void dragLeft_Component_Point_ok() {
        JFrame frame = showFrameForDragTests();

        gt.dragLeft(frame, new Point(30, 40), new Point(150, 170));

        assertEqualsRetrying("MOUSE_PRESSED,(30,40),absolute(80,90),button=1,modifiers=Button1,clickCount=1\n" +
                "MOUSE_RELEASED,(150,170),absolute(200,220),button=1,modifiers=Button1,clickCount=0", firstTwoLinesOfBlackboard());
    }

    @Test
    void dragRight_ok() {
        showFrameForDragTests();

        gt.dragRight(80, 90, 200, 220);

        assertEqualsRetrying("MOUSE_PRESSED,(30,40),absolute(80,90),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                "MOUSE_RELEASED,(150,170),absolute(200,220),button=3,modifiers=⌘+Button3,clickCount=0", firstTwoLinesOfBlackboard());
    }

    @Test
    void dragRight_Point_ok() {
        showFrameForDragTests();

        gt.dragRight(new Point(80, 90), new Point(200, 220));

        assertEqualsRetrying("MOUSE_PRESSED,(30,40),absolute(80,90),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                "MOUSE_RELEASED,(150,170),absolute(200,220),button=3,modifiers=⌘+Button3,clickCount=0", firstTwoLinesOfBlackboard());
    }

    @Test
    void dragRight_Component_ok() {
        JFrame frame = showFrameForDragTests();

        gt.dragRight(frame, 30, 40, 150, 170);

        assertEqualsRetrying("MOUSE_PRESSED,(30,40),absolute(80,90),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                "MOUSE_RELEASED,(150,170),absolute(200,220),button=3,modifiers=⌘+Button3,clickCount=0", firstTwoLinesOfBlackboard());
    }

    @Test
    void dragRight_Component_Point_ok() {
        JFrame frame = showFrameForDragTests();

        gt.dragRight(frame, new Point(130, 40), new Point(50, 170));

        assertEqualsRetrying("MOUSE_PRESSED,(130,40),absolute(180,90),button=3,modifiers=⌘+Button3,clickCount=1\n" +
                "MOUSE_RELEASED,(50,170),absolute(100,220),button=3,modifiers=⌘+Button3,clickCount=0", firstTwoLinesOfBlackboard());
    }


    @Test
    void waitWhile_ok() {
        long endTicks = System.currentTimeMillis() + 50;

        gt.waitWhile(() -> System.currentTimeMillis() < endTicks);

        assertTrue(System.currentTimeMillis() >= endTicks);
    }

    @Test
    void waitFor_ok() {
        long startTicks = System.currentTimeMillis();
        int waitMillis = 50;
        gt.waitFor(Duration.ofMillis(waitMillis));
        assertTrue(System.currentTimeMillis() >= startTicks + waitMillis);
    }

    @Test
    void waitForMillis_ok() {
        long startTicks = System.currentTimeMillis();
        int waitMillis = 50;
        gt.waitForMillis(waitMillis);
        assertTrue(System.currentTimeMillis() >= startTicks + waitMillis);
    }

    @Test
    void waitForUser_String_ok() {
        // NOTE: this test relies on implementation details ("internal") and
        // may need to be updated when the implementation changes.

        async(() -> {
            gt.waitForUser("Pause Sample");
            blackboard().add("pause ended");
        });


        // wait for the "PauseWindow" (with the correct message)
        assertTrueRetrying(() -> hasWindowNamed(PauseUI.PAUSE_WINDOW_NAME));
        assertEquals("Pause Sample", textFieldNamed(PauseUI.MESSAGE_FIELD_NAME).getText().trim());

        // end the pause by clicking the button
        clickLeft(buttonNamed(PauseUI.CONTINUE_BUTTON_NAME));

        // Wait for the pause window disappear and execution continuation
        assertTrueRetrying(() -> !hasWindowNamed(PauseUI.PAUSE_WINDOW_NAME));
        assertEqualsRetrying("pause ended", blackboard()::text);
    }

    @Test
    void waitForUser_pause_ok() {
        // NOTE: this test relies on implementation details, e.g, it assumes the
        // internal class "PauseUI" is used for pausing. So if the
        // implementation changes the test may need to be updated.

        async(() -> {
            gt.pause();
            blackboard().add("pause ended");
        });

        // wait for the "PauseWindow"
        assertTrueRetrying(() -> hasWindowNamed(PauseUI.PAUSE_WINDOW_NAME));

        // end the pause by clicking the button
        clickLeft(buttonNamed(PauseUI.CONTINUE_BUTTON_NAME));

        // Wait for the pause window disappear and execution continuation
        assertTrueRetrying(() -> !hasWindowNamed(PauseUI.PAUSE_WINDOW_NAME));
        assertEqualsRetrying("pause ended", blackboard()::text);
    }


    @Test
    void poll_ok() {
        // endTime in 100ms, ends before timeout
        Date endTime = new Date(System.currentTimeMillis() + 100);
        Date t = gt.poll(Date::new, d -> d.after(endTime));

        assertTrue(t.after(endTime));
    }

    @Test
    void poll_timeouts() {
        gt.setTimeout(Duration.ofSeconds(1));
        assertThrows(TimeoutUncheckedException.class,
                () -> gt.poll(Date::new, d -> false));
    }


    @Test
    void pollNoFail_ok() {
        // endTime in 100ms, ends before timeout
        Date endTime = new Date(System.currentTimeMillis() + 100);

        Date t = gt.pollNoFail(Date::new, d -> d.after(endTime));

        assertTrue(t.after(endTime));
    }

    @Test
    void pollNoFail_timeouts() {
        gt.setTimeout(Duration.ofSeconds(1));
        Date t = gt.pollNoFail(Date::new, d -> false);

        assertTrue(t.compareTo(new Date()) <= 0);
    }

    @Test
    void poll_explicitTimeout_ok() {
        // endTime in 100ms, ends before timeout
        Date endTime = new Date(System.currentTimeMillis() + 100);

        Date t = gt.poll(Date::new, d -> d.after(endTime), Duration.ofSeconds(60));

        assertTrue(t.after(endTime));
    }

    @Test
    void poll_explicitTimeout_timeouts() {
        assertThrows(TimeoutUncheckedException.class,
                () -> gt.poll(Date::new, d -> false, Duration.ofSeconds(1)));
    }


    @Test
    void pollNoFail_explicitTimeout_ok() {
        // endTime in 100ms, ends before timeout
        Date endTime = new Date(System.currentTimeMillis() + 100);

        Date t = gt.pollNoFail(Date::new, d -> d.after(endTime), Duration.ofSeconds(60));

        assertTrue(t.after(endTime));
    }

    @Test
    void pollNoFail_explicitTimeout_timeouts() {
        Date t = gt.pollNoFail(Date::new, d -> false, Duration.ofSeconds(1));

        assertTrue(t.compareTo(new Date()) <= 0);
    }

    @Test
    void getPixelColor_ok() {
        JFrame frame = showFrameWithColors();

        int left = frame.getLocation().x;
        int top = frame.getLocation().y;

        assertEquals(Color.white, gt.getPixelColor(left + 20, top + 20));
        assertEquals(Color.black, gt.getPixelColor(left + 50, top + 50));

        // The other color we cannot directly compare as getPixelColor does not
        // always return the "native" colors. So we just check the "color-ish"
        assertTrue(isBlueish(gt.getPixelColor(left + 90, top + 90)));
        assertTrue(isGreenish(gt.getPixelColor(left + 20, top + 90)));
        assertTrue(isRedish(gt.getPixelColor(left + 90, top + 20)));
    }

    @Test
    void createScreenCapture_ok() {
        JFrame frame = showFrameWithColors();

        BufferedImage image = gt.createScreenCapture(frame.getBounds());

        assertEquals(Color.white, new Color(image.getRGB(20, 20)));
        assertEquals(Color.black, new Color(image.getRGB(50, 50)));

        // The other color we cannot directly compare as createScreenCapture
        // does not always reproduces the "native" colors. So we just check
        // the "color-ish"
        assertTrue(isBlueish(new Color(image.getRGB(90, 90))));
        assertTrue(isGreenish(new Color(image.getRGB(20, 90))));
        assertTrue(isRedish(new Color(image.getRGB(90, 20))));
    }

    @Test
    void delay_ok() {
        final int duration = 10;

        long start = System.currentTimeMillis();
        gt.delay(duration);

        assertTrue(System.currentTimeMillis() >= start + duration);
    }

    @Test
    void isAndSetAutoWaitForIdle_ok() {
        // by default no AutoWaitForIdle
        assertFalse(gt.isAutoWaitForIdle());

        gt.setAutoWaitForIdle(true);
        assertTrue(gt.isAutoWaitForIdle());

        gt.setAutoWaitForIdle(false);
        assertFalse(gt.isAutoWaitForIdle());
    }

    @Test
    void getAndSetAutoDelay_ok() {
        // by default AutoDelay is 0
        assertEquals(0, gt.getAutoDelay());

        gt.setAutoDelay(3);
        assertEquals(3, gt.getAutoDelay());
    }


    @Test
    void reset_ok() {
        // "releaseAllKeys" test setup:
        //
        // to test if "releaseAllKeys" is called we need to press some
        // keys and don't release them
        JTextField tf = showFrameWithTextField();

        tf.addKeyListener(logKeyEventsToBlackboardAdapter());
        setFocusOwner(tf);

        gt.keyPress(KeyEvent.VK_H);
        // end of "releaseAllKeys" test setup

        Duration t = gt.initialTimeout();
        try {
            gt.setInitialTimeout(Duration.ofMillis(4));
            gt.blackboard().add("foo");

            gt.reset();

            assertEquals(Duration.ofMillis(4), gt.timeout());
            assertEquals("", gt.blackboard().text());
            assertEqualsRetrying("keyPressed: 72\n" +
                    "keyReleased: 72", blackboard()::text);
        } finally {
            gt.setInitialTimeout(t);
        }
    }

    private KeyAdapter logKeyEventsToBlackboardAdapter() {
        return new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                blackboard().add("keyPressed: " + e.getKeyCode());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                blackboard().add("keyReleased: " + e.getKeyCode());
            }
        };
    }

    @Test
    void cleanup_ok() {
        Duration t = gt.initialTimeout();
        try {
            gt.setInitialTimeout(Duration.ofMillis(4));
            gt.setTimeout(Duration.ofMillis(5));
            gt.blackboard().add("foo");
            showFrameWithTextField();

            gt.cleanup();

            assertEquals(Duration.ofMillis(4), gt.timeout());
            assertEquals("", gt.blackboard().text());
            assertEquals(0, allWindows().size());
            // no easy way to test the '"Release" all not yet released keys.' feature.
        } finally {
            gt.setInitialTimeout(t);
        }
    }

    @Test
    void setTimeout_ok() {
        gt.setTimeout(Duration.ofMillis(2));
        assertEquals(Duration.ofMillis(2), gt.timeout());
    }

    @Test
    void setTimeoutMillis_ok() {
        gt.setTimeoutMillis(2);
        assertEquals(Duration.ofMillis(2), gt.timeout());
    }

    @Test
    void initialTimout_ok() {
        Duration t = gt.initialTimeout();
        try {
            gt.setInitialTimeout(Duration.ofMillis(3));
            assertEquals(Duration.ofMillis(3), gt.initialTimeout());
        } finally {
            gt.setInitialTimeout(t);
        }
    }

    @Test
    void runWithTimeout_ok() {
        assertThrows(TimeoutUncheckedException.class, () ->
                gt.runWithTimeout(Duration.ofMillis(1), () -> gt.poll(() -> true, v -> false)));
    }


    @Test
    void waitForIdle_ok() {
        // put stuff in the event queue
        invokeLater(() -> blackboard().add("foo"));
        invokeLater(() -> blackboard().add("bar"));

        // wait until the event queue is empty, i.e. we are idle
        gt.waitForIdle();

        assertEquals("foo\nbar", blackboard().text());
    }

    @Test
    void allWindows_ok() {
        showFramesForWindowsTests();

        assertEqualsRetrying(3, () -> gt.allWindows().size());
    }

    @Test
    void hasWindowWith_ok() {
        showFramesForWindowsTests();

        // one match
        assertTrue(gt.hasWindowWith(w -> ((JFrame) w).getTitle().startsWith("TitleAA")));

        // more than one match
        assertTrue(gt.hasWindowWith(w -> ((JFrame) w).getTitle().startsWith("TitleA")));

        // no match
        assertFalse(gt.hasWindowWith(w -> ((JFrame) w).getTitle().startsWith("TitleC")));
    }

    @Test
    void windowWith_ok() {
        showFramesForWindowsTests();

        // one match
        Window wnd = gt.windowWith(w -> ((JFrame) w).getTitle().startsWith("TitleAA"));
        assertTrue(((JFrame) wnd).getTitle().startsWith("TitleAA"));

        // more than one match
        assertThrows(NoSuchElementException.class,
                () -> gt.windowWith(w -> ((JFrame) w).getTitle().startsWith("TitleA")));

        // no match
        assertThrows(NoSuchElementException.class,
                () -> gt.windowWith(w -> ((JFrame) w).getTitle().startsWith("TitleC")));
    }

    @Test
    void windowNamed_ok() {
        showNameInputFrame();

        Window wnd = gt.windowNamed("nameInput");

        assertEquals("nameInput", wnd.getName());
    }

    @Test
    void windowNamed_2ok() {
        showNameInputFrame();

        JFrame wnd = gt.windowNamed(JFrame.class, "nameInput");

        assertEquals("nameInput", wnd.getName());
    }

    @Test
    void windowNamed_missing_fails() {

        assertThrows(NoSuchElementException.class, () ->
                gt.windowNamed("missing"));
    }

    @Test
    void anyWindowWith_ok() {
        showFramesForWindowsTests();

        // one match
        Window wnd = gt.anyWindowWith(w -> ((JFrame) w).getTitle().startsWith("TitleAA"));
        assertTrue(((JFrame) wnd).getTitle().startsWith("TitleAA"));

        // more than one match
        Window wnd2 = gt.anyWindowWith(w -> ((JFrame) w).getTitle().startsWith("TitleA"));
        assertTrue(((JFrame) wnd2).getTitle().startsWith("TitleA"));

        // no match
        assertThrows(NoSuchElementException.class,
                () -> gt.anyWindowWith(w -> ((JFrame) w).getTitle().startsWith("TitleC")));
    }

    @Test
    void allWindowsWith_ok() {
        showFramesForWindowsTests();

        assertEqualsRetrying(3, () -> gt.allWindowsWith(w -> ((JFrame) w).getTitle().startsWith("Title")).size());

        assertEqualsRetrying(2, () -> gt.allWindowsWith(w -> ((JFrame) w).getTitle().startsWith("TitleA")).size());

        assertEqualsRetrying(1, () -> gt.allWindowsWith(w -> ((JFrame) w).getTitle().startsWith("TitleAA")).size());

        assertEqualsRetrying(0, () -> gt.allWindowsWith(w -> ((JFrame) w).getTitle().startsWith("TitleC")).size());

    }

    @Test
    void close_ok() {
        JFrame frame = showFrameForMouseTests();

        gt.close(frame);

        assertTrueRetrying(() -> allWindows().isEmpty());
    }

    @Test
    void waitForWindowWith_Class_ok() {
        async(MyGT::showFrameWithTextField);

        JFrame frame = gt.waitForWindowWith(JFrame.class, w -> true);

        assertNotNull(frame);
    }

    @Test
    void waitForWindowWith_ok() {

        async(MyGT::showFrameWithTextField);
        Window wnd = gt.waitForWindowWith(w -> true);

        assertNotNull(wnd);
    }

    @Test
    void dumpAllComponents_noComponents_ok() {

        // no components created

        PrintStreamToBuffer out = newPrintStreamToBuffer();
        gt.dumpAllComponents(out);

        assertEquals("", out.text());
    }

    @Test
    void dumpAllComponents_someComponents_ok() {

        // show some components
        showNameInputFrame();

        PrintStreamToBuffer out = newPrintStreamToBuffer();
        gt.dumpAllComponents(out);

        assertEquals("JFrame (javax.swing)\t\"nameInput\"\t\"\"\t@(50,50) 595x39\n" +
                        "    JRootPane (javax.swing)\tnull\tnull\t@(0,0) 595x39\n" +
                        "        JPanel (javax.swing)\t\"null.glassPane\"\tnull\t@(0,0) 595x39\n" +
                        "        JLayeredPane (javax.swing)\t\"null.layeredPane\"\tnull\t@(0,0) 595x39\n" +
                        "            JPanel (javax.swing)\t\"null.contentPane\"\tnull\t@(0,0) 595x39\n" +
                        "                JPanel (javax.swing)\tnull\tnull\t@(0,0) 595x39\n" +
                        "                    JTextField (javax.swing)\t\"firstname\"\t\"\"\t@(5,6) 250x26\n" +
                        "                    JTextField (javax.swing)\t\"lastname\"\t\"\"\t@(260,6) 250x26\n" +
                        "                    JButton (javax.swing)\t\"ok\"\t\"OK\"\t@(515,5) 75x29\n",
                out.text());
    }

    @Test
    void dumpAllComponents_SystemOut_someComponents_ok() {

        // show some components
        showNameInputFrame();


        PrintStreamToBuffer out = newPrintStreamToBuffer();
        try(RunOnClose r = systemOutRedirect(out)) {
            gt.dumpAllComponents();
        }

        assertEquals("JFrame (javax.swing)\t\"nameInput\"\t\"\"\t@(50,50) 595x39\n" +
                        "    JRootPane (javax.swing)\tnull\tnull\t@(0,0) 595x39\n" +
                        "        JPanel (javax.swing)\t\"null.glassPane\"\tnull\t@(0,0) 595x39\n" +
                        "        JLayeredPane (javax.swing)\t\"null.layeredPane\"\tnull\t@(0,0) 595x39\n" +
                        "            JPanel (javax.swing)\t\"null.contentPane\"\tnull\t@(0,0) 595x39\n" +
                        "                JPanel (javax.swing)\tnull\tnull\t@(0,0) 595x39\n" +
                        "                    JTextField (javax.swing)\t\"firstname\"\t\"\"\t@(5,6) 250x26\n" +
                        "                    JTextField (javax.swing)\t\"lastname\"\t\"\"\t@(260,6) 250x26\n" +
                        "                    JButton (javax.swing)\t\"ok\"\t\"OK\"\t@(515,5) 75x29\n",
                out.text());
    }

    @Test
    void featureGroupAccess() {
        assertSame(gt,gt._assert());
        assertSame(gt,gt._component());
        assertSame(gt,gt._debug());
        assertSame(gt,gt._dialog());
        assertSame(gt,gt._edt());
        assertSame(gt,gt._focus());
        assertSame(gt,gt._frame());
        assertSame(gt,gt._idle());
        assertSame(gt,gt._keyboard());
        assertSame(gt,gt._mouse());
        assertSame(gt,gt._poll());
        assertSame(gt,gt._robotAPI());
        assertSame(gt,gt._timeout());
        assertSame(gt,gt._wait());
        assertSame(gt,gt._window());
    }
}