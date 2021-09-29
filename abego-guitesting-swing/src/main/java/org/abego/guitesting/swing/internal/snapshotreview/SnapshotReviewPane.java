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
import org.abego.guitesting.swing.ScreenCaptureSupport.SnapshotIssue;
import org.abego.guitesting.swing.internal.util.Util;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.io.File;

import static javax.swing.SwingUtilities.invokeLater;
import static org.abego.commons.io.FileUtil.toFile;
import static org.abego.commons.lang.IntUtil.limit;
import static org.abego.guitesting.swing.internal.util.Util.bordered;
import static org.abego.guitesting.swing.internal.util.Util.button;
import static org.abego.guitesting.swing.internal.util.Util.copyFile;
import static org.abego.guitesting.swing.internal.util.Util.flowLeft;
import static org.abego.guitesting.swing.internal.util.Util.label;
import static org.abego.guitesting.swing.internal.util.Util.labelWithBorder;
import static org.abego.guitesting.swing.internal.util.Util.loadIcon;
import static org.abego.guitesting.swing.internal.util.Util.newAction;
import static org.abego.guitesting.swing.internal.util.Util.scrolling;

class SnapshotReviewPane extends JPanel {

    // @formatter:off
    private static final Color EXPECTED_BORDER_COLOR = new Color(0x59A869);
    private static final Color ACTUAL_BORDER_COLOR = new Color(0xC64D3F);
    private static final Color DIFFERENCE_BORDER_COLOR = new Color(0x6E6E6E);
    private static final int BORDER_SIZE = 3;

    private final DefaultListModel<? extends SnapshotIssue> issuesListModel;
    private final JList<? extends SnapshotIssue> issuesList;

    private final Action previousScreenshotAction = newAction("Previous (↑)", KeyStroke.getKeyStroke("UP"), e -> changeSelectedIndex(-1)); //NON-NLS
    private final Action nextScreenshotAction = newAction("Next (↓)", KeyStroke.getKeyStroke("DOWN"), e -> changeSelectedIndex(1)); //NON-NLS
    private final JLabel labelForName = label();
    private final JComponent imagesContainer = flowLeft();
    private final Action addAltenativeSnapshotAction = newAction("Make Actual an Alternative (A)", KeyStroke.getKeyStroke("A"), e -> addAltenativeSnapshot()); //NON-NLS
    private final Action overwriteSnapshotAction = newAction("Overwrite Expected (O)", KeyStroke.getKeyStroke("O"), e -> overwriteSnapshot()); //NON-NLS
    private final Action ignoreCurrentIssueAction = newAction("Ignore Issue (Esc)", KeyStroke.getKeyStroke("ESCAPE"), e -> ignoreCurrentIssue()); //NON-NLS
    private final JButton ignoreButton = button(ignoreCurrentIssueAction);
    private final JLabel[] labelsForImages = new JLabel[]{new JLabel(), new JLabel(), new JLabel()};

    private int expectedImageIndex;
    private final Action rotateImageLeftAction = newAction("Rotate Images Left (←)", KeyStroke.getKeyStroke("LEFT"), e -> rotateImagesLeft()); //NON-NLS
    private final Action rotateImageRightAction = newAction("Rotate Images Right (→)", KeyStroke.getKeyStroke("RIGHT"), e -> rotateImagesRight()); //NON-NLS
    // @formatter:on

    SnapshotReviewPane(Seq<? extends SnapshotIssue> issues) {
        this.issuesListModel = newIssueListModel(issues);
        this.issuesList = new JList<>(issuesListModel);

        initComponents();
        layoutComponents();
        invokeLater(() -> {
            // select the first issue in the list (if there is any)
            issuesList.setSelectedIndex(0);

            // make sure we have a focus
            ignoreButton.requestFocusInWindow();
        });
    }

    private static DefaultListModel<? extends SnapshotIssue> newIssueListModel(
            Seq<? extends SnapshotIssue> issues) {

        DefaultListModel<SnapshotIssue> listModel = new DefaultListModel<>();
        for (SnapshotIssue i : issues) {
            listModel.addElement(i);
        }
        return listModel;
    }

    private void initComponents() {
        issuesList.setVisibleRowCount(8);
        issuesList.setCellRenderer(new IssueListCellRenderer());
        issuesList.addListSelectionListener(e -> onSelectionChanged());
    }

    private void layoutComponents() {
        JComponent content = bordered()
                .top(topBar())
                .center(scrolling(imagesContainer))
                .bottom(bordered()
                        .top(flowLeft(
                                button(previousScreenshotAction),
                                button(nextScreenshotAction)))
                        .bottom(scrolling(issuesList)));

        setLayout(new BorderLayout());
        add(content);
    }

    private void onSelectionChanged() {invokeLater(this::updateImages);}

