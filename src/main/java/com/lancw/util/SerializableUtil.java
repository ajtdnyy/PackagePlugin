/*
 * File:SerializableUtil.java
 * Date:2016-2-27 15:27:46
 * Encoding:UTF-8
 * Author:lancw
 * Description:将文件目录树序列化存储，方便下次读取，提升打包效率
 */
package com.lancw.util;

import com.lancw.model.FileTree;
import com.lancw.plugin.MainFrame;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import javax.swing.JOptionPane;

/**
 * 序列化工具类
 *
 * @author lancw
 */
public class SerializableUtil {

    public static final String SERIALIZABLE_FILE_NAME_STRING = System.getProperty("user.dir") + "/cacheData";

    /**
     * 序列化方法
     *
     * @param person
     */
    public static void serializable(Object person) {
        ObjectOutputStream outputStream = null;
        try {
            outputStream = new ObjectOutputStream(new FileOutputStream(SERIALIZABLE_FILE_NAME_STRING));
            outputStream.writeObject(person);
        } catch (IOException e) {
            MainFrame.LOGGER.log(Level.SEVERE, null, e);
            JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * 反序列化的方法
     *
     * @return
     */
    public static Object deSerializable() {
        ObjectInputStream ois = null;
        try {
            if (!new File(SERIALIZABLE_FILE_NAME_STRING).exists()) {
                return new FileTree(new File("root"), null);
            }
            ois = new ObjectInputStream(new FileInputStream(SERIALIZABLE_FILE_NAME_STRING));
            return ois.readObject();
        } catch (Exception e) {
            MainFrame.LOGGER.log(Level.SEVERE, null, e);
            JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
        } finally {
            try {
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException e) {
            }
        }
        return null;
    }
}
