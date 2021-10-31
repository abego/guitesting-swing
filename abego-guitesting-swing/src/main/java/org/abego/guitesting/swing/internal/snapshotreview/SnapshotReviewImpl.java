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

import org.abego.commons.seq.Seq;
import org.abego.guitesting.swing.GT;
import org.abego.guitesting.swing.GuiTesting;
import org.abego.guitesting.swing.ScreenCaptureSupport.SnapshotIssue;
import org.abego.guitesting.swing.SnapshotReview;

import javax.swing.JFrame;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static javax.swing.SwingUtilities.invokeLater;

public class SnapshotReviewImpl implements SnapshotReview {

    //region State
    private final Supplier<Seq<SnapshotIssue>> issuesSupplier;
    //endregion
    //region Construction
    private SnapshotReviewImpl(Supplier<Seq<SnapshotIssue>> issuesSupplier) {
        this.issuesSupplier = issuesSupplier;
    }

    public static SnapshotReview newSnapshotReview(
            Supplier<Seq<SnapshotIssue>> issuesSupplier) {
        return new SnapshotReviewImpl(issuesSupplier);
    }
    //endregion
    //region Commands
    @Override
    public void showIssues(Consumer<JFrame> framePreShowCode) {

        Seq<SnapshotIssue> issues = getSnapshotIssues();

        invokeLater(() -> {
            SnapshotReviewWidget widget = SnapshotReviewWidget.snapshotReviewWidget(issues);
            //noinspection StringConcatenation
            JFrame frame = new JFrame("Snapshot Review (" + issues.size() + " issues)"); //NON-NLS
            frame.setName(SNAPSHOT_REVIEW_FRAME_NAME);
            frame.setContentPane(widget.getContent());
            framePreShowCode.accept(frame);
            frame.setVisible(true);
        });
    }

    private Seq<SnapshotIssue> getSnapshotIssues() {
        return issuesSupplier.get();
    }
    //endregion
    //region main
    public static void main(String[] args) {
        GT gt = GuiTesting.newGT();

        gt.newSnapshotReview().showIssues();
    }
    //endregion

}
