package com.agile.common.data;

import cloud.agileframework.common.annotation.Alias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 佟盟
 * 日期 2020-11-13 13:47
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DemoE {
    @Alias("paramA")
    private String paramC;
    private String paramB;
    @Alias("paramB")
    private String paramD;
}
