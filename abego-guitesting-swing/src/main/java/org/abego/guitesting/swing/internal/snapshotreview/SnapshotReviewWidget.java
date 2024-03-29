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
import org.abego.commons.seq.SeqUtil;
import org.abego.commons.swing.JComponentUtil;
import org.abego.guitesting.swing.ScreenCaptureSupport.SnapshotIssue;
import org.abego.guitesting.swing.internal.util.SwingUtil;
import org.abego.guitesting.swing.internal.util.prop.Bindings;
import org.abego.guitesting.swing.internal.util.prop.DependencyCollector;
import org.abego.guitesting.swing.internal.util.prop.Prop;
import org.abego.guitesting.swing.internal.util.prop.PropNullable;
import org.abego.guitesting.swing.internal.util.prop.PropService;
import org.abego.guitesting.swing.internal.util.prop.PropServices;
import org.abego.guitesting.swing.internal.util.widget.BorderedWidget;
import org.abego.guitesting.swing.internal.util.widget.CheckBoxWidget;
import org.abego.guitesting.swing.internal.util.widget.GUIKitForSwing;
import org.abego.guitesting.swing.internal.util.widget.HStackWidget;
import org.abego.guitesting.swing.internal.util.widget.LabelWidget;
import org.abego.guitesting.swing.internal.util.widget.ToolbarButtonWidget;
import org.abego.guitesting.swing.internal.util.widget.VListWidget;
import org.abego.guitesting.swing.internal.util.widget.Widget;
import org.eclipse.jdt.annotation.Nullable;

import javax.swing.Action;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;

import static java.lang.Boolean.TRUE;
import static org.abego.commons.io.FileUtil.copyFile;
import static org.abego.commons.io.FileUtil.toFile;
import static org.abego.commons.swing.ActionUtil.newAction;
import static org.abego.commons.swing.JComponentUtil.onJComponentBecomesVisible;
import static org.abego.guitesting.swing.internal.snapshotreview.ExpectedActualDifferenceImageWidget.expectedActualDifferenceImageWidget;
import static org.abego.guitesting.swing.internal.snapshotreview.ImagesLegendWidget.imagesLegendWidget;
import static org.abego.guitesting.swing.internal.snapshotreview.SnapshotVariantImpl.snapshotVariant;
import static org.abego.guitesting.swing.internal.snapshotreview.SnapshotVariantsIndicatorWidget.variantsIndicatorWidget;
import static org.abego.guitesting.swing.internal.util.SwingUtil.LIGHTER_GRAY;
import static org.abego.guitesting.swing.internal.util.SwingUtil.newDefaultListModel;
import static org.abego.guitesting.swing.internal.util.SwingUtil.scrollingNoBorder;
import static org.abego.guitesting.swing.internal.util.boxstyle.BoxStyle.BorderStyle.SOLID;
import static org.abego.guitesting.swing.internal.util.boxstyle.BoxStyle.newBoxStyle;
import static org.abego.guitesting.swing.internal.util.widget.BorderedWidget.borderedWidget;
import static org.abego.guitesting.swing.internal.util.widget.CheckBoxWidget.checkBoxWidget;
import static org.abego.guitesting.swing.internal.util.widget.LabelWidget.labelWidget;
import static org.abego.guitesting.swing.internal.util.widget.ToolbarButtonWidget.toolbarButtonWidget;
import static org.abego.guitesting.swing.internal.util.widget.ToolbarSeparatorWidget.toolbarSeparatorWidget;
import static org.abego.guitesting.swing.internal.util.widget.VListWidget.vListWidget;

class SnapshotReviewWidget implements Widget {

    //region State/Model
    private final PropService propService = PropServices.getDefault();
    private final DefaultListModel<SnapshotIssue> remainingIssues;
    //region @Prop public @Nullable SnapshotIssue selectedIssue
    private final PropNullable<@Nullable SnapshotIssue> selectedIssue = propService.newPropNullable(null, this, "selectedIssue");

    @Nullable
    private SnapshotIssue getSelectedIssue() {
        return selectedIssue.get();
    }

    private void setSelectedIssue(SnapshotIssue issue) {
        selectedIssue.set(issue);
    }

    @Nullable
    private SnapshotIssue getSelectedIssue(DependencyCollector dependencyCollector) {
        return selectedIssue.get(dependencyCollector);
    }

    //endregion
    //region @Prop public Boolean shrinkToFit = TRUE
    @SuppressWarnings("DuplicateStringLiteralInspection")
    private final Prop<Boolean> shrinkToFitProp = propService.newProp(TRUE, this, "shrinkToFit");
    //endregion
    //region @Prop public Integer expectedImageIndex = 0
    private final Prop<Integer> expectedImageIndexProp = propService.newProp(0);

    private int getExpectedImageIndex() {
        return expectedImageIndexProp.get();
    }

