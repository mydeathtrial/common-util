package com.agile.common.data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author 佟盟
 * 日期 2019/10/31 11:32
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class DemoC {
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
    private StringBuilder string_builder;
    private BigDecimal big_decimal;

    private List<Long> integer_List;
    private LinkedList<Long> integer_linkedList;
    private Set<Float> floatSet;
    private TreeSet<Integer> floatTreeSet;
    private Queue<String> longQueue;
    private PriorityQueue<Long> longPriorityQueue;
    private Map<String, Long> stringIntegerMap;
    private LinkedHashMap<String, Integer> integerIntegerLinkedHashMap;

    private List<DemoA> demoAList;
    private Map<String, DemoA> stringDemoAMap;
}
