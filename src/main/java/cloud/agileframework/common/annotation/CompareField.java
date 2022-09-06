package cloud.agileframework.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 佟盟
 * 日期 2021-05-27 19:10
 * 描述 对象差异信息，引用类型属性差异比较
 * @version 1.0
 * @since 1.0
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface CompareField {
    /**
     * 忽略父的remark属性
     *
     * @return 默认false
     */
    boolean ignoreParentRemark() default false;

    /**
     * 忽略父的name属性
     *
     * @return 默认false
     */
    boolean ignoreParentName() default false;
}
