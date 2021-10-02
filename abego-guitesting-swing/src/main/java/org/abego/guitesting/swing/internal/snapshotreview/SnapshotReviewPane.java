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
import org.abego.guitesting.swing.internal.Icons;
import org.abego.guitesting.swing.internal.util.Util;
import org.eclipse.jdt.annotation.Nullable;

import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.Objects;

import static java.lang.Math.max;
import static javax.swing.SwingUtilities.invokeLater;
import static org.abego.commons.io.FileUtil.toFile;
import static org.abego.commons.lang.IntUtil.limit;
import static org.abego.guitesting.swing.internal.util.Util.DEFAULT_FLOW_GAP;
import static org.abego.guitesting.swing.internal.util.Util.bordered;
import static org.abego.guitesting.swing.internal.util.Util.button;
import static org.abego.guitesting.swing.internal.util.Util.copyFile;
import static org.abego.guitesting.swing.internal.util.Util.flowLeft;
import static org.abego.guitesting.swing.internal.util.Util.label;
import static org.abego.guitesting.swing.internal.util.Util.labelWithBorder;
import static org.abego.guitesting.swing.internal.util.Util.newAction;
import static org.abego.guitesting.swing.internal.util.Util.newListCellRenderer;
import static org.abego.guitesting.swing.internal.util.Util.onComponentResized;
import static org.abego.guitesting.swing.internal.util.Util.scrolling;
import static org.abego.guitesting.swing.internal.util.Util.transparentButton;

class SnapshotReviewPane extends JPanel {

    private static final Color EXPECTED_BORDER_COLOR = new Color(0x59A869);
    private static final Color ACTUAL_BORDER_COLOR = new Color(0xC64D3F);
    private static final Color DIFFERENCE_BORDER_COLOR = new Color(0x6E6E6E);
    private static final int BORDER_SIZE = 3;
    private static final int VISIBLE_ISSUES_COUNT = 8;

    private final DefaultListModel<? extends SnapshotIssue> issuesListModel;
    private final JList<? extends SnapshotIssue> issuesList;
    private final Action previousScreenshotAction = newAction("Previous (↑)", KeyStroke.getKeyStroke("UP"), e -> changeSelectedIndex(-1)); //NON-NLS
    private final Action nextScreenshotAction = newAction("Next (↓)", KeyStroke.getKeyStroke("DOWN"), e -> changeSelectedIndex(1)); //NON-NLS
    private final JLabel selectedIssueDescriptionLabel = label();
    private final JComponent imagesContainer = flowLeft();
    private final Action addAltenativeSnapshotAction = newAction("Make Actual an Alternative (A)", KeyStroke.getKeyStroke("A"), Icons.alternativeIcon(), e -> addAltenativeSnapshot()); //NON-NLS
    private final Action overwriteSnapshotAction = newAction("Overwrite Expected (O)", KeyStroke.getKeyStroke("O"), Icons.overwriteIcon(), e -> overwriteSnapshot()); //NON-NLS
    private final Action ignoreCurrentIssueAction = newAction("Ignore Issue (Esc)", KeyStroke.getKeyStroke("ESCAPE"), Icons.ignoreIcon(), e -> ignoreCurrentIssue()); //NON-NLS
    private final JButton ignoreButton = button(ignoreCurrentIssueAction);
    private final JLabel[] labelsForImages = new JLabel[]{new JLabel(), new JLabel(), new JLabel()};
    private int expectedImageIndex;
    private final Action rotateImageAction = newAction("", KeyStroke.getKeyStroke("RIGHT"), "Rotate Images (→)", Icons.rotateRightIcon(), e -> rotateImages()); //NON-NLS
    @Nullable
    private SnapshotImages snapshotImages;

    SnapshotReviewPane(Seq<? extends SnapshotIssue> issues) {
        this.issuesListModel = Util.newDefaultListModel(issues);
        this.issuesList = new JList<>(issuesListModel);

        initComponents();
        styleComponents();
        layoutComponents();

        invokeLater(() -> {
            // select the first issue in the list (if there is any)
            issuesList.setSelectedIndex(0);

            // make sure we have a focus
            ignoreButton.requestFocusInWindow();
        });
    }

    private void initComponents() {
        Util.addAll(imagesContainer, labelsForImages);

        issuesList.setVisibleRowCount(VISIBLE_ISSUES_COUNT);
        issuesList.setCellRenderer(
                newListCellRenderer(SnapshotIssue.class, SnapshotIssue::getLabel));
        issuesList.addListSelectionListener(e -> onSelectedIssueChanged());
        onComponentResized(imagesContainer, e -> onImagesAreaChanges());
    }

    private void styleComponents() {
        imagesContainer.setBackground(Color.white);
        imagesContainer.setOpaque(true);
    }

    private void layoutComponents() {
        JComponent content = bordered()
                .top(topBar())
                .center(scrolling(imagesContainer))
                .bottom(bordered()
                        .top(flowLeft(
                                label("Issues                "), //NON-NLS
                                button(previousScreenshotAction),
                                button(nextScreenshotAction)))
                        .bottom(scrolling(issuesList)));

        setLayout(new BorderLayout());
        add(content);
    }

    //TODO: derive the code for the "on...Changed" methods from the
    //  @DependsOn annotations
    private void onLabelsForImagesChanged() {
        invokeLater(this::updateImagesContainer);
    }

    private void onSelectedIssueChanged() {
        invokeLater(() -> {
            updateLabelsForImages();
            updateImagesContainer();
            updateSelectedIssueDescriptionLabel();
            ensureSelectionIfPossible();
        });
    }

