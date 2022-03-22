package cloud.agileframework.common.util.serializer;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;

public class BooleanToNumberStringSerializer implements ObjectSerializer {
    private static final Logger logger = LoggerFactory.getLogger(BooleanToNumberStringSerializer.class);

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        if (!(object instanceof Boolean)) {
            return;
        }
        serializer.write((Boolean) object ? "1" : "0");
    }
}
