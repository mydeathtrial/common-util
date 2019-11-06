package com.agile.common.data;

import com.google.common.collect.Maps;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

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
    private LinkedList<Integer> integerLinkedList;
    private Set<Float> floatSet;
    private TreeSet<Float> floatTreeSet;
    private Queue<Long> longQueue;
    private PriorityQueue<Long> longPriorityQueue;
    private Map<String, Integer> stringIntegerMap;
    private LinkedHashMap<Integer, Integer> integerIntegerLinkedHashMap;

    private List<DemoA> demoAList;
    private Map<String, DemoA> stringDemoAMap;

    private Integer[] integersArray;

    private DemoEnum demoEnum;

    private DemoEnum[] demoEnumArray;

    public static Map<String, Object> testData() {
        Map<String, Object> map = Maps.newHashMap();
        map.put("attrString1", "attrString1Value");
        map.put("attr_string2", "attrString2Value");
        map.put("attrInt", "100");
        map.put("attrLong", "222L");
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

        map.put("date", "19900905 11:12 下午");
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
}