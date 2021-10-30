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
import org.abego.guitesting.swing.internal.util.DependencyCollector;
import org.abego.guitesting.swing.internal.util.JCheckBoxBindable;
import org.abego.guitesting.swing.internal.util.JLabelBindable;
import org.abego.guitesting.swing.internal.util.Prop;
import org.abego.guitesting.swing.internal.util.PropNullable;
import org.abego.guitesting.swing.internal.util.SeqUtil2;
import org.eclipse.jdt.annotation.Nullable;

import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.Color;

import static java.lang.Boolean.TRUE;
import static javax.swing.SwingUtilities.invokeLater;
import static org.abego.commons.io.FileUtil.toFile;
import static org.abego.guitesting.swing.internal.snapshotreview.ExpectedActualDifferenceImageView.expectedActualDifferenceImageView;
import static org.abego.guitesting.swing.internal.snapshotreview.ImagesLegend.imagesLegend;
import static org.abego.guitesting.swing.internal.snapshotreview.SnapshotIssuesVList.snapshotIssuesVList;
import static org.abego.guitesting.swing.internal.snapshotreview.VariantsIndicator.variantsIndicator;
import static org.abego.guitesting.swing.internal.snapshotreview.VariantsInfoImpl.variantsInfo;
import static org.abego.guitesting.swing.internal.util.Bordered.bordered;
import static org.abego.guitesting.swing.internal.util.FileUtil.copyFile;
import static org.abego.guitesting.swing.internal.util.JCheckBoxBindable.checkBoxUpdateable;
import static org.abego.guitesting.swing.internal.util.JLabelBindable.labelBindable;
import static org.abego.guitesting.swing.internal.util.Prop.newComputedProp;
import static org.abego.guitesting.swing.internal.util.Prop.newProp;
import static org.abego.guitesting.swing.internal.util.PropNullable.newComputedPropNullable;
import static org.abego.guitesting.swing.internal.util.PropNullable.newPropNullable;
import static org.abego.guitesting.swing.internal.util.SwingUtil.DEFAULT_FLOW_GAP;
import static org.abego.guitesting.swing.internal.util.SwingUtil.flowLeftWithBottomLine;
import static org.abego.guitesting.swing.internal.util.SwingUtil.newAction;
import static org.abego.guitesting.swing.internal.util.SwingUtil.newDefaultListModel;
import static org.abego.guitesting.swing.internal.util.SwingUtil.scrollingNoBorder;
import static org.abego.guitesting.swing.internal.util.SwingUtil.separatorBar;
import static org.abego.guitesting.swing.internal.util.SwingUtil.toolbarButton;

class SnapshotReviewWidget implements Widget {

    //region State/Model
    private final DefaultListModel<SnapshotIssue> remainingIssues;
    @SuppressWarnings("DuplicateStringLiteralInspection")
    private final PropNullable<@Nullable SnapshotIssue> selectedIssue = newPropNullable(null, this, "selectedIssue");
    @SuppressWarnings("DuplicateStringLiteralInspection")
    private final Prop<Boolean> shrinkToFitProp = newProp(TRUE, this, "shrinkToFit");
    private final Prop<Integer> expectedImageIndexProp = newProp(0);
    private final Prop<String> selectedIssueDescriptionProp = newComputedProp(this::getSelectedIssueDescription, this, "selectedIssueDescription");
    private final PropNullable<VariantsInfo> variantsInfoProp = newComputedPropNullable(this::getVariantsInfo);

    //endregion
    //region Actions
    @SuppressWarnings("FieldCanBeLocal")
    private final Action addAlternativeSnapshotAction;
    @SuppressWarnings("FieldCanBeLocal")
    private final Action overwriteSnapshotAction;
    @SuppressWarnings("FieldCanBeLocal")
    private final Action ignoreCurrentIssueAction;
    @SuppressWarnings("FieldCanBeLocal")
    private final Action rotateImageAction;
    //endregion
    //region Components
    private final JLabelBindable selectedIssueDescriptionLabel = labelBindable();
    private final JButton overwriteButton = toolbarButton();
    private final JButton addAlternativeButton = toolbarButton();
    private final JButton ignoreButton = toolbarButton();
    private final ImagesLegend imagesLegend = imagesLegend();
    private final JButton rotateButton = toolbarButton();
    private final JCheckBoxBindable shrinkToFitCheckBox = checkBoxUpdateable();
    private final VariantsIndicator variantsIndicator = variantsIndicator();
    private final ExpectedActualDifferenceImageView expectedActualDifferenceImageView
            = expectedActualDifferenceImageView();
    private final SnapshotIssuesVList<SnapshotIssue> snapshotIssuesVList = snapshotIssuesVList();
    private final JComponent content = new JPanel();

