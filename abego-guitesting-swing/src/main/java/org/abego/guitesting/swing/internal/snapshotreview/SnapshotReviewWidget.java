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
import java.awt.BorderLayout;

import static javax.swing.SwingUtilities.invokeLater;
import static org.abego.commons.io.FileUtil.toFile;
import static org.abego.guitesting.swing.internal.snapshotreview.ExpectedActualDifferenceImageViewerWidget.newExpectedActualDifferenceWidget;
import static org.abego.guitesting.swing.internal.snapshotreview.VariantsInfoImpl.newVariantsInfoImpl;
import static org.abego.guitesting.swing.internal.util.FileUtil.copyFile;
import static org.abego.guitesting.swing.internal.util.SwingUtil.DEFAULT_FLOW_GAP;
import static org.abego.guitesting.swing.internal.util.SwingUtil.bordered;
import static org.abego.guitesting.swing.internal.util.SwingUtil.flowLeftWithBottomLine;
import static org.abego.guitesting.swing.internal.util.SwingUtil.iconButton;
import static org.abego.guitesting.swing.internal.util.SwingUtil.label;
import static org.abego.guitesting.swing.internal.util.SwingUtil.newAction;
import static org.abego.guitesting.swing.internal.util.SwingUtil.newDefaultListModel;
import static org.abego.guitesting.swing.internal.util.SwingUtil.scrollingNoBorder;
import static org.abego.guitesting.swing.internal.util.SwingUtil.separatorBar;
import static org.abego.guitesting.swing.internal.util.UpdateableSwingUtil.checkBox;

//TODO: better split between init, style (e.g. border), layout, binding/updating
class SnapshotReviewWidget<T extends SnapshotIssue> implements Widget {


    // Actions
    private final Action addAltenativeSnapshotAction;
    @SuppressWarnings("FieldCanBeLocal")
    private final Action ignoreCurrentIssueAction;
    private final Action overwriteSnapshotAction;
    private final Action rotateImageAction;
    @SuppressWarnings("FieldCanBeLocal")
    private final Action toggleShrinkToFitAction;

    // Components (for more see #layoutComponent)
    private final JPanel content = new JPanel();
    private final JLabel selectedIssueDescriptionLabel = label();
    private final ExpectedActualDifferenceImageViewerWidget expectedActualDifferenceImageViewerWidget
            = newExpectedActualDifferenceWidget();
    private final ImagesLegendWidget imagesLegendWidget = ImagesLegendWidget.newImagesLegendWidget();
    private final VariantsIndicatorWidget<T> variantsIndicatorWidget = new VariantsIndicatorWidget<>();
    private final JButton overwriteButton;
    private final JButton addAlternativeButton;
    private final JButton rotateButton;
    private final JButton ignoreButton;
    private final JCheckBoxUpdateable shrinkToFitCheckBox;
    private final SnapshotIssuesListWidget<T> snapshotIssuesListWidget;

    // UI State
    private final DefaultListModel<T> issuesListModel;

