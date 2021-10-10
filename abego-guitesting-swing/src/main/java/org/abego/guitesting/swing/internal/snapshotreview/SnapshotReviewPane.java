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
import org.abego.guitesting.swing.internal.util.BorderedPanel;
import org.abego.guitesting.swing.internal.util.JCheckBoxUpdateable;
import org.abego.guitesting.swing.internal.util.SwingUtil;
import org.eclipse.jdt.annotation.NonNull;
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
import java.awt.Font;
import java.awt.Rectangle;
import java.util.Objects;

import static java.lang.Math.max;
import static javax.swing.BorderFactory.createLineBorder;
import static javax.swing.SwingUtilities.invokeLater;
import static org.abego.commons.io.FileUtil.toFile;
import static org.abego.guitesting.swing.internal.util.FileUtil.copyFile;
import static org.abego.guitesting.swing.internal.util.SwingUtil.DEFAULT_FLOW_GAP;
import static org.abego.guitesting.swing.internal.util.SwingUtil.bordered;
import static org.abego.guitesting.swing.internal.util.UpdateableSwingUtil.checkBox;
import static org.abego.guitesting.swing.internal.util.SwingUtil.flowLeft;
import static org.abego.guitesting.swing.internal.util.SwingUtil.flowLeftWithBottomLine;
import static org.abego.guitesting.swing.internal.util.SwingUtil.iconButton;
import static org.abego.guitesting.swing.internal.util.SwingUtil.label;
import static org.abego.guitesting.swing.internal.util.SwingUtil.newAction;
import static org.abego.guitesting.swing.internal.util.SwingUtil.newListCellRenderer;
import static org.abego.guitesting.swing.internal.util.SwingUtil.onComponentResized;
import static org.abego.guitesting.swing.internal.util.SwingUtil.scrolling;
import static org.abego.guitesting.swing.internal.util.SwingUtil.separatorBar;
import static org.abego.guitesting.swing.internal.util.SwingUtil.vlist;

class SnapshotReviewPane extends JPanel {

    private static final Color EXPECTED_BORDER_COLOR = new Color(0x59A869);
    private static final Color ACTUAL_BORDER_COLOR = new Color(0xC64D3F);
    private static final Color DIFFERENCE_BORDER_COLOR = new Color(0x6E6E6E);
    private static final int BORDER_SIZE = 3;
    private static final int LEGEND_BORDER_SIZE = 2;
    private static final int VISIBLE_ISSUES_COUNT = 8;

    // Actions
    private final Action addAltenativeSnapshotAction;
    @SuppressWarnings("FieldCanBeLocal")
    private final Action ignoreCurrentIssueAction;
    private final Action nextScreenshotAction;
    private final Action overwriteSnapshotAction;
    private final Action previousScreenshotAction;
    private final Action rotateImageAction;
    @SuppressWarnings("FieldCanBeLocal")
    private final Action toggleShrinkToFitAction;

    // Components (for more see #layoutComponent)
    private final JLabel[] labelsForImages;
    private final JLabel[] labelsForLegend;
    private final JLabel selectedIssueDescriptionLabel;
    private final JComponent imagesLegendContainer;
    private final JComponent imagesContainer;
    private final JButton ignoreButton;
    private final JCheckBoxUpdateable shrinkToFitCheckBox;
    private final JList<? extends SnapshotIssue> issuesList;

    // UI State
    private final DefaultListModel<? extends SnapshotIssue> issuesListModel;

    // State
    private int expectedImageIndex;
    private boolean shrinkToFit;
    @Nullable
    private SnapshotImages snapshotImages;

