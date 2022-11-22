package cloud.agileframework.common.util.file;

import com.google.common.collect.Lists;
import junit.framework.TestCase;

import java.io.File;

public class FileUtilTest extends TestCase {

    public void testCreateZip() {
        try {
            FileUtil.createZip("D:\\", "asd.zip",new File("D:\\a.xlsx"), new File("D:\\b.xlsx"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}