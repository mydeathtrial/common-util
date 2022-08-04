package cloud.agileframework.common.util.object;

import com.google.common.collect.Lists;
import org.junit.Test;

/**
 * @author 佟盟
 * 日期 2021-05-18 15:55
 * 描述 测试对象比较
 * @version 1.0
 * @since 1.0
 */
public class ObjectUtilTest {
    @Test
    public void compare() {
        final O1 source = new O1("tudou", 12, Lists.newArrayList("李磊", "张娜拉"));

        final O1 target = new O1("tudou", 11, Lists.newArrayList("李磊", "张天爱"));

        target.setO1(source);
        final O2 target1 = new O2(null, new String[]{"李磊", "张天爱"}, 12);

        System.out.println(ObjectUtil.getDifferencePropertiesDesc(source,
                target));

        System.out.println(ObjectUtil.getDifferencePropertiesDesc(source, target1));
    }


}
