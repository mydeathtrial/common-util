package cloud.agileframework.common.util.string;

import cloud.agileframework.common.util.object.DifferentField;
import cloud.agileframework.common.util.object.O1;
import cloud.agileframework.common.util.object.O2;
import cloud.agileframework.common.util.object.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

/**
 * @author 佟盟
 * 日期 2021-05-25 17:45
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class StringUtilTest {
    @Test
    public void test(){
            String tem = "{\n" +
                    "    \"name\": \"BeJson\",\n" +
                    "    \"url\": \"http://www.bejson.com\",\n" +
                    "    \"page\": 88,\n" +
                    "    \"isNonProfit\": true,\n" +
                    "    \"address\": {\n" +
                    "        \"street\": \"科技园路.\",\n" +
                    "        \"city\": \"江苏苏州\",\n" +
                    "        \"country\": \"中国\"\n" +
                    "    },\n" +
                    "    \"links\": [\n" +
                    "        {\n" +
                    "            \"name\": \"Google\",\n" +
                    "            \"url\": \"http://www.google.com\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"name\": \"Baidu\",\n" +
                    "            \"url\": \"http://www.baidu.com\"\n" +
                    "        },\n" +
                    "        {\n" +
                    "            \"name\": \"SoSo\",\n" +
                    "            \"url\": ${tudou$}\n" +
                    "        }\n" +
                    "    ]\n" +
                    "}";

        List<DifferentField> dif2 = ObjectUtil.getDifferenceProperties(
                new O1("tudou", 12, Lists.newArrayList("李磊", "张娜拉")),
                new O2(null, new String[]{"李磊", "张天爱"}, 12));

            HashMap<Object, Object> map = Maps.newHashMap();
//            map.put("tudou", Lists.newArrayList("1","2"));
            map.put("tudou", JSON.toJSON(dif2));
            System.out.println(StringUtil.parsingPlaceholder("${","$}",tem,map));
    }
}
