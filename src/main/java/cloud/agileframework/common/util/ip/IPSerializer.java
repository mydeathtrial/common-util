package cloud.agileframework.common.util.ip;

import cloud.agileframework.common.util.clazz.TypeReference;
import cloud.agileframework.common.util.object.ObjectUtil;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.IOException;
import java.lang.reflect.Type;

public class IPSerializer implements ObjectSerializer, ObjectDeserializer {

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        if (object == null) {
            return;
        }
        String ip;
        Long ipLong;
        if (object instanceof Number) {
            ipLong = ObjectUtil.to(((Number) object).longValue(), new TypeReference<>(fieldType));
        } else {
            if (NumberUtils.isCreatable(object.toString())) {
                ipLong = ObjectUtil.to(object, new TypeReference<>(Long.class));
            } else {
                serializer.write(object);
                return;
            }
        }
        ip = IPv4Util.longToIP(ipLong);
        serializer.write(ip);
    }

    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        Object value = parser.parse();
        if (value instanceof Number) {
            return ObjectUtil.to(value, new TypeReference<>(type));
        } else {
            if (NumberUtils.isCreatable(value.toString())) {
                return ObjectUtil.to(value, new TypeReference<>(type));
            }
            return ObjectUtil.to(IPv4Util.ipToLong(value.toString()), new TypeReference<>(type));
        }
    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }
}
