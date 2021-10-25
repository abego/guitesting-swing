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
import org.abego.guitesting.swing.internal.util.JCheckBoxUpdateable;
import org.abego.guitesting.swing.internal.util.SeqUtil2;
import org.eclipse.jdt.annotation.Nullable;

import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import java.awt.Color;

import static javax.swing.SwingUtilities.invokeLater;
import static org.abego.commons.io.FileUtil.toFile;
import static org.abego.guitesting.swing.internal.snapshotreview.ExpectedActualDifferenceImageView.expectedActualDifferenceImageView;
import static org.abego.guitesting.swing.internal.snapshotreview.ImagesLegend.imagesLegend;
import static org.abego.guitesting.swing.internal.snapshotreview.SnapshotIssuesVList.snapshotIssuesVList;
import static org.abego.guitesting.swing.internal.snapshotreview.VariantsIndicator.variantsIndicator;
import static org.abego.guitesting.swing.internal.snapshotreview.VariantsInfoImpl.variantsInfo;
import static org.abego.guitesting.swing.internal.util.Bordered.bordered;
import static org.abego.guitesting.swing.internal.util.FileUtil.copyFile;
import static org.abego.guitesting.swing.internal.util.JCheckBoxUpdateable.checkBoxUpdateable;
import static org.abego.guitesting.swing.internal.util.SwingUtil.DEFAULT_FLOW_GAP;
import static org.abego.guitesting.swing.internal.util.SwingUtil.flowLeftWithBottomLine;
import static org.abego.guitesting.swing.internal.util.SwingUtil.label;
import static org.abego.guitesting.swing.internal.util.SwingUtil.newAction;
import static org.abego.guitesting.swing.internal.util.SwingUtil.newDefaultListModel;
import static org.abego.guitesting.swing.internal.util.SwingUtil.scrollingNoBorder;
import static org.abego.guitesting.swing.internal.util.SwingUtil.separatorBar;
import static org.abego.guitesting.swing.internal.util.SwingUtil.toolbarButton;

class SnapshotReviewWidget<T extends SnapshotIssue> implements Widget {

    //region State/Model
    private final DefaultListModel<T> remainingIssues;
    //endregion
    //region Actions
    private final Action addAlternativeSnapshotAction;
    private final Action overwriteSnapshotAction;
    @SuppressWarnings("FieldCanBeLocal")
    private final Action ignoreCurrentIssueAction;
    private final Action rotateImageAction;
    @SuppressWarnings("FieldCanBeLocal")
    private final Action toggleShrinkToFitAction;
    //endregion
    //region Components
    private final JLabel selectedIssueDescriptionLabel = label();
    private final JButton overwriteButton = toolbarButton();
    private final JButton addAlternativeButton = toolbarButton();
    private final JButton ignoreButton = toolbarButton();
    private final ImagesLegend imagesLegend = imagesLegend();
    private final JButton rotateButton = toolbarButton();
    private final JCheckBoxUpdateable shrinkToFitCheckBox = checkBoxUpdateable();
    private final VariantsIndicator<T> variantsIndicator = variantsIndicator();
    private final ExpectedActualDifferenceImageView expectedActualDifferenceImageView
            = expectedActualDifferenceImageView();
    private final SnapshotIssuesVList<T> snapshotIssuesVList = snapshotIssuesVList();
    private final JComponent content = new JPanel();

    //endregion
    //region Construction
    private SnapshotReviewWidget(Seq<T> issues) {
        // init State
        remainingIssues = newDefaultListModel(
                issues.sortedBy(SnapshotIssue::getLabel));

        // init Actions
        addAlternativeSnapshotAction = newAction("Make Actual an Alternative (A)", KeyStroke.getKeyStroke("A"), Icons.alternativeIcon(), e -> addAlternativeSnapshot()); //NON-NLS
        ignoreCurrentIssueAction = newAction("Ignore Issue (Esc)", KeyStroke.getKeyStroke("ESCAPE"), Icons.ignoreIcon(), e -> ignoreCurrentIssue()); //NON-NLS
        overwriteSnapshotAction = newAction("Overwrite Expected (O)", KeyStroke.getKeyStroke("O"), Icons.overwriteIcon(), e -> overwriteSnapshot()); //NON-NLS
        rotateImageAction = newAction("Rotate Images (→)", KeyStroke.getKeyStroke("RIGHT"), Icons.rotateRightIcon(), e -> rotateImages()); //NON-NLS;
        toggleShrinkToFitAction = newAction("Shrink to Fit (#)", KeyStroke.getKeyStroke("NUMBER_SIGN"), e -> toggleShrinkToFit()); //NON-NLS

        // init Components
        snapshotIssuesVList.setListModel(remainingIssues);
        shrinkToFitCheckBox.setSelectedCondition(this::getShrinkToFit);

        overwriteButton.setAction(overwriteSnapshotAction);
        addAlternativeButton.setAction(addAlternativeSnapshotAction);
        ignoreButton.setAction(ignoreCurrentIssueAction);
        rotateButton.setAction(rotateImageAction);
        shrinkToFitCheckBox.setAction(toggleShrinkToFitAction);

        styleComponents();
        layoutComponents();

        // Notifications support
        initBindings();

        // More initialization
        invokeLater(() -> {
            // make sure we have a focus
            ignoreButton.requestFocusInWindow();
        });
    }

    public static <T extends SnapshotIssue> SnapshotReviewWidget<T> snapshotReviewWidget(Seq<T> issues) {
        return new SnapshotReviewWidget<>(issues);
    }

