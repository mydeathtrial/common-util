package com.agile.common.util.properties;

import com.agile.common.constant.Constant;
import com.agile.common.util.file.JarUtil;
import com.agile.common.util.files.SupportEnum;
import com.agile.common.util.string.StringUtil;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

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
     * 日志
     */
    private static Logger log = LoggerFactory.getLogger(PropertiesUtil.class);

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
    }

    /**
     * 去读编译目录下的配置文件
     */
    private static void readDir() {
        Set<String> fileNames = Sets.newHashSet();
        try {
            Collections.list(PropertiesUtil.class.getClassLoader().getResources(""))
                    .forEach(url -> readDir(fileNames, new File(url.getPath())));

            String classPath = PropertiesUtil.class.getResource(CLASSES_DIR_SPLIT).getPath();
            fileNames.stream().sorted((a, b) -> StringUtil.split(b, File.separator).length - StringUtil.split(a, File.separator).length).forEach(fileName -> {
                try {
                    read(fileName.replace(classPath, "").replace(DIR_SPLIT, CLASSES_DIR_SPLIT), new FileInputStream(new File(fileName)));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        resourceNames.stream()
                .sorted((a, b) -> b.split(Constant.RegularAbout.SLASH).length - a.split(Constant.RegularAbout.SLASH).length)
                .forEach(resourceName -> read(resourceName, PropertiesUtil.class.getResourceAsStream(resourceName)));
    }

    /**
     * 读取配置文件
     *
     * @param fileName    文件名
     * @param inputStream 输入流
     */
    private static void read(String fileName, InputStream inputStream) {

        if (fileName == null || inputStream == null) {
            return;
        }
        ByteArrayOutputStream outputStream = toByteArrayOutputStream(inputStream);
        if (outputStream == null) {
            return;
        }
        try {

            inputStream = new ByteArrayInputStream(outputStream.toByteArray());
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

            if (log.isDebugEnabled()) {
                log.debug(fileName);
            }
        } catch (IOException e) {
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
    public static void readProperties(InputStream in) throws IOException {
        properties.load(in);
    }

    /**
     * map数据读取到配置中
     *
     * @param map map数据
     */
    public static void readProperties(Map<?, ?> map) {
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
    public static void readYml(InputStream in) {
        Yaml yaml = new Yaml();
        Map<String, String> dataMap = yaml.load(in);
        ymlToMap(properties, dataMap, null);
    }

    /**
     * yml转Map
     *
     * @param allMap    转换后的map容器
     * @param map       递归用
     * @param parentKey 递归用
     */
    public static void ymlToMap(Map<Object, Object> allMap, Map map, String parentKey) {
        if (map == null) {
            return;
        }
        for (Object o : map.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            Object currentKey = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                if (parentKey == null) {
                    ymlToMap(allMap, (Map) value, currentKey.toString());
                } else {
                    ymlToMap(allMap, (Map) value, parentKey + PROPERTIES_KEY_SPLIT + currentKey.toString());
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
     * 输入流，为重复读取流
     *
     * @param inputStream 输入流
     * @return 用于重复读取的流对象
     */
    private static ByteArrayOutputStream toByteArrayOutputStream(InputStream inputStream) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
            return outputStream;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
