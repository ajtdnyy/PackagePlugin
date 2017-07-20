/*
 * 文 件 名:  FileUtil.java
 * 版    权:  Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  lancw
 * 修改时间:  2014-7-31
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.lancw.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lancw.model.FileTree;
import com.lancw.model.HttpConfig;
import com.lancw.model.SvnFilePath;
import com.lancw.plugin.MainFrame;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 * 项目名称：PackagePlugin 类名称：FileUtil 类描述： 创建人：lancw 创建时间：2014-7-31 11:22:37 修改人：lancw 修改时间：2014-7-31 11:22:37 修改备注：
 * <p>
 * @version 1.0
 */
public class FileUtil {

    static boolean flag;
    static Connection conn;

    /**
     * 序列化中的数据
     */
    public static final FileTree FILE_TREE = (FileTree) SerializableUtil.deSerializable();

    /**
     * 删除目录（文件夹）以及目录下的文件
     * <p>
     * @param sPath 被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String sPath) {
        //如果sPath不以文件分隔符结尾，自动添加文件分隔符
        if (!sPath.endsWith(File.separator)) {
            sPath = sPath + File.separator;
        }
        File dirFile = new File(sPath);
        //如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        //删除文件夹下的所有文件(包括子目录)
        File[] fs = dirFile.listFiles();
        for (File file : fs) {
            //删除子文件
            if (file.isFile()) {
                flag = deleteFile(file.getAbsolutePath());
                if (!flag) {
                    break;
                }
            } else { //删除子目录
                flag = deleteDirectory(file.getAbsolutePath());
                if (!flag) {
                    break;
                }
            }
        }
        if (!flag) {
            return false;
        }
        //删除当前目录
        return dirFile.delete();
    }

    /**
     * 删除单个文件
     * <p>
     * @param sPath 被删除文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    private static boolean deleteFile(String sPath) {
        flag = false;
        File file = new File(sPath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            file.delete();
            flag = true;
        }
        return flag;
    }

    /**
     * 复制文件
     *
     * @param s 源路径
     * @param t 目标路径
     * @return 目标路径
     */
    public static String copy(String s, String t) {
        File f = new File(s);
        if (f.isDirectory() || !f.isFile()) {
            MainFrame.updateInfo("忽略目录或不存在的文件：" + s);
            return "";
        }
        File sp = f.getParentFile();
        String name = f.getName();
        name = name.substring(0, name.indexOf('.'));
        for (File file : sp.listFiles()) {
            if (file.isFile()) {
                if (file.getName().contains(name + "$")) {//将内部类一并复制
                    String tgFile = new File(t).getParentFile().getAbsolutePath() + "/" + file.getName();
                    String tgp = fileChannelCopy(file.getAbsolutePath(), tgFile);
                    MainFrame.addFilePath(new SvnFilePath(tgp, null));
                }
            }
        }
        return fileChannelCopy(s, t);
    }

    /**
     * 通过文件隧道复制文件
     *
     * @param s 源路径
     * @param t 目标路径
     * @return 目标路径
     */
    private static String fileChannelCopy(String s, String t) {
        FileInputStream fi = null;
        FileOutputStream fo = null;
        FileChannel in = null;
        FileChannel out = null;
        try {
            fi = new FileInputStream(s);
            File f = new File(t);
            f.getParentFile().mkdirs();
            fo = new FileOutputStream(t);
            in = fi.getChannel();//得到对应的文件通道
            out = fo.getChannel();//得到对应的文件通道
            in.transferTo(0, in.size(), out);//连接两个通道，并且从in通道读取，然后写入out通道
            return t;
        } catch (Exception e) {
            MainFrame.LOGGER.log(Level.SEVERE, null, e);
            JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
            return "";
        } finally {
            try {
                if (fi != null) {
                    fi.close();
                }
                if (in != null) {
                    in.close();
                }
                if (fo != null) {
                    fo.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                MainFrame.LOGGER.log(Level.SEVERE, null, e);
            }
        }
    }

    /**
     * 查找文件
     * <p>
     * @param rootDir 查找起始目录
     * @param name    查找文件名文件夹名称
     * @param depth   查找目录深度
     * @param isVague 是否模糊匹配
     * @return
     */
    public static List<File> searchFile(File rootDir, String name, int depth, boolean isVague) {
        List<File> files = new ArrayList<>();
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            return files;
        }
        File[] listFiles = rootDir.listFiles();
        for (File listFile : listFiles) {
            if ((!isVague && listFile.getName().equals(name)) || (isVague && listFile.getName().contains(name))) {
                files.add(listFile);
            }
            if (listFile.isDirectory() && !listFile.getName().startsWith(".") && !listFile.isHidden() && depth > 0) {
                files.addAll(searchFile(listFile, name, depth--, isVague));
            }
        }
        return files;
    }

