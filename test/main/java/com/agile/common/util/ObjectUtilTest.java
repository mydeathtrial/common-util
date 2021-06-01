package com.agile.common.util;

import com.agile.common.data.DemoA;
import com.agile.common.data.DemoC;
import com.agile.common.util.clazz.TypeReference;
import com.agile.common.util.date.DateUtil;
import com.agile.common.util.object.ObjectUtil;
import com.alibaba.fastjson.JSON;
import org.junit.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

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
    public void objectToMap1() {


        Map<String, Object> data = DemoA.testData();
        DemoA a = ObjectUtil.to(data, new TypeReference<DemoA>() {
        });
        long start = System.currentTimeMillis();
        IntStream.range(0,20).forEach(i->{
            ObjectUtil.to(data, new TypeReference<DemoA>() {
            });
        });

        System.out.println("总耗时："+(System.currentTimeMillis()-start));

//        Object jsona =   TypeUtils.cast(data,DemoA.class, ParserConfig.getGlobalInstance());
//        long start2 = System.currentTimeMillis();
//        IntStream.range(0,1000000).forEach(i->{
//            TypeUtils.cast(data,DemoA.class, ParserConfig.getGlobalInstance());
//        });
//
//        System.out.println("总耗时："+(System.currentTimeMillis()-start2));

        DemoA b = new DemoA();
        ObjectUtil.copyProperties(a,b);
        System.out.println();
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

    @Test
    public void parseDate2() {
        ObjectUtil.to(new ArrayList<String>(){{add("123");}}, new TypeReference<List>() {
        });
    }
}
