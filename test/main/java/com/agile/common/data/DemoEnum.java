package com.agile.common.data;

/**
 * @author 佟盟
 * 日期 2019/11/6 14:02
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public enum DemoEnum {
    aa(0),BB(1),abc(2);
    private int a;

    DemoEnum(int a) {
        this.a = a;
    }

    public static void main(String[] args) {
        aa.name();
    }

    @Override
    public String toString() {
        return a+"";
    }
}
