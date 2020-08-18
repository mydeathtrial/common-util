package cloud.agileframework.common.util.file;

import com.google.common.collect.Sets;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * @author 佟盟
 * 日期 2019/11/28 14:25
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class JarUtil {

    private static final String CLASSES_DIR_SPLIT = "/";

    public static void parseConsumer(JarFile jar, String packagePath, Consumer<Set<String>> consumer) {
        consumer.accept(getFile(jar, packagePath));
    }

    public static <A> A parseFunction(JarFile jar, String packagePath, Function<Set<String>, A> function) {
        return function.apply(getFile(jar, packagePath));
    }

    /**
     * 从工程依赖的所有jar中，根据packagePath路径获取文件
     *
     * @param packagePath 路径
     * @return 匹配到的文件路径集合
     */
    public static Set<String> getFile(String packagePath) {
        return getFile(packagePath, true, (String) null);
    }

    /**
     * 从工程依赖的所有jar中，根据packagePath路径获取文件，且文件后缀符合suffixes
     *
     * @param packagePath 路径
     * @param suffixes    文件后缀
     * @return 匹配到的文件路径集合
     */
    public static Set<String> getFile(String packagePath, boolean isInclude, String... suffixes) {
        Set<String> set = Sets.newHashSet();
        try {
            Collections.list(JarUtil.class.getClassLoader().getResources(toEntryName(packagePath)))
                    .stream()
                    .map(path -> {
                        if ("jar".equalsIgnoreCase(path.getProtocol())) {
                            return path.getPath().substring(0, path.getPath().indexOf("!")).replace("file:", "");
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .forEach(path -> {
                        try {
                            JarFile jar = new JarFile(URLDecoder.decode(path, StandardCharsets.UTF_8.name()));
                            if (suffixes == null) {
                                set.addAll(getFile(jar, packagePath));
                            } else {
                                set.addAll(getFile(jar, packagePath, isInclude, suffixes));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            return set;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return set;
    }

    /**
     * 获取jar中的文件路径集合
     *
     * @param jar         jar文件
     * @param packagePath 包名字
     * @param suffixes    文件后缀
     * @return 文件路径集合
     */
    public static Set<String> getFile(JarFile jar, String packagePath, boolean isInclude, String... suffixes) {
        return getFile(jar, packagePath).stream().filter(name -> {
            if (name.endsWith(CLASSES_DIR_SPLIT)) {
                return false;
            }
            boolean isTrue = !isInclude;
            for (String suffix : suffixes) {
                if (name.endsWith(suffix)) {
                    isTrue = isInclude;
                    break;
                }
            }
            return isTrue;
        }).collect(Collectors.toSet());
    }

    /**
     * 获取jar中的文件路径集合
     *
     * @param jar         jar文件
     * @param packagePath 包名字
     * @return 文件路径集合
     */
    public static Set<String> getFile(JarFile jar, String packagePath) {
        String finalPackagePath = toEntryName(packagePath);
        return Collections.list(jar.entries())
                .stream()
                .filter(entry -> entry.getName().startsWith(finalPackagePath))
                .map(entry -> CLASSES_DIR_SPLIT + entry.getName())
                .collect(Collectors.toSet());
    }

    /**
     * 路径转换为jar中识别的包名
     *
     * @param packagePath 包路径
     * @return jar中的包路径
     */
    public static String toEntryName(String packagePath) {
        packagePath = packagePath
                .replace("\\", CLASSES_DIR_SPLIT)
                .replace(".", CLASSES_DIR_SPLIT);
        if (packagePath.startsWith(File.separator) || packagePath.startsWith(CLASSES_DIR_SPLIT)) {
            packagePath = packagePath.substring(1);
        }
        if (packagePath.endsWith(File.separator) || packagePath.endsWith(CLASSES_DIR_SPLIT)) {
            packagePath = packagePath.substring(0, packagePath.length() - 1);
        }
        return packagePath;
    }

}