    //endregion
    //region Properties
    private String getSelectedIssueDescription() {
        @Nullable
        VariantsInfo<T> info = getVariantsInfo();
        if (info == null) {
            return " ";
        }

        String label = SnapshotIssueUtil.labelWithLastPartFirst(info.getIssue());
        int variantsCount = info.getVariantsCount();
        return (variantsCount > 1)
                ? String.format("[%d of %d] %s", //NON-NLS
                info.getVariantsIndex() + 1, variantsCount, label)
                : label;
    }

    private int getExpectedImageIndex() {
        return expectedActualDifferenceImageView.getExpectedImageIndex();
    }

    private void setExpectedImageIndex(int value) {
        expectedActualDifferenceImageView.setExpectedImageIndex(value);
        onExpectedImageIndexChanged();
    }

    private boolean getShrinkToFit() {
        return expectedActualDifferenceImageView.getShrinkToFit();
    }

    private void setShrinkToFit(boolean value) {
        expectedActualDifferenceImageView.setShrinkToFit(value);
    }

    private void toggleShrinkToFit() {
        setShrinkToFit(!getShrinkToFit());
        onShrinkToFitChanged();
    }

    @Nullable
    private T getSelectedIssue() {
        return snapshotIssuesVList.getSelectedIssue();
    }

    @Nullable
    private VariantsInfo<T> getVariantsInfo() {
        T issue = getSelectedIssue();
        if (issue == null) {
            return null;
        }

        //noinspection CallToSuspiciousStringMethod
        Seq<T> variants = SeqUtil2.newSeq(remainingIssues.elements())
                .filter(i -> i.getSnapshotName().equals(issue.getSnapshotName()));
        return variantsInfo(issue, variants);
    }

    //endregion
    //region Widget related
    @Override
    public JComponent getComponent() {
        return content;
    }
    //endregion
    //region Action related
    private void overwriteSnapshot() {
        @Nullable T currentIssue = getSelectedIssue();
        if (currentIssue != null) {
            copyFile(
                    toFile(currentIssue.getActualImage()),
                    toFile(currentIssue.getOverwriteURL()));
            removeIssueAndVariants(currentIssue);
        }
    }

    private void addAlternativeSnapshot() {
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

    private void removeIssue(T issue) {
        remainingIssues.removeElement(issue);
    }

    private void removeIssueAndVariants(T issue) {
        String name = issue.getSnapshotName();
        for (int i = remainingIssues.size() - 1; i >= 0; i--) {
            T issueInModel = remainingIssues.get(i);
            //noinspection CallToSuspiciousStringMethod
            if (issueInModel.getSnapshotName().equals(name)) {
                remainingIssues.removeElementAt(i);
            }
        }
    }

    private void rotateImages() {
        setExpectedImageIndex((getExpectedImageIndex() + 1) % 3);
    }

    //endregion
    //region Binding related
    private void initBindings() {
        snapshotIssuesVList.addSelectedIssueChangeListener(e -> onSelectedIssueChanged());
    }

    private void onSelectedIssueChanged() {
        expectedActualDifferenceImageView.setSnapshotIssue(getSelectedIssue());
        variantsIndicator.setVariantsInfo(getVariantsInfo());
        updateSelectedIssueDescriptionLabel();
    }

    private void onExpectedImageIndexChanged() {
        imagesLegend.setExpectedImageIndex(getExpectedImageIndex());
    }

    private void onShrinkToFitChanged() {
        shrinkToFitCheckBox.update();
    }

    private void updateSelectedIssueDescriptionLabel() {
        invokeLater(() ->
                selectedIssueDescriptionLabel.setText(getSelectedIssueDescription()));
    }

    //endregion
    //region Style related
    private static final Color EXPECTED_BORDER_COLOR = new Color(0x59A869);
    private static final Color ACTUAL_BORDER_COLOR = new Color(0xC64D3F);
    private static final Color DIFFERENCE_BORDER_COLOR = new Color(0x6E6E6E);

    private void styleComponents() {
        imagesLegend.setExpectedBorderColor(EXPECTED_BORDER_COLOR);
        imagesLegend.setActualBorderColor(ACTUAL_BORDER_COLOR);
        imagesLegend.setDifferenceBorderColor(DIFFERENCE_BORDER_COLOR);
        expectedActualDifferenceImageView.setExpectedBorderColor(EXPECTED_BORDER_COLOR);
        expectedActualDifferenceImageView.setActualBorderColor(ACTUAL_BORDER_COLOR);
        expectedActualDifferenceImageView.setDifferenceBorderColor(DIFFERENCE_BORDER_COLOR);
    }

    //endregion
    //region Layout related
    private void layoutComponents() {
        bordered(content)
                .top(bordered()
                        .top(flowLeftWithBottomLine(selectedIssueDescriptionLabel))
                        .bottom(flowLeftWithBottomLine(DEFAULT_FLOW_GAP, 0,
                                overwriteButton,
                                addAlternativeButton,
                                ignoreButton,
                                separatorBar(),
                                imagesLegend.getComponent(),
                                rotateButton,
                                separatorBar(),
                                shrinkToFitCheckBox,
                                separatorBar()))
                        .component())
                .left(variantsIndicator.getComponent())
                .center(scrollingNoBorder(expectedActualDifferenceImageView.getComponent()))
                .bottom(snapshotIssuesVList.getComponent());
    }

    //endregion
}
