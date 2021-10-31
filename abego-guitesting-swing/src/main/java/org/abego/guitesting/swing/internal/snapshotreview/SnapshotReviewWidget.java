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
import org.abego.guitesting.swing.internal.util.VList;
import org.abego.guitesting.swing.internal.util.prop.DependencyCollector;
import org.abego.guitesting.swing.internal.util.JCheckBoxBindable;
import org.abego.guitesting.swing.internal.util.JLabelBindable;
import org.abego.guitesting.swing.internal.util.Widget;
import org.abego.guitesting.swing.internal.util.prop.Prop;
import org.abego.guitesting.swing.internal.util.prop.PropNullable;
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
import static org.abego.guitesting.swing.internal.snapshotreview.ExpectedActualDifferenceImageWidget.expectedActualDifferenceImageView;
import static org.abego.guitesting.swing.internal.snapshotreview.ImagesLegendWidget.imagesLegendWidget;
import static org.abego.guitesting.swing.internal.snapshotreview.SnapshotVariantsIndicator.variantsIndicator;
import static org.abego.guitesting.swing.internal.snapshotreview.SnapshotVariantImpl.variantsInfo;
import static org.abego.guitesting.swing.internal.util.BorderUtil.borderTopLighterGray;
import static org.abego.guitesting.swing.internal.util.Bordered.bordered;
import static org.abego.guitesting.swing.internal.util.FileUtil.copyFile;
import static org.abego.guitesting.swing.internal.util.JCheckBoxBindable.checkBoxBindable;
import static org.abego.guitesting.swing.internal.util.JLabelBindable.labelBindable;
import static org.abego.guitesting.swing.internal.util.prop.Prop.newComputedProp;
import static org.abego.guitesting.swing.internal.util.prop.Prop.newProp;
import static org.abego.guitesting.swing.internal.util.prop.PropNullable.newComputedPropNullable;
import static org.abego.guitesting.swing.internal.util.prop.PropNullable.newPropNullable;
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
    private final PropNullable<@Nullable SnapshotIssue> selectedIssue = newPropNullable(null, this, "selectedIssue");
    @SuppressWarnings("DuplicateStringLiteralInspection")
    private final Prop<Boolean> shrinkToFitProp = newProp(TRUE, this, "shrinkToFit");
    private final Prop<Integer> expectedImageIndexProp = newProp(0);
    private final Prop<String> selectedIssueDescriptionProp = newComputedProp(this::getSelectedIssueDescription, this, "selectedIssueDescription");
    private final PropNullable<SnapshotVariant> variantsInfoProp = newComputedPropNullable(this::getVariantsInfo);

    private String getSelectedIssueDescription(DependencyCollector dependencyCollector) {
        @Nullable
        SnapshotVariant info = getVariantsInfo(dependencyCollector);
        if (info == null) {
            return " ";
        }

        String label = labelWithLastPartFirst(info.getIssue());
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
    private SnapshotVariant getVariantsInfo(DependencyCollector dependencyCollector) {
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
    //region Actions
    private final Action addAlternativeSnapshotAction = newAction("Make Actual an Alternative (A)", KeyStroke.getKeyStroke("A"), Icons.alternativeIcon(), e -> addAlternativeSnapshot()); //NON-NLS
    private final Action ignoreCurrentIssueAction = newAction("Ignore Issue (Esc)", KeyStroke.getKeyStroke("ESCAPE"), Icons.ignoreIcon(), e -> ignoreCurrentIssue()); //NON-NLS
    private final Action overwriteSnapshotAction = newAction("Overwrite Expected (O)", KeyStroke.getKeyStroke("O"), Icons.overwriteIcon(), e -> overwriteSnapshot()); //NON-NLS
    private final Action rotateImageAction = newAction("Rotate Images (→)", KeyStroke.getKeyStroke("RIGHT"), Icons.rotateRightIcon(), e -> rotateImages()); //NON-NLS;
    //endregion
    //region Components
    private final JLabelBindable selectedIssueDescriptionLabel = labelBindable();
    private final JButton overwriteButton = toolbarButton();
    private final JButton addAlternativeButton = toolbarButton();
    private final JButton ignoreButton = toolbarButton();
    private final ImagesLegendWidget imagesLegendWidget = imagesLegendWidget();
    private final JButton rotateButton = toolbarButton();
    private final JCheckBoxBindable shrinkToFitCheckBox = checkBoxBindable();
    private final SnapshotVariantsIndicator snapshotVariantsIndicator = variantsIndicator();
    private final ExpectedActualDifferenceImageWidget expectedActualDifferenceImageWidget
            = expectedActualDifferenceImageView();
    private final VList<SnapshotIssue> snapshotIssuesVList = VList.vList();
    private final JComponent content = new JPanel();

    //endregion
    //region Construction
    private SnapshotReviewWidget(Seq<SnapshotIssue> issues) {
        remainingIssues = newDefaultListModel(
                issues.sortedBy(SnapshotIssue::getLabel));
        styleComponents();
        layoutComponents();
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

    /**
     * Returns the "simple" name of the snapshot first (the part behind the last
     * '.'), followed by the package and class part, separated by a " - ".
     */
    private static <T extends SnapshotIssue> String labelWithLastPartFirst(T issue) {
        StringBuilder result = new StringBuilder();
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


    //endregion
    //region Widget related
    @Override
    public JComponent getContent() {
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
    //region Style related
    private static final Color EXPECTED_BORDER_COLOR = new Color(0x59A869);
    private static final Color ACTUAL_BORDER_COLOR = new Color(0xC64D3F);
    private static final Color DIFFERENCE_BORDER_COLOR = new Color(0x6E6E6E);

    private void styleComponents() {
        imagesLegendWidget.setExpectedBorderColor(EXPECTED_BORDER_COLOR);
        imagesLegendWidget.setActualBorderColor(ACTUAL_BORDER_COLOR);
        imagesLegendWidget.setDifferenceBorderColor(DIFFERENCE_BORDER_COLOR);
        expectedActualDifferenceImageWidget.setExpectedBorderColor(EXPECTED_BORDER_COLOR);
        expectedActualDifferenceImageWidget.setActualBorderColor(ACTUAL_BORDER_COLOR);
        expectedActualDifferenceImageWidget.setDifferenceBorderColor(DIFFERENCE_BORDER_COLOR);

        shrinkToFitCheckBox.setText("Shrink to Fit"); //NON-NLS

        snapshotIssuesVList.setCellTextProvider(SnapshotReviewWidget::labelWithLastPartFirst);
        snapshotIssuesVList.getContent().setBorder(borderTopLighterGray());
        snapshotIssuesVList.setTitle("Issues:"); //NON-NLS
        snapshotIssuesVList.setPreviousItemText("Previous issue"); //NON-NLS
        snapshotIssuesVList.setNextItemText("Next issue"); //NON-NLS
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
                                imagesLegendWidget.getContent(),
                                rotateButton,
                                separatorBar(),
                                shrinkToFitCheckBox,
                                separatorBar()))
                        .component())
                .left(snapshotVariantsIndicator.getContent())
                .center(scrollingNoBorder(expectedActualDifferenceImageWidget.getContent()))
                .bottom(snapshotIssuesVList.getContent());
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
        snapshotIssuesVList.bindSelectedItemTo(selectedIssue);
        expectedActualDifferenceImageWidget.bindSnapshotIssueTo(selectedIssue);
        expectedActualDifferenceImageWidget.bindShrinkToFitTo(shrinkToFitProp);
        expectedActualDifferenceImageWidget.bindExpectedImageIndexTo(expectedImageIndexProp);
        imagesLegendWidget.bindExpectedImageIndexTo(expectedImageIndexProp);
        snapshotVariantsIndicator.bindVariantsInfoTo(variantsInfoProp);
    }

    //endregion
}
