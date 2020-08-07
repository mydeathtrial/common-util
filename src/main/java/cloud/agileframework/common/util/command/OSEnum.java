package cloud.agileframework.common.util.command;

import lombok.Getter;

import java.util.stream.Stream;

/**
 * @author 佟盟
 * 日期 2020/1/12 14:43
 * 描述 操作系统类型
 * @version 1.0
 * @since 1.0
 */
public enum OSEnum {
    /**
     * 其他
     */
    Any("any"),
    Linux("linux"),
    Mac_OS("mac","os"),
    Mac_OS_X("mac","OS","X"),
    Windows("windows"),
    OS2("os/2"),
    Solaris("solaris"),
    SunOS("sunos"),
    MPEiX("mpe/ix"),
    HP_UX("hp-ux"),
    AIX("aix"),
    OS390("os/390"),
    FreeBSD("freebsd"),
    Irix("irix"),
    Digital_Unix("digital","unix"),
    NetWare_411("netware"),
    OSF1("osf1"),
    OpenVMS("openvms"),
    Others("others");

    OSEnum(String... desc){
        this.desc = desc;
    }

    @Getter
    private String[] desc;

    /**
     * 获取操作系统名字
     *
     * @return 操作系统名
     */
    public static OSEnum currentOS() {
        String osDesc = System.getProperty("os.name").toLowerCase();
        OSEnum[] oses = OSEnum.values();
        for (OSEnum os:oses) {
            String[] desc = os.getDesc();
            boolean is = Stream.of(desc).anyMatch(osDesc::contains);
            if(is){
                return os;
            }
        }
        return OSEnum.Others;
    }
}
