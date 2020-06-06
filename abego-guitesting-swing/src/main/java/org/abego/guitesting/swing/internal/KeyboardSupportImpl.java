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

package org.abego.guitesting.swing.internal;

import org.abego.guitesting.swing.KeyboardSupport;
import org.abego.guitesting.swing.WaitForIdleSupport;

import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

final class KeyboardSupportImpl implements KeyboardSupport {

    private final Robot robot;
    private final WaitForIdleSupport waitForIdleSupport;

    /**
     * Make sure to match every keyPressed without a corresponding keyRelease.
     * Otherwise key events may be lost in later runs (at least when working
     * mac OS and IntelliJ.)
     */
    private final Set<Integer> keycodesToRelease = new HashSet<>();

    private KeyboardSupportImpl(Robot robot, WaitForIdleSupport waitForIdleSupport) {
        this.robot = robot;
        this.waitForIdleSupport = waitForIdleSupport;
    }

    static KeyboardSupport newKeyboardSupport(Robot robot, WaitForIdleSupport waitForIdleSupport) {
        return new KeyboardSupportImpl(robot, waitForIdleSupport);
    }

    @Override
    public void type(String s) {

        // Use the clipboard to "type" (/paste) the text
        StringSelection stringSelection = new StringSelection(s);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, stringSelection);

        waitForIdle();

        int shortCutCode = SwingUtil.isMacOS() ? KeyEvent.VK_META : KeyEvent.VK_CONTROL;
        keyPress(shortCutCode);
        keyPress(KeyEvent.VK_V);
        keyRelease(KeyEvent.VK_V);
        keyRelease(shortCutCode);

        waitForIdle();
    }

    @Override
    public void typeKeycode(int keycode) {
        waitForIdle();
        keyPress(keycode);
        waitForIdle();
        keyRelease(keycode);
        waitForIdle();
    }

    @Override
    public void keyPress(int keyCode) {
        robot.keyPress(keyCode);
        keycodesToRelease.add(keyCode);
        waitForIdle();
    }

    @Override
    public void keyRelease(int keyCode) {
        robot.keyRelease(keyCode);
        keycodesToRelease.remove(keyCode);
        waitForIdle();
    }

    @Override
    public void releaseAllKeys() {
        for (int keyCode : keycodesToRelease) {
            robot.keyRelease(keyCode);
        }
        keycodesToRelease.clear();

        waitForIdle();
    }

    private void waitForIdle() {
        waitForIdleSupport.waitForIdle();
    }
}
