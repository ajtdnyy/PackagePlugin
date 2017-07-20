/*
 * 文 件 名:  PropertiesUtil.java
 * 版    权:  Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  lancw
 * 修改时间:  2014-7-31
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.lancw.util;

import com.lancw.plugin.MainFrame;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * 项目名称：PackagePlugin
 * 类名称：PropertiesUtil
 * 类描述：
 * 创建人：lancw
 * 创建时间：2014-7-31 10:06:54
 * 修改人：lancw
 * 修改时间：2014-7-31 10:06:54
 * 修改备注：
 * <p>
 * @version 1.0
 */
public class PropertiesUtil {

    private static final Logger logger = MainFrame.LOGGER;

    public static String getProperty(String name) {
	return getProperty(name, "");
    }

    public static String getProperty(String name, String defaultValue) {
	FileInputStream fis = null;
	try {
	    Properties prop = new Properties();
	    File file = new File(System.getProperty("user.dir") + "/conf.ini");
	    if (file.exists()) {
		fis = new FileInputStream(file);
		prop.load(fis);
	    }
	    return prop.getProperty(name, defaultValue);
	} catch (Exception ex) {
	    logger.log(Level.SEVERE, null, ex);
	    JOptionPane.showMessageDialog(null, ex.getLocalizedMessage());
	} finally {
	    try {
		if (fis != null) {
		    fis.close();
		}
	    } catch (IOException ex) {
		logger.log(Level.SEVERE, null, ex);
	    }
	}
	return null;
    }

    public static void saveProperty(String key, String val) {
	FileOutputStream fos = null;
	FileInputStream fis = null;
	try {
	    Properties prop = new Properties();
	    File file = new File(System.getProperty("user.dir") + "/conf.ini");
	    if (file.exists()) {
		fis = new FileInputStream(file);
		prop.load(fis);
	    }
	    prop.setProperty(key, val);
	    fos = new FileOutputStream(file);
	    prop.store(fos, "配置文件");
	} catch (Exception ex) {
	    logger.log(Level.SEVERE, null, ex);
	    JOptionPane.showMessageDialog(null, ex.getLocalizedMessage());
	} finally {
	    try {
		if (fos != null) {
		    fos.close();
		}
		if (fis != null) {
		    fis.close();
		}
	    } catch (IOException ex) {
		logger.log(Level.SEVERE, null, ex);
	    }
	}
    }
}
