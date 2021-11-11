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

package org.abego.guitesting.swing.internal.util.widget;

import org.abego.guitesting.swing.internal.util.SwingUtil;
import org.abego.guitesting.swing.internal.util.prop.Prop;
import org.abego.guitesting.swing.internal.util.prop.PropField;
import org.abego.guitesting.swing.internal.util.prop.PropFieldNullable;
import org.abego.guitesting.swing.internal.util.prop.PropServices;
import org.abego.guitesting.swing.internal.util.prop.PropFactory;
import org.abego.guitesting.swing.internal.util.prop.PropNullable;
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
import javax.swing.ListModel;
import java.awt.Color;
import java.util.Objects;
import java.util.function.Function;

import static javax.swing.SwingUtilities.invokeLater;
import static org.abego.commons.lang.IntUtil.limit;
import static org.abego.guitesting.swing.internal.util.Bordered.bordered;
import static org.abego.guitesting.swing.internal.util.SwingUtil.DEFAULT_FLOW_GAP;
import static org.abego.guitesting.swing.internal.util.SwingUtil.ensureSelectionIsVisible;
import static org.abego.guitesting.swing.internal.util.SwingUtil.flowLeft;
import static org.abego.guitesting.swing.internal.util.SwingUtil.newAction;
import static org.abego.guitesting.swing.internal.util.SwingUtil.newListCellRenderer;
import static org.abego.guitesting.swing.internal.util.SwingUtil.scrollingNoBorder;
import static org.abego.guitesting.swing.internal.util.SwingUtil.toolbarButton;

public final class VListWidget<T> implements Widget {

    //region State/Model
    private final PropFactory propFactory = PropServices.newProps();
    //region @Prop public ListModel<T> listModel = new DefaultListModel<>()
    private final PropField<ListModel<T>> listModelProp =
            propFactory.newProp(new DefaultListModel<>(), this, "listModel");

    public ListModel<T> getListModel() {
        return listModelProp.get();
    }

    public void setListModel(ListModel<T> listModel) {
        listModelProp.set(listModel);
    }

    //endregion
    //region @Prop public Function<T, String> cellTextProvider = Objects::toString
    private final PropField<Function<T, String>> cellTextProviderProp =
            propFactory.newProp(Objects::toString, this, "cellTextProvider");

    public Function<T, String> getCellTextProvider() {
        return cellTextProviderProp.get();
    }

    public void setCellTextProvider(Function<T, String> textProvider) {
        cellTextProviderProp.set(textProvider);
    }

    //endregion
    //region  @Prop public String previousItemText = "Previous item"
    private final PropField<String> previousItemTextProp =
            propFactory.newProp("Previous item", this, "previousItemText"); // NON-NLS NON-NLS

    public String getPreviousItemText() {
        return getPreviousItemTextProp().get();
    }

    public void setPreviousItemText(String value) {
        previousItemTextProp.set(value);
    }

    public Prop<String> getPreviousItemTextProp() {
        return previousItemTextProp;
    }

    //endregion
    //region @Prop public String nextItemText = "Next item"
    private final PropField<String> nextItemTextProp =
            propFactory.newProp("Next item", this, "nextItemText"); //NON-NLS

    public String getNextItemText() {
        return getNextItemTextProp().get();
    }

    public void setNextItemText(String value) {
        nextItemTextProp.set(value);
    }

    public Prop<String> getNextItemTextProp() {
        return nextItemTextProp;
    }

    //endregion
    //region @Prop public String title = "Items:"
    private final PropField<String> titleProp =
            propFactory.newProp("Items:", this, "title"); //NON-NLS

    public String getTitle() {
        return titleProp.get();
    }

    public void setTitle(String value) {
        getTitleProp().set(value);
    }

    public PropField<String> getTitleProp() {
        return titleProp;
    }

    //endregion
    private int lastSelectedIndex = -1;

    //endregion
    //region Actions
    //TODO customizes/generalize the actionn titles/texts
    private final Action previousItemAction = SwingUtil.newAction("", KeyStroke.getKeyStroke("UP"), Resources.previousItemIcon(), e -> selectPreviousItem()); //NON-NLS
    private final Action nextItemAction = newAction("", KeyStroke.getKeyStroke("DOWN"), Resources.nextItemIcon(), e -> selectNextItem()); //NON-NLS;

