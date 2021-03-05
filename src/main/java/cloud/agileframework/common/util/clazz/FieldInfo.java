package cloud.agileframework.common.util.clazz;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 佟盟
 * 日期 2020-09-16 11:13
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class FieldInfo {
    private Map<Method, Integer> setters;
    private Boolean noSetters;
    private Map<Method, Integer> getters;
    private Boolean noGetters;

    public void putSetter(Method setter) {
        if (setters == null) {
            this.setters = Maps.newConcurrentMap();
        }
        Integer order = setters.get(setter);
        if (order == null) {
            setters.put(setter, 0);
        } else {
            setters.put(setter, ++order);
        }
    }

    public void putGetter(Method getter) {
        if (getters == null) {
            this.getters = Maps.newConcurrentMap();
        }
        Integer order = getters.get(getter);
        if (order == null) {
            getters.put(getter, 0);
        } else {
            getters.put(getter, ++order);
        }
    }

    public List<Method> getters() {
        if (getters != null) {
            return getters.entrySet().stream().sorted((a, b) -> b.getValue().compareTo(a.getValue())).map(Map.Entry::getKey).collect(Collectors.toList());
        }
        return Lists.newArrayListWithExpectedSize(0);
    }

    public List<Method> setters() {
        if (setters != null) {
            return setters.entrySet().stream().sorted((a, b) -> b.getValue().compareTo(a.getValue())).map(Map.Entry::getKey).collect(Collectors.toList());
        }
        return Lists.newArrayListWithExpectedSize(0);
    }

    public Boolean isNoSetters() {
        return noSetters;
    }

    public void setNoSetters(boolean noSetters) {
        this.noSetters = noSetters;
    }

    public Boolean isNoGetters() {
        return noGetters;
    }

    public void setNoGetters(boolean noGetters) {
        this.noGetters = noGetters;
    }
}
