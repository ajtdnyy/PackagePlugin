package com.lancw.action;

import com.lancw.annotation.MaskAnnotation;
import com.lancw.plugin.MainFrame;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * 项目名称：PackagePlugin
 * 类名称：ListSelectionListenerImpl
 * 类描述：
 * 创建人：lancw
 * 创建时间：2016-3-3 15:12:25
 * 修改人：lancw
 * 修改时间：2016-3-3 15:12:25
 * 修改备注：
 * <p>
 * @version 1.0
 */
public class ListSelectionListenerImpl implements ListSelectionListener {

    private final MainFrame frame;

    public ListSelectionListenerImpl(MainFrame frame) {
        this.frame = frame;
    }

    @Override
    @MaskAnnotation
    public void valueChanged(ListSelectionEvent e) {
        if (e.getSource().equals(frame.getHistoryList())) {
            frame.historyListChange(e);
        } else if (e.getSource() instanceof JList) {
            if (e.getValueIsAdjusting()) {
                frame.dbListValueChanged();
            }
        } else {
            frame.tableValueChanged(e);
        }
    }
}