    SnapshotReviewPane(Seq<? extends SnapshotIssue> issues) {
        // init State
        issuesListModel = SwingUtil.newDefaultListModel(issues);
        shrinkToFit = true;

        // init Actions
        addAltenativeSnapshotAction = newAction("Make Actual an Alternative (A)", KeyStroke.getKeyStroke("A"), Icons.alternativeIcon(), e -> addAltenativeSnapshot()); //NON-NLS
        ignoreCurrentIssueAction = newAction("Ignore Issue (Esc)", KeyStroke.getKeyStroke("ESCAPE"), Icons.ignoreIcon(), e -> ignoreCurrentIssue()); //NON-NLS
        nextScreenshotAction = newAction("Next issue (↓)", KeyStroke.getKeyStroke("DOWN"), Icons.nextIssueIcon(), e -> selectNextIssue()); //NON-NLS
        overwriteSnapshotAction = newAction("Overwrite Expected (O)", KeyStroke.getKeyStroke("O"), Icons.overwriteIcon(), e -> overwriteSnapshot()); //NON-NLS
        previousScreenshotAction = newAction("Previous issue (↑)", KeyStroke.getKeyStroke("UP"), Icons.previousIssueIcon(), e -> selectPreviousIssue()); //NON-NLS
        rotateImageAction = newAction("Rotate Images (→)", KeyStroke.getKeyStroke("RIGHT"), Icons.rotateRightIcon(), e -> rotateImages()); //NON-NLS;
        toggleShrinkToFitAction = newAction("Shrink to Fit (#)", KeyStroke.getKeyStroke("NUMBER_SIGN"), e -> toggleShrinkToFit()); //NON-NLS

        // init Components
        ignoreButton = iconButton(ignoreCurrentIssueAction);
        issuesList = vlist(issuesListModel, l -> {
            l.setVisibleRowCount(VISIBLE_ISSUES_COUNT);
            l.setCellRenderer(
                    newListCellRenderer(SnapshotIssue.class, SnapshotIssue::getLabel));
        });
        labelsForImages = new JLabel[]{new JLabel(), new JLabel(), new JLabel()};
        labelsForLegend = new JLabel[]{
                legendLabel(" Expected ", EXPECTED_BORDER_COLOR),//NON-NLS
                legendLabel(" Actual ", ACTUAL_BORDER_COLOR), //NON-NLS
                legendLabel(" Difference ", DIFFERENCE_BORDER_COLOR) //NON-NLS)
        };
        imagesContainer = flowLeft(c -> {
            c.setOpaque(true);
            c.setBackground(Color.white);
        }, labelsForImages);
        imagesLegendContainer = flowLeft(DEFAULT_FLOW_GAP, 0, labelsForLegend);

        selectedIssueDescriptionLabel = label();
        shrinkToFitCheckBox = checkBox(this::getShrinkToFit, toggleShrinkToFitAction);

        layoutComponents();

        // Notifications support
        initNotifications();

        // More initialization
        invokeLater(() -> {
            // select the first issue in the list (if there is any)
            issuesList.setSelectedIndex(0);

            // make sure we have a focus
            ignoreButton.requestFocusInWindow();
        });
    }

    private void selectPreviousIssue() {
        SwingUtil.changeSelectedIndex(issuesList, -1);
    }

    private void selectNextIssue() {
        SwingUtil.changeSelectedIndex(issuesList,1);
    }

    private static JLabel legendLabel(String title, Color color) {
        JLabel label = new JLabel(title);
        label.setBorder(createLineBorder(color, LEGEND_BORDER_SIZE));
        label.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
        label.setForeground(Color.GRAY);
        return label;
    }

    private void layoutComponents() {
        JComponent content = bordered()
                .top(topPart())
                .center(scrolling(imagesContainer))
                .bottom(bottomPart());

        setLayout(new BorderLayout());
        add(content);
    }

    private void initNotifications() {
        issuesList.addListSelectionListener(e -> onSelectedIssueChanged());
        onComponentResized(imagesContainer, e -> onImagesContainerVisibleRectChanged());
    }

    private JComponent topPart() {
        return bordered()
                .top(flowLeftWithBottomLine(selectedIssueDescriptionLabel))
                .bottom(flowLeft(DEFAULT_FLOW_GAP, 0,
                        iconButton(overwriteSnapshotAction),
                        iconButton(addAltenativeSnapshotAction),
                        ignoreButton,
                        separatorBar(),
                        imagesLegendContainer,
                        iconButton(rotateImageAction),
                        separatorBar(),
                        shrinkToFitCheckBox,
                        separatorBar()));
    }

