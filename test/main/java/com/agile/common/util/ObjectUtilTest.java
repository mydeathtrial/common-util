package com.agile.common.util;

import com.agile.common.data.DemoA;
import com.agile.common.data.DemoC;
import com.agile.common.util.clazz.TypeReference;
import com.agile.common.util.date.DateUtil;
import com.agile.common.util.object.ObjectUtil;
import org.junit.Test;

import java.util.Map;

/**
 * @author 佟盟
 * 日期 2019/10/31 9:52
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class ObjectUtilTest {

    /**
     * 测试Map转对象
     */
    @Test
    public void mapToObject() {
        ObjectUtil.to(DemoA.testData(), new TypeReference<DemoA>() {
        });
    }

    /**
     * 测试对象转Map
     */
    @Test
    public void objectToMap() {
        DemoA demoA = ObjectUtil.to(DemoA.testData(), new TypeReference<DemoA>() {
        });
        ObjectUtil.to(demoA, new TypeReference<Map<String, Object>>() {
        });
    }

    /**
     * 测试对象转对象
     */
    @Test
    public void objectToObject() {
        DemoA demoA = ObjectUtil.to(DemoA.testData(), new TypeReference<DemoA>() {
        });
        ObjectUtil.to(demoA, new TypeReference<DemoC>() {
        });
    }

    /**
     * 测试Map转对象
     */
    @Test
    public void parseDate() {
        DateUtil.parse("19900905 11:12 下午");
    }

}
