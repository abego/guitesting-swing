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

package org.abego.guitesting.swing.app;

import org.abego.commons.lang.ThrowableUtil;
import org.abego.guitesting.swing.GuiTesting;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import java.awt.Font;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.logging.Logger.getLogger;

public class GuiTestingSwingApp {
    private static final Logger LOGGER = getLogger(GuiTestingSwingApp.class.getName());

    public static void main(String[] args) {
        try {
            GuiTesting.reviewSnapshotIssues(args);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e, () -> "GuiTestingSwingApp.main failed"); //NON-NLS
            showInErrorDialog(e);
        }
    }

    private static void showInErrorDialog(Exception e) {
        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textArea.setOpaque(false);
        textArea.setText(
                String.format("Fatal error. Application will end.\n\nDetails\n=======\n\n%s\n", //NON-NLS
                        ThrowableUtil.allMessagesOrClassName(e)));
        textArea.setEditable(false);
        JOptionPane.showMessageDialog(null, textArea);
    }

}
