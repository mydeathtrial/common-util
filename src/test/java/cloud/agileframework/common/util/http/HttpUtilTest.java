package cloud.agileframework.common.util.http;

import com.google.common.collect.Maps;
import junit.framework.TestCase;

import java.util.HashMap;

public class HttpUtilTest extends TestCase {

    public void testGet() {
    }

    public void testPost() {
        HashMap<Object, Object> map = Maps.newHashMap();
        map.put("a", "1");
        map.put("b", "2");
        HttpUtil.post("www.baidu.com", map);
    }
}