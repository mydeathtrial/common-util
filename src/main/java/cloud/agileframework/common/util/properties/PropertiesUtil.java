package cloud.agileframework.common.util.properties;

import cloud.agileframework.common.constant.Constant;
import cloud.agileframework.common.util.array.ArrayUtil;
import cloud.agileframework.common.util.clazz.TypeReference;
import cloud.agileframework.common.util.file.JarUtil;
import cloud.agileframework.common.util.object.ObjectUtil;
import cloud.agileframework.common.util.stream.StreamUtil;
import cloud.agileframework.common.util.string.StringUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author 佟盟
 * 日期 2019/11/28 15:20
 * 描述 配置文件加载工具
 * @version 1.0
 * @since 1.0
 */
public class PropertiesUtil {
    /**
     * 越过类文件
     */
    private static final String CLASS = ".class";

    /**
     * 编译文件中的文件夹分隔符
     */
    private static final String CLASSES_DIR_SPLIT = "/";

    /**
     * 文件夹分隔符
     */
    private static final String DIR_SPLIT = "\\";

    /**
     * 配置文件的key值分隔符
     */
    private static final String PROPERTIES_KEY_SPLIT = ".";

    /**
     * 配置文件优先级
     */
    private static final String[] OVERRIDE_CONFIG = new String[]{"application"};

    /**
     * 日志
     */
    private static final Log log = LogFactory.getLog(PropertiesUtil.class);

    /**
     * 最终配置集
     */
    private static final Properties PROPERTIES = new Properties();

    /**
     * 扫描到的配置文件集
     */
    private static final Set<String> FILE_NAMES = Sets.newHashSet();

    /**
     * 初始化静态块
     */
    static {
        // 获取启动类
        String className = ((StackTraceElement) ArrayUtil.last(new RuntimeException().getStackTrace())).getClassName();

        // 加载agile系列配置文件
        readJar("cloud.agileframework.conf", PropertiesUtil::toProperties);
        readJar("com.agile.conf", PropertiesUtil::toProperties);

        // 读取jar包配置
        readJar(className.substring(0, className.lastIndexOf(".")), PropertiesUtil::toProperties);

        // 读取编译目录配置文件
        readDir(PropertiesUtil::toProperties);

        // 读取环境变量
        readEnv();

        parsePlaceholder();
    }

    /**
     * 按文件优先级处理文件
     *
     * @param consumer 处理文件的方法
     */
    public static void traverseFile(BiConsumer<String, InputStream> consumer) {
        traverseFile(consumer, null);
    }

    /**
     * 按照文件优先级处理文件
     *
     * @param consumer    处理文件的方法
     * @param packagePath 以点分割的包路径，如com.baidu.xxx
     */
    public static void traverseFile(BiConsumer<String, InputStream> consumer, String packagePath) {
        if (packagePath == null) {
            // 加载agile系列配置文件
            readJar("cloud.agileframework.conf", consumer);
            readJar("com.agile.conf", consumer);

            // 获取启动类
            String className = ((StackTraceElement) ArrayUtil.last(new RuntimeException().getStackTrace())).getClassName();

            // 读取jar包配置
            readJar(className.substring(0, className.lastIndexOf(".")), consumer);
        } else {
            readJar(packagePath, consumer);
        }

        // 读取编译目录配置文件
        readDir(consumer);
    }

    private static void parsePlaceholder() {
        for (Map.Entry<Object, Object> v : PROPERTIES.entrySet()) {
            if (v.getValue() instanceof String) {
                PROPERTIES.setProperty(String.valueOf(v.getKey()), StringUtil.parsingPlaceholder("${", "}", String.valueOf(v.getValue()), PROPERTIES));
            }
        }
    }

