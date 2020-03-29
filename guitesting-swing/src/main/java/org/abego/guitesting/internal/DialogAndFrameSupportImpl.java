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

import org.abego.commons.swing.JDialogUtil;
import org.abego.commons.swing.JFrameUtil;
import org.abego.guitesting.DialogAndFrameSupport;

import org.eclipse.jdt.annotation.Nullable;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;

final class DialogAndFrameSupportImpl implements DialogAndFrameSupport {


    private DialogAndFrameSupportImpl() {
    }

    static DialogAndFrameSupport newDialogAndFrameSupport() {
        return new DialogAndFrameSupportImpl();
    }


    @Override
    public void showInDialog(Component component) {
        JDialogUtil.showInDialog(null, component);
    }

    @Override
    public void showInDialogTitled(String title, Component component) {
    	JDialogUtil.showInDialog(title, component);

    }

    @Override
    public JFrame showInFrameTitled(String title, @Nullable Component component, @Nullable Point position, @Nullable Dimension size) {
        return showInFrameHelper(title, component, position, size);
    }

    private JFrame showInFrameHelper(@Nullable String title, @Nullable Component component, @Nullable Point position, @Nullable Dimension size) {
        JFrame frame = JFrameUtil.showInFrame(title, component, position, size);

        SwingUtilities.invokeLater(frame::toFront);

        return frame;
    }

    @Override
    public JFrame showInFrame(Component component, @Nullable Point position, @Nullable Dimension size) {
        return showInFrameHelper(null, component, position, size);
    }

}
