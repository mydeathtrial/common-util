package cloud.agileframework.common.util.template;


import cloud.agileframework.common.util.clazz.TypeReference;
import cloud.agileframework.common.util.object.ObjectUtil;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import java.io.StringWriter;
import java.util.Map;

public class VelocityUtil {

    public static String parse(String template, Object param) {
        StringWriter outWrite = new StringWriter();
        VelocityContext velocityContext = new VelocityContext(ObjectUtil.to(param, new TypeReference<Map<String, Object>>() {
        }));
        new VelocityEngine().evaluate(velocityContext, outWrite, "", template);
        return outWrite.toString();
    }
}
