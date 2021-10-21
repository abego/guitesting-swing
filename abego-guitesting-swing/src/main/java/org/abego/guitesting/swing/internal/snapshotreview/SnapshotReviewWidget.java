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
import org.abego.guitesting.swing.internal.util.SeqUtil2;
import org.abego.guitesting.swing.internal.util.SwingUtil;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import javax.swing.Action;
import javax.swing.DefaultListModel;
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

import static javax.swing.BorderFactory.createLineBorder;
import static javax.swing.SwingUtilities.invokeLater;
import static org.abego.commons.io.FileUtil.toFile;
import static org.abego.guitesting.swing.internal.snapshotreview.ExpectedActualDifferenceImageViewerWidget.newExpectedActualDifferenceWidget;
import static org.abego.guitesting.swing.internal.util.FileUtil.copyFile;
import static org.abego.guitesting.swing.internal.util.SwingUtil.DEFAULT_FLOW_GAP;
import static org.abego.guitesting.swing.internal.util.SwingUtil.bordered;
import static org.abego.guitesting.swing.internal.util.SwingUtil.borderedWithTopLine;
import static org.abego.guitesting.swing.internal.util.SwingUtil.flowLeft;
import static org.abego.guitesting.swing.internal.util.SwingUtil.flowLeftWithBottomLine;
import static org.abego.guitesting.swing.internal.util.SwingUtil.iconButton;
import static org.abego.guitesting.swing.internal.util.SwingUtil.label;
import static org.abego.guitesting.swing.internal.util.SwingUtil.newAction;
import static org.abego.guitesting.swing.internal.util.SwingUtil.newDefaultListModel;
import static org.abego.guitesting.swing.internal.util.SwingUtil.newListCellRenderer;
import static org.abego.guitesting.swing.internal.util.SwingUtil.scrollingNoBorder;
import static org.abego.guitesting.swing.internal.util.SwingUtil.separatorBar;
import static org.abego.guitesting.swing.internal.util.SwingUtil.vlist;
import static org.abego.guitesting.swing.internal.util.UpdateableSwingUtil.checkBox;

//TODO: better split between init, style (e.g. border), layout, binding/updating
class SnapshotReviewWidget<T extends SnapshotIssue> implements Widget {


