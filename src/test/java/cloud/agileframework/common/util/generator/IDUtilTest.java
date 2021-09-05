package cloud.agileframework.common.util.generator;

import com.google.common.collect.Lists;
import junit.framework.TestCase;

import java.util.List;
import java.util.stream.IntStream;

public class IDUtilTest extends TestCase {

    private List<Long> list = Lists.newCopyOnWriteArrayList();

    public void testGeneratorId() {
        Runnable work = () -> {
            Long id = IDUtil.generatorId();
            if (list.contains(id)) {
                throw new RuntimeException();
            }
//            System.out.println(id);
            list.add(id);
        };
        IntStream.range(0, 100000).parallel().forEach((i) -> work.run());
        System.out.println(list.size());
    }

}