    private void setExpectedImageIndex(Integer value) {
        expectedImageIndexProp.set(value);
    }

    //endregion
    //region @Prop public String selectedIssueDescription {}
    private final Prop<String> selectedIssueDescriptionProp = propService.newPropComputed(this::getSelectedIssueDescription, this, "selectedIssueDescription");

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

    //endregion
    //region @Prop public @Nullable SnapshotVariant variantsInfo {}
    private final PropNullable<SnapshotVariant> variantsInfoProp = propService.newPropComputedNullable(this::getVariantsInfo);

    @Nullable
    private SnapshotVariant getVariantsInfo(DependencyCollector dependencyCollector) {
        SnapshotIssue issue = getSelectedIssue(dependencyCollector);
        if (issue == null) {
            return null;
        }

        //noinspection CallToSuspiciousStringMethod
        Seq<SnapshotIssue> variants = SeqUtil.newSeq(remainingIssues.elements())
                .filter(i -> i.getSnapshotName().equals(issue.getSnapshotName()));
        return snapshotVariant(issue, variants);
    }

    //endregion
    //endregion
    //region Actions
    private final Action copyDescriptionAction = newAction("Copy Description to Clipboard", KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), Resources.copyIcon(), e -> copyDescription()); //NON-NLS
    private final Action addAlternativeSnapshotAction = newAction("Make Actual an Alternative", KeyStroke.getKeyStroke("A"), Resources.alternativeIcon(), e -> addAlternativeSnapshot()); //NON-NLS
    private final Action ignoreCurrentIssueAction = newAction("Ignore Issue", KeyStroke.getKeyStroke("ESCAPE"), Resources.ignoreIcon(), e -> ignoreCurrentIssue()); //NON-NLS
    private final Action overwriteSnapshotAction = newAction("Overwrite Expected", KeyStroke.getKeyStroke("O"), Resources.overwriteIcon(), e -> overwriteSnapshot()); //NON-NLS
    private final Action rotateImageAction = newAction("Rotate Images", KeyStroke.getKeyStroke("RIGHT"), Resources.rotateRightIcon(), e -> rotateImages()); //NON-NLS;

    private void copyDescription() {
        @Nullable SnapshotIssue currentIssue = getSelectedIssue();
        if (currentIssue != null) {
            String text = selectedIssueDescriptionProp.get();
            Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .setContents(
                            new StringSelection(text),
                            null
                    );
        }
    }

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
        //noinspection CallToSuspiciousStringMethod
        SwingUtil.removeIf(remainingIssues, i -> i.getSnapshotName().equals(name));
    }

    private void rotateImages() {
        setExpectedImageIndex((getExpectedImageIndex() + 1) % 3);
    }

    //endregion
    //region Components
    private final LabelWidget selectedIssueDescriptionLabel = labelWidget();
    private final ToolbarButtonWidget copyDescriptionButton = toolbarButtonWidget();
    private final ToolbarButtonWidget overwriteButton = toolbarButtonWidget();
    private final ToolbarButtonWidget addAlternativeButton = toolbarButtonWidget();
    private final ToolbarButtonWidget ignoreButton = toolbarButtonWidget();
    private final ImagesLegendWidget imagesLegend = imagesLegendWidget();
    private final ToolbarButtonWidget rotateButton = toolbarButtonWidget();
    private final CheckBoxWidget shrinkToFitCheckBox = checkBoxWidget();
    private final SnapshotVariantsIndicatorWidget snapshotVariantsIndicator = variantsIndicatorWidget();
    private final ExpectedActualDifferenceImageWidget expectedActualDifferenceImage
            = expectedActualDifferenceImageWidget();
    private final VListWidget<SnapshotIssue> snapshotIssuesVList = vListWidget();
    private final BorderedWidget contentWidget = borderedWidget();

    //endregion
    //region Construction
    private SnapshotReviewWidget(Seq<SnapshotIssue> issues) {
        remainingIssues = newDefaultListModel(
                issues.sortedBy( // snapshotName and index
                        si -> si.getSnapshotName() +" - " + si.getIndex()));
        layoutComponents();
        styleComponents();
        initBindings();

        // make the first item of the "remainingIssues" the "selected issue"
        if (!remainingIssues.isEmpty()) {
            setSelectedIssue(remainingIssues.get(0));
        }

        // make sure we have a focus (when the widget is displayed)
        onJComponentBecomesVisible(contentWidget.getContent(),
                () -> shrinkToFitCheckBox.getContent().requestFocusInWindow());
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
        return contentWidget.getContent();
    }

    public void close() {
        selectedIssueDescriptionLabel.close();
        imagesLegend.close();
        shrinkToFitCheckBox.close();
        snapshotVariantsIndicator.close();
        expectedActualDifferenceImage.close();
        snapshotIssuesVList.close();

        bindings.close();
    }

    //endregion
    //region Style related
    private static final int TOOLBAR_SPACING = 5;
    private static final int TOOLBAR_PADDING = 5;
    private static final int TITLEBAR_PADDING_TB = 2;
    private static final int TITLEBAR_PADDING_LR = 5;

    private static final Color EXPECTED_BORDER_COLOR = new Color(0x59A869);
    private static final Color ACTUAL_BORDER_COLOR = new Color(0xC64D3F);
    private static final Color DIFFERENCE_BORDER_COLOR = new Color(0x6E6E6E);

    private void styleComponents() {

        imagesLegend.setExpectedBorderColor(EXPECTED_BORDER_COLOR);
        imagesLegend.setActualBorderColor(ACTUAL_BORDER_COLOR);
        imagesLegend.setDifferenceBorderColor(DIFFERENCE_BORDER_COLOR);
        expectedActualDifferenceImage.setExpectedBorderColor(EXPECTED_BORDER_COLOR);
        expectedActualDifferenceImage.setActualBorderColor(ACTUAL_BORDER_COLOR);
        expectedActualDifferenceImage.setDifferenceBorderColor(DIFFERENCE_BORDER_COLOR);

        shrinkToFitCheckBox.setText("Shrink to Fit"); //NON-NLS

        snapshotIssuesVList.setCellTextProvider(SnapshotReviewWidget::labelWithLastPartFirst);
        snapshotIssuesVList.setTitle("Issues:"); //NON-NLS
        snapshotIssuesVList.setPreviousItemText("Previous issue"); //NON-NLS
        snapshotIssuesVList.setNextItemText("Next issue"); //NON-NLS

        snapshotIssuesVList.setBoxStyle(newBoxStyle()
                .borderTop(1, LIGHTER_GRAY));
        selectedIssueDescriptionLabel.setBoxStyle(newBoxStyle()
                .paddingLeftRight(5));
        imagesLegend.setBoxStyle(newBoxStyle()
                .paddingLeftRight(TOOLBAR_PADDING));
        titleBar.setBoxStyle(newBoxStyle()
                .padding(TITLEBAR_PADDING_TB, TITLEBAR_PADDING_LR)
                .borderBottom(1, SOLID, LIGHTER_GRAY));
        toolbar.setBoxStyle(newBoxStyle()
                .paddingLeftRight(TOOLBAR_PADDING)
                .borderBottom(1, SOLID, LIGHTER_GRAY));
        toolbar.setSpacing(TOOLBAR_SPACING);
    }

    //endregion
    //region Layout related
    private final HStackWidget titleBar = GUIKitForSwing.getDefault().hStackWidget();
    private final HStackWidget toolbar = GUIKitForSwing.getDefault().hStackWidget();

    private void layoutComponents() {
        titleBar.setItems(copyDescriptionButton, selectedIssueDescriptionLabel);
        toolbar.setItems(
                overwriteButton,
                addAlternativeButton,
                ignoreButton,
                toolbarSeparatorWidget(),
                imagesLegend,
                rotateButton,
                toolbarSeparatorWidget(),
                shrinkToFitCheckBox,
                toolbarSeparatorWidget());
        contentWidget
                .top(borderedWidget()
                        .top(titleBar)
                        .bottom(toolbar))
                .left(snapshotVariantsIndicator)
                .center(scrollingNoBorder(expectedActualDifferenceImage.getContent()))
                .bottom(snapshotIssuesVList);
    }

    //endregion
    //region Binding related
    private final Bindings bindings = propService.newBindings();

    private void initBindings() {
        snapshotIssuesVList.setListModel(remainingIssues);
        bindings.bind(selectedIssue, snapshotIssuesVList.getSelectedItemProp());

        copyDescriptionButton.setAction(copyDescriptionAction);
        overwriteButton.setAction(overwriteSnapshotAction);
        addAlternativeButton.setAction(addAlternativeSnapshotAction);
        ignoreButton.setAction(ignoreCurrentIssueAction);
        rotateButton.setAction(rotateImageAction);

        bindings.bind(selectedIssueDescriptionProp, selectedIssueDescriptionLabel.getTextProp());
        bindings.bind(shrinkToFitProp, shrinkToFitCheckBox.getSelectedProp());
        bindings.bind(selectedIssue, expectedActualDifferenceImage.getSnapshotIssueProp());
        bindings.bind(shrinkToFitProp, expectedActualDifferenceImage.getShrinkToFitProp());
        bindings.bind(expectedImageIndexProp, expectedActualDifferenceImage.getExpectedImageIndexProp());
        bindings.bind(expectedImageIndexProp, imagesLegend.getExpectedImageIndexProp());
        bindings.bind(variantsInfoProp, snapshotVariantsIndicator.getVariantsInfoProp());
    }

    //endregion
}
