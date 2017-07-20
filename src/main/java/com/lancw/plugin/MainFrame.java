/*
 * 文 件 名:  MainFrame.java
 * 版    权:  Copyright YYYY-YYYY,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  lancw
 * 修改时间:  2014-5-6
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package com.lancw.plugin;

import com.datePicker.DatePicker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.lancw.action.ActionListenerImpl;
import com.lancw.action.ListSelectionListenerImpl;
import com.lancw.handler.AnnotationInvocationHandler;
import com.lancw.model.HttpConfig;
import com.lancw.model.SvnFilePath;
import com.lancw.util.EncodeUtils;
import com.lancw.util.FileUtil;
import com.lancw.util.FormatUtil;
import com.lancw.util.PropertiesUtil;
import com.lancw.util.SerializableUtil;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.ibatis.abator.api.AbatorRunner;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.pushingpixels.substance.api.SubstanceLookAndFeel;
import org.pushingpixels.substance.api.skin.CremeCoffeeSkin;
import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNLogClient;
import org.tmatesoft.svn.core.wc.SVNRevision;

/**
 *
 * @author office
 */
public class MainFrame extends javax.swing.JFrame {

    static {
        DAVRepositoryFactory.setup();
        SVNRepositoryFactoryImpl.setup();
        FSRepositoryFactory.setup();
    }