    private void onImagesAreaChanges() {
        invokeLater(this::updateLabelsForImages);
    }

    private void onExpectedImageIndexChanged() {
        invokeLater(this::updateLabelsForImages);
    }

    private JComponent topBar() {
        return bordered()
                .top(flowLeft(
                        labelWithBorder(" Expected ", EXPECTED_BORDER_COLOR, BORDER_SIZE), //NON-NLS
                        labelWithBorder(" Actual ", ACTUAL_BORDER_COLOR, BORDER_SIZE), //NON-NLS
                        labelWithBorder(" Difference ", DIFFERENCE_BORDER_COLOR, BORDER_SIZE), //NON-NLS
                        transparentButton(rotateImageAction)))
                .center(flowLeft(DEFAULT_FLOW_GAP, 0,
                        button(overwriteSnapshotAction),
                        button(addAltenativeSnapshotAction),
                        ignoreButton))
                .bottom(flowLeft(selectedIssueDescriptionLabel));
    }

    @DependsOn({"selectedIssue"})
    private void updateImagesContainer() {
        boolean hasSelection = getSelectedIssue() != null;
        Util.setVisible(hasSelection, labelsForImages);
    }

    /**
     * When no item is selected but there are items in the list
     * automatically select the first item.
     */
    @DependsOn("selectedIssue")
    private void ensureSelectionIfPossible() {
        if (getSelectedIssue() == null && issuesListModel.size() > 0) {
            invokeLater(() -> issuesList.setSelectedIndex(0));
        }
    }

    @DependsOn("selectedIssueDescription")
    private void updateSelectedIssueDescriptionLabel() {
        selectedIssueDescriptionLabel.setText(getSelectedIssueDescription());
    }

    @DependsOn({"snapshotImages", "expectedImageIndex"})
    private void updateLabelsForImages() {
        @Nullable SnapshotImages images = getSnapshotImages();
        if (images != null) {
            setIconAndLinedBorder(
                    labelsForImages[(expectedImageIndex) % 3],
                    images.getExpectedImage(),
                    EXPECTED_BORDER_COLOR);
            setIconAndLinedBorder(
                    labelsForImages[(expectedImageIndex + 1) % 3],
                    images.getActualImage(),
                    ACTUAL_BORDER_COLOR);
            setIconAndLinedBorder(
                    labelsForImages[(expectedImageIndex + 2) % 3],
                    images.getDifferenceImage(),
                    DIFFERENCE_BORDER_COLOR);

            onLabelsForImagesChanged();
        }
    }

    @DependsOn("selectedIssue")
    private String getSelectedIssueDescription() {
        @Nullable
        SnapshotIssue issue = getSelectedIssue();
        return issue != null ? issue.getLabel() : " ";//NON-NLS
    }


    @SuppressWarnings("DuplicateStringLiteralInspection")
    @DependsOn({"selectedIssue", "imagesArea"})
    @Nullable
    private SnapshotImages getSnapshotImages() {
        @Nullable SnapshotIssue issue = getSelectedIssue();
        if (issue == null) {
            return null;
        }
        @Nullable SnapshotImages images = snapshotImages;
        if (images == null
                || images.getIssue() != issue
                || !Objects.equals(getImagesArea(), images.getArea())) {
            images = new SnapshotImages(issue, getImagesArea(), e -> {});
            snapshotImages = images;
        }
        return images;
    }

    @Nullable
    private Dimension getImagesArea() {
        Rectangle visibleRect = imagesContainer.getVisibleRect();
        int w = visibleRect.width - 4 * Util.DEFAULT_FLOW_GAP - 6 * BORDER_SIZE;
        int h = visibleRect.height - 2 * Util.DEFAULT_FLOW_GAP - 2 * BORDER_SIZE;
        return new Dimension(max(0, w), max(0, h));
    }

    private void setIconAndLinedBorder(JLabel label, ImageIcon icon, Color borderColor) {
        label.setIcon(icon);
        label.setBorder(Util.lineBorder(borderColor, BORDER_SIZE));
    }

    private void overwriteSnapshot() {
        @Nullable SnapshotIssue currentIssue = getSelectedIssue();
        if (currentIssue != null) {
            copyFile(
                    toFile(currentIssue.getActualImage()),
                    toFile(currentIssue.getOverwriteURL()));
            removeIssue(currentIssue);
        }
    }

    private void removeIssue(SnapshotIssue issue) {
        invokeLater(() -> issuesListModel.removeElement(issue));
    }

    private void addAltenativeSnapshot() {
        @Nullable SnapshotIssue currentIssue = getSelectedIssue();
        if (currentIssue != null) {
            copyFile(
                    toFile(currentIssue.getActualImage()),
                    toFile(currentIssue.getAddAlternativeURL()));
            removeIssue(currentIssue);
        }
    }

    private void ignoreCurrentIssue() {
        @Nullable SnapshotIssue currentIssue = getSelectedIssue();
        if (currentIssue != null) {
            removeIssue(currentIssue);
        }
    }

    private void rotateImages() {
        expectedImageIndex = (expectedImageIndex + 1) % 3;
        onExpectedImageIndexChanged();
    }

    @SuppressWarnings("ConstantConditions") // to remove "nullable" warning
    @Nullable
    private SnapshotIssue getSelectedIssue() {
        return issuesList.getSelectedValue();
    }

    private void changeSelectedIndex(int diff) {
        int size = issuesListModel.size();
        if (size > 0) {
            int newIndex = limit(issuesList.getSelectedIndex() + diff, size - 1);
            issuesList.setSelectedIndex(newIndex);
        }
    }
}