    //endregion
    //region Construction
    private SnapshotReviewWidget(Seq<SnapshotIssue> issues) {
        // init State
        remainingIssues = newDefaultListModel(
                issues.sortedBy(SnapshotIssue::getLabel));

        // init Actions
        addAlternativeSnapshotAction = newAction("Make Actual an Alternative (A)", KeyStroke.getKeyStroke("A"), Icons.alternativeIcon(), e -> addAlternativeSnapshot()); //NON-NLS
        ignoreCurrentIssueAction = newAction("Ignore Issue (Esc)", KeyStroke.getKeyStroke("ESCAPE"), Icons.ignoreIcon(), e -> ignoreCurrentIssue()); //NON-NLS
        overwriteSnapshotAction = newAction("Overwrite Expected (O)", KeyStroke.getKeyStroke("O"), Icons.overwriteIcon(), e -> overwriteSnapshot()); //NON-NLS
        rotateImageAction = newAction("Rotate Images (→)", KeyStroke.getKeyStroke("RIGHT"), Icons.rotateRightIcon(), e -> rotateImages()); //NON-NLS;

        // init Components
        styleComponents();
        layoutComponents();

        // Binding support
        initBindings();

        // More initialization
        invokeLater(() -> {
            // make the first item of the "remainingIssues" the "selected issue"
            if (!remainingIssues.isEmpty()) {
                setSelectedIssue(remainingIssues.get(0));
            }

            // make sure we have a focus
            shrinkToFitCheckBox.requestFocusInWindow();
        });
    }

    public static SnapshotReviewWidget snapshotReviewWidget(Seq<SnapshotIssue> issues) {
        return new SnapshotReviewWidget(issues);
    }

    //endregion
    //region Properties
    private String getSelectedIssueDescription(DependencyCollector dependencyCollector) {
        @Nullable
        VariantsInfo info = getVariantsInfo(dependencyCollector);
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
        return expectedImageIndexProp.get();
    }

    private void setExpectedImageIndex(Integer value) {
        expectedImageIndexProp.set(value);
    }

    private boolean getShrinkToFit() {
        return shrinkToFitProp.get();
    }

    private void setShrinkToFit(boolean value) {
        shrinkToFitProp.set(value);
    }

    @Nullable
    private SnapshotIssue getSelectedIssue() {
        return selectedIssue.get();
    }

    private void setSelectedIssue(SnapshotIssue issue) {
        selectedIssue.set(issue);
    }

    @Nullable
    private SnapshotIssue getSelectedIssue(DependencyCollector dependencyCollector) {
        dependencyCollector.dependsOnProperty(selectedIssue);
        return selectedIssue.get();
    }

    @Nullable
    private VariantsInfo getVariantsInfo(DependencyCollector dependencyCollector) {
        SnapshotIssue issue = getSelectedIssue(dependencyCollector);
        if (issue == null) {
            return null;
        }

        //noinspection CallToSuspiciousStringMethod
        Seq<SnapshotIssue> variants = SeqUtil2.newSeq(remainingIssues.elements())
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
        @Nullable SnapshotIssue currentIssue = getSelectedIssue();
        if (currentIssue != null) {
            copyFile(
                    toFile(currentIssue.getActualImage()),
                    toFile(currentIssue.getOverwriteURL()));
            removeIssueAndVariants(currentIssue);
        }
    }

    private void addAlternativeSnapshot() {
        @Nullable SnapshotIssue currentIssue = getSelectedIssue();
        if (currentIssue != null) {
            copyFile(
                    toFile(currentIssue.getActualImage()),
                    toFile(currentIssue.getAddAlternativeURL()));
            removeIssueAndVariants(currentIssue);
        }
    }

    private void ignoreCurrentIssue() {
        @Nullable SnapshotIssue currentIssue = getSelectedIssue();
        if (currentIssue != null) {
            removeIssue(currentIssue);
        }
    }

    private void removeIssue(SnapshotIssue issue) {
        remainingIssues.removeElement(issue);
    }

    private void removeIssueAndVariants(SnapshotIssue issue) {
        String name = issue.getSnapshotName();
        for (int i = remainingIssues.size() - 1; i >= 0; i--) {
            SnapshotIssue issueInModel = remainingIssues.get(i);
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
        snapshotIssuesVList.setListModel(remainingIssues);

        overwriteButton.setAction(overwriteSnapshotAction);
        addAlternativeButton.setAction(addAlternativeSnapshotAction);
        ignoreButton.setAction(ignoreCurrentIssueAction);
        rotateButton.setAction(rotateImageAction);

        selectedIssueDescriptionLabel.bindTextTo(selectedIssueDescriptionProp);
        shrinkToFitCheckBox.bindSelectedTo(shrinkToFitProp);
        snapshotIssuesVList.bindSelectedIssueTo(selectedIssue);
        expectedActualDifferenceImageView.bindSnapshotIssueTo(selectedIssue);
        expectedActualDifferenceImageView.bindShrinkToFitTo(shrinkToFitProp);
        expectedActualDifferenceImageView.bindExpectedImageIndexTo(expectedImageIndexProp);
        imagesLegend.bindExpectedImageIndexTo(expectedImageIndexProp);
        variantsIndicator.bindVariantsInfoTo(variantsInfoProp);
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

        shrinkToFitCheckBox.setText("Shrink to Fit");
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