    /**
     * 去读编译目录下的配置文件
     */
    private static void readDir(BiConsumer<String, InputStream> consumer) {
        Set<String> fileNames = Sets.newHashSet();
        Set<String> overrideConfigFileNames = Sets.newHashSet();
        try {
            Collections.list(PropertiesUtil.class.getClassLoader().getResources(""))
                    .forEach(url -> {
                        try {
                            readDir(fileNames, new File(URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8.name())), consumer);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    });

            String classPath = PropertiesUtil.class.getResource(CLASSES_DIR_SPLIT).getPath();

            fileNames.stream().filter(filterOverrideConfigName(overrideConfigFileNames)).sorted(getStringComparator()).forEach(toRead(classPath, consumer));

            overrideConfigFileNames.stream().sorted(getStringComparator()).forEach(toRead(classPath, consumer));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据编译目录读取配置方法
     *
     * @param classPath 编译目录
     * @return 方法
     */
    private static Consumer<String> toRead(String classPath, BiConsumer<String, InputStream> consumer) {
        return fileName -> {
            try {
                read(fileName.replace(URLDecoder.decode(classPath, StandardCharsets.UTF_8.name()), "").replace(DIR_SPLIT, CLASSES_DIR_SPLIT), new FileInputStream(new File(fileName)), consumer);
            } catch (FileNotFoundException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        };
    }

    /**
     * 字符串比较方法
     *
     * @return 方法
     */
    private static Comparator<String> getStringComparator() {
        return (a, b) -> {
            final String regex = "[\\\\/]";
            int s = b.split(regex).length - a.split(regex).length;
            if (s == 0) {
                s = a.compareTo(b);
            }
            return s;
        };
    }

    /**
     * 过滤优先级较高的配置文件名字
     *
     * @param overrideConfigFileNames 过滤后装填的容器
     * @return 方法
     */
    private static Predicate<String> filterOverrideConfigName(Set<String> overrideConfigFileNames) {
        return name -> {
            for (String overrideConfigName : OVERRIDE_CONFIG) {
                if (name.contains(overrideConfigName + ".")) {
                    overrideConfigFileNames.add(name);
                    return false;
                }
            }
            return true;
        };
    }

    /**
     * 读取文件目录
     *
     * @param dir 文件夹
     */
    private static void readDir(Set<String> fileNames, File dir, BiConsumer<String, InputStream> consumer) {
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files == null) {
                return;
            }
            for (File file : files) {
                if (file.isFile() && !file.getName().endsWith(CLASS)) {
                    try {
                        fileNames.add(URLDecoder.decode(file.toURI().getPath(), StandardCharsets.UTF_8.name()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (file.isDirectory()) {
                    readDir(fileNames, file, consumer);
                }
            }
        }

    }

    public static void readJar(String packagePath) {
        readJar(packagePath, PropertiesUtil::toProperties);
    }

    /**
     * 读取jar中的配置文件
     *
     * @param packagePath 扫描的包名
     */
    public static void readJar(String packagePath, BiConsumer<String, InputStream> consumer) {
        Set<String> resourceNames = JarUtil.getFile(packagePath, false, CLASS);
        Set<String> overrideConfigFileNames = Sets.newHashSet();

        resourceNames.stream()
                .filter(filterOverrideConfigName(overrideConfigFileNames))
                .sorted(getStringComparator())
                .forEach(resourceName -> read(resourceName, PropertiesUtil.class.getResourceAsStream(resourceName), consumer));

        overrideConfigFileNames.stream()
                .sorted(getStringComparator())
                .forEach(resourceName -> read(resourceName, PropertiesUtil.class.getResourceAsStream(resourceName), consumer));
    }

    public static void read(String fileName, InputStream inputStream) {
        read(fileName, inputStream, PropertiesUtil::toProperties);
    }

    /**
     * 读取配置文件
     *
     * @param fileName    文件名
     * @param inputStream 输入流
     */
    public static void read(String fileName, InputStream inputStream, BiConsumer<String, InputStream> consumer) {

        if (fileName == null || inputStream == null) {
            return;
        }

        consumer.accept(fileName, inputStream);
    }

    /**
     * 文件内容转换为配置信息
     *
     * @param fileName    文件名
     * @param inputStream 文件流
     */
    private static void toProperties(String fileName, InputStream inputStream) {
        try {

            final String properties = "properties";
            if (fileName.endsWith(properties)) {
                readProperties(inputStream);
            } else {
                final String yml = "yml";
                final String yaml = "yaml";
                if (fileName.endsWith(yml) || fileName.endsWith(yaml)) {
                    readYml(inputStream);
                }
            }
            if (!fileName.endsWith(CLASS)) {
                if (!fileName.startsWith(Constant.RegularAbout.SLASH)) {
                    fileName = Constant.RegularAbout.SLASH + fileName;
                }
                FILE_NAMES.add(fileName);
            }

//            System.out.println(fileName);
            if (log.isDebugEnabled()) {
                log.debug(fileName);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(String.format("read file(%s) to properties is error", fileName), e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 读取环境变量
     */
    private static void readEnv() {
        readProperties(System.getProperties());
        readProperties(System.getenv());
    }

    /**
     * 读取properties文件流
     *
     * @param in 文件流
     * @throws IOException 异常
     */
    private static void readProperties(InputStream in) throws IOException {
        PROPERTIES.load(in);
    }

    /**
     * map数据读取到配置中
     *
     * @param map map数据
     */
    private static void readProperties(Map<?, ?> map) {
        if (map == null) {
            return;
        }
        map.forEach((key, value) -> {
            try {
                PROPERTIES.setProperty(key.toString(), String.valueOf(value));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 读取yml文件流
     *
     * @param in yml文件流
     */
    private static void readYml(InputStream in) {
        Yaml yaml = new Yaml();
        Map<String, Object> dataMap = yaml.load(in);
        ymlToMap(PROPERTIES, dataMap, null);
    }

    /**
     * yml转Map
     *
     * @param allMap    转换后的map容器
     * @param map       递归用
     * @param parentKey 递归用
     */
    private static void ymlToMap(Map<Object, Object> allMap, Map<String, Object> map, String parentKey) {
        if (map == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object currentKey = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                if (parentKey == null) {
                    ymlToMap(allMap, (Map<String, Object>) value, currentKey.toString());
                } else {
                    ymlToMap(allMap, (Map<String, Object>) value, parentKey + PROPERTIES_KEY_SPLIT + currentKey.toString());
                }
            } else if (value != null) {
                if (parentKey == null) {
                    allMap.put(currentKey.toString(), value.toString());
                }
                if (parentKey != null) {
                    allMap.put(parentKey + PROPERTIES_KEY_SPLIT + currentKey.toString(), value.toString());
                }
            }
        }
    }

    /**
     * 获取所有配置
     *
     * @return 配置文件集合
     */
    public static Properties getProperties() {
        return PROPERTIES;
    }

    /**
     * 获取所有扫描到的配置文件名
     *
     * @return 文件名集
     */
    public static Set<String> getFileNames() {
        return FILE_NAMES;
    }

    /**
     * 覆盖配置
     *
     * @param key   key
     * @param value value
     */
    public static void setProperties(String key, String value) {
        if (value == null) {
            return;
        }
        PROPERTIES.setProperty(key, value);
    }

    /**
     * 为配置值叠加
     *
     * @param key   key
     * @param value value
     */
    public static void appendProperties(String key, String value) {
        String v = PROPERTIES.getProperty(key);
        if (v == null) {
            PROPERTIES.setProperty(key, value);
        } else {
            PROPERTIES.setProperty(key, v + value);
        }
    }

    /**
     * 取配置
     *
     * @param key key
     * @return 值
     */
    public static String getProperty(String key) {
        Object value = PROPERTIES.get(key);
        if (value != null) {
            return value.toString();
        }
        return null;
    }

    /**
     * 获取工程配置信息带默认值
     *
     * @param key 句柄
     * @return 值
     */
    public static String getProperty(String key, String defaultValue) {
        Object value = getProperty(key);
        if (!ObjectUtils.isEmpty(value)) {
            return value.toString();
        }
        return defaultValue;
    }

    /**
     * 根据前缀取配置
     *
     * @param prefix 前缀
     * @return 配置集
     */
    public static Properties getPropertyByPrefix(String prefix) {
        Properties properties = new Properties();
        Set<String> propertyNames = getProperties().stringPropertyNames();
        for (String name : propertyNames) {
            if (name.startsWith(prefix)) {
                properties.put(name, getProperties().get(name));
            }
        }
        return properties;

    }

    /**
     * 取配置，并转换
     *
     * @param key   key
     * @param clazz 转换类型
     * @param <T>   泛型
     * @return 结果
     */
    public static <T> T getProperty(String key, Class<T> clazz) {
        return ObjectUtil.to(getProperty(key), new TypeReference<T>(clazz));
    }

    public static <T> T getProperty(String var1, Class<T> var2, String defaultValue) {
        return ObjectUtil.to(getProperty(var1, defaultValue), new TypeReference<T>(var2));
    }

    /**
     * 根据文件名取classpath目录下的json数据
     *
     * @param fileName 不带后缀文件名
     * @return JSONObject数据
     */
    public static JSON getJson(String fileName) {
        try {
            InputStream stream = null;

            File file = new File(fileName);
            if (file.exists()) {
                stream = new FileInputStream(file);
            } else {
                final String suffix = ".json";
                if (!fileName.endsWith(suffix)) {
                    fileName = fileName + suffix;
                }
                String path = getFileClassPath(fileName);
                if (path != null) {
                    stream = PropertiesUtil.class.getResourceAsStream(path);
                }
            }

            if (stream == null) {
                return null;
            }

            return (JSON) JSON.parse(StreamUtil.toString(stream));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 取配置文件内容
     *
     * @param fileName 文件名字
     * @return 文件内容
     */
    public static String getFileContent(String fileName) {
        InputStream inputStream = getFileStream(fileName);
        return StreamUtil.toString(inputStream);
    }

    /**
     * 取配置文件流
     *
     * @param fileName 文件名字
     * @return 文件流
     */
    public static InputStream getFileStream(String fileName) {
        String path = getFileClassPath(fileName);
        if (path == null) {
            return null;
        }
        return PropertiesUtil.class.getResourceAsStream(path);
    }

    /**
     * 取配置文件编译路径
     *
     * @param fileName 文件名字
     * @return 编译路径
     */
    public static String getFileClassPath(String fileName) {
        final String regex = "[\\\\/]";
        Set<String> set = getFilePaths("/" + fileName);
        return set.stream().min(Comparator.comparingInt(a -> a.split(regex).length)).orElse(null);
    }

    /**
     * 根据文件名取classpath目录下的json数据
     *
     * @param fileName 不带后缀文件名
     * @return JSONObject数据
     */
    public static String getFilePath(String fileName) {
        String path = getFileClassPath(fileName);
        if (path == null) {
            return null;
        }
        URL absolutePath = PropertiesUtil.class.getResource(path);
        if (absolutePath == null) {
            return path;
        }
        try {
            return URLDecoder.decode(absolutePath.getPath(), StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return path;
    }

    public static Set<String> getFilePaths(String fileName) {
        return getFileNames().stream().filter(name -> name.endsWith(fileName)).collect(Collectors.toSet());
    }
}
