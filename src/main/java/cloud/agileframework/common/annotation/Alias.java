package cloud.agileframework.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 佟盟
 * 日期 2020/4/24 18:25
 * 描述 对象深度转换器别名注解
 * @version 1.0
 * @since 1.0
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Alias {
    String[] value();
}