    /**
     * 递归查找单个文件，找到结果就返回
     *
     * @param rootDirPath 查找起始目录
     * @param paths       要查找的文件路径
     * @param depth       查找深度
     * @return
     */
    public static File searchOneFile(String rootDirPath, String[] paths, int depth) {
        File rootDir = new File(rootDirPath);
        if (!rootDir.exists() || !rootDir.isDirectory()) {
            return null;
        }
        FileTree tree = FILE_TREE.searchByPath(paths);//优先通过缓存数据查找
        if (tree != null) {
            return new File(tree.getAbsolutePath());
        }
        File[] listFiles = rootDir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return !pathname.isHidden();
            }
        });
        for (File listFile : listFiles) {
            if (!listFile.isHidden() && !listFile.getName().endsWith(".java") && !listFile.getName().startsWith(".")) {
                FILE_TREE.addChild(listFile);//扫描过的文件存储到多叉树中
            }
            if (!checkPath(paths, listFile)) {//路径不匹配时跳出当前路径遍历
                continue;
            }
            if (listFile.getName().equals(paths[paths.length - 1])) {
                File pFile = listFile.getParentFile();
                if (pFile.getName().equals(paths[paths.length - 2]) && pFile.getParentFile().getName().equals(paths[paths.length - 3])) {
                    return listFile;
                }
            }
            if (listFile.isDirectory() && !listFile.getName().startsWith(".") && !listFile.isHidden() && depth > 0) {
                File file = searchOneFile(listFile.getAbsolutePath(), paths, depth--);
                if (file != null) {
                    return file;
                }
            }
        }
        return null;
    }

    /**
     * 摸查项目输出目录
     * <p>
     * @param projectPath
     * @param kind        output 或 src
     * @return
     */
    public static String readXML(String projectPath, String kind) {
        try {
            SAXReader saxReader = new SAXReader();
            File classpath = new File(projectPath + "/.classpath");
            if (classpath.exists()) {
                Document document = saxReader.read(classpath);
                List<Element> out = (List<Element>) document.selectNodes("/classpath/classpathentry[@kind='" + kind + "']");
                String tmp = "";
                for (Element out1 : out) {
                    String combineaccessrules = out1.attributeValue("combineaccessrules");
                    if ("false".equals(combineaccessrules) && "src".equals(kind)) {
                        continue;
                    }
                    tmp += out1.attributeValue("path") + ",";
                }
                return tmp.isEmpty() ? tmp : tmp.substring(0, tmp.length() - 1);
            }
        } catch (DocumentException ex) {
            MainFrame.LOGGER.log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, ex.getLocalizedMessage());
        }
        return "";
    }

    private static final String HTTP_CONFIG_FILE = System.getProperty("user.dir") + "/httpConfig.xml";
    public static final LinkedHashMap<String, HttpConfig> CONFIG_MAP = new LinkedHashMap<>();

    /**
     * 获取配置
     *
     * @param name
     * @return
     * @throws Exception
     */
    public static HttpConfig getConfig(String name) throws Exception {
        HttpConfig hc = CONFIG_MAP.get(name);
        if (hc == null) {
            SAXReader reader = new SAXReader();
            File xml = new File(HTTP_CONFIG_FILE);
            Document doc;
            Element root;
            if (xml.exists()) {
                try (FileInputStream in = new FileInputStream(xml); Reader read = new InputStreamReader(in, "UTF-8")) {
                    doc = reader.read(read);
                    root = doc.getRootElement();
                    List<Element> els = root.selectNodes("/root/configs/config");
                    for (Element el : els) {
                        String nameStr = el.attributeValue("name");
                        String encodeType = el.attributeValue("encodeType");
                        String charset = el.attributeValue("charset");
                        String requestType = el.attributeValue("requestType");
                        String sendXML = el.attributeValue("sendXML");
                        String packHead = el.attributeValue("packHead");
                        String lowercaseEncode = el.attributeValue("lowercaseEncode");
                        String url = el.elementTextTrim("url");
                        String header = el.elementTextTrim("header");
                        String parameter = el.elementTextTrim("parameter");
                        String encodeField = el.elementTextTrim("encodeField");
                        String encodeKey = el.elementTextTrim("encodeKey");
                        String contentType = el.elementTextTrim("contentType");

                        HttpConfig config = new HttpConfig(nameStr, url, charset, header, parameter, requestType, contentType);
                        config.setSendXML(Boolean.valueOf(sendXML));
                        config.setEncodeKey(encodeKey);
                        config.setEncodeType(encodeType);
                        config.setEncodeFieldName(encodeField);
                        config.setLowercaseEncode(Boolean.valueOf(lowercaseEncode));
                        config.setPackHead(Boolean.valueOf(packHead));
                        CONFIG_MAP.put(nameStr, config);
                        if (nameStr.equals(name)) {
                            hc = config;
                        }
                    }
                }
            }
        }
        return hc;
    }

    /**
     * 删除配置
     *
     * @param name
     * @throws Exception
     */
    public static void removeHttpConfig(String name) throws Exception {
        SAXReader reader = new SAXReader();
        File xml = new File(HTTP_CONFIG_FILE);
        Document doc;
        Element root;
        try (FileInputStream in = new FileInputStream(xml); Reader read = new InputStreamReader(in, "UTF-8")) {
            doc = reader.read(read);
            root = doc.getRootElement();
            Element cfg = (Element) root.selectSingleNode("/root/configs");
            Element e = (Element) root.selectSingleNode("/root/configs/config[@name='" + name + "']");
            if (e != null) {
                cfg.remove(e);
                CONFIG_MAP.remove(name);
            }
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("UTF-8");
            XMLWriter writer = new XMLWriter(new FileOutputStream(xml), format);
            writer.write(doc);
            writer.close();
        }
    }

    /**
     * 保存http请求信息到xml
     *
     * @param hc
     * @return
     * @throws Exception
     */
    public static boolean saveHttpConfig(HttpConfig hc) throws Exception {
        SAXReader reader = new SAXReader();
        File xml = new File(HTTP_CONFIG_FILE);
        Document doc = null;
        Element root = null;
        boolean isNew = true;
        if (!xml.exists()) {
            doc = DocumentHelper.createDocument();
            root = DocumentHelper.createElement("root");
            root.addElement("configs");
            doc.add(root);
        }
        if (doc == null) {
            try (FileInputStream in = new FileInputStream(xml); Reader read = new InputStreamReader(in, "UTF-8")) {
                doc = reader.read(read);
                root = doc.getRootElement();
            }
        }
        Element cfg = (Element) root.selectSingleNode("/root/configs");
        Element e = (Element) root.selectSingleNode("/root/configs/config[@name='" + hc.getName() + "']");
        if (e != null) {
            isNew = false;
            cfg.remove(e);
        }
        CONFIG_MAP.put(hc.getName(), hc);

        Element cfg1 = cfg.addElement("config");
        cfg1.addAttribute("name", hc.getName());
        cfg1.addAttribute("encodeType", hc.getEncodeType());
        cfg1.addAttribute("charset", hc.getCharset());
        cfg1.addAttribute("requestType", hc.getRequestType());
        cfg1.addAttribute("sendXML", hc.getSendXML().toString());
        cfg1.addAttribute("packHead", hc.getPackHead().toString());
        cfg1.addAttribute("lowercaseEncode", hc.getLowercaseEncode().toString());

        cfg1.addElement("url").setText(hc.getUrl());
        cfg1.addElement("header").setText(hc.getHeaderStr());
        cfg1.addElement("parameter").setText(hc.getParameterStr());
        cfg1.addElement("encodeField").setText(hc.getEncodeFieldName());
        cfg1.addElement("encodeKey").setText(hc.getEncodeKey());

        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("UTF-8");
        XMLWriter writer = new XMLWriter(new FileOutputStream(xml), format);
        writer.write(doc);
        writer.close();
        return isNew;
    }

    /**
     * 检查路径是否匹配
     * <p>
     * @param paths
     * @param file
     * @return
     */
    private static boolean checkPath(String[] paths, File file) {
        String oldPath = "";
        String fileName = file.getName();
        String pname = file.getParentFile().getName();
        for (String path : paths) {
            if ((path.equals(fileName) && pname.equals(oldPath))) {
                return true;
            }
            oldPath = path;
        }
        return false;
    }

    /**
     * 输出excel接口文档
     *
     * @param param         请求参数 如：a=1&b=&c=
     * @param url           接口地址
     * @param interfaceName 接口名称
     * @param result        请求结果
     * @throws Exception
     */
    public static void writeExcel(String param, String url, String interfaceName, JsonObject result) throws Exception {
        HSSFWorkbook wb = new HSSFWorkbook();
        STYLE_MAP.clear();
        HSSFSheet sheet = wb.createSheet(interfaceName);
        int rowIndex = 0;

        setRegionBorder(sheet, rowIndex, 0, 9);//这操作会自动创建row
        HSSFRow row = sheet.getRow(rowIndex++);//如果这里再创建row原来合并的单元格式样式将被覆盖
        getCell(row, 0, HSSFColor.DARK_YELLOW.index, "业务描述");
        setRegionBorder(sheet, rowIndex, 0, 9);
        row = sheet.getRow(rowIndex++);
        getCell(row, 0, HSSFColor.LIGHT_YELLOW.index, "");

        setRegionBorder(sheet, rowIndex, 3, 7);
        row = sheet.getRow(rowIndex++);
        getCell(row, 0, -1, "创建者");
        getCell(row, 2, -1, "创建时间");
        getCell(row, 3, -1, DateUtil.formatDate(new Date(), "yyyy-MM-dd"));
        getCell(row, 8, -1, "适用");
        getCell(row, 9, -1, "教师端");

        setRegionBorder(sheet, rowIndex, 1, 7);
        row = sheet.getRow(rowIndex++);
        getCell(row, 0, -1, "接口名称");
        getCell(row, 1, -1, url.substring(url.indexOf("app")));
        getCell(row, 8, -1, "协议");
        getCell(row, 9, -1, "HTTP(POST)");

        setRegionBorder(sheet, rowIndex, 1, 9);
        row = sheet.getRow(rowIndex++);
        getCell(row, 0, -1, "接口描述");

        setRegionBorder(sheet, rowIndex, 2, 7);
        row = sheet.getRow(rowIndex++);
        getCell(row, 0, -1, "变更历史");
        getCell(row, 1, -1, "时间");
        getCell(row, 2, -1, "操作内容");
        getCell(row, 8, -1, "操作人");
        getCell(row, 9, -1, "备注");
        rowIndex = createEmptyRow(sheet, rowIndex);
        rowIndex = createEmptyRow(sheet, rowIndex);
        rowIndex = createEmptyRow(sheet, rowIndex);

        rowIndex = setTitle(sheet, rowIndex++, "参数");
        if (param != null && !param.trim().isEmpty()) {
            String[] pms = param.split("&");
            for (String pm : pms) {
                setData(sheet, rowIndex++, pm.split("=")[0], "String");
            }
        }
        rowIndex = setTitle(sheet, rowIndex++, "返回值");
        LinkedHashMap<Object, Object> map = new LinkedHashMap<>();
        analysisJson(map, result, "");
        index = rowIndex;
        analysisMap(map, sheet);
        sheet.autoSizeColumn(0, true);
        sheet.autoSizeColumn(1, true);
        sheet.autoSizeColumn(9, true);
        try (FileOutputStream fileOut = new FileOutputStream(interfaceName + ".xls")) {
            wb.write(fileOut);
        }
    }

    /**
     * 创建空行
     *
     * @param sheet
     * @param rowIndex
     */
    private static int createEmptyRow(HSSFSheet sheet, int rowIndex) {
        setRegionBorder(sheet, rowIndex, 2, 7);
        HSSFRow row = sheet.getRow(rowIndex);
        getCell(row, 0, -1, "");
        getCell(row, 1, -1, "");
        getCell(row, 8, -1, "");
        getCell(row, 9, -1, "");
        sheet.getRow(rowIndex++);
        return rowIndex;
    }

    /**
     * 创建单元格
     *
     * @param row
     * @param idx
     * @return
     */
    private static HSSFCell getCell(HSSFRow row, int idx, int color, Object val) {
        HSSFCell cell = row.getCell(idx) == null ? row.createCell(idx) : row.getCell(idx);
        cell.setCellStyle(getCellStyle(row.getSheet().getWorkbook(), color));
        cell.setCellValue(val.toString());
        return cell;
    }

    /**
     * 获取合并单元格式
     *
     * @param sheet
     * @param row
     * @param columnFrom
     * @param columnTo
     * @return
     */
    private static void setRegionBorder(HSSFSheet sheet, int row, int columnFrom, int columnTo) {
        CellRangeAddress region = new CellRangeAddress(row, row, columnFrom, columnTo);
        sheet.addMergedRegion(region);
        final short border = CellStyle.BORDER_THIN;
        HSSFWorkbook wb = sheet.getWorkbook();
        RegionUtil.setBorderBottom(border, region, sheet, wb);
        RegionUtil.setBorderTop(border, region, sheet, wb);
        RegionUtil.setBorderLeft(border, region, sheet, wb);
        RegionUtil.setBorderRight(border, region, sheet, wb);
        RegionUtil.setBottomBorderColor(HSSFColor.BLACK.index, region, sheet, wb);
        RegionUtil.setTopBorderColor(HSSFColor.BLACK.index, region, sheet, wb);
        RegionUtil.setLeftBorderColor(HSSFColor.BLACK.index, region, sheet, wb);
        RegionUtil.setRightBorderColor(HSSFColor.BLACK.index, region, sheet, wb);
    }

    /**
     * 获取单元格式样式
     *
     * @param wb
     * @param color
     * @return
     */
    private static HSSFCellStyle getCellStyle(HSSFWorkbook wb, int color) {
        if (STYLE_MAP.get(color) != null) {
            return STYLE_MAP.get(color);
        }
        HSSFCellStyle style = wb.createCellStyle();
        if (color != -1) {
            style.setFillForegroundColor(Short.valueOf(color + ""));
            style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            style.setAlignment(CellStyle.ALIGN_CENTER);
        }
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(HSSFColor.BLACK.index);
        style.setLeftBorderColor(HSSFColor.BLACK.index);
        style.setRightBorderColor(HSSFColor.BLACK.index);
        style.setTopBorderColor(HSSFColor.BLACK.index);
        STYLE_MAP.put(color, style);
        return style;
    }
    private static final LinkedHashMap<Integer, HSSFCellStyle> STYLE_MAP = new LinkedHashMap();

    /**
     * 设置标题单元格
     *
     * @param sheet
     * @param rowIndex
     * @param style
     * @param title
     * @return
     */
    private static int setTitle(HSSFSheet sheet, int rowIndex, String title) {
        setRegionBorder(sheet, rowIndex, 0, 9);
        HSSFRow row = sheet.getRow(rowIndex) == null ? sheet.createRow(rowIndex) : sheet.getRow(rowIndex);
        rowIndex++;
        getCell(row, 0, HSSFColor.LIGHT_YELLOW.index, title);
        setData(sheet, rowIndex++, "参数名", "数据类型", "是否必须", "描述", "备注");
        return rowIndex;
    }

    /**
     * 设置单元格数据
     *
     * @param sheet
     * @param rowIndex
     * @param data
     */
    private static void setData(HSSFSheet sheet, int rowIndex, String... data) {
        setRegionBorder(sheet, rowIndex, 3, 8);
        HSSFRow row = sheet.getRow(rowIndex) == null ? sheet.createRow(rowIndex) : sheet.getRow(rowIndex);
        getCell(row, 0, -1, data.length > 0 ? data[0] : "");
        getCell(row, 1, -1, data.length > 1 ? data[1] : "");
        getCell(row, 2, -1, data.length > 2 ? data[2] : "");
        getCell(row, 3, -1, data.length > 3 ? data[3] : "");
        getCell(row, 9, -1, data.length > 4 ? data[4] : "");
    }

    /**
     * 解析josn串到TreeMap
     *
     * @param treeMap
     * @param json
     * @param tmp
     */
    public static void analysisJson(LinkedHashMap<Object, Object> treeMap, JsonObject json, String tmp) {
        LinkedHashMap<Object, Object> map = new LinkedHashMap<>();
        json.entrySet().forEach((t) -> {
            Object k = t.getKey();
            Object val = t.getValue();
            if (val instanceof JsonObject) {
                map.put(tmp + k, val);
            } else if (val instanceof JsonArray) {
                JsonArray arr = (JsonArray) val;
                map.put(tmp + k, arr.size() > 0 ? arr.get(0) : "");
            } else {
                treeMap.put(tmp + k, val);
            }
        });
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            Object key = entry.getKey();
            LinkedHashMap<Object, Object> tmpMap = new LinkedHashMap<>();
            tmpMap.put(key, value);
            treeMap.put(key, tmpMap);
        }
        for (Map.Entry<Object, Object> entry : treeMap.entrySet()) {
            Object value = entry.getValue();
            Object key = entry.getKey();
            if (value instanceof LinkedHashMap) {
                LinkedHashMap<Object, Object> v = (LinkedHashMap<Object, Object>) value;
                if (v.get(key) instanceof JsonObject) {
                    analysisJson(v, (JsonObject) v.get(key), tmp + "  ");
                }
            }
        }
    }

    private static int index;

    /**
     * 解析TreeMap生成表格
     *
     * @param map
     * @param sheet
     */
    private static void analysisMap(LinkedHashMap<Object, Object> map, HSSFSheet sheet) {
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof LinkedHashMap) {
                LinkedHashMap sub = (LinkedHashMap) value;
                analysisMap(sub, sheet);
            } else {
                String val = value instanceof JsonObject ? "Object[data]" : value.toString();
                setData(sheet, index++, key.toString(), val);
            }
        }
    }
}
