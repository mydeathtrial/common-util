package com.agile.common.util.properties;

import com.agile.common.constant.Constant;
import com.agile.common.util.clazz.TypeReference;
import com.agile.common.util.file.JarUtil;
import com.agile.common.util.files.SupportEnum;
import com.agile.common.util.object.ObjectUtil;
import com.agile.common.util.string.StringUtil;
import com.google.common.collect.Sets;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
    private static final String[] overrideConfig = new String[]{"application"};

    /**
     * 日志
     */
    private static Log log = LogFactory.getLog(PropertiesUtil.class);

    /**
     * 最终配置集
     */
    private static Properties properties = new Properties();

    /**
     * 扫描到的配置文件集
     */
    private static Set<String> fileNames = Sets.newHashSet();

    /**
     * 初始化静态块
     */
    static {
        // 读取jar包配置
        readJar("com.agile");

        // 读取编译目录配置文件
        readDir();

        // 读取环境变量
        readEnv();

        parsePlaceholder();
    }

    private static void parsePlaceholder() {
        if (properties == null) {
            return;
        }
        for (Map.Entry<Object, Object> v : properties.entrySet()) {
            if (v.getValue() instanceof String) {
                properties.setProperty(String.valueOf(v.getKey()), StringUtil.parsingPlaceholder("${", "}", String.valueOf(v.getValue()), properties));
            }
        }
    }

    /**
     * 去读编译目录下的配置文件
     */
    private static void readDir() {
        Set<String> fileNames = Sets.newHashSet();
        Set<String> overrideConfigFileNames = Sets.newHashSet();
        try {
            Collections.list(PropertiesUtil.class.getClassLoader().getResources(""))
                    .forEach(url -> {
                        try {
                            readDir(fileNames, new File(URLDecoder.decode(url.getPath(), StandardCharsets.UTF_8.name())));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    });

            String classPath = PropertiesUtil.class.getResource(CLASSES_DIR_SPLIT).getPath();

            fileNames.stream().filter(filterOverrideConfigName(overrideConfigFileNames)).sorted(getStringComparator()).forEach(toRead(classPath));

            overrideConfigFileNames.stream().sorted(getStringComparator()).forEach(toRead(classPath));
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
    private static Consumer<String> toRead(String classPath) {
        return fileName -> {
            try {
                read(fileName.replace(classPath, "").replace(DIR_SPLIT, CLASSES_DIR_SPLIT), new FileInputStream(new File(fileName)));
            } catch (FileNotFoundException e) {
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
            for (String overrideConfigName : overrideConfig) {
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
    private static void readDir(Set<String> fileNames, File dir) {
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
                    readDir(fileNames, file);
                }
            }
        }

    }

    /**
     * 读取jar中的配置文件
     *
     * @param packagePath 扫描的包名
     */
    public static void readJar(String packagePath) {
        Set<String> resourceNames = JarUtil.getFile(packagePath, false, CLASS);
        Set<String> overrideConfigFileNames = Sets.newHashSet();

        resourceNames.stream()
                .filter(filterOverrideConfigName(overrideConfigFileNames))
                .sorted(getStringComparator())
                .forEach(resourceName -> read(resourceName, PropertiesUtil.class.getResourceAsStream(resourceName)));

        overrideConfigFileNames.stream()
                .sorted(getStringComparator())
                .forEach(resourceName -> read(resourceName, PropertiesUtil.class.getResourceAsStream(resourceName)));
    }

    /**
     * 读取配置文件
     *
     * @param fileName    文件名
     * @param inputStream 输入流
     */
    public static void read(String fileName, InputStream inputStream) {

        if (fileName == null || inputStream == null) {
            return;
        }

        try {

            if (fileName.endsWith(SupportEnum.properties.name())) {
                readProperties(inputStream);
            } else if (fileName.endsWith(SupportEnum.yml.name()) || fileName.endsWith(SupportEnum.yaml.name())) {
                readYml(inputStream);
            }
            if (!fileName.endsWith(CLASS)) {
                if (!fileName.startsWith(Constant.RegularAbout.SLASH)) {
                    fileName = Constant.RegularAbout.SLASH + fileName;
                }
                fileNames.add(fileName);
            }

            System.out.println(fileName);
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
        properties.load(in);
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
                properties.setProperty(key.toString(), String.valueOf(value));
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
        ymlToMap(properties, dataMap, null);
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
        return properties;
    }

    /**
     * 获取所有扫描到的配置文件名
     *
     * @return 文件名集
     */
    public static Set<String> getFileNames() {
        return fileNames;
    }

    /**
     * 覆盖配置
     *
     * @param key   key
     * @param value value
     */
    public static void setProperties(String key, String value) {
        properties.setProperty(key, value);
    }

    /**
     * 为配置值叠加
     *
     * @param key   key
     * @param value value
     */
    public static void appendProperties(String key, String value) {
        String v = properties.getProperty(key);
        if (v == null) {
            properties.setProperty(key, value);
        } else {
            properties.setProperty(key, v + value);
        }
    }

    /**
     * 取配置
     *
     * @param key key
     * @return 值
     */
    public static String getProperty(String key) {
        Object value = properties.get(key);
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
        if (!ObjectUtil.isEmpty(value)) {
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

}
