package cloud.agileframework.common.util.object;

import cloud.agileframework.common.annotation.CompareField;
import cloud.agileframework.common.annotation.Remark;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author 佟盟
 * 日期 2021-05-18 15:58
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class O1 {
    @Remark(value = "姓名",ignoreCompare = false)
    private String name;
    @Remark(value = "年龄",ignoreCompare = false)
    private int age;
    @Remark(value = "朋友",ignoreCompare = false)
    private List<String> friends;
    @CompareField
    @Remark(value = "媳妇儿",ignoreCompare = false)
    private O1 o1;

    public O1(String name, int age, List<String> friends) {
        this.name = name;
        this.age = age;
        this.friends = friends;
    }
}