    private String lastDatabase = "";
    private boolean forceQuery = false;
    private SVNClientManager manager;
    private final DatePicker enDatePicker;
    private final DatePicker beginDatePicker;
    private final ActionListenerImpl actionlistenerImpl;
    private final ListSelectionListenerImpl listSelectionListenerImpl;
    private final JFileChooser jfc = new JFileChooser();
    private final List<String> logs = new ArrayList<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private static String jarPath = System.getProperty("user.dir") + "/" + "out";
    private final File tmpDir = new File(System.getProperty("user.dir") + "/" + "temp");
    private static final Set<SvnFilePath> SVNFILEPATHS = new LinkedHashSet<>();
    public static final Logger LOGGER = Logger.getLogger(MainFrame.class.getName());
    private final HashMap<Long, Map<String, SVNLogEntryPath>> detailData = new LinkedHashMap<>();

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        long time = System.currentTimeMillis();
        initComponents();
        beginDatePicker = new DatePicker(this, true, beginDate);
        enDatePicker = new DatePicker(this, true, endDate);
        actionlistenerImpl = new ActionListenerImpl(this);
        listSelectionListenerImpl = new ListSelectionListenerImpl(this);
        initData();
        LOGGER.log(Level.INFO, "数据初始化完成，耗时{0}毫秒", (System.currentTimeMillis() - time));
    }

    private void initData() {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                setMinimumSize(new Dimension(800, 600));
                setTitle("辅助工具 v20170705 powered by lancw 461354751@qq.com");
                setLocationRelativeTo(null);
                setDefaultCloseOperation(EXIT_ON_CLOSE);
                Calendar c = Calendar.getInstance();
                sdf.applyPattern("yyyy-MM-dd");

                endDate.setText(sdf.format(c.getTime()));
                c.add(Calendar.DATE, -7);
                beginDate.setText(sdf.format(c.getTime()));

                historyList.setModel(new DefaultListModel<String>());
                historyList.addListSelectionListener(listSelectionListenerImpl);

                svnName.setText(PropertiesUtil.getProperty(SVN_USER_NAME));
                svnPassword.setText(PropertiesUtil.getProperty(SVN_PASSWORD));
                outputPath.setText(PropertiesUtil.getProperty(OUTPUT_PATH));
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                String dir = PropertiesUtil.getProperty(PROJECT_PATH);
                jfc.setCurrentDirectory(new File(dir));
                path.setText(dir);
                isWebProject.setSelected(Boolean.valueOf(PropertiesUtil.getProperty(IS_WEB_PROJECT, "true")));
                rootNameProject.setSelected(Boolean.valueOf(PropertiesUtil.getProperty(IS_ADD_PROJECT_NAME, "true")));
                createFileList.setSelected(Boolean.valueOf(PropertiesUtil.getProperty(IS_CREATE_FILE_LIST, "false")));
                xmlToInf.setSelected(Boolean.valueOf(PropertiesUtil.getProperty(IS_XML_TO_WEB_INF, "false")));
                detailTable.getColumnModel().getColumn(0).setPreferredWidth(600);
                if (!tmpDir.exists()) {
                    tmpDir.mkdir();
                }
                new File(jarPath).mkdirs();
                clearTemp();
                ButtonGroup bg1 = new ButtonGroup();
                bg1.add(radioDES3);
                bg1.add(radioMD5);
                bg1.add(radioSHA);
                ButtonGroup bg2 = new ButtonGroup();
                bg2.add(radioGet);
                bg2.add(radioPost);

                logTable.getSelectionModel().addListSelectionListener(listSelectionListenerImpl);
                detailTable.getSelectionModel().addListSelectionListener(listSelectionListenerImpl);
                initLogs();
                initItem();
                if (System.getProperty("java.version").startsWith("1.8")) {
                    logTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer());
                    detailTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer());
                }
                initIbatisConfig();
                tablesList.getSelectionModel().addListSelectionListener(listSelectionListenerImpl);
            }
        });
    }

    /**
     * 通过此方法添加的监听将通过代理 调用前显示遮罩
     * <p>
     */
    public void initActionPerform() {
        InvocationHandler handler = new AnnotationInvocationHandler(this, this.getActionlistenerImpl());
        ActionListenerImpl c = this.getActionlistenerImpl();
        ActionListener impl = (ActionListener) Proxy.newProxyInstance(c.getClass().getClassLoader(), c.getClass().getInterfaces(), handler);
        showLogBtn.addActionListener(impl);
        packBtn.addActionListener(impl);
        httpRequestBtn.addActionListener(impl);
        readDatabase.addActionListener(impl);
        generateSQLMap.addActionListener(impl);
    }

    /**
     * 通过此方法添加的监听将通过代理 调用前显示遮罩
     * <p>
     */
    public void initListSelectionListener() {
        InvocationHandler handler2 = new AnnotationInvocationHandler(this, this.getListSelectionListenerImpl());
        ListSelectionListenerImpl cc = this.getListSelectionListenerImpl();
        ListSelectionListener listener = (ListSelectionListener) Proxy.newProxyInstance(cc.getClass().getClassLoader(), cc.getClass().getInterfaces(), handler2);
        dbList.addListSelectionListener(listener);
    }

    public JList getHistoryList() {
        return historyList;
    }

    public void historyListChange(ListSelectionEvent e) {
        try {
            HttpConfig hc = FileUtil.getConfig((String) historyList.getSelectedValue());
            if (hc == null) {
                return;
            }
            historyName.setText(hc.getName());
            url.setText(hc.getUrl());
            header.setText(hc.getHeaderStr());
            data.setText(hc.getParameterStr());
            toJson.setSelected(hc.getPackHead());
            postXML.setSelected(hc.getSendXML());
            toLower.setSelected(hc.getLowercaseEncode());
            encodeField.setText(hc.getEncodeFieldName());
            contentType.setSelectedItem(hc.getContentType() == null ? "default" : hc.getContentType());
            key.setText(hc.getEncodeKey());
            String method = hc.getRequestType();
            String encode = hc.getEncodeType();
            if ("get".equals(method)) {
                radioGet.setSelected(true);
            } else {
                radioPost.setSelected(true);
            }
            if (null != encode) {
                switch (encode) {
                    case "sha":
                        radioSHA.setSelected(true);
                        break;
                    case "md5":
                        radioMD5.setSelected(true);
                        break;
                    default:
                        radioDES3.setSelected(true);
                        break;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 数据库列表选择事件处理
     */
    public void dbListValueChanged() {
        forceQuery = false;
        queryTables();
    }

    /**
     * 表选择事件处理
     * <p>
     * @param e
     */
    public void tableValueChanged(ListSelectionEvent e) {
        DefaultListSelectionModel t = (DefaultListSelectionModel) e.getSource();
        if (t.equals(tablesList.getSelectionModel())) {
            try {
                if (tablesList.getSelectedRowCount() <= 0) {
                    return;
                }
                String info1 = "当前数据库：" + lastDatabase;
                info1 += " 共计" + tablesList.getModel().getRowCount() + "张表";
                info1 += "共选中表" + tablesList.getSelectedRowCount() + "(未选中时将当前数据库所有表进行生成)";
                databaseInfoBar.setText(info1);
            } catch (Exception ex) {
                showIbatisInfo(ex.getLocalizedMessage());
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }
        } else if (t.equals(detailTable.getSelectionModel())) {
            if (detailTable.getSelectedRowCount() > 0 && e.getValueIsAdjusting()) {
                statusLabel.setText("共选择了" + logTable.getSelectedRowCount() + "条日志，共选择了" + detailTable.getSelectedRowCount() + "个文件（不选择默认全部[" + detailTable.getRowCount() + "]）");
            }
        } else if (t.equals(logTable.getSelectionModel())) {
            if (logTable.getSelectedRowCount() > 0 && e.getValueIsAdjusting()) {
                int[] cols = logTable.getSelectedRows();
                DefaultTableModel mod = (DefaultTableModel) detailTable.getModel();
                mod.setRowCount(0);
                Map<String, SVNLogEntryPath> map = new HashMap<>();
                for (int i : cols) {
                    i = logTable.convertRowIndexToModel(i);//排序后要转移行号
                    Object key1 = logTable.getModel().getValueAt(i, 1);
                    map.putAll(detailData.get(Long.valueOf(key1.toString())));
                }
                for (Map.Entry<String, SVNLogEntryPath> entrySet : map.entrySet()) {
                    SVNLogEntryPath path1 = entrySet.getValue();
                    mod.addRow(new Object[]{path1.getPath(), getType(path1.getType()), path1.getCopyPath(), getCopyRevision(path1.getCopyRevision())});
                }
                statusLabel.setText("共选择了" + logTable.getSelectedRowCount() + "条日志，共选择了" + detailTable.getSelectedRowCount() + "个文件（不选择默认全部[" + detailTable.getRowCount() + "]）");
            }
        }
    }

    /**
     * 查询指定数据库里的表，并显示在列表中
     */
    private void queryTables() {
        try {
            String sel = (String) dbList.getSelectedValue();
            sel = sel == null ? "" : sel;
            if (!sel.equals(lastDatabase) || forceQuery) {
                lastDatabase = (String) dbList.getSelectedValue();
                DefaultTableModel model;
                Connection conn = getConnection();
                Statement stmt = conn.createStatement();
                String sql = "select  TABLE_NAME,create_time from Information_schema.tables WHERE table_type='BASE TABLE' and TABLE_SCHEMA='" + lastDatabase + "'";
                sql += " order by table_name asc";
                ResultSet ret = stmt.executeQuery(sql);
                model = (DefaultTableModel) tablesList.getModel();
                model.setRowCount(0);
                sdf.applyPattern("yyyy-MM-dd");
                while (ret.next()) {
                    String tableName = ret.getString(1);
                    String modelName = "";
                    String[] tmp = tableName.split("_");
                    for (String tmp1 : tmp) {
                        modelName += tmp1.replaceFirst(new String(new char[]{tmp1.charAt(0)}), new String(new char[]{Character.toUpperCase(tmp1.charAt(0))}));
                    }
                    Date date = ret.getDate(2);
                    model.addRow(new String[]{tableName, modelName, date != null ? sdf.format(date) : ""});
                }
                tablesList.setModel(model);
                stmt.close();
                conn.close();
                databaseInfoBar.setText("当前数据库：" + lastDatabase + " 共计" + model.getRowCount() + "张表");
            }
            oldModel = null;
        } catch (Exception ex) {
            showIbatisInfo(ex.getLocalizedMessage());
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    public static void addFilePath(SvnFilePath path) {
        SVNFILEPATHS.add(path);
    }

    /**
     * 清空临时目录
     */
    private void clearTemp() {
        for (File file : tmpDir.listFiles()) {
            if (file.isFile()) {
                file.delete();
            } else {
                FileUtil.deleteDirectory(file.getAbsolutePath());
            }
        }
    }

    /**
     * 初始化日志记录
     */
    private void initLogs() {
        try {
            File file = new File(System.getProperty("user.dir") + "/logs");
            if (!file.exists()) {
                file.mkdirs();
            }
            FileHandler fileHandler = new FileHandler(file.getPath() + "/log.log");
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 显示svn日志
     */
    public void showLog() {
        long time = System.currentTimeMillis();
        String userName = svnName.getText();
        String password = new String(svnPassword.getPassword());
        String pathStr = path.getText();
        String begin = beginDate.getText();
        String end = endDate.getText();
        try {
            if (userName == null || userName.isEmpty()) {
                throw new Exception("svn用户名不能为空！");
            }
            if (password.isEmpty()) {
                throw new Exception("svn密码不能为空！");
            }
            if (pathStr == null || pathStr.isEmpty()) {
                throw new Exception("项目路径不能为空！");
            }

            saveProperties();
            DefaultSVNOptions options = new DefaultSVNOptions();
            manager = SVNClientManager.newInstance(options, userName, password); //如果需要用户名密码
            SVNLogClient logClient = manager.getLogClient();
            detailData.clear();
            logs.clear();
            ((DefaultTableModel) logTable.getModel()).setRowCount(0);
            ((DefaultTableModel) detailTable.getModel()).setRowCount(0);
            sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
            begin += " 00:00:00";
            end += " 23:59:59";
            String[] strs = pathStr.split(";");
            for (String string : strs) {
                File proj = new File(string);
                DirEntryHandler handler = new DirEntryHandler(proj.getName()); // 在svn
                logClient.doLog(new File[]{proj}, SVNRevision.UNDEFINED, SVNRevision.create(sdf.parse(begin)), SVNRevision.create(sdf.parse(end)), false, true, 1000, handler); // 列出当前svn地址的目录，对每个文件进行处理
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, ex.getLocalizedMessage());
        } finally {
            LOGGER.log(Level.INFO, "\u65e5\u5fd7\u52a0\u8f7d\u5b8c\u6210\uff0c\u7528\u65f6{0}\u6beb\u79d2", (System.currentTimeMillis() - time));
        }
    }

    /**
     * 更新打包详情信息
     * <p>
     * @param msg
     */
    public static void updateInfo(final String msg) {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                info.append(msg + System.getProperty("line.separator"));
                info.invalidate();
            }
        });
    }

    /**
     * 初始化httpRequest中已存储的http请求信息及添加输入框右键菜单监听事件
     */
    private void initItem() {
        Integer index = Integer.valueOf(PropertiesUtil.getProperty(HISTORY_ITEM_INDEX, "0"));
        DefaultListModel lm = (DefaultListModel) historyList.getModel();
        if (!new File(System.getProperty("user.dir") + "/httpConfig.xml").exists()) {
            for (int i = 1; i <= index; i++) {
                lm.addElement(PropertiesUtil.getProperty(HISTORY_NAME + i));
                historyName.setText(PropertiesUtil.getProperty(HISTORY_NAME + i));
                url.setText(PropertiesUtil.getProperty(HISTORY_URL + i));
                header.setText(PropertiesUtil.getProperty(HISTORY_HEADER + i));
                data.setText(PropertiesUtil.getProperty(HISTORY_DATA + i));
                toJson.setSelected(Boolean.valueOf(PropertiesUtil.getProperty(HISTORY_PACKAGE + i)));
                postXML.setSelected(Boolean.valueOf(PropertiesUtil.getProperty(IS_POST_XML + i)));
                toLower.setSelected(Boolean.valueOf(PropertiesUtil.getProperty(HISTORY_TO_LOWER_CASE + i)));
                encodeField.setText(PropertiesUtil.getProperty(HISTORY_FIELD + i));
                key.setText(PropertiesUtil.getProperty(HISTORY_KEY + i));
                String method = PropertiesUtil.getProperty(HISTORY_METHOD + i);
                String encode = PropertiesUtil.getProperty(HISTORY_ENCODE + i);

                HttpConfig hc = new HttpConfig(PropertiesUtil.getProperty(HISTORY_NAME + i), url.getText(), charset.getSelectedItem().toString(), header.getText(), data.getText(), method, null);
                hc.setSendXML(postXML.isSelected());
                hc.setEncodeKey(key.getText());
                hc.setEncodeType(encode);
                hc.setEncodeFieldName(encodeField.getText());
                hc.setLowercaseEncode(toLower.isSelected());
                hc.setPackHead(toJson.isSelected());
                try {
                    FileUtil.saveHttpConfig(hc);
                } catch (Exception ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
        initHistoryItem();
        MouseListener.addListener(svnName);
        MouseListener.addListener(path);
        MouseListener.addListener(info);
        MouseListener.addListener(historyName);
        MouseListener.addListener(url);
        MouseListener.addListener(header);
        MouseListener.addListener(data);
        MouseListener.addListener(respBody);
        MouseListener.addListener(encodeField);
        MouseListener.addListener(key);
        MouseListener.addListener(ibatisInfo);
        MouseListener.addListener(textInput);
        MouseListener.addListener(textOutput);
        MouseListener.addListener(fieldSplit);
    }

    private void initHistoryItem() {
        DefaultListModel lm = (DefaultListModel) historyList.getModel();
        lm.clear();
        try {
            FileUtil.getConfig("");//初始化数据
            String hisFilter = historyFilter.getText();
            for (Map.Entry<String, HttpConfig> entry : FileUtil.CONFIG_MAP.entrySet()) {
                String key1 = entry.getKey();
                if (hisFilter != null && !hisFilter.isEmpty()) {
                    HttpConfig hc = entry.getValue();
                    if (!key1.toLowerCase().contains(hisFilter.toLowerCase()) && !hc.getUrl().toLowerCase().contains(hisFilter.toLowerCase())) {
                        continue;
                    }
                }
                if (key1.contains("登录")) {
                    lm.insertElementAt(key1, 0);
                } else {
                    lm.addElement(key1);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 读取数据库配置
     */
    private void initIbatisConfig() {
        try {
            SAXReader reader = new SAXReader();
            FileInputStream in = new FileInputStream(new File(AbatorRunner.ABATOR_CONFIG_FILE_PATH));
            Reader read = new InputStreamReader(in, "utf8");
            Document doc = reader.read(read);
            Element jdbcNode = (Element) doc.selectSingleNode("/abatorConfiguration/abatorContext/jdbcConnection");
            String jdbcUrl = jdbcNode.attributeValue("connectionURL");//jdbc:mysql://127.0.0.1:3306/enterprise
            dbAddress.setText(jdbcUrl.replace("jdbc:mysql://", "").split(":")[0]);
            dbPassword.setText(jdbcNode.attributeValue("password"));
            dbUserName.setText(jdbcNode.attributeValue("userId"));
            String tmp = jdbcUrl.replace("jdbc:mysql://", "").split(":")[1];
            tmp = tmp.substring(0, tmp.indexOf('/'));
            dbPort.setText(tmp);
            Element modelNode = (Element) doc.selectSingleNode("/abatorConfiguration/abatorContext/javaModelGenerator");
            modelPackage.setText(modelNode.attributeValue("targetPackage"));
            Element daoNode = (Element) doc.selectSingleNode("/abatorConfiguration/abatorContext/daoGenerator");
            daoPackage.setText(daoNode.attributeValue("targetPackage"));
            Element mapNode = (Element) doc.selectSingleNode("/abatorConfiguration/abatorContext/sqlMapGenerator");
            sqlMapPackage.setText(mapNode.attributeValue("targetPackage"));
            outPath.setText(mapNode.attributeValue("targetProject"));
            read.close();
            in.close();
            EventQueue.invokeLater(() -> {
                try {
                    initDatabaseList();
                } catch (Exception ex) {
                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                    showIbatisInfo(ex.getLocalizedMessage());
                }
            });
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            showIbatisInfo(ex.getLocalizedMessage());
        }
    }

    /**
     * 初始化数据库列表
     * <p>
     * @throws Exception
     */
    public void initDatabaseList() throws Exception {
        Connection conn = getConnection();
        Statement stmt = conn.createStatement();
        ResultSet ret = stmt.executeQuery("show databases;");
        DefaultListModel model = new DefaultListModel();
        while (ret.next()) {
            model.addElement(ret.getString("Database"));
        }
        dbList.setModel(model);
        lastDatabase = "";
        stmt.close();
        conn.close();
    }

    /**
     * 获取数据库连接
     * <p>
     * @return @throws Exception
     */
    private Connection getConnection() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        String address = dbAddress.getText();
        String port = dbPort.getText();
        String user = dbUserName.getText();
        String pwd = new String(dbPassword.getPassword());
        String database = (String) dbList.getSelectedValue();
        database = database == null ? "mysql" : database;
        Connection conn = DriverManager.getConnection("jdbc:mysql://" + address + ":" + port + "/" + database + "?user=" + user + "&password=" + pwd);
        return conn;
    }

    /**
     * 更新ibatis生成输出信息
     * <p>
     * @param text
     */
    public void showIbatisInfo(final String text) {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                ibatisInfo.append(text + System.getProperty("line.separator"));
                ibatisInfo.invalidate();
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tab = new javax.swing.JTabbedPane();
        jPanel7 = new javax.swing.JPanel();
        packageTabPanel = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        beginDate = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        path = new javax.swing.JTextField();
        choosePath = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        svnName = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        svnPassword = new javax.swing.JPasswordField();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        logTable = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        detailTable = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        endDate = new javax.swing.JTextField();
        showLogBtn = new javax.swing.JButton();
        packBtn = new javax.swing.JButton();
        isWebProject = new javax.swing.JCheckBox();
        rootNameProject = new javax.swing.JCheckBox();
        xmlToInf = new javax.swing.JCheckBox();
        createFileList = new javax.swing.JCheckBox();
        statusLabel = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        logFilterName = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        outputPath = new javax.swing.JTextField();
        chooseOutputPath = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        info = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        url = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        header = new javax.swing.JTextArea();
        jScrollPane5 = new javax.swing.JScrollPane();
        data = new javax.swing.JTextArea();
        jLabel9 = new javax.swing.JLabel();
        jScrollPane6 = new javax.swing.JScrollPane();
        respBody = new javax.swing.JTextArea();
        radioPost = new javax.swing.JRadioButton();
        jLabel10 = new javax.swing.JLabel();
        radioGet = new javax.swing.JRadioButton();
        httpRequestBtn = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        radioSHA = new javax.swing.JRadioButton();
        radioMD5 = new javax.swing.JRadioButton();
        radioDES3 = new javax.swing.JRadioButton();
        jLabel12 = new javax.swing.JLabel();
        encodeField = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        key = new javax.swing.JTextField();
        toJson = new javax.swing.JCheckBox();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        historyName = new javax.swing.JTextField();
        saveHistory = new javax.swing.JButton();
        charset = new javax.swing.JComboBox();
        toLower = new javax.swing.JCheckBox();
        postXML = new javax.swing.JCheckBox();
        keepSession = new javax.swing.JCheckBox();
        tokenFieldName = new javax.swing.JTextField();
        analysisResult = new javax.swing.JCheckBox();
        jScrollPane10 = new javax.swing.JScrollPane();
        historyList = new javax.swing.JList<>();
        historyFilter = new javax.swing.JTextField();
        contentType = new javax.swing.JComboBox<>();
        jPanel5 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel4 = new javax.swing.JPanel();
        generateSQLMap = new javax.swing.JButton();
        jLabel18 = new javax.swing.JLabel();
        dbAddress = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        dbPort = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        dbUserName = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        dbPassword = new javax.swing.JPasswordField();
        jScrollPane8 = new javax.swing.JScrollPane();
        dbList = new javax.swing.JList();
        jScrollPane9 = new javax.swing.JScrollPane();
        tablesList = new javax.swing.JTable();
        readDatabase = new javax.swing.JButton();
        databaseInfoBar = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        modelPackage = new javax.swing.JTextField();
        jLabel24 = new javax.swing.JLabel();
        daoPackage = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        sqlMapPackage = new javax.swing.JTextField();
        jLabel26 = new javax.swing.JLabel();
        outPath = new javax.swing.JTextField();
        jButton4 = new javax.swing.JButton();
        isOverwrite = new javax.swing.JCheckBox();
        fileChooser = new javax.swing.JButton();
        filter = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        ibatisInfo = new javax.swing.JTextArea();
        jPanel8 = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        exchangeBtn = new javax.swing.JButton();
        jScrollPane13 = new javax.swing.JScrollPane();
        textInput = new javax.swing.JTextArea();
        jScrollPane14 = new javax.swing.JScrollPane();
        textOutput = new javax.swing.JTextArea();
        fieldSplit = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        javaType = new javax.swing.JComboBox<>();
        jLabel29 = new javax.swing.JLabel();
        prefix = new javax.swing.JTextField();
        jsonField = new javax.swing.JCheckBox();
        xmlEnum = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(932, 631));

        tab.setMinimumSize(new java.awt.Dimension(900, 600));
        tab.setName(""); // NOI18N
        tab.setPreferredSize(new java.awt.Dimension(900, 600));

        jPanel7.setMinimumSize(new java.awt.Dimension(900, 600));
        jPanel7.setPreferredSize(new java.awt.Dimension(900, 600));
        jPanel7.setLayout(new java.awt.GridLayout(1, 1));

        jLabel1.setText("起始日期");

        beginDate.setEditable(false);
        beginDate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                beginDateMouseClicked(evt);
            }
        });

        jLabel2.setText("项目地址");

        path.setToolTipText("可以同时选择多个项目，需svn客户端检出，eclipse插件检出的无效");

        choosePath.setText("浏览");
        choosePath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                choosePathActionPerformed(evt);
            }
        });

        jLabel3.setText("SVN用户");

        jLabel4.setText("SVN密码");

        jSplitPane1.setDividerLocation(150);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        logTable.setAutoCreateRowSorter(true);
        logTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "projectName", "Revision", "Author", "Date", "Message"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(logTable);

        jSplitPane1.setTopComponent(jScrollPane1);

        detailTable.setAutoCreateRowSorter(true);
        detailTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Path", "Action", "Copy from path", "Revision"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(detailTable);

        jSplitPane1.setRightComponent(jScrollPane2);

        jLabel5.setText("截止日期");

        endDate.setEditable(false);
        endDate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                endDateMouseClicked(evt);
            }
        });

        showLogBtn.setText("查看");
        showLogBtn.setActionCommand("showLog");

        packBtn.setText("打包");
        packBtn.setActionCommand("packing");

        isWebProject.setSelected(true);
        isWebProject.setText("class打包到WEB-INF");

        rootNameProject.setText("根路径为项目名");

        xmlToInf.setText("xml打包到WEB-INF");
        xmlToInf.setToolTipText("放置在包路径中的xml文件是否也按包路径打包");

        createFileList.setText("生成文件清单");
        createFileList.setToolTipText("生成打包的文件清单到txt");

        statusLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);

        jLabel17.setText("日志过滤");

        logFilterName.setToolTipText("只显示用户名，消息包含过滤内容的日志");

        jLabel19.setText("输出目录");

        chooseOutputPath.setText("浏览");
        chooseOutputPath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooseOutputPathActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel1))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(beginDate, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                                    .addComponent(svnName, javax.swing.GroupLayout.Alignment.LEADING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel5))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(svnPassword)
                                    .addComponent(endDate, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(xmlToInf)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(rootNameProject)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(isWebProject))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel17)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(logFilterName)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(createFileList))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel19))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(path, javax.swing.GroupLayout.DEFAULT_SIZE, 647, Short.MAX_VALUE)
                                    .addComponent(outputPath))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(choosePath)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(showLogBtn))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(chooseOutputPath)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(packBtn)))))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jSplitPane1))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(svnName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(beginDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(svnPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel17)
                            .addComponent(logFilterName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(endDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(xmlToInf)
                            .addComponent(rootNameProject)
                            .addComponent(isWebProject)
                            .addComponent(createFileList))
                        .addGap(5, 5, 5)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(choosePath)
                            .addComponent(showLogBtn)
                            .addComponent(path, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(chooseOutputPath)
                            .addComponent(packBtn)
                            .addComponent(outputPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel19))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 403, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        packageTabPanel.addTab("选择打包文件", jPanel1);

        info.setEditable(false);
        info.setColumns(20);
        info.setRows(5);
        jScrollPane3.setViewportView(info);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 902, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 553, Short.MAX_VALUE)
                .addContainerGap())
        );

        packageTabPanel.addTab("打包详情", jPanel2);

        jPanel7.add(packageTabPanel);

        tab.addTab("打增量包", jPanel7);

        jPanel3.setMinimumSize(new java.awt.Dimension(900, 600));
        jPanel3.setPreferredSize(new java.awt.Dimension(900, 600));

        jLabel6.setText("请求地址");

        jLabel7.setText("头部参数");

        jLabel8.setText("请求参数");

        header.setColumns(20);
        header.setLineWrap(true);
        header.setRows(1);
        jScrollPane4.setViewportView(header);

        data.setColumns(20);
        data.setLineWrap(true);
        data.setRows(5);
        jScrollPane5.setViewportView(data);

        jLabel9.setText("返回内容");

        respBody.setEditable(false);
        respBody.setColumns(20);
        respBody.setLineWrap(true);
        respBody.setRows(5);
        jScrollPane6.setViewportView(respBody);

        radioPost.setSelected(true);
        radioPost.setText("post");

        jLabel10.setText("请求方式");

        radioGet.setText("get");

        httpRequestBtn.setText("执行");
        httpRequestBtn.setActionCommand("doHttpRequest");

        jLabel11.setText("加密方式");

        radioSHA.setSelected(true);
        radioSHA.setText("sha1");

        radioMD5.setText("md5");

        radioDES3.setText("des3");

        jLabel12.setText("加密字段");

        jLabel13.setText("加密key");

        toJson.setSelected(true);
        toJson.setText("头部参数打包成token包的JSON串");

        jLabel14.setText("历史记录");

        jLabel15.setText("记录名称");

        saveHistory.setText("保存记录");
        saveHistory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveHistoryActionPerformed(evt);
            }
        });

        charset.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "UTF-8", "GBK", "gb18030", "iso-8859-1" }));

        toLower.setText("加密串小写（MD5,SHA1）");

        postXML.setText("POST XML");

        keepSession.setText("保持本次token");
        keepSession.setToolTipText("后续请求中头部参数中token将替换为本次的token");

        tokenFieldName.setText("token");
        tokenFieldName.setToolTipText("保持的token将替换掉头部参数为该名称字段对应参数值");

        analysisResult.setText("生成markdown文档");

        historyList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        historyList.setDragEnabled(true);
        historyList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                historyListMouseClicked(evt);
            }
        });
        jScrollPane10.setViewportView(historyList);

        historyFilter.setToolTipText("根据名称或url过滤接口列表");
        historyFilter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                historyFilterKeyReleased(evt);
            }
        });

        contentType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "default", "text/xml", "application/json", "multipart/form-data", "application/x-www-form-urlencoded" }));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(historyFilter, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel9)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8)
                            .addComponent(jLabel15))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane5)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(historyName, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(saveHistory)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(keepSession)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tokenFieldName, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(charset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 27, Short.MAX_VALUE))
                            .addComponent(jScrollPane6, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(url)
                            .addComponent(jScrollPane4)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addComponent(jLabel12)
                            .addComponent(jLabel11))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(radioGet)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioPost)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(httpRequestBtn))
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addGap(49, 49, 49)
                                        .addComponent(analysisResult)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(postXML)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(contentType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE))))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(encodeField, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel13)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(key))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(radioMD5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioDES3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioSHA)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(toJson)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(toLower)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(jLabel15)
                    .addComponent(historyName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(saveHistory)
                    .addComponent(charset, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(keepSession)
                    .addComponent(tokenFieldName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(historyFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(url, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(10, 10, 10)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel7))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel8))
                        .addGap(7, 7, 7)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(radioPost)
                            .addComponent(jLabel10)
                            .addComponent(radioGet)
                            .addComponent(analysisResult)
                            .addComponent(postXML)
                            .addComponent(contentType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(radioSHA)
                            .addComponent(radioMD5)
                            .addComponent(radioDES3)
                            .addComponent(jLabel11)
                            .addComponent(toJson)
                            .addComponent(toLower))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(encodeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel12)
                            .addComponent(jLabel13)
                            .addComponent(key, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(httpRequestBtn)
                .addContainerGap())
        );

        tab.addTab("接口调试", jPanel3);

        jPanel5.setMinimumSize(new java.awt.Dimension(900, 600));
        jPanel5.setName(""); // NOI18N
        jPanel5.setPreferredSize(new java.awt.Dimension(900, 600));
        jPanel5.setLayout(new java.awt.GridLayout(1, 1));

        generateSQLMap.setText("生成");
        generateSQLMap.setToolTipText("未选择表时生成当前数据库里的所有表的Model");
        generateSQLMap.setActionCommand("generateSQLMap");

        jLabel18.setText("地址：");

        dbAddress.setText("127.0.0.1");

        jLabel20.setText("端口：");

        dbPort.setText("3306");

        jLabel21.setText("用户名：");

        dbUserName.setText("root");

        jLabel22.setText("密码：");

        dbList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane8.setViewportView(dbList);

        tablesList.setAutoCreateRowSorter(true);
        tablesList.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "表名", "Model名", "建表日期"
            }
        ));
        jScrollPane9.setViewportView(tablesList);

        readDatabase.setText("读取");
        readDatabase.setActionCommand("initDatabaseList");

        jLabel23.setText("模型");

        jLabel24.setText("DAO");

        jLabel25.setText("SQLMap");

        jLabel26.setText("输出");

        jButton4.setText("取消表选择");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        isOverwrite.setSelected(true);
        isOverwrite.setText("覆盖原文件");

        fileChooser.setText("浏览");
        fileChooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileChooserActionPerformed(evt);
            }
        });

        filter.setToolTipText("过滤需要显示的表名称");
        filter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                filterKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel26)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(outPath, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(fileChooser)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(filter, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton4)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jScrollPane9)))
                    .addComponent(databaseInfoBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel18)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dbAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel20)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dbPort, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel21)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dbUserName, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel22)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dbPassword, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(readDatabase)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(generateSQLMap)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(isOverwrite))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel23)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(modelPackage, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jLabel24)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(daoPackage, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel25)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(sqlMapPackage, javax.swing.GroupLayout.PREFERRED_SIZE, 191, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 226, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(dbAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel20)
                    .addComponent(dbPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21)
                    .addComponent(dbUserName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel22)
                    .addComponent(dbPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(generateSQLMap)
                    .addComponent(readDatabase)
                    .addComponent(isOverwrite))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(modelPackage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel24)
                    .addComponent(daoPackage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel25)
                    .addComponent(sqlMapPackage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jButton4)
                        .addComponent(filter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel26)
                        .addComponent(outPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(fileChooser)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane8)
                    .addComponent(jScrollPane9, javax.swing.GroupLayout.DEFAULT_SIZE, 430, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(databaseInfoBar, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("编辑配置", jPanel4);

        ibatisInfo.setEditable(false);
        ibatisInfo.setColumns(20);
        ibatisInfo.setLineWrap(true);
        ibatisInfo.setRows(5);
        jScrollPane7.setViewportView(ibatisInfo);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 922, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane7, javax.swing.GroupLayout.DEFAULT_SIZE, 573, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("输出信息", jPanel6);

        jPanel5.add(jTabbedPane1);

        tab.addTab("生成ibatis", jPanel5);

        exchangeBtn.setText("转换");
        exchangeBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                exchangeBtnMouseClicked(evt);
            }
        });

        textInput.setColumns(20);
        textInput.setRows(5);
        textInput.setText("元素名称 长度 必填 样例 说明\nServerName 16 是 orderStatus 固定值orderStatus\nCustID 16 是 1300000465 工厂编号（在二级节点的商品明细中存在多个不同工厂编码时，需要不其中一个保持一致）\nSign 50 是 见Post发送样例 数字签名(生成方式请参见Post发送样例)\nContent 1280 是 见XML示例 内容为XML格式的单据状态信息");
        jScrollPane13.setViewportView(textInput);

        textOutput.setColumns(20);
        textOutput.setRows(5);
        jScrollPane14.setViewportView(textOutput);

        fieldSplit.setText(" ");

        jLabel16.setText("字段分隔符");

        jLabel28.setText("生成类型");

        javaType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "实体", "枚举" }));

        jLabel29.setText("枚举前缀");

        prefix.setText("CODE");

        jsonField.setText("@JSONField");

        xmlEnum.setText("@XmlEnumValue");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane14)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addComponent(jLabel28)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(javaType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jsonField)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(xmlEnum)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel29)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(prefix, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel16)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fieldSplit, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(exchangeBtn)
                .addContainerGap(307, Short.MAX_VALUE))
            .addComponent(jScrollPane13)
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(exchangeBtn)
                    .addComponent(fieldSplit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16)
                    .addComponent(jLabel28)
                    .addComponent(javaType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel29)
                    .addComponent(prefix, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jsonField)
                    .addComponent(xmlEnum))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane13, javax.swing.GroupLayout.PREFERRED_SIZE, 293, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane14, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        tab.addTab("Text2Java", jPanel8);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 932, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(tab, javax.swing.GroupLayout.DEFAULT_SIZE, 932, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 631, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(tab, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void choosePathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_choosePathActionPerformed
        String out = path.getText();
        if (out != null && !out.isEmpty()) {
            jfc.setCurrentDirectory(new File(out.split(";")[0]));
        }
        jfc.setMultiSelectionEnabled(true);
        int opt = jfc.showOpenDialog(this);
        if (JFileChooser.APPROVE_OPTION == opt) {
            File[] files = jfc.getSelectedFiles();
            String paths = "";
            for (File file : files) {
                paths += file.getAbsolutePath() + ";";
            }
            path.setText(paths.substring(0, paths.length() - 1));
        }
    }//GEN-LAST:event_choosePathActionPerformed

    private void endDateMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_endDateMouseClicked
        Point p = endDate.getLocationOnScreen();
        p.y += endDate.getHeight();
        enDatePicker.setLocation(p);
        enDatePicker.setVisible(true);
    }//GEN-LAST:event_endDateMouseClicked

    private void beginDateMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_beginDateMouseClicked
        Point p = beginDate.getLocationOnScreen();
        p.y += beginDate.getHeight();
        beginDatePicker.setLocation(p);
        beginDatePicker.setVisible(true);
    }//GEN-LAST:event_beginDateMouseClicked

    private void saveHistoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveHistoryActionPerformed
        String name = historyName.getText();
        if (name == null || name.isEmpty()) {
            JOptionPane.showMessageDialog(rootPane, "记录名称不能为空");
            return;
        }
        try {
            HttpConfig hc = new HttpConfig(name, url.getText(), charset.getSelectedItem().toString(), header.getText(), data.getText(), radioPost.isSelected() ? "post" : "get", contentType.getSelectedItem().toString());
            hc.setSendXML(postXML.isSelected());
            hc.setEncodeKey(key.getText());
            hc.setEncodeType(radioDES3.isSelected() ? "des3" : radioMD5.isSelected() ? "md5" : "sha");
            hc.setEncodeFieldName(encodeField.getText());
            hc.setLowercaseEncode(toLower.isSelected());
            hc.setPackHead(toJson.isSelected());
            boolean isNew = FileUtil.saveHttpConfig(hc);
            JOptionPane.showMessageDialog(rootPane, "保存成功");
            if (isNew) {
                DefaultListModel lm = (DefaultListModel) historyList.getModel();
                lm.addElement(name);
                historyList.setSelectedIndex(lm.getSize() - 1);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(rootPane, "保存失败：" + ex.getLocalizedMessage());
        }
    }//GEN-LAST:event_saveHistoryActionPerformed

    private void chooseOutputPathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooseOutputPathActionPerformed
        String out = outputPath.getText();
        if (out != null && !out.isEmpty()) {
            jfc.setCurrentDirectory(new File(out));
        }
        jfc.setMultiSelectionEnabled(false);
        int opt = jfc.showOpenDialog(this);
        if (JFileChooser.APPROVE_OPTION == opt) {
            outputPath.setText(jfc.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_chooseOutputPathActionPerformed

    /**
     * 生成SQLMap文件
     */
    public void generateSQLMap() {
        int count = tablesList.getRowCount();
        if (count == 0) {
            JOptionPane.showMessageDialog(rootPane, "未选择任何需要生成的表");
            return;
        }
        FileInputStream in = null;
        try {
            SAXReader reader = new SAXReader();
            in = new FileInputStream(new File(AbatorRunner.ABATOR_CONFIG_FILE_PATH));
            Reader read = new InputStreamReader(in, "utf8");
            Document doc = reader.read(read);
            Element jdbcNode = (Element) doc.selectSingleNode("/abatorConfiguration/abatorContext/jdbcConnection");
            jdbcNode.setAttributeValue("connectionURL", "jdbc:mysql://" + dbAddress.getText() + ":" + dbPort.getText() + "/" + lastDatabase);
            jdbcNode.setAttributeValue("userId", dbUserName.getText());
            jdbcNode.setAttributeValue("password", new String(dbPassword.getPassword()));
            Element jarNode = (Element) doc.selectSingleNode("/abatorConfiguration/abatorContext/jdbcConnection/classPathEntry");
            jarNode.setAttributeValue("location", "lib/mysql-connector-java-5.1.26.jar");
            String out = outPath.getText();
            out = out == null ? "out" : out;
            Element modelNode = (Element) doc.selectSingleNode("/abatorConfiguration/abatorContext/javaModelGenerator");
            modelNode.setAttributeValue("targetPackage", modelPackage.getText());
            modelNode.setAttributeValue("targetProject", out);
            Element daoNode = (Element) doc.selectSingleNode("/abatorConfiguration/abatorContext/daoGenerator");
            daoNode.setAttributeValue("targetPackage", daoPackage.getText());
            daoNode.setAttributeValue("targetProject", out);
            Element mapNode = (Element) doc.selectSingleNode("/abatorConfiguration/abatorContext/sqlMapGenerator");
            mapNode.setAttributeValue("targetPackage", sqlMapPackage.getText());
            mapNode.setAttributeValue("targetProject", out);
            Element contextNode = (Element) doc.selectSingleNode("/abatorConfiguration/abatorContext");
            List nodes = contextNode.selectNodes("/abatorConfiguration/abatorContext/table");
            for (Object node : nodes) {
                contextNode.remove((Element) node);
            }
            int[] rows = tablesList.getSelectedRows();
            StringBuilder sqlMap = new StringBuilder();
            StringBuilder beans = new StringBuilder();

            if (rows != null && rows.length > 0) {
                for (int row : rows) {
                    String db = (String) tablesList.getValueAt(row, 0);
                    String mod = (String) tablesList.getValueAt(row, 1);
                    Element table = contextNode.addElement("table");
                    table.setAttributeValue("tableName", db);
                    table.setAttributeValue("domainObjectName", mod);
                    generateBeans(sqlMap, beans, db, mod);
                }
            } else {
                for (int i = 0; i < tablesList.getModel().getRowCount(); i++) {
                    String key1 = (String) tablesList.getValueAt(i, 0);
                    String value = (String) tablesList.getValueAt(i, 1);
                    Element table = contextNode.addElement("table");
                    table.setAttributeValue("tableName", key1);
                    table.setAttributeValue("domainObjectName", value);
                    generateBeans(sqlMap, beans, key1, value);
                }
            }
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("utf8");
            XMLWriter writer = new XMLWriter(new FileOutputStream(new File(AbatorRunner.ABATOR_CONFIG_FILE_PATH)), format);
            writer.write(doc);
            writer.close();
            File beansFile = new File(outPath.getText() + "/sqlMapConfigAndIbatisBeans.xml");
            FileWriter writer1 = new FileWriter(beansFile);
            writer1.write(sqlMap.toString());
            writer1.write(System.getProperty("line.separator"));
            writer1.write(beans.toString());
            writer1.flush();
            writer1.close();
            AbatorRunner.run(this);
            jTabbedPane1.setSelectedIndex(1);
        } catch (Exception ex) {
            showIbatisInfo(ex.getLocalizedMessage());
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void generateBeans(StringBuilder sqlMap, StringBuilder beans, String db, String mod) {
        String lineSeparator = System.getProperty("line.separator");
        String sqlMapPath = sqlMapPackage.getText().replace(".", "/");
        sqlMap.append("<sqlMap resource=\"").append(sqlMapPath).append("/").append(db).append("_SqlMap.xml\" />").append(lineSeparator);
        String className = mod;
        mod = mod.startsWith("T") ? mod.substring(1, mod.length()) : mod;
        mod = mod.replaceFirst(new String(new char[]{mod.charAt(0)}), new String(new char[]{Character.toLowerCase(mod.charAt(0))}));
        beans.append("<bean id=\"").append(mod).append("DAO\" class=\"").append(daoPackage.getText()).append(".").append(className).append("DAOImpl\">").append(lineSeparator);
        beans.append("\t<description></description>").append(lineSeparator);
        beans.append("\t<property name=\"sqlMapClient\" ref=\"sqlMapClient\" />").append(lineSeparator).append("</bean>").append(lineSeparator);
    }

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        tablesList.getSelectionModel().clearSelection();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void fileChooserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileChooserActionPerformed
        String out = outPath.getText();
        if (out != null && !out.isEmpty()) {
            jfc.setCurrentDirectory(new File(out));
        }
        jfc.setMultiSelectionEnabled(false);
        int opt = jfc.showOpenDialog(this);
        if (JFileChooser.APPROVE_OPTION == opt) {
            outPath.setText(jfc.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_fileChooserActionPerformed

    private DefaultTableModel oldModel = null;
    private void filterKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_filterKeyReleased
        DefaultTableModel model = (DefaultTableModel) tablesList.getModel();
        if (oldModel == null) {
            oldModel = model;
        }
        model = new DefaultTableModel(new String[][]{}, new String[]{"表名", "Model名", "建表日期"});
        String words = filter.getText();
        if (words == null || words.trim().isEmpty()) {
            tablesList.setModel(oldModel);
            return;
        }
        for (int i = 0; i < oldModel.getRowCount(); i++) {
            Object obj = tablesList.getValueAt(i, 0);
            if (obj.toString().contains(words)) {
                model.addRow(new String[]{obj.toString(), tablesList.getValueAt(i, 1).toString(), tablesList.getValueAt(i, 2).toString()});
            }
        }
        tablesList.setModel(model);
    }//GEN-LAST:event_filterKeyReleased

    private void historyListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_historyListMouseClicked
        if (MouseEvent.BUTTON3 == evt.getButton()) {
            int index = historyList.getSelectedIndex();
            if (index < 0) {
                return;
            }
            int opr = JOptionPane.showConfirmDialog(rootPane, "确认要删除当前历史记录吗？");
            if (opr == JOptionPane.OK_OPTION) {
                try {
                    FileUtil.removeHttpConfig((String) historyList.getSelectedValue());
                    DefaultListModel dlm = (DefaultListModel) historyList.getModel();
                    dlm.removeElementAt(index);
                } catch (Exception ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }//GEN-LAST:event_historyListMouseClicked

    private void historyFilterKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_historyFilterKeyReleased
        initHistoryItem();
    }//GEN-LAST:event_historyFilterKeyReleased

    private void exchangeBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exchangeBtnMouseClicked
        String text = textInput.getText();
        if (text == null || text.trim().isEmpty()) {
            textOutput.setText("未输入任何文本");
        } else {
            String type = javaType.getSelectedItem().toString();
            Boolean json = jsonField.isSelected();
            Boolean xml = xmlEnum.isSelected();
            String pre = prefix.getText();
            pre = pre == null || pre.trim().isEmpty() ? "CODE" : pre;
            try {
                String spe = fieldSplit.getText();
                StringBuilder sb = new StringBuilder();
                String[] input = text.split("\\n");
                String common = "/**" + input[0].replaceAll(spe, ":%s ") + ":%s */\n";
                for (int i = 1; i < input.length; i++) {
                    String tmp = input[i];
                    if (tmp != null && !tmp.trim().isEmpty()) {
                        String[] arr = tmp.split(spe);
                        sb.append(String.format(common, arr));
                        if ("实体".equals(type)) {
                            sb.append("private String ").append(arr[0]).append(";\n");
                        } else {
                            if (xml) {
                                sb.append("@XmlEnumValue(\"").append(arr[0]).append("\")\n");
                            }
                            sb.append(pre.toUpperCase()).append("_").append(arr[0].toUpperCase()).append("(");
                            for (int k = 0; k < arr.length; k++) {
                                String t = arr[k];
                                sb.append("\"").append(t).append("\"");
                                if (k != arr.length - 1) {
                                    sb.append(",");
                                }
                            }
                            sb.append(")");
                            if (i != input.length - 1) {
                                sb.append(",\n");
                            } else {
                                sb.append(";\n");
                            }
                        }
                    }
                }
                if ("实体".equals(type)) {
                    common = "/**" + input[0].replaceAll(spe, ":%s ") + ":%s *@return */\n";
                    String pcom = "/**" + input[0].replaceAll(spe, ":%s ") + ":%s *@param %s*/\n";
                    for (int i = 1; i < input.length; i++) {
                        String tmp = input[i];
                        if (tmp != null && !tmp.trim().isEmpty()) {
                            String[] arr = tmp.split(spe);
                            sb.append(String.format(common, arr));
                            if (json) {
                                sb.append("@JSONField(name = \"").append(arr[0]).append("\")\n");
                            }
                            sb.append("public String get").append(arr[0]).append("(){\nreturn ").append(arr[0]).append(";\n}\n");
                            String[] param = new String[arr.length + 1];
                            System.arraycopy(arr, 0, param, 0, arr.length);
                            param[arr.length] = arr[0];
                            sb.append(String.format(pcom, param));
                            sb.append("public void set").append(arr[0]).append("(String ").append(arr[0]).append("){\n this.").append(arr[0]).append("=").append(arr[0]).append(";\n}\n");
                        }
                    }
                } else {
                    sb.append("private final String code;\nprivate final String text;\npublic String getCode(){\nreturn code;\n}\npublic String getText(){\nreturn text;\n}\n");
                }
                textOutput.setText(sb.toString());
            } catch (Exception e) {
                textOutput.setText("数据异常:" + e.getClass().getName() + "_" + e.getLocalizedMessage());
            }

        }
    }//GEN-LAST:event_exchangeBtnMouseClicked

    public void doHttpRequest() {
        Long[] uids = new Long[]{};
        String tmp = data.getText();
        if (uids.length == 0) {
            doHttpRequest(data.getText());
        } else {//批量调用某个接口，userId为可变参数
            for (Long uid : uids) {
                doHttpRequest(tmp + "&userId=" + uid.toString());
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * 发http请求
     *
     * @param pp
     */
    public void doHttpRequest(String pp) {
        try {
            HttpClient client = new HttpClient();
            String urlstr = url.getText();
            HttpMethod method = null;
            boolean fileFlag = false;
            ArrayList<Part> parts = new ArrayList<Part>();
            String ct = this.contentType.getSelectedItem().toString();
            if (urlstr != null && !urlstr.isEmpty()) {
                String datastr = pp == null ? data.getText() : pp;
                System.out.println("请求参数：" + datastr);
                if (radioGet.isSelected()) {
                    method = new GetMethod(urlstr);
                    if (datastr != null && !datastr.isEmpty()) {
                        method.setQueryString(datastr);
                        respBody.append("queryString:" + datastr + "\r\n");
                    }
                } else {
                    method = new PostMethod(urlstr);
                    if (datastr != null && !datastr.isEmpty()) {
                        if (postXML.isSelected()) {
                            RequestEntity ent = new StringRequestEntity(datastr, "default".equals(ct) ? "text/xml" : ct, charset.getSelectedItem().toString());
                            ((PostMethod) method).setRequestEntity(ent);
                        } else {
                            String[] strs = datastr.split("&");
                            for (String ss : strs) {
                                ss = ss.replace("==", "####");
                                String[] tmp = ss.split("=");
                                if (tmp.length == 2) {
                                    tmp[1] = tmp[1].replace("####", "==");
                                    PostMethod post = (PostMethod) method;
                                    post.addParameter(tmp[0], tmp[1]);
                                    respBody.append(tmp[0] + "=" + tmp[1] + "\r\n");
                                    if (tmp[0].startsWith("FILE_")) {
                                        fileFlag = true;
                                        parts.add(new FilePart(tmp[0].replace("FILE_", ""), new File(tmp[1])));
                                    } else {
                                        parts.add(new StringPart(tmp[0], URLEncoder.encode(tmp[1], (String) charset.getSelectedItem())));
                                    }
                                } else {
                                    respBody.append("忽略空参数：" + tmp[0] + "\r\n");
                                }
                            }
                        }
                    }
                }
            } else {
                Thread.sleep(100);
                throw new Exception("请求地址不能为空");
            }
            if (fileFlag) {
                PostMethod post = (PostMethod) method;
                Part[] ps = new Part[parts.size()];
                for (int i = 0; i < parts.size(); i++) {
                    ps[i] = parts.get(i);
                }
                MultipartRequestEntity fileEntity = new MultipartRequestEntity(ps, post.getParams());
                post.setRequestEntity(fileEntity);
            }
            String headerstr = header.getText();
            if (headerstr != null && !headerstr.isEmpty()) {
                Random random = new Random();
                String nonce = random.nextInt(999999) + "";
                headerstr = headerstr.replaceAll("random", nonce);
                sdf.applyPattern("yyyyMMddHHmmss");
                String timestamp = sdf.format(new Date());
                respBody.append("nonce=" + nonce + "\r\ntimestamp=" + timestamp + "\r\n");
                headerstr = headerstr.replaceAll("now", timestamp);
                String[] hs = headerstr.split("&");
                HashMap<String, String> token = new HashMap<String, String>();
                for (String s : hs) {
                    String[] tmp = s.split("=");
                    if (tmp.length >= 2) {
                        if (tmp.length > 2) {
                            for (int i = 2; i < tmp.length; i++) {
                                tmp[1] += "=" + tmp[i];
                            }
                        }
                        method.addRequestHeader(tmp[0], getEncodeString(tmp[1], tmp[0]));
                        if (toJson.isSelected()) {
                            token.put(tmp[0], getEncodeString(tmp[1], tmp[0]));
                        }
                    }
                    respBody.append("header:" + method.getRequestHeader(tmp[0]));
                }
                if (toJson.isSelected()) {

                    method.addRequestHeader("token", gson.toJson(token));
                    respBody.append("header:" + method.getRequestHeader("token"));
                }
            }
            method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, (String) charset.getSelectedItem());
            method.getParams().setParameter(HttpMethodParams.HTTP_ELEMENT_CHARSET, (String) charset.getSelectedItem());
            method.getParams().setParameter(HttpMethodParams.HTTP_URI_CHARSET, (String) charset.getSelectedItem());
            client.executeMethod(method);
            String result = method.getResponseBodyAsString();
            respBody.append("请求状态:" + method.getStatusCode() + "\r\n");
            if (method.getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY) {
                respBody.append("跳转地址：" + method.getResponseHeader("Location").getValue() + "\r\n");
            }
            JsonObject json = null;
            if (keepSession.isSelected()) {
                json = gson.fromJson(result.substring(1, result.length() - 1), JsonObject.class);
                if (json.has("result") && json.get("result") instanceof JsonObject) {
                    sessionToken = json.getAsJsonObject("result").get("token").getAsString();
                    keepSession.setSelected(false);
                }
            }
            respBody.append(FormatUtil.formatJsonOutput(result) + "\r\n----------------------------------------------------\r\n");
            if (analysisResult.isSelected()) {
                String tmp = result.startsWith("\"") ? result.substring(1, result.length() - 1) : result;
                json = gson.fromJson(tmp, JsonObject.class);
                StringBuilder sb = new StringBuilder();
                String[] adds = urlstr.split("/");
                int idx = urlstr.indexOf(adds[3]);
                sb.append("**简要描述：**\n\n- ").append(historyName.getText()).append("\n\n**请求URL：**\n\n- ` ").append(urlstr.substring(idx));
                sb.append(" `\n\n**请求方式：**\n\n- ").append(radioPost.isSelected() ? "POST" : "GET").append(" \n\n**参数：** \n\n|参数名|必选|类型|说明|\n|:----|:---|:-----|-----|\n");
                String param = data.getText();
                if (param != null && !param.trim().isEmpty()) {
                    if (ct.contains("json")) {
                        JsonObject jo = gson.fromJson(param, JsonObject.class);
                        for (Map.Entry<String, JsonElement> entry : jo.entrySet()) {
                            removeArrayEle(entry.getValue());
                        }
                        analysisJsonStr(sb, jo);
                    } else {
                        String[] pms = param.split("&");
                        for (String pm : pms) {
                            sb.append("|").append(pm.split("=")[0]).append("|  |String|  |\n");
                        }
                    }
                }
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    removeArrayEle(entry.getValue());
                }
                sb.append("**返回示例**\n\n```\n").append(FormatUtil.formatJsonOutput(json.toString())).append("\n```\n\n**返回参数说明** \n\n|参数名|类型|说明|备注|\n|:-----|:-----|-----| |\n");
                analysisJsonStr(sb, json);
                sb.append(" **备注** \n\n- 更多返回错误代码请看首页的错误代码描述");
                try (FileWriter writer = new FileWriter(historyName.getText() + ".txt")) {
                    writer.write(sb.toString());
                    writer.flush();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            respBody.append("异常" + ex.getLocalizedMessage());
        }
    }

    private void analysisJsonStr(StringBuilder sb, Object json) {
        LinkedHashMap<Object, Object> map = new LinkedHashMap<>();
        Object jo = json;
        if (jo instanceof JsonObject) {
            FileUtil.analysisJson(map, (JsonObject) jo, "");
        } else if (jo instanceof JsonArray) {
            JsonArray ja = (JsonArray) jo;
            Object subjo = ja.get(0);
            if (subjo instanceof JsonObject) {
                FileUtil.analysisJson(map, (JsonObject) subjo, "");
            }
        } else {
            sb.append("**result有误").append(jo);
        }
        analysisMap(map, sb, "");
    }

    /**
     * 清除集合中其他元素。只保留第一个
     *
     * @param val
     */
    private void removeArrayEle(Object val) {
        if (val instanceof JsonArray) {
            JsonArray arr = (JsonArray) val;
            int len = arr.size();
            for (int i = 0; i < len; i++) {
                if (i > 0) {
                    arr.remove(1);
                } else {
                    Object v = arr.get(i);
                    removeArrayEle(v);
                }
            }
        } else if (val instanceof JsonObject) {
            JsonObject jo = (JsonObject) val;
            jo.entrySet().forEach((next) -> {
                removeArrayEle(next.getValue());
            });
        }
    }

    /**
     * 解析TreeMap生成表格
     *
     * @param map
     * @param sheet
     */
    private static void analysisMap(LinkedHashMap<Object, Object> map, StringBuilder sb, String key1) {
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof LinkedHashMap) {
                sb.append("|").append(key).append("|").append(getValueType(value)).append("|  |  |\n");
            } else if (!key.equals(key1)) {
                sb.append("|").append(key).append("|").append(getValueType(value)).append("|  |  |\n");
            }
        }
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key.equals(key1)) {
                continue;
            }
            if (value instanceof LinkedHashMap) {
                sb.append("\n\n**").append(key).append("参数说明** \n\n|参数名|类型|说明|备注|\n|:-----|:-----|-----| |\n");
                analysisMap((LinkedHashMap) value, sb, key.toString());
            }
        }
    }

    private static String getValueType(Object value1) {
        if (value1 instanceof JsonPrimitive) {
            JsonPrimitive jp = (JsonPrimitive) value1;
            if (jp.isNumber() && jp.toString().contains(".")) {
                return "Double";
            }
            return jp.isBoolean() ? "Boolean" : jp.isNumber() ? "Integer" : jp.isString() ? "String" : "String";
        } else if (value1 instanceof JsonObject || value1 instanceof JsonArray || value1 instanceof LinkedHashMap) {
            return "Object[data]";
        }
        return "String";
    }

    /**
     * 打包
     */
    public void packing() {
        saveProperties();
        if (detailTable.getRowCount() == 0) {
            JOptionPane.showMessageDialog(rootPane, "未选择任何需要打包的文件。");
            return;
        }
        packageTabPanel.setSelectedIndex(1);
        final long time = System.currentTimeMillis();

        try {
            updateInfo("处理中，请稍等....");
            int[] sel = detailTable.getSelectedRows();
            int cot = detailTable.getModel().getRowCount();
            DefaultTableModel mod = (DefaultTableModel) detailTable.getModel();
            SVNFILEPATHS.clear();
            clearTemp();
            String[] paths = path.getText().split(";");
            if (sel.length > 0) {
                for (int i : sel) {
                    int index = detailTable.convertRowIndexToModel(i);
                    String val = mod.getValueAt(index, 0).toString();
                    if (!"Deleted".equals(mod.getValueAt(index, 1))) {
                        SVNFILEPATHS.add(new SvnFilePath(val, paths));
                    } else {
                        updateInfo("已忽略删除操作：" + val);
                    }
                }
            } else if (cot > 0) {
                for (int i = 0; i < cot; i++) {
                    int index = detailTable.convertRowIndexToModel(i);
                    String val = mod.getValueAt(index, 0).toString();
                    if (!"Deleted".equals(mod.getValueAt(index, 1))) {
                        SVNFILEPATHS.add(new SvnFilePath(val, paths));
                    } else {
                        updateInfo("已忽略删除操作：" + val);
                    }
                }
            }
            sdf.applyPattern("yyyyMMddHHmmss");
            File projectPath = new File(path.getText().split(";")[0]);
            String outpath = outputPath.getText();
            jarPath = (outpath == null || outpath.isEmpty()) ? jarPath : outpath;
            String jarfile = jarPath + File.separator + projectPath.getName() + "_" + sdf.format(new Date()) + ".zip";
            FileWriter writer;
            int count, innerClass;
            FileOutputStream fo = new FileOutputStream(jarfile);
            ZipOutputStream zo = new ZipOutputStream(fo);
            zo.setEncoding("utf8");
            byte[] buffer = new byte[1024];
            writer = null;
            if (createFileList.isSelected()) {
                writer = new FileWriter(jarfile.replace(".zip", ".txt"));
            }
            count = 0;
            innerClass = 0;
            for (SvnFilePath sfp : SVNFILEPATHS) {
                String str = sfp.getLocalFilePath();
                if (str == null) {
                    updateInfo("文件不存在或为目录：" + sfp.getPath());
                    continue;
                }
                File file = new File(sfp.getLocalFilePath());
                if (str.endsWith(".classpath") || str.endsWith(".project")) {
                    updateInfo("已忽略文件：" + str);
                    continue;
                }
                if (str.indexOf("temp") > 0) {
                    str = str.substring(str.indexOf("temp") + 5, str.length());
                }
                if (isWebProject.isSelected()) {
                    if (str.contains(".class") || str.contains(".properties")) {
                        str = "WEB-INF/classes/" + str;
                    }
                }
                if (xmlToInf.isSelected()) {
                    if (str.contains(".xml") && !str.contains("WEB-INF")) {
                        str = "WEB-INF/classes/" + str;
                    }
                }
                if (!str.contains(".class")) {
                    str = str.replace("src/main/resources/", "WEB-INF/classes/");
                    str = str.replace("src/main/webapp/", "");
                    str = str.replace("src/", "");
                    str = str.replace("WebRoot/", "");
                }
                if (rootNameProject.isSelected()) {
                    str = projectPath.getName() + "/" + str;
                }
                updateInfo("正在创建：" + str.replace("\\", "/") + (str.contains("$") ? "[内部类]" : ""));
                if (writer != null) {
                    writer.append(str.replace("\\", "/"));
                }
                FileInputStream fis = new FileInputStream(file);
                zo.putNextEntry(new ZipEntry(str));
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    zo.write(buffer, 0, len);
                }
                zo.closeEntry();
                fis.close();
                count++;
                if (str.contains("$")) {
                    innerClass++;
                }
                Thread.sleep(10);
            }
            zo.flush();
            zo.finish();
            fo.flush();
            fo.close();
            if (writer != null) {
                writer.flush();
                writer.close();
            }
            updateInfo("打包成功：" + jarfile.replace("\\", "/") + " 共计：" + count + "个文件,其中包含" + (count - innerClass) + "个文件" + innerClass + "个内部类，用时" + (System.currentTimeMillis() - time) + "毫秒");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, null, e);
            updateInfo("打包失败：" + e.getLocalizedMessage());
        } finally {
            try {
                SerializableUtil.serializable(FileUtil.FILE_TREE);
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }
    }

    private String getEncodeString(String str, String fieldName) {
        String encode = encodeField.getText();
        String headTokenFieldName = tokenFieldName.getText();
        if (fieldName.equals(headTokenFieldName) && !sessionToken.isEmpty()) {
            return sessionToken;
        }
        if (!fieldName.equals(encode)) {
            return str;
        }
        String result = encode;
        if (encode != null && !encode.isEmpty()) {
            if (radioSHA.isSelected()) {
                result = EncodeUtils.encodeBySHA(str);
            } else if (radioDES3.isSelected()) {
                result = EncodeUtils.encryptByDES3(str, key.getText());
            } else if (radioMD5.isSelected()) {
                result = EncodeUtils.encodeByMD5(str);
            }
        }
        if (!radioDES3.isSelected()) {
            if (toLower.isSelected()) {
                result = result.toLowerCase();
            } else {
                result = result.toUpperCase();
            }
        }
        return result;
    }

    public ActionListenerImpl getActionlistenerImpl() {
        return actionlistenerImpl;
    }

    public ListSelectionListenerImpl getListSelectionListenerImpl() {
        return listSelectionListenerImpl;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Create and display the form
         */
        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                SubstanceLookAndFeel.setSkin(new CremeCoffeeSkin());
                MainFrame frame = new MainFrame();
                frame.initActionPerform();
                frame.initListSelectionListener();
                frame.setVisible(true);
            }
        });
    }

    private void saveProperties() {
        PropertiesUtil.saveProperty(SVN_USER_NAME, svnName.getText());
        PropertiesUtil.saveProperty(OUTPUT_PATH, outputPath.getText());
        PropertiesUtil.saveProperty(SVN_PASSWORD, new String(svnPassword.getPassword()));
        PropertiesUtil.saveProperty(PROJECT_PATH, path.getText());
        PropertiesUtil.saveProperty(IS_ADD_PROJECT_NAME, rootNameProject.isSelected() + "");
        PropertiesUtil.saveProperty(IS_CREATE_FILE_LIST, createFileList.isSelected() + "");
        PropertiesUtil.saveProperty(IS_WEB_PROJECT, isWebProject.isSelected() + "");
        PropertiesUtil.saveProperty(IS_XML_TO_WEB_INF, xmlToInf.isSelected() + "");
    }

    class DirEntryHandler implements ISVNLogEntryHandler {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        private String projectName = "";

        public DirEntryHandler(String projectName) {
            this.projectName = projectName;
        }

        @Override
        public void handleLogEntry(SVNLogEntry logEntry) throws SVNException {
            String filter = logFilterName.getText().toLowerCase();
            String author = logEntry.getAuthor().toLowerCase();
            String msg = logEntry.getMessage().toLowerCase();
            if (filter != null && !filter.isEmpty() && !(author.contains(filter) || msg.contains(filter))) {
                return;
            }
            detailData.put(logEntry.getRevision(), logEntry.getChangedPaths());
            String str1 = projectName + "|" + logEntry.getRevision() + "|" + logEntry.getAuthor() + "|" + sdf.format(logEntry.getDate()) + "|" + logEntry.getMessage();
            logs.add(str1);
            ((DefaultTableModel) logTable.getModel()).setRowCount(0);
            ((DefaultTableModel) detailTable.getModel()).setRowCount(0);
            for (int i = logs.size() - 1; i >= 0; i--) {
                String str = logs.get(i);
                ((DefaultTableModel) logTable.getModel()).addRow(str.split("\\|"));
            }
            for (Map.Entry<String, SVNLogEntryPath> entry : logEntry.getChangedPaths().entrySet()) {
                SVNLogEntryPath path = entry.getValue();
                ((DefaultTableModel) detailTable.getModel()).addRow(new Object[]{path.getPath(), getType(path.getType()), path.getCopyPath(), getCopyRevision(path.getCopyRevision())});
            }
        }

    }

    private String getCopyRevision(Long version) {
        return version < 0 ? "" : version.toString();
    }

    private String getType(char type) {
        switch (type) {
            case 'A':
                return "Added";
            case 'M':
                return "Modified";
            case 'D':
                return "Deleted";
            case 'R':
                return "Replaced";
            default:
                return "";
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox analysisResult;
    private javax.swing.JTextField beginDate;
    private javax.swing.JComboBox charset;
    private javax.swing.JButton chooseOutputPath;
    private javax.swing.JButton choosePath;
    private javax.swing.JComboBox<String> contentType;
    private javax.swing.JCheckBox createFileList;
    private javax.swing.JTextField daoPackage;
    private javax.swing.JTextArea data;
    private javax.swing.JLabel databaseInfoBar;
    private javax.swing.JTextField dbAddress;
    private javax.swing.JList dbList;
    private javax.swing.JPasswordField dbPassword;
    private javax.swing.JTextField dbPort;
    private javax.swing.JTextField dbUserName;
    private javax.swing.JTable detailTable;
    private javax.swing.JTextField encodeField;
    private javax.swing.JTextField endDate;
    private javax.swing.JButton exchangeBtn;
    private javax.swing.JTextField fieldSplit;
    private javax.swing.JButton fileChooser;
    private javax.swing.JTextField filter;
    private javax.swing.JButton generateSQLMap;
    private javax.swing.JTextArea header;
    private javax.swing.JTextField historyFilter;
    private javax.swing.JList<String> historyList;
    private javax.swing.JTextField historyName;
    private javax.swing.JButton httpRequestBtn;
    private javax.swing.JTextArea ibatisInfo;
    private static javax.swing.JTextArea info;
    public static javax.swing.JCheckBox isOverwrite;
    private javax.swing.JCheckBox isWebProject;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JComboBox<String> javaType;
    private javax.swing.JCheckBox jsonField;
    private javax.swing.JCheckBox keepSession;
    private javax.swing.JTextField key;
    private javax.swing.JTextField logFilterName;
    private javax.swing.JTable logTable;
    private javax.swing.JTextField modelPackage;
    private javax.swing.JTextField outPath;
    private javax.swing.JTextField outputPath;
    private javax.swing.JButton packBtn;
    private javax.swing.JTabbedPane packageTabPanel;
    private javax.swing.JTextField path;
    private javax.swing.JCheckBox postXML;
    private javax.swing.JTextField prefix;
    private javax.swing.JRadioButton radioDES3;
    private javax.swing.JRadioButton radioGet;
    private javax.swing.JRadioButton radioMD5;
    private javax.swing.JRadioButton radioPost;
    private javax.swing.JRadioButton radioSHA;
    private javax.swing.JButton readDatabase;
    private javax.swing.JTextArea respBody;
    private javax.swing.JCheckBox rootNameProject;
    private javax.swing.JButton saveHistory;
    private javax.swing.JButton showLogBtn;
    private javax.swing.JTextField sqlMapPackage;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JTextField svnName;
    private javax.swing.JPasswordField svnPassword;
    private javax.swing.JTabbedPane tab;
    private javax.swing.JTable tablesList;
    private javax.swing.JTextArea textInput;
    private javax.swing.JTextArea textOutput;
    private javax.swing.JCheckBox toJson;
    private javax.swing.JCheckBox toLower;
    private javax.swing.JTextField tokenFieldName;
    private javax.swing.JTextField url;
    private javax.swing.JCheckBox xmlEnum;
    private javax.swing.JCheckBox xmlToInf;
    // End of variables declaration//GEN-END:variables
    private final String HISTORY_NAME = "historyName";
    private final String HISTORY_URL = "historyUrl";
    private final String HISTORY_HEADER = "historyHeader";
    private final String HISTORY_DATA = "historyData";
    private final String HISTORY_METHOD = "historyMethod";
    private final String HISTORY_ENCODE = "historyEncode";
    private final String HISTORY_PACKAGE = "historyPackage";
    private final String HISTORY_FIELD = "historyField";
    private final String HISTORY_KEY = "historyKey";
    private final String HISTORY_TO_LOWER_CASE = "historyToLowerCase";
    private final String HISTORY_ITEM_INDEX = "itemIndex";
    private final String SVN_USER_NAME = "userName";
    private final String SVN_PASSWORD = "password";
    private final String PROJECT_PATH = "projectPath";
    private final String IS_WEB_PROJECT = "isWebProject";
    private final String IS_ADD_PROJECT_NAME = "isAddProjectName";
    private final String IS_CREATE_FILE_LIST = "isCreateFileList";
    private final String IS_XML_TO_WEB_INF = "isXMLToWebInf";
    private final String IS_POST_XML = "isPostXML";
    private final String OUTPUT_PATH = "outputPath";
    private static String sessionToken = "";
    private static Gson gson = new GsonBuilder().create();
}
