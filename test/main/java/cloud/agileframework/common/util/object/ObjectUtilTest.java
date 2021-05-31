package cloud.agileframework.common.util.object;

import cloud.agileframework.common.util.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

/**
 * @author 佟盟
 * 日期 2021-05-18 15:55
 * 描述 测试对象比较
 * @version 1.0
 * @since 1.0
 */
public class ObjectUtilTest {
    @Test
    public void compare(){
        final O1 source = new O1("tudou", 12, Lists.newArrayList("李磊", "张娜拉"));

        final O1 target = new O1("tudou", 11, Lists.newArrayList("李磊", "张天爱"));

        target.setO1(source);
        final O2 target1 = new O2(null, new String[]{"李磊", "张天爱"}, 12);

        List<DifferentField> dif = ObjectUtil.getDifferenceProperties(source,
                target);
        System.out.println(JSON.toJSONString(dif));

        List<DifferentField> dif2 = ObjectUtil.getDifferenceProperties(source, target1);
        System.out.println(JSONUtil.toJSONString(dif2, SerializerFeature.PrettyFormat,SerializerFeature.WriteMapNullValue));
    }


}
