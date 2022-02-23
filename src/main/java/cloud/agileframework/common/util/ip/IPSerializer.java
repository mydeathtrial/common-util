package cloud.agileframework.common.util.ip;

import cloud.agileframework.common.util.clazz.TypeReference;
import cloud.agileframework.common.util.object.ObjectUtil;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;

public class IPSerializer implements ObjectSerializer, ObjectDeserializer {
    private static final Logger logger = LoggerFactory.getLogger(IPSerializer.class);

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        if (object == null) {
            return;
        }
        String ipLongString = String.valueOf(object);
        try {

            Long ipLong = NumberUtils.createLong(ipLongString);
            serializer.write(IPv4Util.longToIP(ipLong));
        } catch (Exception e) {
            logger.error(String.format("数据%s转换为ip时出现异常", ipLongString), e);
        }
    }

    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        String val = (String) parser.parse();
        return ObjectUtil.to(IPv4Util.ipToLong(val),new TypeReference<>(type));
    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }
}
