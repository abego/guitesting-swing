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

import org.abego.event.EventServices;
import org.abego.guitesting.swing.ScreenCaptureSupport.SnapshotIssue;
import org.abego.guitesting.swing.internal.util.Widget;
import org.abego.guitesting.swing.internal.util.prop.Prop;
import org.abego.guitesting.swing.internal.util.prop.PropNullable;
import org.abego.guitesting.swing.internal.util.prop.PropNullableBindable;
import org.abego.guitesting.swing.internal.util.SwingUtil;
import org.eclipse.jdt.annotation.Nullable;

import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import java.awt.Color;
import java.util.Objects;

import static javax.swing.SwingUtilities.invokeLater;
import static org.abego.commons.lang.IntUtil.limit;
import static org.abego.guitesting.swing.internal.util.BorderUtil.borderTopLighterGray;
import static org.abego.guitesting.swing.internal.util.Bordered.bordered;
import static org.abego.guitesting.swing.internal.util.prop.Prop.newProp;
import static org.abego.guitesting.swing.internal.util.prop.PropNullableBindable.newPropNullableBindable;
import static org.abego.guitesting.swing.internal.util.SwingUtil.DEFAULT_FLOW_GAP;
import static org.abego.guitesting.swing.internal.util.SwingUtil.ensureSelectionIsVisible;
import static org.abego.guitesting.swing.internal.util.SwingUtil.flowLeft;
import static org.abego.guitesting.swing.internal.util.SwingUtil.label;
import static org.abego.guitesting.swing.internal.util.SwingUtil.newAction;
import static org.abego.guitesting.swing.internal.util.SwingUtil.newListCellRenderer;
import static org.abego.guitesting.swing.internal.util.SwingUtil.scrollingNoBorder;
import static org.abego.guitesting.swing.internal.util.SwingUtil.toolbarButton;

//TODO Generalized to a "VList" widget?
class SnapshotIssuesVList implements Widget {

    //region State/Model
    //region issuesListModel
    private final Prop<ListModel<SnapshotIssue>> issuesListModelProp =
            newProp(new DefaultListModel<>(), this, "issuesListModel");

    public ListModel<SnapshotIssue> getIssuesListModel() {
        return issuesListModelProp.get();
    }

    public void setIssuesListModel(ListModel<SnapshotIssue> listModel) {
        issuesListModelProp.set(listModel);
    }

    //endregion
    private int lastSelectedIndex = -1;

    //endregion
    //region Actions
    private final Action previousScreenshotAction = newAction("Previous issue (↑)", KeyStroke.getKeyStroke("UP"), Icons.previousIssueIcon(), e -> selectPreviousIssue()); //NON-NLS
    private final Action nextScreenshotAction = newAction("Next issue (↓)", KeyStroke.getKeyStroke("DOWN"), Icons.nextIssueIcon(), e -> selectNextIssue()); //NON-NLS;

    private void selectPreviousIssue() {
        SwingUtil.changeSelectedIndex(issuesList, -1);
    }

    private void selectNextIssue() {
        SwingUtil.changeSelectedIndex(issuesList, 1);
    }

    //endregion
    //region Components
    private final JButton previousScreenshotButton = toolbarButton();
    private final JButton nextScreenshotButton = toolbarButton();
    private final JComponent topBar = new JPanel();
    private final JList<SnapshotIssue> issuesList = new JList<>();
    private final JComponent content = new JPanel();

    private void initComponents() {
        issuesList.setCellRenderer(newListCellRenderer(
                SnapshotIssue.class, SnapshotIssueUtil::labelWithLastPartFirst));
    }

    //endregion
    //region Construction
    private SnapshotIssuesVList() {
        initComponents();
        styleComponents();
        layoutComponents();
        initBindings();
    }

    public static SnapshotIssuesVList snapshotIssuesVList() {
        return new SnapshotIssuesVList();
    }

    //endregion
    //region selectedIssue
    private final PropNullableBindable<SnapshotIssue> selectedIssueProp =
            newPropNullableBindable(null, this, "selectedIssue");

    @Nullable
    public SnapshotIssue getSelectedIssue() {
        return selectedIssueProp.get();
    }

    public void setSelectedIssue(@Nullable SnapshotIssue value) {
        selectedIssueProp.set(value);
    }

    public void bindSelectedIssueTo(PropNullable<SnapshotIssue> prop) {
        selectedIssueProp.bindTo(prop);
    }

    //endregion
    //region Widget related
    @Override
    public JComponent getContent() {
        return content;
    }

    //endregion
    //region Style related
    private final static Color TOP_BAR_COLOR = new Color(0xE2E6Ec);

    private void styleComponents() {
        topBar.setBackground(TOP_BAR_COLOR);
        issuesList.setBorder(null);
    }

    //endregion
    //region Layout related
    private void layoutComponents() {
        bordered(topBar)
                .left(flowLeft(DEFAULT_FLOW_GAP, 0, label("Issues:"))) //NON-NLS
                .right(flowLeft(DEFAULT_FLOW_GAP, 0,
                        previousScreenshotButton,
                        nextScreenshotButton));

        bordered(content)
                .top(topBar)
                .bottom(scrollingNoBorder(issuesList));
    }

    //endregion
    //region Binding related
    private void initBindings() {
        previousScreenshotButton.setAction(previousScreenshotAction);
        nextScreenshotButton.setAction(nextScreenshotAction);

        EventServices.getDefault().addPropertyObserver(issuesListModelProp, e -> onIssuesListModelPropChanged());
        issuesList.addListSelectionListener(e -> onSelectedIssueInUIChanged());
        EventServices.getDefault().addPropertyObserver(selectedIssueProp, e -> onSelectedIssuePropChanged());
    }

    private void onSelectedIssueInUIChanged() {
        updateSelectedIssueProp();

        invokeLater(() -> {
            // ensure an item is selected, preferably at the "last selected index"
            if (getSelectedIssue() == null) {
                int size = getIssuesListModel().getSize();
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

            // scroll list if required
            ensureSelectionIsVisible(issuesList);
        });
    }

    private void updateSelectedIssueProp() {
        SnapshotIssue value = issuesList.getSelectedValue();
        if (!Objects.equals(getSelectedIssue(), value)) {
            selectedIssueProp.set(value);
        }
    }

    private void onSelectedIssuePropChanged() {
        invokeLater(() -> issuesList.setSelectedValue(selectedIssueProp.get(), true));
    }

    private void onIssuesListModelPropChanged() {
        issuesList.setModel(getIssuesListModel());
    }

    //endregion
}
