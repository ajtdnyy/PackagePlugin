package com.lancw.action;

import com.lancw.annotation.MaskAnnotation;
import com.lancw.plugin.MainFrame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;

/**
 * 项目名称：PackagePlugin
 * 类名称：ActionListenerImpl
 * 类描述：
 * 创建人：lancw
 * 创建时间：2016-3-3 13:10:17
 * 修改人：lancw
 * 修改时间：2016-3-3 13:10:17
 * 修改备注：
 * <p>
 * @version 1.0
 */
public class ActionListenerImpl implements ActionListener {

    private final MainFrame frame;

    public ActionListenerImpl(MainFrame frame) {
        this.frame = frame;
    }

    @Override
    @MaskAnnotation
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (null != cmd) {
            switch (cmd) {
                case "showLog":
                    frame.showLog();
                    break;
                case "packing":
                    frame.packing();
                    break;
                case "doHttpRequest":
                    frame.doHttpRequest();
                    break;
                case "generateSQLMap":
                    frame.generateSQLMap();
                    break;
                case "initDatabaseList":
                    try {
                        frame.initDatabaseList();
                    } catch (Exception ex) {
                        MainFrame.LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                        frame.showIbatisInfo(ex.getLocalizedMessage());
                    }
                    break;
                default:
                    break;
            }
        }
    }

}
