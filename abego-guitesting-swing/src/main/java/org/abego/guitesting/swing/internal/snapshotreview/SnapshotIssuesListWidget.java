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

import org.abego.guitesting.swing.ScreenCaptureSupport.SnapshotIssue;
import org.abego.guitesting.swing.internal.Icons;
import org.abego.guitesting.swing.internal.util.SwingUtil;
import org.eclipse.jdt.annotation.Nullable;

import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.KeyStroke;
import java.awt.Color;
import java.util.function.Consumer;

import static javax.swing.SwingUtilities.invokeLater;
import static org.abego.guitesting.swing.internal.util.SwingUtil.DEFAULT_FLOW_GAP;
import static org.abego.guitesting.swing.internal.util.SwingUtil.bordered;
import static org.abego.guitesting.swing.internal.util.SwingUtil.borderedWithTopLine;
import static org.abego.guitesting.swing.internal.util.SwingUtil.flowLeft;
import static org.abego.guitesting.swing.internal.util.SwingUtil.iconButton;
import static org.abego.guitesting.swing.internal.util.SwingUtil.label;
import static org.abego.guitesting.swing.internal.util.SwingUtil.newAction;
import static org.abego.guitesting.swing.internal.util.SwingUtil.newListCellRenderer;
import static org.abego.guitesting.swing.internal.util.SwingUtil.scrollingNoBorder;
import static org.abego.guitesting.swing.internal.util.SwingUtil.vlist;

class SnapshotIssuesListWidget<T extends SnapshotIssue> implements Widget {
    private final static Color TITLE_BAR_COLOR = new Color(0xE2E6Ec);
    private final DefaultListModel<T> issuesListModel;
    private final Action nextScreenshotAction;
    private final Action previousScreenshotAction;
    private final JList<T> issuesList;
    private final JComponent content;

    private SnapshotIssuesListWidget(DefaultListModel<T> issuesListModel) {
        this.issuesListModel = issuesListModel;
        nextScreenshotAction = newAction("Next issue (↓)", KeyStroke.getKeyStroke("DOWN"), Icons.nextIssueIcon(), e -> selectNextIssue()); //NON-NLS
        previousScreenshotAction = newAction("Previous issue (↑)", KeyStroke.getKeyStroke("UP"), Icons.previousIssueIcon(), e -> selectPreviousIssue()); //NON-NLS
        issuesList = vlist(issuesListModel);
        issuesList.setCellRenderer(
                newListCellRenderer(SnapshotIssue.class, SnapshotIssue::getLabel));

        content = borderedWithTopLine()
                .top(bordered(l -> l.setBackground(TITLE_BAR_COLOR))
                        .left(flowLeft(DEFAULT_FLOW_GAP, 0, label("Issues:"))) //NON-NLS
                        .right(flowLeft(DEFAULT_FLOW_GAP, 0,
                                iconButton(previousScreenshotAction),
                                iconButton(nextScreenshotAction))))
                .bottom(scrollingNoBorder(issuesList));

        issuesList.addListSelectionListener(e -> onSelectedIssueChanged2());
        // TODO: use different trigger (e.g. when displayed?)
        invokeLater(() -> {
            // select the first issue in the list (if there is any)
            issuesList.setSelectedIndex(0);
        });
    }

    public static <T extends SnapshotIssue> SnapshotIssuesListWidget<T> newSnapshotIssuesListWidget(
            DefaultListModel<T> issuesListModel) {
        return new SnapshotIssuesListWidget<>(issuesListModel);
    }

    public int getSelectedIndex() {
        return issuesList.getSelectedIndex();
    }

    public void setSelectedIndex(int index) {
        issuesList.setSelectedIndex(index);
    }

    @Nullable
    public T getSelectedIssue() {
        return issuesList.getSelectedValue();
    }

    public void addSelectedIssueChangeListener(Consumer<SnapshotIssuesListWidget<T>> listener) {
        issuesList.addListSelectionListener(e -> listener.accept(this));
    }

    @Override
    public JComponent getComponent() {
        return content;
    }

    private void selectPreviousIssue() {
        SwingUtil.changeSelectedIndex(issuesList, -1);
    }

    private void selectNextIssue() {
        SwingUtil.changeSelectedIndex(issuesList, 1);
    }

    /**
     * When no item is selected but there are items in the list
     * automatically select the first item.
     */
    private void ensureSelectionIfPossible() {
        if (getSelectedIssue() == null && issuesListModel.size() > 0) {
            invokeLater(() -> issuesList.setSelectedIndex(0));
        }
    }

    private void onSelectedIssueChanged2() {
        invokeLater(() -> {
            ensureSelectionIfPossible();
            if (issuesList.getSelectedIndex() >= 0) {
                issuesList.ensureIndexIsVisible(issuesList.getSelectedIndex());
            }
        });
    }
}