    /**
     * Defines the visual appearance of individual components, how they look like.
     */
    static class Style {
        private static final Color TITLE_BAR_COLOR = new Color(0xE2E6Ec);
        private static final int LEGEND_BORDER_SIZE = 2;
        private static final int VISIBLE_ISSUES_COUNT = 8;
        private static final int BULLET_SIZE = 24;
        private static final Font BULLET_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, BULLET_SIZE);
    }

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
    private final JPanel content = new JPanel();
    private final JLabel[] labelsForLegend;
    private final JLabel selectedIssueDescriptionLabel;
    private final JComponent imagesLegendContainer;
    private final ExpectedActualDifferenceImageViewerWidget expectedActualDifferenceImageViewerWidget;
    private final JComponent variantsIndicator;
    private final JButton ignoreButton;
    private final JCheckBoxUpdateable shrinkToFitCheckBox;
    private final JList<T> issuesList;

    // UI State
    private final DefaultListModel<T> issuesListModel;


    SnapshotReviewWidget(Seq<T> issues) {
        // init State
        issuesListModel = newDefaultListModel(
                issues.sortedBy(SnapshotIssue::getLabel));

        // init Actions
        addAltenativeSnapshotAction = newAction("Make Actual an Alternative (A)", KeyStroke.getKeyStroke("A"), Icons.alternativeIcon(), e -> addAltenativeSnapshot()); //NON-NLS
        ignoreCurrentIssueAction = newAction("Ignore Issue (Esc)", KeyStroke.getKeyStroke("ESCAPE"), Icons.ignoreIcon(), e -> ignoreCurrentIssue()); //NON-NLS
        nextScreenshotAction = newAction("Next issue (↓)", KeyStroke.getKeyStroke("DOWN"), Icons.nextIssueIcon(), e -> selectNextIssue()); //NON-NLS
        overwriteSnapshotAction = newAction("Overwrite Expected (O)", KeyStroke.getKeyStroke("O"), Icons.overwriteIcon(), e -> overwriteSnapshot()); //NON-NLS
        previousScreenshotAction = newAction("Previous issue (↑)", KeyStroke.getKeyStroke("UP"), Icons.previousIssueIcon(), e -> selectPreviousIssue()); //NON-NLS
        rotateImageAction = newAction("Rotate Images (→)", KeyStroke.getKeyStroke("RIGHT"), Icons.rotateRightIcon(), e -> rotateImages()); //NON-NLS;
        toggleShrinkToFitAction = newAction("Shrink to Fit (#)", KeyStroke.getKeyStroke("NUMBER_SIGN"), e -> toggleShrinkToFit()); //NON-NLS

        expectedActualDifferenceImageViewerWidget = newExpectedActualDifferenceWidget();
        // init Components
        ignoreButton = iconButton(ignoreCurrentIssueAction);
        issuesList = vlist(issuesListModel, l -> {
            l.setVisibleRowCount(Style.VISIBLE_ISSUES_COUNT);
            l.setCellRenderer(
                    newListCellRenderer(SnapshotIssue.class, SnapshotIssue::getLabel));
        });
        issuesList.setBorder(null);
        labelsForLegend = new JLabel[]{
                legendLabel(" Expected ", expectedActualDifferenceImageViewerWidget.getExpectedBorderColor()),//NON-NLS
                legendLabel(" Actual ", expectedActualDifferenceImageViewerWidget.getActualBorderColor()), //NON-NLS
                legendLabel(" Difference ", expectedActualDifferenceImageViewerWidget.getDifferenceBorderColor()) //NON-NLS)
        };
        imagesLegendContainer = flowLeft(DEFAULT_FLOW_GAP, 0, labelsForLegend);

        selectedIssueDescriptionLabel = label();
        shrinkToFitCheckBox = checkBox(this::getShrinkToFit, toggleShrinkToFitAction);
        variantsIndicator = flowLeft(DEFAULT_FLOW_GAP, 0);
        // make the panel so small only one bullet fits into the row,
        // so the bullets are stacked vertically
        variantsIndicator.setPreferredSize(new Dimension(Style.BULLET_SIZE, Integer.MAX_VALUE));
        variantsIndicator.setOpaque(true);
        variantsIndicator.setBackground(Color.white);
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

    private static JLabel legendLabel(String title, Color color) {
        JLabel label = new JLabel(title);
        label.setBorder(createLineBorder(color, Style.LEGEND_BORDER_SIZE));
        label.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
        label.setForeground(Color.GRAY);
        //noinspection StringConcatenation
        label.setToolTipText(
                title.trim() + " images (see below) have a border in this color and are located at this position in the sequence."); //NON-NLS
        return label;
    }

    private void selectPreviousIssue() {
        SwingUtil.changeSelectedIndex(issuesList, -1);
    }

    private void selectNextIssue() {
        SwingUtil.changeSelectedIndex(issuesList, 1);
    }

    private void layoutComponents() {
        JComponent content = bordered()
                .top(topPart())
                .left(variantsIndicator)
                .center(scrollingNoBorder(expectedActualDifferenceImageViewerWidget.getComponent()))
                .bottom(bottomPart());

        getComponent().setLayout(new BorderLayout());
        getComponent().add(content);
    }

    @Override
    public JComponent getComponent() {
        return content;
    }

    private void initNotifications() {
        issuesList.addListSelectionListener(e -> onSelectedIssueChanged());
    }

    private JComponent topPart() {
        return bordered()
                .top(flowLeftWithBottomLine(selectedIssueDescriptionLabel))
                .bottom(flowLeftWithBottomLine(DEFAULT_FLOW_GAP, 0,
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
        return borderedWithTopLine()
                .top(bordered(l -> l.setBackground(Style.TITLE_BAR_COLOR))
                        .left(flowLeft(DEFAULT_FLOW_GAP, 0, label("Issues:"))) //NON-NLS
                        .right(flowLeft(DEFAULT_FLOW_GAP, 0,
                                iconButton(previousScreenshotAction),
                                iconButton(nextScreenshotAction))))
                .bottom(scrollingNoBorder(issuesList));
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
        T issue = getSelectedIssue();
        if (issue == null) {
            return " ";
        }

        StringBuilder result = new StringBuilder();

        int variantsCount = getVariantsCount(issue);
        if (variantsCount > 1) {
            result.append("[");
            result.append(getVariantsIndex(issue) + 1);
            result.append(" of "); //NON-NLS
            result.append(variantsCount);
            result.append("] ");
        }
        // display the "simple" name of the snapshot first,
        // followed by the package and class part,
        // separated by a "-".
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

    private void overwriteSnapshot() {
        @Nullable T currentIssue = getSelectedIssue();
        if (currentIssue != null) {
            copyFile(
                    toFile(currentIssue.getActualImage()),
                    toFile(currentIssue.getOverwriteURL()));
            removeIssueAndVariants(currentIssue);
        }
    }

    @SuppressWarnings("ConstantConditions") // to remove "nullable" warning
    @Nullable
    private T getSelectedIssue() {
        return issuesList.getSelectedValue();
    }

    private void removeIssueAndVariants(T issue) {
        String name = issue.getSnapshotName();
        invokeLater(() -> {
            int selectedIndex = issuesList.getSelectedIndex();
            for (int i = issuesListModel.size() - 1; i >= 0; i--) {
                T issueInModel = issuesListModel.get(i);
                //noinspection CallToSuspiciousStringMethod
                if (issueInModel.getSnapshotName().equals(name)) {
                    issuesListModel.removeElementAt(i);
                }
            }
            issuesList.setSelectedIndex(selectedIndex);
        });
    }

    private void removeIssue(T issue) {
        invokeLater(() -> {
            int selectedIndex = issuesList.getSelectedIndex();
            issuesListModel.removeElement(issue);
            issuesList.setSelectedIndex(selectedIndex);
        });
    }

    private int getVariantsCount(T issue) {
        return getVariants(issue).size();
    }

    private int getVariantsIndex(T issue) {
        return getVariants(issue).indexOf(issue);
    }

    private Seq<T> getVariants(T issue) {
        //noinspection CallToSuspiciousStringMethod
        return SeqUtil2.newSeq(issuesListModel.elements()).filter(
                i -> i.getSnapshotName().equals(issue.getSnapshotName()));
    }

    private void addAltenativeSnapshot() {
        @Nullable T currentIssue = getSelectedIssue();
        if (currentIssue != null) {
            copyFile(
                    toFile(currentIssue.getActualImage()),
                    toFile(currentIssue.getAddAlternativeURL()));
            removeIssueAndVariants(currentIssue);
        }
    }

    private void ignoreCurrentIssue() {
        @Nullable T currentIssue = getSelectedIssue();
        if (currentIssue != null) {
            removeIssue(currentIssue);
        }
    }

    private void rotateImages() {
        setExpectedImageIndex((getExpectedImageIndex() + 1) % 3);
    }

    private int getExpectedImageIndex() {
        return expectedActualDifferenceImageViewerWidget.getExpectedImageIndex();
    }

    private void setExpectedImageIndex(int value) {
        expectedActualDifferenceImageViewerWidget.setExpectedImageIndex(value);
        onExpectedImageIndexChanged();
    }

    private void updateLabelsForLegend() {
        imagesLegendContainer.removeAll();
        imagesLegendContainer.add(labelsForLegend[(3 - getExpectedImageIndex()) % 3]);
        imagesLegendContainer.add(labelsForLegend[(4 - getExpectedImageIndex()) % 3]);
        imagesLegendContainer.add(labelsForLegend[(5 - getExpectedImageIndex()) % 3]);
        imagesLegendContainer.validate();
    }


    private void onSelectedIssueChanged() {
        invokeLater(() -> {
            expectedActualDifferenceImageViewerWidget.setSnapshotIssue(getSelectedIssue());
            updateVariantsIndicator();
            updateSelectedIssueDescriptionLabel();
            ensureSelectionIfPossible();
            if (issuesList.getSelectedIndex() >= 0) {
                issuesList.ensureIndexIsVisible(issuesList.getSelectedIndex());
            }
        });
    }


    private void onExpectedImageIndexChanged() {
        invokeLater(this::updateLabelsForLegend);
    }

    private void onShrinkToFitChanged() {
        invokeLater(shrinkToFitCheckBox::update);
    }


    private void updateVariantsIndicator() {
        @Nullable T selectedIssue = getSelectedIssue();
        variantsIndicator.removeAll();
        if (selectedIssue != null) {
            int n = getVariantsCount(selectedIssue);
            int sel = getVariantsIndex(selectedIssue);
            for (int i = 0; n > 1 && i < n; i++) {
                variantsIndicator.add(
                        label(i == sel ? "●" : "○", c -> c.setFont(Style.BULLET_FONT)));
            }
            variantsIndicator.repaint();
            variantsIndicator.revalidate();
        }
    }

    private boolean getShrinkToFit() {
        return expectedActualDifferenceImageViewerWidget.getShrinkToFit();
    }

    private void setShrinkToFit(boolean value) {
        expectedActualDifferenceImageViewerWidget.setShrinkToFit(value);
    }

    private void toggleShrinkToFit() {
        setShrinkToFit(!getShrinkToFit());
        onShrinkToFitChanged();
    }

}