    private void selectPreviousItem() {
        SwingUtil.changeSelectedIndex(jList, -1);
    }

    private void selectNextItem() {
        SwingUtil.changeSelectedIndex(jList, 1);
    }

    //endregion
    //region Components
    private final JButton previousItemButton = toolbarButton();
    private final JButton nextItemButton = toolbarButton();
    private final JComponent topBar = new JPanel();
    private final JLabel titleLabel = new JLabel();
    private final JList<T> jList = new JList<>();
    private final JComponent content = new JPanel();

    //endregion
    //region Construction/Closing
    private VListWidget() {
        styleComponents();
        layoutComponents();
        initBindings();
    }

    public static <T> VListWidget<T> vListWidget() {
        return new VListWidget<>();
    }

    public void close() {
        propFactory.close();
    }

    //endregion
    //region selectedItem
    private final PropFieldNullable<T> selectedItemProp =
            propFactory.newPropNullable(null, this, "selectedItem");

    @Nullable
    public T getSelectedItem() {
        return selectedItemProp.get();
    }

    public void setSelectedItem(@Nullable T value) {
        getSelectedItemProp().set(value);
    }

    public PropFieldNullable<T> getSelectedItemProp() {
        return selectedItemProp;
    }

    //endregion
    //region Widget related
    @Override
    public JComponent getContent() {
        return content;
    }

    //endregion
    //region Style related
    private final static Color TOP_BAR_COLOR = new Color(0xE2E6Ec);

    private void styleComponents() {
        topBar.setBackground(TOP_BAR_COLOR);
        jList.setBorder(null);
    }

    //endregion
    //region Layout related
    private void layoutComponents() {
        bordered(topBar)
                //TODO: generalize/customize label/text
                .left(flowLeft(DEFAULT_FLOW_GAP, 0, titleLabel)) //NON-NLS
                .right(flowLeft(DEFAULT_FLOW_GAP, 0,
                        previousItemButton,
                        nextItemButton));

        bordered(content)
                .top(topBar)
                .bottom(scrollingNoBorder(jList));
    }

    //endregion
    //region Binding related
    private void initBindings() {
        previousItemButton.setAction(previousItemAction);
        nextItemButton.setAction(nextItemAction);

        jList.addListSelectionListener(e -> onSelectedItemInUIChanged());

        cellTextProviderProp.runDependingSwingCode(() -> jList.setCellRenderer(newListCellRenderer(getCellTextProvider())));
        listModelProp.runDependingSwingCode(() -> jList.setModel(getListModel()));
        selectedItemProp.runDependingSwingCode(()-> jList.setSelectedValue(selectedItemProp.get(), true));

        //noinspection StringConcatenation
        previousItemTextProp.runDependingSwingCode(() -> previousItemButton.setToolTipText(getPreviousItemText() + " (↑)"));
        //noinspection StringConcatenation
        nextItemTextProp.runDependingSwingCode(() -> nextItemButton.setToolTipText(getNextItemText() + " (↓)"));
        titleProp.runDependingSwingCode(() -> titleLabel.setText(getTitle()));
    }

    private void onSelectedItemInUIChanged() {
        updateSelectedItemProp();

        invokeLater(() -> {
            // ensure an item is selected, preferably at the "last selected index"
            if (getSelectedItem() == null) {
                int size = getListModel().getSize();
                if (size > 0) {
                    int i = limit(lastSelectedIndex, 0, size - 1);
                    jList.setSelectedIndex(i);
                }
            }

            // remember the last selected index
            int selectedIndex = jList.getSelectedIndex();
            if (selectedIndex >= 0) {
                lastSelectedIndex = selectedIndex;
            }

            // scroll list if required
            ensureSelectionIsVisible(jList);
        });
    }

    private void updateSelectedItemProp() {
        T value = jList.getSelectedValue();
        if (!Objects.equals(getSelectedItem(), value)) {
            selectedItemProp.set(value);
        }
    }

    //endregion
}
