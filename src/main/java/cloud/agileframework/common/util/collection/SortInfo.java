package cloud.agileframework.common.util.collection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author 佟盟
 * 日期 2021-01-05 18:08
 * 描述 排序信息
 * @version 1.0
 * @since 1.0
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SortInfo implements Serializable {
    private String property;
    private boolean sort;
}