    private JComponent topBar() {
        return bordered()
                .top(flowLeft(
                        labelWithBorder(" Expected ", EXPECTED_BORDER_COLOR, BORDER_SIZE), //NON-NLS
                        labelWithBorder(" Actual ", ACTUAL_BORDER_COLOR, BORDER_SIZE), //NON-NLS
                        labelWithBorder(" Difference ", DIFFERENCE_BORDER_COLOR, BORDER_SIZE), //NON-NLS
                        labelForName))
                .bottom(flowLeft(
                        button(overwriteSnapshotAction),
                        button(addAltenativeSnapshotAction),
                        ignoreButton,
                        button(rotateImageLeftAction),
                        button(rotateImageRightAction)));
    }

    private void updateImages() {
        @Nullable
        SnapshotIssue selectedIssue = getSelectedIssueOrNull();

        imagesContainer.removeAll();
        if (selectedIssue != null) {
            expectedImageIndex = 0;
            //noinspection StringConcatenation
            labelForName.setText(" Snapshot: " + selectedIssue.getLabel()); //NON-NLS

            setImagesInLabels(selectedIssue);
            imagesContainer.add(labelsForImages[0]);
            imagesContainer.add(labelsForImages[1]);
            imagesContainer.add(labelsForImages[2]);
            imagesContainer.revalidate();
            repaint();
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    private SnapshotIssue getSelectedIssueOrNull() {
        return issuesList.getSelectedValue();
    }

    private void setImagesInLabels(@NonNull SnapshotIssue currentIssue) {
        setLabelForExpectedImage(labelsForImages[(expectedImageIndex) % 3], currentIssue);
        setLabelForActualImage(labelsForImages[(expectedImageIndex + 1) % 3], currentIssue);
        setLabelForDiffImage(labelsForImages[(expectedImageIndex + 2) % 3], currentIssue);
    }

    private void setLabelForExpectedImage(JLabel label, SnapshotIssue selectedIssue) {
        setLabelWithImageAndBorder(label, toFile(selectedIssue.getExpectedImage()), EXPECTED_BORDER_COLOR);
    }

    private void setLabelForActualImage(JLabel label, SnapshotIssue selectedIssue) {
        setLabelWithImageAndBorder(label, toFile(selectedIssue.getActualImage()), ACTUAL_BORDER_COLOR);
    }

    private void setLabelForDiffImage(JLabel label, SnapshotIssue selectedIssue) {
        setLabelWithImageAndBorder(label, toFile(selectedIssue.getDifferenceImage()), DIFFERENCE_BORDER_COLOR);
    }

    private void setLabelWithImageAndBorder(JLabel label, File file, Color borderColor) {
        label.setBorder(Util.lineBorder(borderColor, BORDER_SIZE));
        label.setIcon(loadIcon(file));
    }

    private void overwriteSnapshot() {
        @Nullable SnapshotIssue currentIssue = getSelectedIssueOrNull();
        if (currentIssue != null) {
            copyFile(
                    toFile(currentIssue.getActualImage()),
                    toFile(currentIssue.getOverwriteURL()));
            removeIssue(currentIssue);
        }
    }

    private void removeIssue(SnapshotIssue issue) {
        invokeLater(() -> {
            int i = issuesList.getSelectedIndex();
            issuesListModel.removeElement(issue);
            if (issuesListModel.size() > 0) {
                issuesList.setSelectedIndex(i);
            } else {
                imagesContainer.removeAll();
                imagesContainer.revalidate();
                repaint();
            }
        });
    }

    private void addAltenativeSnapshot() {
        @Nullable SnapshotIssue currentIssue = getSelectedIssueOrNull();
        if (currentIssue != null) {
            copyFile(
                    toFile(currentIssue.getActualImage()),
                    toFile(currentIssue.getAddAlternativeURL()));
            removeIssue(currentIssue);
        }
    }

    private void ignoreCurrentIssue() {
        @Nullable SnapshotIssue currentIssue = getSelectedIssueOrNull();
        if (currentIssue != null) {
            removeIssue(currentIssue);
        }
    }

    private void rotateImagesLeft() {rotateImagesHelper(2);}

    private void rotateImagesHelper(int increment) {
        expectedImageIndex = (expectedImageIndex + increment) % 3;
        setImagesInLabels();
    }

    private void setImagesInLabels() {
        @Nullable SnapshotIssue currentIssue = getSelectedIssueOrNull();
        if (currentIssue != null) {
            setImagesInLabels(currentIssue);
            revalidate();
            repaint();
        }
    }

    private void rotateImagesRight() {rotateImagesHelper(1);}

    private void changeSelectedIndex(int diff) {
        int size = issuesListModel.size();
        if (size > 0) {
            int newIndex = limit(issuesList.getSelectedIndex() + diff, size - 1);
            issuesList.setSelectedIndex(newIndex);
        }
    }

    private static class IssueListCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(
                JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            JLabel listCellRendererComponent =
                    (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof SnapshotIssue) {
                listCellRendererComponent.setText(((SnapshotIssue) value).getLabel());
            }
            return listCellRendererComponent;
        }
    }
}
