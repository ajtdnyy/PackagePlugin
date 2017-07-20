/*
 * 文 件 名:  MyActionListener.java
 * 版    权:  Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  lancw
 * 修改时间:  2014-6-16
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.lancw.plugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

/**
 * 项目名称：PackagePlugin
 * 类名称：MyActionListener
 * 类描述：
 * 创建人：lancw
 * 创建时间：2014-6-16 15:23:06
 * 修改人：lancw
 * 修改时间：2014-6-16 15:23:06
 * 修改备注：
 * <p>
 * @version 1.0
 */
public class MyActionListener extends MouseAdapter implements ActionListener {

    private final JTextComponent jtc;
    private final JPopupMenu menu = new JPopupMenu();

    public MyActionListener(JTextComponent jtc) {
	menu.add(getMenuItem("全选", "selectAll"));
	menu.add(getMenuItem("复制", "copy"));
	menu.add(getMenuItem("粘贴", "paste"));
	menu.add(getMenuItem("剪切", "cut"));
	menu.add(getMenuItem("清空", "clear"));
	this.jtc = jtc;
	jtc.addMouseListener(this);
    }

    public void showMenu(int x, int y) {
	menu.show(jtc, x, y);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
	if (e.getButton() == MouseEvent.BUTTON3) {
	    jtc.requestFocus();
	    showMenu(e.getX(), e.getY());
	}
    }

    private JMenuItem getMenuItem(String text, String cmd) {
	JMenuItem item = new JMenuItem(text);
	item.setActionCommand(cmd);
	item.addActionListener(this);
	return item;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	String cmd = e.getActionCommand();
	if ("selectAll".equals(cmd)) {
	    jtc.selectAll();
	} else if ("copy".equals(cmd)) {
	    jtc.copy();
	} else if ("paste".equals(cmd)) {
	    jtc.paste();
	} else if ("cut".equals(cmd)) {
	    jtc.cut();
	} else if ("clear".equals(cmd)) {
	    jtc.setText("");
	}
    }

}
