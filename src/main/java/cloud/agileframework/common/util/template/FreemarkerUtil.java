package cloud.agileframework.common.util.template;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class FreemarkerUtil {
    private static final Configuration CFG = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
    static {
        CFG.setDefaultEncoding(StandardCharsets.UTF_8.name());
    }
    
    public static String parse(Template template, Object param) throws TemplateException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStreamWriter outWrite = new OutputStreamWriter(out);
        template.process(param, outWrite);
        return out.toString();
    }

    public static String parse(String templateString, Object param) throws TemplateException, IOException {
        return parse(new Template(templateString,templateString,CFG),param);
    }
}
