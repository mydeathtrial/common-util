package cloud.agileframework.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 佟盟
 * 日期 2021-05-17 18:37
 * 描述 日志属性，用于记录操作日志组件中，获取属性的中文含义
 * @version 1.0
 * @since 1.0
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Remark {
    /**
     * 属性名
     */
    String value();

    /**
     * 是否忽略
     */
    boolean ignore() default false;
}
