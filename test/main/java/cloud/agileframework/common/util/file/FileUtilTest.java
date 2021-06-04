package cloud.agileframework.common.util.file;

import com.google.common.collect.Lists;
import junit.framework.TestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class FileUtilTest extends TestCase {

    public void testCreateZip() throws IOException {
        FileUtil.createZip(Lists.newArrayList(new File("D:\\a.xlsx"),new File("D:\\b.xlsx")),"D:\\","asd.zip");

    }
}