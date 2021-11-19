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

import org.abego.guitesting.swing.internal.util.boxstyle.BoxStyle;
import org.abego.guitesting.swing.internal.util.prop.Bindings;
import org.abego.guitesting.swing.internal.util.prop.PropNullable;
import org.abego.guitesting.swing.internal.util.prop.PropService;
import org.abego.guitesting.swing.internal.util.prop.PropServices;
import org.abego.guitesting.swing.internal.util.widget.LabelWidget;
import org.abego.guitesting.swing.internal.util.widget.VStackWidget;
import org.abego.guitesting.swing.internal.util.widget.Widget;
import org.eclipse.jdt.annotation.Nullable;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import static org.abego.guitesting.swing.internal.util.widget.LabelWidget.labelWidget;
import static org.abego.guitesting.swing.internal.util.widget.VStackWidget.vStackWidget;

class SnapshotVariantsIndicatorWidget implements Widget {
    //region State/Model
    private final PropService propService = PropServices.getDefault();
    //region @Prop public @Nullable SnapshotVariant : variantsInfo
    private final PropNullable<SnapshotVariant> variantsInfoProp =
            propService.newPropNullable(null, this, "variantsInfo");

    @Nullable
    public SnapshotVariant getVariantsInfo() {
        return variantsInfoProp.get();
    }

    @SuppressWarnings("unused")
    public void setVariantsInfo(@Nullable SnapshotVariant value) {
        getVariantsInfoProp().set(value);
    }

    public PropNullable<SnapshotVariant> getVariantsInfoProp() {
        return variantsInfoProp;
    }

    //endregion
    //endregion
    //region Components
    private final VStackWidget contentWidget = vStackWidget();

    //endregion
    //region Construction
    private SnapshotVariantsIndicatorWidget() {
        styleComponents();
        layoutComponents();
        initBindings();
    }

    public static SnapshotVariantsIndicatorWidget variantsIndicatorWidget() {
        return new SnapshotVariantsIndicatorWidget();
    }

    //endregion
    //region Widget related
    @Override
    public JComponent getContent() {
        return contentWidget.getContent();
    }

    public void close() {
        bindings.close();
    }

    //endregion
    //region Style related
    private static final int BULLET_SIZE = 24;
    private static final Font BULLET_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, BULLET_SIZE);
    private static final BoxStyle BULLET_STYLE = BoxStyle.newBoxStyle().font(BULLET_FONT).create();

    private void styleComponents() {
        contentWidget.setBoxStyle(BoxStyle.newBoxStyle()
                .background(Color.WHITE));
    }

    //endregion
    //region Layout related
    private void layoutComponents() {
        // give the widget a fixed preferred width so the indicator does not
        // collapse when there are no items in the list.
        contentWidget.getContent().setPreferredSize(
                new Dimension(BULLET_SIZE, Integer.MAX_VALUE));
    }

    //endregion
    //region Binding related
    private final Bindings bindings = propService.newBindings();

    private void initBindings() {
        bindings.bindSwingCode(this::updateContent, variantsInfoProp);
    }

    private void updateContent() {
        @Nullable SnapshotVariant info = getVariantsInfo();
        if (info != null) {
            int n = info.getVariantsCount();
            int sel = info.getVariantsIndex();
            List<Widget> items = new ArrayList<>();
            for (int i = 0; n > 1 && i < n; i++) {
                LabelWidget label = labelWidget();
                label.setText(i == sel ? "●" : "○");
                label.setBoxStyle(BULLET_STYLE);
                items.add(label);
            }
            contentWidget.addAll(items);
        }
    }
    //endregion
}
