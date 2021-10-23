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

import static javax.swing.SwingUtilities.invokeLater;
import static org.abego.commons.io.FileUtil.toFile;
import static org.abego.guitesting.swing.internal.snapshotreview.ExpectedActualDifferenceImageView.expectedActualDifferenceImageView;
import static org.abego.guitesting.swing.internal.snapshotreview.SnapshotIssuesVList.snapshotIssuesVList;
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

//TODO: better split between init, style (e.g. border), layout, binding/updating
class SnapshotReviewWidget<T extends SnapshotIssue> implements Widget {

    // State/Model
    private final DefaultListModel<T> remainingIssues;

    // Actions
    private final Action addAltenativeSnapshotAction;
    private final Action overwriteSnapshotAction;
    @SuppressWarnings("FieldCanBeLocal")
    private final Action ignoreCurrentIssueAction;
    private final Action rotateImageAction;
    @SuppressWarnings("FieldCanBeLocal")
    private final Action toggleShrinkToFitAction;

    // Components (for more see #layoutComponent)
    private final JLabel selectedIssueDescriptionLabel = label();
    private final JButton overwriteButton = toolbarButton();
    private final JButton addAlternativeButton = toolbarButton();
    private final JButton ignoreButton = toolbarButton();
    private final ImagesLegend imagesLegend = ImagesLegend.imagesLegend();
    private final JButton rotateButton = toolbarButton();
    private final JCheckBoxUpdateable shrinkToFitCheckBox = checkBoxUpdateable();
    private final VariantsIndicator<T> variantsIndicator = VariantsIndicator.variantsIndicator();
    private final ExpectedActualDifferenceImageView expectedActualDifferenceImageView
            = expectedActualDifferenceImageView();
    private final SnapshotIssuesVList<T> snapshotIssuesVList = snapshotIssuesVList();
    private final JComponent content = new JPanel();

    private SnapshotReviewWidget(Seq<T> issues) {
        // init State
        remainingIssues = newDefaultListModel(
                issues.sortedBy(SnapshotIssue::getLabel));

        // init Actions
        addAltenativeSnapshotAction = newAction("Make Actual an Alternative (A)", KeyStroke.getKeyStroke("A"), Icons.alternativeIcon(), e -> addAltenativeSnapshot()); //NON-NLS
        ignoreCurrentIssueAction = newAction("Ignore Issue (Esc)", KeyStroke.getKeyStroke("ESCAPE"), Icons.ignoreIcon(), e -> ignoreCurrentIssue()); //NON-NLS
        overwriteSnapshotAction = newAction("Overwrite Expected (O)", KeyStroke.getKeyStroke("O"), Icons.overwriteIcon(), e -> overwriteSnapshot()); //NON-NLS
        rotateImageAction = newAction("Rotate Images (→)", KeyStroke.getKeyStroke("RIGHT"), Icons.rotateRightIcon(), e -> rotateImages()); //NON-NLS;
        toggleShrinkToFitAction = newAction("Shrink to Fit (#)", KeyStroke.getKeyStroke("NUMBER_SIGN"), e -> toggleShrinkToFit()); //NON-NLS

        // init Components
        snapshotIssuesVList.setListModel(remainingIssues);
        shrinkToFitCheckBox.setSelectedCondition(this::getShrinkToFit);

        overwriteButton.setAction(overwriteSnapshotAction);
        addAlternativeButton.setAction(addAltenativeSnapshotAction);
        ignoreButton.setAction(ignoreCurrentIssueAction);
        rotateButton.setAction(rotateImageAction);
        shrinkToFitCheckBox.setAction(toggleShrinkToFitAction);

        styleComponents();
        layoutComponents();

        // Notifications support
        initNotifications();

        // More initialization
        invokeLater(() -> {
            // make sure we have a focus
            ignoreButton.requestFocusInWindow();
        });
    }

    public static <T extends SnapshotIssue> SnapshotReviewWidget<T> snapshotReviewWidget(Seq<T> issues) {
        return new SnapshotReviewWidget<>(issues);
    }

    private void styleComponents() {
        //TODO demonstrate the difference between a style derived from a different
        //  component style and from a "central" style definition.
        imagesLegend.setExpectedBorderColor(expectedActualDifferenceImageView.getExpectedBorderColor());
        imagesLegend.setActualBorderColor(expectedActualDifferenceImageView.getActualBorderColor());
        imagesLegend.setDifferenceBorderColor(expectedActualDifferenceImageView.getDifferenceBorderColor());
    }

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

    @Override
    public JComponent getComponent() {
        return content;
    }

    private void initNotifications() {
        snapshotIssuesVList.addSelectedIssueChangeListener(e -> onSelectedIssueChanged());
    }

    private void updateSelectedIssueDescriptionLabel() {
        invokeLater(() ->
                selectedIssueDescriptionLabel.setText(getSelectedIssueDescription()));
    }

    private String getSelectedIssueDescription() {
        @Nullable
        VariantsInfo<T> info = getVariantsInfo();
        if (info == null) {
            return " ";
        }

        //TODO use some "template engine" like code
        StringBuilder result = new StringBuilder();

        int variantsCount = info.getVariantsCount();
        if (variantsCount > 1) {
            result.append("[");
            result.append(info.getVariantsIndex() + 1);
            result.append(" of "); //NON-NLS
            result.append(variantsCount);
            result.append("] ");
        }
        result.append(SnapshotIssueUtil.labelWithLastPartFirst(info.getIssue()));
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

    private void removeIssue(T issue) {
        remainingIssues.removeElement(issue);
    }

    private void rotateImages() {
        setExpectedImageIndex((getExpectedImageIndex() + 1) % 3);
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

    private int getExpectedImageIndex() {
        return expectedActualDifferenceImageView.getExpectedImageIndex();
    }

    private void setExpectedImageIndex(int value) {
        expectedActualDifferenceImageView.setExpectedImageIndex(value);
        onExpectedImageIndexChanged();
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
    private VariantsInfo<T> getVariantsInfo() {
        T issue = getSelectedIssue();
        if (issue == null) {
            return null;
        }

        return variantsInfo(issue, getVariants(issue));
    }

    private Seq<T> getVariants(T issue) {
        //noinspection CallToSuspiciousStringMethod
        return SeqUtil2.newSeq(remainingIssues.elements()).filter(
                i -> i.getSnapshotName().equals(issue.getSnapshotName()));
    }

    @Nullable
    private T getSelectedIssue() {
        return snapshotIssuesVList.getSelectedIssue();
    }

}
