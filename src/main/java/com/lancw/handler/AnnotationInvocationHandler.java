package com.lancw.handler;

import com.lancw.annotation.MaskAnnotation;
import com.lancw.plugin.MainFrame;
import com.lancw.plugin.ProgressDialog;
import java.awt.EventQueue;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * 项目名称：PackagePlugin
 * 类名称：AnnotationInvocationHandler
 * 类描述：
 * 创建人：lancw
 * 创建时间：2016-3-3 10:45:00
 * 修改人：lancw
 * 修改时间：2016-3-3 10:45:00
 * 修改备注：
 * <p>
 * @version 1.0
 */
public class AnnotationInvocationHandler implements InvocationHandler {

    private final Object proxyObj;
    private final JFrame mainFrame;
    private boolean maskFlag;
    private ProgressDialog dialog;
    private Timer timer;

    public AnnotationInvocationHandler(Object frame, Object proxyObject) {
        mainFrame = (JFrame) frame;
        this.proxyObj = proxyObject;
    }

    @Override
    public Object invoke(Object proxy, final Method method, Object[] args) throws Throwable {
        Annotation[] annotation = proxyObj.getClass().getMethod(method.getName(), method.getParameterTypes()).getDeclaredAnnotations();
        int timeout = 0;
        for (Annotation a : annotation) {
            if (a instanceof MaskAnnotation) {//解析代理对象注解
                MaskAnnotation n = (MaskAnnotation) a;
                maskFlag = n.needMask();
                timeout = n.timeout();
                break;
            }
        }
        new MyThread(maskFlag, method, args, MyThread.TYPE_SHOW).start();//打开遮罩
        Thread.sleep(100);//线程休眠100毫秒，以确保遮罩成功打开
        timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                MainFrame.LOGGER.info("超时自动关闭遮罩");
                dispose();
            }
        }, timeout);
        new MyThread(maskFlag, method, args, MyThread.TYPE_INVOKE).start();//执行业务流程
        return null;
    }

    /**
     * 关闭遮罩层
     */
    private void dispose() {
        JDialog.setDefaultLookAndFeelDecorated(true);
        if (dialog != null) {
            dialog.dispose();
        }
        if (timer != null) {
            timer.cancel();
        }
    }

    class MyThread extends Thread implements Runnable {

        boolean flag;
        Method method;
        Object[] args;
        String type;
        static final String TYPE_SHOW = "show";
        static final String TYPE_INVOKE = "invoke";

        public MyThread(boolean flag, Method method, Object[] args, String type) {
            this.flag = flag;
            this.method = method;
            this.args = args;
            this.type = type;
        }

        @Override
        public void run() {
            if (TYPE_SHOW.equals(type)) {
                if (flag) {
                    JDialog.setDefaultLookAndFeelDecorated(false);
                    EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            dialog = new ProgressDialog(mainFrame, true);
                            dialog.initTimer();
                            dialog.setVisible(true);
                        }
                    });
                }
            } else if (TYPE_INVOKE.equals(type)) {
                EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            method.invoke(proxyObj, args);
                            dispose();
                        } catch (IllegalAccessException ex) {
                            Logger.getLogger(AnnotationInvocationHandler.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IllegalArgumentException ex) {
                            Logger.getLogger(AnnotationInvocationHandler.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (InvocationTargetException ex) {
                            Logger.getLogger(AnnotationInvocationHandler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
            }
        }

    }
}
