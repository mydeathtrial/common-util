package com.agile.common.util.http;

import com.agile.common.constant.Constant;

/**
 * @author 佟盟
 * 日期 2020/6/4 22:21
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public enum Protocol {
    /**
     * Http
     */
    Http,
    /**
     * Https
     */
    Https;

    /**
     * 根据地址提取通信协议
     * @param url 地址
     * @return 协议
     */
    public static Protocol extract(String url){
        return url.trim().toLowerCase().startsWith(Constant.RegularAbout.HTTPS)?Https:Http;
    }
}
