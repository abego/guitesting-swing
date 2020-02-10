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

import org.abego.guitesting.GuiTesting;
import org.abego.guitesting.swing.JTreeTestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import java.awt.Dimension;
import java.awt.Point;

import static org.abego.guitesting.swing.JTreeTestUtil.JTreeDebugStringFlag.MARK_HIDDEN_ITEM;
import static org.abego.guitesting.swing.JTreeTestUtil.JTreeDebugStringFlag.MARK_SELECTED_ITEM;

class JTreeTestUtilTest {
    private final static GuiTesting gt = GuiTesting.newGuiTesting();

    private static TreeNode newTreeNodeSample() {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");

        DefaultMutableTreeNode foo = new DefaultMutableTreeNode("foo");
        foo.add(new DefaultMutableTreeNode("foo-foo"));
        foo.add(new DefaultMutableTreeNode("foo-bar"));
        root.add(foo);

        root.add(new DefaultMutableTreeNode("baz"));
        root.add(new DefaultMutableTreeNode("qux"));
        return root;
    }

    @Test
    void toDebugString_ok() {
        DefaultTreeModel model = new DefaultTreeModel(newTreeNodeSample());
        JTree tree = new JTree(model);
        tree.setSelectionRow(2);
        tree.expandRow(1);
        gt.showInFrame(new JScrollPane(tree));

        String debugString = JTreeTestUtil.toDebugString(tree);

        Assertions.assertEquals(
                "root\n" +
                        "  foo\n" +
                        "    foo-foo\n" +
                        "    foo-bar\n" +
                        "  baz\n" +
                        "  qux\n",
                debugString);
    }

    @Test
    void toDebugString_MARK_SELECTED_ITEM_ok() {
        DefaultTreeModel model = new DefaultTreeModel(newTreeNodeSample());
        JTree tree = new JTree(model);
        tree.setSelectionRow(2);
        tree.expandRow(1);
        gt.showInFrame(new JScrollPane(tree));

        String debugString = JTreeTestUtil.toDebugString(tree, MARK_SELECTED_ITEM);

        Assertions.assertEquals(
                "root\n" +
                        "  foo\n" +
                        "    foo-foo\n" +
                        "    foo-bar\n" +
                        "  baz (selected)\n" +
                        "  qux\n",
                debugString);
    }

    @Test
    void toDebugString_MARK_HIDDEN_ITEM_ok() {
        DefaultTreeModel model = new DefaultTreeModel(newTreeNodeSample());
        JTree tree = new JTree(model);
        tree.setSelectionRow(2);
        tree.expandRow(1);
        // Make the window less tall so the last node is hidden
        gt.showInFrame(new JScrollPane(tree), new Point(50,50),new Dimension(200,90));

        String debugString = JTreeTestUtil.toDebugString(tree, MARK_HIDDEN_ITEM);

        Assertions.assertEquals(
                "root\n" +
                        "  foo\n" +
                        "    foo-foo\n" +
                        "    foo-bar\n" +
                        "  baz\n" +
                        "  qux (hidden)\n",
                debugString);

    }
}