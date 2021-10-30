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
import org.abego.guitesting.swing.internal.util.prop.PropNullable;
import org.abego.guitesting.swing.internal.util.prop.PropNullableBindable;
import org.abego.guitesting.swing.internal.util.SwingUtil;
import org.eclipse.jdt.annotation.Nullable;

import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import java.awt.Color;
import java.util.Objects;

import static javax.swing.SwingUtilities.invokeLater;
import static org.abego.commons.lang.IntUtil.limit;
import static org.abego.guitesting.swing.internal.util.Bordered.bordered;
import static org.abego.guitesting.swing.internal.util.Bordered.borderedWithTopLine;
import static org.abego.guitesting.swing.internal.util.prop.PropNullableBindable.newPropNullableBindable;
import static org.abego.guitesting.swing.internal.util.SwingUtil.DEFAULT_FLOW_GAP;
import static org.abego.guitesting.swing.internal.util.SwingUtil.ensureSelectionIsVisible;
import static org.abego.guitesting.swing.internal.util.SwingUtil.flowLeft;
import static org.abego.guitesting.swing.internal.util.SwingUtil.label;
import static org.abego.guitesting.swing.internal.util.SwingUtil.newAction;
import static org.abego.guitesting.swing.internal.util.SwingUtil.newListCellRenderer;
import static org.abego.guitesting.swing.internal.util.SwingUtil.scrollingNoBorder;
import static org.abego.guitesting.swing.internal.util.SwingUtil.toolbarButton;

class SnapshotIssuesVList<T extends SnapshotIssue> implements Widget {
    private final static Color TITLE_BAR_COLOR = new Color(0xE2E6Ec);

    private final Action previousScreenshotAction;
    private final Action nextScreenshotAction;
    private final JButton previousScreenshotButton;
    private final JButton nextScreenshotButton;
    private final JList<T> issuesList = new JList<>();;
    private final JComponent content;
    private int lastSelectedIndex = -1;

    private SnapshotIssuesVList() {
        nextScreenshotAction = newAction("Next issue (↓)", KeyStroke.getKeyStroke("DOWN"), Icons.nextIssueIcon(), e -> selectNextIssue()); //NON-NLS
        previousScreenshotAction = newAction("Previous issue (↑)", KeyStroke.getKeyStroke("UP"), Icons.previousIssueIcon(), e -> selectPreviousIssue()); //NON-NLS
        previousScreenshotButton = toolbarButton();
        nextScreenshotButton = toolbarButton();

        previousScreenshotButton.setAction(previousScreenshotAction);
        nextScreenshotButton.setAction(nextScreenshotAction);

        issuesList.setBorder(null);
        issuesList.setCellRenderer(newListCellRenderer(
                SnapshotIssue.class, SnapshotIssueUtil::labelWithLastPartFirst));

        content = borderedWithTopLine()
                .top(bordered(l -> l.setBackground(TITLE_BAR_COLOR))
                        .left(flowLeft(DEFAULT_FLOW_GAP, 0, label("Issues:"))) //NON-NLS
                        .right(flowLeft(DEFAULT_FLOW_GAP, 0,
                                previousScreenshotButton,
                                nextScreenshotButton))
                        .component())
                .bottom(scrollingNoBorder(issuesList))
                .component();

        issuesList.addListSelectionListener(e -> onSelectedIssueChanged());
    }

    public static <T extends SnapshotIssue> SnapshotIssuesVList<T> snapshotIssuesVList() {
        return new SnapshotIssuesVList<>();
    }

    public ListModel<T> getListModel() {
        return issuesList.getModel();
    }

    public void setListModel(DefaultListModel<T> issuesListModel) {
        issuesList.setModel(issuesListModel);
    }

    public int getSelectedIndex() {
        return issuesList.getSelectedIndex();
    }

    public void setSelectedIndex(int index) {
        issuesList.setSelectedIndex(index);
    }

    //region selectedIssue
    private PropNullableBindable<T> selectedIssueProp =
            newPropNullableBindable(null, this, "selectedIssue", f -> updateSelectedIssueUI());

    {
        issuesList.addListSelectionListener(e -> updateSelectedIssueProp());
    }


    @Nullable
    public T getSelectedIssue() {
        return selectedIssueProp.get();
    }

    public void setSelectedIssue(@Nullable T value) {
        selectedIssueProp.set(value);
    }

    public void bindSelectedIssueTo(PropNullable<T> prop) {
        selectedIssueProp.bindTo(prop);
    }

    private void updateSelectedIssueUI() {
        invokeLater(() -> {
            T value = selectedIssueProp.get();
            issuesList.setSelectedValue(value, true);
        });
    }

    private void updateSelectedIssueProp() {
        T value = issuesList.getSelectedValue();
        if (!Objects.equals(getSelectedIssue(), value)) {
            selectedIssueProp.set(value);
        }
    }
    //endregion
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

    private void onSelectedIssueChanged() {
        invokeLater(() -> {
            // ensure an item is selected, preferably at the "last selected index"
            if (getSelectedIssue() == null) {
                int size = getListModel().getSize();
                if (size > 0) {
                    int i = limit(lastSelectedIndex, 0, size - 1);
                    issuesList.setSelectedIndex(i);
                }
            }

            // remember the last selected index
            int selectedIndex = issuesList.getSelectedIndex();
            if (selectedIndex >= 0) {
                lastSelectedIndex = selectedIndex;
            }

            ensureSelectionIsVisible(issuesList);
        });
    }

}
