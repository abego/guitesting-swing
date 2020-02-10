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

import org.abego.commons.lang.StringUtil;
import org.eclipse.jdt.annotation.NonNull;

import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.tree.TreePath;
import java.awt.Rectangle;
import java.util.EnumSet;

public class JTreeTestUtil {

    @NonNull
    public static String toDebugString(JTree jTree) {
        return toDebugString(jTree, EnumSet.noneOf(JTreeDebugStringFlag.class));
    }

    @NonNull
    public static String toDebugString(JTree jTree,
                                       JTreeDebugStringFlag firstFlag,
                                       JTreeDebugStringFlag... moreFlags) {
        return toDebugString(jTree, EnumSet.of(firstFlag, moreFlags));
    }

    public @NonNull
    static String toDebugString(JTree jTree, EnumSet<JTreeDebugStringFlag> flags) {
        StringBuilder result = new StringBuilder();
        int n = jTree.getRowCount();
        for (int i = 0; i < n; i++) {
            TreePath path = jTree.getPathForRow(i);
            result.append(StringUtil.repeat("  ", path.getPathCount() - 1));
            String t = jTree.convertValueToText(
                    path.getLastPathComponent(),
                    jTree.isPathSelected(path),
                    jTree.isExpanded(path),
                    true, i, true);
            result.append(t);
            if (jTree.isPathSelected(path) && flags.contains(JTreeDebugStringFlag.MARK_SELECTED_ITEM)) {
                result.append(PackagePrivateUtil.SELECTED_ITEM_SUFFIX);
            }
            if (!isInViewport(jTree, path) && flags.contains(JTreeDebugStringFlag.MARK_HIDDEN_ITEM)) {
                result.append(PackagePrivateUtil.HIDDEN_ITEM_SUFFIX);
            }
            result.append("\n");
        }
        return result.toString();
    }

    private static boolean isInViewport(JTree jTree, TreePath path) {
        Rectangle nodeRect = jTree.getPathBounds(path);
        if (nodeRect == null || (!(jTree.getParent() instanceof JViewport))) {
            return false;
        }

        JViewport viewport = (JViewport) jTree.getParent();
        Rectangle viewRect = viewport.getViewRect();
        return nodeRect.intersects(viewRect);
    }

    public enum JTreeDebugStringFlag {
        MARK_SELECTED_ITEM,
        MARK_HIDDEN_ITEM,
    }

}
