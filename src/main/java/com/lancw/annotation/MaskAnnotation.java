package com.lancw.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 项目名称：PackagePlugin
 * 类名称：MaskAnnotation
 * 类描述：
 * 创建人：lancw
 * 创建时间：2016-3-3 10:02:06
 * 修改人：lancw
 * 修改时间：2016-3-3 10:02:06
 * 修改备注：
 * <p>
 * @version 1.0
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MaskAnnotation {

    /**
     * 默认超时10秒
     * <p>
     * @return
     */
    int timeout() default 10000;

    /**
     * 默认需要遮罩
     * <p>
     * @return
     */
    boolean needMask() default true;
}
