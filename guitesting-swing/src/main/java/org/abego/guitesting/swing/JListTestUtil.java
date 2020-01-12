package org.abego.guitesting.swing;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import java.awt.Component;

public class JListTestUtil {

    public static <T> String toDebugString(JList<T> list) {
        StringBuilder result = new StringBuilder();
        int n = list.getModel().getSize();
        for (int i = 0; i < n; i++) {

            boolean isSelected = list.getSelectionModel().isSelectedIndex(i);
            ListCellRenderer<? super T> r = list.getCellRenderer();
            T item = list.getModel().getElementAt(i);
            Component c = r.getListCellRendererComponent(list, item, i, isSelected, false);
            String text = (c instanceof JLabel) ? ((JLabel) c).getText() : String.valueOf(item);
            result.append(text);
            if (isSelected) {
                result.append(PackagePrivateUtil.SELECTED_ITEM_SUFFIX);
            }
            result.append("\n");
        }
        return result.toString();
    }

}
