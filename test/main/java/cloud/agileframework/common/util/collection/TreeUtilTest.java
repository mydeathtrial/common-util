package cloud.agileframework.common.util.collection;

import com.google.common.collect.Lists;
import junit.framework.TestCase;
import org.apache.commons.lang3.SerializationUtils;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TreeUtilTest extends TestCase {
    private static final ArrayList<TreeNode> list;

    static {
        list = Lists.newArrayList();

        char root = 65;
        while (root < 123) {
            int i = 100;
            String id;
            String parentId = null;
            while (i > 0) {
                if (i != 100) {
                    char finalRoot = root;
                    parentId = IntStream.range(0, 100 - i).mapToObj(s -> finalRoot + "").collect(Collectors.joining());
                    id = parentId + root;
                } else {
                    id = root + "";
                }

                list.add(new TreeNode(id, parentId, 0));
                i--;
            }
            if (root == 90) {
                root = 97;
                continue;
            }
            root = (char) (root + 1);
        }

    }

    public void testCreateTree() {

        long start = System.currentTimeMillis();
        int i = 10;
        while (i > 0) {
            SortedSet<TreeNode> a = TreeUtil.createTree(SerializationUtils.clone(list), null, "/", "full");
            i--;
        }
        System.out.println("old" + (System.currentTimeMillis() - start));
    }

    public void testCpu() {
        System.out.println(Runtime.getRuntime().availableProcessors());
    }
}