    @NonNull
    private BorderedPanel bottomPart() {
        return bordered()
                .top(bordered()
                        .left(flowLeft(DEFAULT_FLOW_GAP, 0, label("Issues:"))) //NON-NLS
                        .right(flowLeft(DEFAULT_FLOW_GAP, 0,
                                iconButton(previousScreenshotAction),
                                iconButton(nextScreenshotAction))))
                .bottom(scrolling(issuesList));
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

    private void updateSelectedIssueDescriptionLabel() {
        selectedIssueDescriptionLabel.setText(getSelectedIssueDescription());
    }

    private String getSelectedIssueDescription() {
        @Nullable
        SnapshotIssue issue = getSelectedIssue();
        return issue != null ? issue.getLabel() : " ";//NON-NLS
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

    @SuppressWarnings("ConstantConditions") // to remove "nullable" warning
    @Nullable
    private SnapshotIssue getSelectedIssue() {
        return issuesList.getSelectedValue();
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

    private void updateLabelsForLegend() {
        imagesLegendContainer.removeAll();
        imagesLegendContainer.add(labelsForLegend[(3 - expectedImageIndex) % 3]);
        imagesLegendContainer.add(labelsForLegend[(4 - expectedImageIndex) % 3]);
        imagesLegendContainer.add(labelsForLegend[(5 - expectedImageIndex) % 3]);
        imagesLegendContainer.validate();
    }


    private void onSelectedIssueChanged() {
        invokeLater(() -> {
            updateLabelsForImages();
            updateImagesContainer();
            updateSelectedIssueDescriptionLabel();
            ensureSelectionIfPossible();
        });
    }

    private void onImagesContainerVisibleRectChanged() {
        if (getShrinkToFit()) {
            invokeLater(this::updateLabelsForImages);
        }
    }

    private void onLabelsForImagesChanged() {
        invokeLater(this::updateImagesContainer);
    }

    private void onExpectedImageIndexChanged() {
        invokeLater(() -> {
            updateLabelsForImages();
            updateLabelsForLegend();
        });
    }

    private void onShrinkToFitChanged() {
        invokeLater(() -> {
            shrinkToFitCheckBox.update();
            updateLabelsForImages();
        });
    }

    @Nullable
    private SnapshotImages getSnapshotImages() {
        @Nullable SnapshotIssue issue = getSelectedIssue();
        if (issue == null) {
            return null;
        }
        @Nullable SnapshotImages images = snapshotImages;
        if (images == null
                || !Objects.equals(images.getIssue(), issue)
                || !Objects.equals(getImagesArea(), images.getArea())) {
            images = new SnapshotImages(issue, getImagesArea());
            snapshotImages = images;
        }
        return images;
    }

    private void setIconAndLinedBorder(JLabel label, ImageIcon icon, Color borderColor) {
        label.setIcon(icon);
        label.setBorder(SwingUtil.lineBorder(borderColor, BORDER_SIZE));
    }

    @Nullable
    private Dimension getImagesArea() {
        if (getShrinkToFit()) {
            Rectangle visibleRect = imagesContainer.getVisibleRect();
            int w = visibleRect.width - 4 * SwingUtil.DEFAULT_FLOW_GAP - 6 * BORDER_SIZE;
            int h = visibleRect.height - 2 * SwingUtil.DEFAULT_FLOW_GAP - 2 * BORDER_SIZE;
            return new Dimension(max(0, w), max(0, h));
        } else {
            return null;
        }
    }

    private void updateImagesContainer() {
        boolean hasSelection = getSelectedIssue() != null;
        SwingUtil.setVisible(hasSelection, labelsForImages);
    }

    public boolean getShrinkToFit() {
        return shrinkToFit;
    }

    private void toggleShrinkToFit() {
        shrinkToFit = !shrinkToFit;
        onShrinkToFitChanged();
    }

}
