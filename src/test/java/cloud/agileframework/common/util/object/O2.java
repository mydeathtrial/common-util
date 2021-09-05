package cloud.agileframework.common.util.object;

import cloud.agileframework.common.annotation.Remark;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 佟盟
 * 日期 2021-05-18 15:59
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class O2 {
    @Remark("名字")
    private String name;
    private String[] friends;
    private long money;
}