    SnapshotReviewWidget(Seq<T> issues) {
        // init State
        issuesListModel = newDefaultListModel(
                issues.sortedBy(SnapshotIssue::getLabel));

        // init Actions
        addAltenativeSnapshotAction = newAction("Make Actual an Alternative (A)", KeyStroke.getKeyStroke("A"), Icons.alternativeIcon(), e -> addAltenativeSnapshot()); //NON-NLS
        ignoreCurrentIssueAction = newAction("Ignore Issue (Esc)", KeyStroke.getKeyStroke("ESCAPE"), Icons.ignoreIcon(), e -> ignoreCurrentIssue()); //NON-NLS
        overwriteSnapshotAction = newAction("Overwrite Expected (O)", KeyStroke.getKeyStroke("O"), Icons.overwriteIcon(), e -> overwriteSnapshot()); //NON-NLS
        rotateImageAction = newAction("Rotate Images (→)", KeyStroke.getKeyStroke("RIGHT"), Icons.rotateRightIcon(), e -> rotateImages()); //NON-NLS;
        toggleShrinkToFitAction = newAction("Shrink to Fit (#)", KeyStroke.getKeyStroke("NUMBER_SIGN"), e -> toggleShrinkToFit()); //NON-NLS

        // init Components
        overwriteButton = iconButton(overwriteSnapshotAction);
        addAlternativeButton = iconButton(addAltenativeSnapshotAction);
        ignoreButton = iconButton(ignoreCurrentIssueAction);
        rotateButton = iconButton(rotateImageAction);

        snapshotIssuesListWidget = SnapshotIssuesListWidget.newSnapshotIssuesListWidget(issuesListModel);
        shrinkToFitCheckBox = checkBox(this::getShrinkToFit, toggleShrinkToFitAction);

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

    private void styleComponents() {
        //TODO demonstrate the difference between a style derived from a different
        //  component style and from a "central" style definition.
        imagesLegendWidget.setExpectedBorderColor(expectedActualDifferenceImageViewerWidget.getExpectedBorderColor());
        imagesLegendWidget.setActualBorderColor(expectedActualDifferenceImageViewerWidget.getActualBorderColor());
        imagesLegendWidget.setDifferenceBorderColor(expectedActualDifferenceImageViewerWidget.getDifferenceBorderColor());
    }

    private void layoutComponents() {
        JComponent content = bordered()
                .top(bordered()
                        .top(flowLeftWithBottomLine(selectedIssueDescriptionLabel))
                        .bottom(flowLeftWithBottomLine(DEFAULT_FLOW_GAP, 0,
                                overwriteButton,
                                addAlternativeButton,
                                ignoreButton,
                                separatorBar(),
                                imagesLegendWidget.getComponent(),
                                rotateButton,
                                separatorBar(),
                                shrinkToFitCheckBox,
                                separatorBar())))
                .left(variantsIndicatorWidget.getComponent())
                .center(scrollingNoBorder(expectedActualDifferenceImageViewerWidget.getComponent()))
                .bottom(snapshotIssuesListWidget.getComponent());

        getComponent().setLayout(new BorderLayout());
        getComponent().add(content);
    }

    @Override
    public JComponent getComponent() {
        return content;
    }

    private void initNotifications() {
        snapshotIssuesListWidget.addSelectedIssueChangeListener(e -> onSelectedIssueChanged());
    }

    private void updateSelectedIssueDescriptionLabel() {
        selectedIssueDescriptionLabel.setText(getSelectedIssueDescription());
    }

    private String getSelectedIssueDescription() {
        @Nullable
        VariantsInfo<T> info = getVariantsInfo();
        if (info == null) {
            return " ";
        }

        StringBuilder result = new StringBuilder();

        int variantsCount = info.getVariantsCount();
        if (variantsCount > 1) {
            result.append("[");
            result.append(info.getVariantsIndex() + 1);
            result.append(" of "); //NON-NLS
            result.append(variantsCount);
            result.append("] ");
        }
        // display the "simple" name of the snapshot first,
        // followed by the package and class part,
        // separated by a "-".
        String s = info.getIssue().getLabel();
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

    private void removeIssueAndVariants(T issue) {
        String name = issue.getSnapshotName();
        invokeLater(() -> {
            int selectedIndex = snapshotIssuesListWidget.getSelectedIndex();
            for (int i = issuesListModel.size() - 1; i >= 0; i--) {
                T issueInModel = issuesListModel.get(i);
                //noinspection CallToSuspiciousStringMethod
                if (issueInModel.getSnapshotName().equals(name)) {
                    issuesListModel.removeElementAt(i);
                }
            }
            snapshotIssuesListWidget.setSelectedIndex(selectedIndex);
        });
    }

    private void removeIssue(T issue) {
        invokeLater(() -> {
            int selectedIndex = snapshotIssuesListWidget.getSelectedIndex();
            issuesListModel.removeElement(issue);
            snapshotIssuesListWidget.setSelectedIndex(selectedIndex);
        });
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

    private void onSelectedIssueChanged() {
        invokeLater(() -> {
            expectedActualDifferenceImageViewerWidget.setSnapshotIssue(getSelectedIssue());
            variantsIndicatorWidget.setVariantsInfo(getVariantsInfo());
            updateSelectedIssueDescriptionLabel();
        });
    }

    private void onExpectedImageIndexChanged() {
        invokeLater(() -> imagesLegendWidget.setExpectedImageIndex(getExpectedImageIndex()));
    }

    private void onShrinkToFitChanged() {
        invokeLater(shrinkToFitCheckBox::update);
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

    @Nullable
    private VariantsInfo<T> getVariantsInfo() {
        T issue = getSelectedIssue();
        if (issue == null) {
            return null;
        }

        return newVariantsInfoImpl(issue, getVariants(issue));
    }

    @Nullable
    private T getSelectedIssue() {
        return snapshotIssuesListWidget.getSelectedIssue();
    }

    private Seq<T> getVariants(T issue) {
        //noinspection CallToSuspiciousStringMethod
        return SeqUtil2.newSeq(issuesListModel.elements()).filter(
                i -> i.getSnapshotName().equals(issue.getSnapshotName()));
    }


}
