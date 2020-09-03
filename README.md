# common-util:公共工具包
设计初衷是为大面积提高代码复用度，涵盖各方面通用代码工具。其中最具特色的工具`cloud.agileframework.common.util.object.ObjectUtil`中的对象深度转换能力，
该能力是Agile系列框架高代码复用率的根本所在。其大量应用于请求参数解析、验证、持久层查询结果转换等功能当中，是目前开源项目中独树一帜的特有组件，其能够完成n层对象嵌套、
集合类型嵌套、模糊识别（如驼峰、下划线风格属性互转）、逗号分隔字符串转集合、日期字符串分析、注解别名、识别set、get、复杂属性拷贝等能力。

----
[![](https://img.shields.io/badge/common--lang3-LATEST-yellow)](https://img.shields.io/badge/common--lang3-LATEST-yellow)
[![](https://img.shields.io/badge/build-maven-green)](https://img.shields.io/badge/build-maven-green)


## 它有什么作用

* **大量工具包**
其包含的工具包种类极其丰富，只有您想象不到，没有它做不到的能力，并且该能力仍在持续更新中

* **特色能力**
力求创作具有特色化能力的工具，避免重复造轮子，对开发中常用的commons-lang3等工具集中已涵盖的能力不做重复设计。特色能力如：
<br> ClassUtil（反射工具，获取各种形式的类信息，包括注解解析等能力）
<br> TreeUtil（树形工具，平行数据集转树形集合结构，支持累加字段，常用于字典数据转换）
<br> DateUtil（日期解析工具，在不提供dataFormat的情况下，自主识别日期）
<br> JarUtil（Jar文件解析工具，一般用于jar内配置文件读取）
<br> HttpUtil（Http、Https协议通信工具）
<br> JSONUtil（路径取参）
<br> MapUtil（Map结构转换）
<br> ObjectUtil（对象深度转换器、拷贝、对象解析等）
<br> PatternUtil（正则工具，匹配、提取）
<br> PropertiesUtil（配置文件加载器，解析properties、yml及各种配置文件位置，其作为Agile框架启动过程中的自动配置文件扫描，并判断配置文件优先级）
<br> ShellUtil（命令行工具）
<br> StringUtil（字符串工具，转换、匹配、提取、占位符解析）
<br> 工具较多，详细请查看javadoc ...

-------
## 快速入门
开始你的第一个项目是非常容易的。

#### 步骤 1: 下载包
您可以从[最新稳定版本]下载包(https://github.com/mydeathtrial/common-util/releases).
该包已上传至maven中央仓库，可在pom中直接声明引用
以版本common-util-2.0.0.jar为例。

#### 步骤 2: 添加maven依赖
```xml
    <!--声明中央仓库-->
    <repositories>
        <repository>
            <id>cent</id>
            <url>https://repo1.maven.org/maven2/</url>
        </repository>
    </repositories>

    <!--添加common-util依赖-->
    <dependencies>
        <dependency>
            <groupId>cloud.agileframework</groupId>
            <artifactId>common-util</artifactId>
            <version>2.0.0</version>
        </dependency>
    </dependencies>
```
#### 步骤 3: 程序中调用，由于工具较多，详细使用请参照javadoc
##### 对象深度转换为例
```java
public class YourClass{
    public void yourMethod(){
        //演示Map转换pojo，更多使用方式请参照javadoc
        DemoA demoA = ObjectUtil.to(data(), new TypeReference<DemoA>() {});
    }
    private String data(){
        Map<String, Object> map = Maps.newHashMap();
        map.put("attrString1", "attrString1Value");
        map.put("attr_string2", "attrString2Value");
        map.put("attrInt", "100");
        map.put("attrLong", "222l");
        map.put("attrChar", "c");
        map.put("attrShort", "33");
        map.put("attrFloat", "44f");
        map.put("attrDouble", "55555555555555555555");
        map.put("attrBoolean", "5");
        map.put("attrByte", "6");
        
        map.put("attr_int", "100");
        map.put("attr_Long", "222");
        map.put("attr_Char", "c");
        map.put("attr_Short", "33");
        map.put("attr_Float", "44.02");
        map.put("attr_Double", "55555555555555555555");
        map.put("attr_Boolean", "false");
        map.put("attr_Byte", "6");
        
        map.put("date", "1990nian09月05 11:12 下午");
        map.put("stringBuilder", "stringBuilderValue");
        map.put("bigDecimal", "123456789012345567678");
        map.put("integerList", new String[]{"1", "2", "3"});
        map.put("integerLinkedList", new ArrayList<String>() {{
            add("1");
            add("2");
        }});
        
        map.put("floatSet", new LinkedList() {{
            add("1");
            add("2");
        }});
        
        map.put("floatTreeSet", new PriorityQueue() {{
            add("1");
            add("2");
        }});
        
        map.put("longQueue", new PriorityQueue() {{
            add("1L");
            add("2l");
        }});
        
        map.put("longPriorityQueue", new PriorityQueue() {{
            add("1L");
            add("2l");
        }});
        
        map.put("stringIntegerMap", new DemoB());
        map.put("integerIntegerLinkedHashMap", new HashMap<String, String>() {{
            put("1", "11");
            put("2", "22");
            put("3", "33");
        }});
        
        HashMap<String, Object> map2 = Maps.newHashMap(map);
        map.put("demoAList", new HashMap[]{map2, map2});
        map.put("stringDemoAMap", new HashMap<String, Object>() {{
            put("1", map2);
            put("2", map2);
        }});
        
        map.put("integersArray", new String[]{"1", "2"});
        map.put("demoEnum", "a_bc");
        map.put("demoEnumArray", new String[]{"aa", "bb", "a_bc"});
        return map;
    }

    public class DemoA {
        private String attr_String1;
        private String attrString2;
        private int attrInt;
        private long attrLong;
        private char attrChar;
        private short attrShort;
        private float attrFloat;
        private double attrDouble;
        private boolean attrBoolean;
        private byte attrByte;
    
        private Integer attr_int;
        private Long attr_long;
        private Character attr_char;
        private Short attr_short;
        private Float attr_float;
        private Double attr_double;
        private Boolean attr_boolean;
        private Byte attr_byte;
    
        private Date date;
        private StringBuilder stringBuilder;
        private BigDecimal bigDecimal;
    
        private List<Integer> integerList;
        private List<?> integerLinkedList;
        private Set<Float> floatSet;
        private TreeSet<Float> floatTreeSet;
        private Queue<Long> longQueue;
        private PriorityQueue<Long> longPriorityQueue;
        private Map<String, Integer> stringIntegerMap;
        private LinkedHashMap<Integer, Integer> integerIntegerLinkedHashMap;
    }

}
```
##### 日期识别为例
```
    public void parseDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        GregorianCalendar date;
        date = DateUtil.parse("19900905 11:12 下午");
        System.out.println(format.format(date.getTime()));

        date = DateUtil.parse("19900905 pm 11:12");
        System.out.println(format.format(date.getTime()));

        date = DateUtil.parse("pm 1990-9-5 11:12");
        System.out.println(format.format(date.getTime()));

        date = DateUtil.parse("11:12 1990/09/05");
        System.out.println(format.format(date.getTime()));

        date = DateUtil.parse("11:2:03 1990年9月05");
        System.out.println(format.format(date.getTime()));

        date = DateUtil.parse("11:02:33 1990-09/05");
        System.out.println(format.format(date.getTime()));

        date = DateUtil.parse("1596782907410");
        System.out.println(format.format(date.getTime()));

        date = DateUtil.parse("6782907410");
        System.out.println(format.format(date.getTime()));
    }
```
结果日志
```
1990-09-05 23:12:00
1990-09-05 23:12:00
1990-09-05 23:12:00
1990-09-05 11:12:00
1990-09-05 11:02:03
1990-09-05 11:02:33
2020-08-07 14:48:27
1970-03-20 20:08:27
```
文档编写中...