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

package org.abego.guitesting.swing.internal.snapshotreview;

import org.abego.guitesting.swing.ScreenCaptureSupport;

final class SnapshotIssueUtil {
    SnapshotIssueUtil() {
        throw new IllegalArgumentException("Must not instantiate"); //NON-NLS
    }

    /**
     * Returns the "simple" name of the snapshot first (the part behind the last
     * '.'), followed by the package and class part, separated by a " - ".
     */
    public static <T extends ScreenCaptureSupport.SnapshotIssue> String labelWithLastPartFirst(T issue) {
        StringBuilder result = new StringBuilder();
        String s = issue.getLabel();
        //noinspection MagicCharacter
        int iDot = s.lastIndexOf('.');
        if (iDot >= 0) {
            result.append(s, iDot + 1, s.length());
            result.append(" - ");
            result.append(s, 0, iDot);
        } else {
            result.append(s);
        }
        return result.toString();
    }
}