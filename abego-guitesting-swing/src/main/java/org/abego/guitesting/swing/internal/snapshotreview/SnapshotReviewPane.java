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
import static org.abego.guitesting.swing.internal.util.SwingUtil.borderedWithTopLine;
import static org.abego.guitesting.swing.internal.util.SwingUtil.flowLeft;
import static org.abego.guitesting.swing.internal.util.SwingUtil.flowLeftWithBottomLine;
import static org.abego.guitesting.swing.internal.util.SwingUtil.iconButton;
import static org.abego.guitesting.swing.internal.util.SwingUtil.label;
import static org.abego.guitesting.swing.internal.util.SwingUtil.newAction;
import static org.abego.guitesting.swing.internal.util.SwingUtil.newDefaultListModel;
import static org.abego.guitesting.swing.internal.util.SwingUtil.newListCellRenderer;
import static org.abego.guitesting.swing.internal.util.SwingUtil.onComponentResized;
import static org.abego.guitesting.swing.internal.util.SwingUtil.scrollingNoBorder;
import static org.abego.guitesting.swing.internal.util.SwingUtil.separatorBar;
import static org.abego.guitesting.swing.internal.util.SwingUtil.vlist;
import static org.abego.guitesting.swing.internal.util.UpdateableSwingUtil.checkBox;

//TODO: better split between init, style (e.g. border), layout, binding/updating
class SnapshotReviewPane<T extends SnapshotIssue> extends JPanel {

    private static final Color EXPECTED_BORDER_COLOR = new Color(0x59A869);
    private static final Color ACTUAL_BORDER_COLOR = new Color(0xC64D3F);
    private static final Color DIFFERENCE_BORDER_COLOR = new Color(0x6E6E6E);
    private static final Color TITLE_BAR_COLOR = new Color(0xE2E6Ec);
    private static final int BORDER_SIZE = 3;
    private static final int LEGEND_BORDER_SIZE = 2;
    private static final int VISIBLE_ISSUES_COUNT = 8;
    private static final int BULLET_SIZE = 24;

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
    private final JComponent variantsIndicator;
    private final JButton ignoreButton;
    private final JCheckBoxUpdateable shrinkToFitCheckBox;
    private final JList<T> issuesList;

    // UI State
    private final DefaultListModel<T> issuesListModel;

    // State
    private int expectedImageIndex;
    private boolean shrinkToFit;
    @Nullable
    private SnapshotImages snapshotImages;

    SnapshotReviewPane(Seq<T> issues) {
        // init State
        issuesListModel = newDefaultListModel(
                issues.sortedBy(SnapshotIssue::getLabel));
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
        issuesList.setBorder(null);
        labelsForLegend = new JLabel[]{
                legendLabel(" Expected ", EXPECTED_BORDER_COLOR),//NON-NLS
                legendLabel(" Actual ", ACTUAL_BORDER_COLOR), //NON-NLS
                legendLabel(" Difference ", DIFFERENCE_BORDER_COLOR) //NON-NLS)
        };
        labelsForImages = new JLabel[]{new JLabel(), new JLabel(), new JLabel()};
        imagesContainer = flowLeft(c -> {
            c.setOpaque(true);
            c.setBackground(Color.white);
            c.setBorder(null);
        }, labelsForImages);
        imagesLegendContainer = flowLeft(DEFAULT_FLOW_GAP, 0, labelsForLegend);

        selectedIssueDescriptionLabel = label();
        shrinkToFitCheckBox = checkBox(this::getShrinkToFit, toggleShrinkToFitAction);
        variantsIndicator = flowLeft(DEFAULT_FLOW_GAP,0);
        // make the panel so small only one bullet fits into the row,
        // so the bullets are stacked vertically
        variantsIndicator.setPreferredSize(new Dimension(BULLET_SIZE, 0));
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
        label.setBorder(createLineBorder(color, LEGEND_BORDER_SIZE));
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
                .center(scrollingNoBorder(imagesContainer))
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
                .top(bordered(l -> l.setBackground(TITLE_BAR_COLOR))
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
            updateVariantsIndicator();
            updateSelectedIssueDescriptionLabel();
            ensureSelectionIfPossible();
            if (issuesList.getSelectedIndex() >= 0) {
                issuesList.ensureIndexIsVisible(issuesList.getSelectedIndex());
            }
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
        SwingUtil.setVisible(getSelectedIssue() != null, labelsForImages);
    }

    private void updateVariantsIndicator() {
        @Nullable T selectedIssue = getSelectedIssue();
        variantsIndicator.setVisible(selectedIssue != null);
        if (selectedIssue != null) {
            variantsIndicator.removeAll();
            int n = getVariantsCount(selectedIssue);
            int sel = getVariantsIndex(selectedIssue);
            for (int i = 0; n > 1 && i < n; i++) {
                Font font = new Font(Font.SANS_SERIF, Font.PLAIN, BULLET_SIZE);
                variantsIndicator.add(label(i == sel ? "●" : "○", c -> {
                    c.setFont(font);
                    c.setPreferredSize(new Dimension(BULLET_SIZE, BULLET_SIZE));
                }));
            }
            variantsIndicator.repaint();
            variantsIndicator.revalidate();
        }
    }

    public boolean getShrinkToFit() {
        return shrinkToFit;
    }

    private void toggleShrinkToFit() {
        shrinkToFit = !shrinkToFit;
        onShrinkToFitChanged();
    }

}
