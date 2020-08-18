package cloud.agileframework.common.constant;

/**
 * @author 佟盟 on 2017/9/2
 */
public class Constant {
    /**
     * 响应信息相关
     */
    public static class ResponseAbout {
        public static final String HEAD = "head";
        public static final String STATE = "state";
        public static final String MSG = "msg";
        public static final String CODE = "code";
        public static final String INFO = "info";
        public static final String APP = "app";
        public static final String SERVICE = "service";
        public static final String METHOD = "method";
        public static final String IP = "ip";
        public static final String URL = "url";
        public static final String RETURN = "return";
        public static final String RESULT = "result";
        public static final String BODY = "body";
        public static final String BODY_STR = "body_str";
    }

    /**
     * 数字相关
     */
    public static class NumberAbout {
        public static final int ZERO = 0;
        public static final int ONE = 1;
        public static final int TWO = 2;
        public static final int THREE = 3;
        public static final int FOUR = 4;
        public static final int FIVE = 5;
        public static final int SIX = 6;
        public static final int SEVEN = 7;
        public static final int EIGHT = 8;
        public static final int NINE = 9;
        public static final int TEN = 10;
        public static final int ELEVEN = 11;
        public static final int TWELVE = 12;
        public static final int THIRTY_ONE = 31;
        public static final int TWENTY_FOUR = 24;
        public static final int HUNDRED = 100;
        public static final int THOUSAND = 1000;
    }

    /**
     * 文件相关
     */
    public static class FileAbout {
        public static final String FILE_NAME = "fileName";
        public static final String FILE_SIZE = "fileSize";
        public static final String CONTENT_TYPE = "contentType";
        public static final String UP_LOUD_FILE_INFO = "upLoadFileInfo";
        public static final String SERVICE_LOGGER_FILE = "service";
    }

    /**
     * 响应头信息相关
     */
    public static class HeaderAbout {
        public static final String ATTACHMENT = "attachment";
    }

    /**
     * 正则表达式
     */
    public static class RegularAbout {
        public static final String NEW_LINE = "\n";
        public static final String NULL = "null";
        public static final String MINUS = "-";
        public static final String BLANK = "";
        public static final String UNDER_LINE = "_";
        public static final String SEMICOLON = ";";
        public static final String COLON = ":";
        public static final String SPOT = ".";
        public static final String DA_KUO_LEFT = "{";
        public static final String DA_KUO_RIGHT = "{";
        public static final String COMMA = ",";
        public static final String SNOW = "*";
        public static final String UP_COMMA = "'";
        public static final String UP_DOUBLE_COMMA = "\"";
        public static final String QUESTION_MARK = "?";
        public static final String SLASH = "/";
        public static final String BACKSLASH = "\\";
        public static final String AND = "&";
        public static final String EQUAL = "=";
        public static final String HUMP = "((?=[\\x21-\\x7e]+)[^A-Za-z0-9])";
        public static final String UPER = "[A-Z]";
        public static final String URL_REGEX = "[\\W_]?";
        public static final String HTTP = "http";
        public static final String HTTPS = "https";
        public static final String FORWARD = "forward";
        public static final String REDIRECT = "redirect";
        public static final String URL_PARAM = "(?<=\\{)[\\w]+(?=[:\\}])";
        public static final String EMAIL = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
        public static final String DOMAIN = "[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(/.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+/.?";
        public static final String INTERNET_UTL = "^http://([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$";
        public static final String MOBILE_PHONE = "^(13[0-9]|14[5|7]|15[0|1|2|3|5|6|7|8|9]|18[0|1|2|3|5|6|7|8|9])\\d{8}$";
        public static final String PHONE = "^(\\(\\d{3,4}-)|\\d{3.4}-)?\\d{7,8}$";
        public static final String CHINA_PHONE = "\\d{3}-\\d{8}|\\d{4}-\\d{7}";
        public static final String ID_CARD = "^\\d{15}|\\d{18}$";
        public static final String SHORT_ID_CARD = "^\\d{8,18}|[0-9x]{8,18}|[0-9X]{8,18}?$";
        public static final String ACCOUNT = "^[a-zA-Z][a-zA-Z0-9_]{4,15}$";
        public static final String PASSWORD = "^[a-zA-Z]\\w{5,17}$";
        public static final String STRONG_PASSWORD = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,10}$";
        public static final String DATE_YYYY_MM_DD = "^((((19|20)\\d{2})-(0?[13-9]|1[012])-(0?[1-9]|[12]\\d|30))|(((19|20)\\d{2})-(0?[13578]|1[02])-31)" +
                "|(((19|20)\\d{2})-0?2-(0?[1-9]|1\\d|2[0-8]))|((((19|20)([13579][26]|[2468][048]|0[48]))|(2000))-0?2-29))$";
        public static final String DATE_YYYYMMDD = "^((((19|20)\\d{2})(0?[13-9]|1[012])(0?[1-9]|[12]\\d|30))|(((19|20)\\d{2})(0?[13578]|1[02])31)|(((19|20)\\d{2})0?2(0?[1-9]|1\\d|2[0-8]))" +
                "|((((19|20)([13579][26]|[2468][048]|0[48]))|(2000))0?229))$";
        public static final String DATE_YYYYIMMIDD = "^((((19|20)\\d{2})/(0?[13-9]|1[012])/(0?[1-9]|[12]\\d|30))|(((19|20)\\d{2})/(0?[13578]|1[02])/31)" +
                "|(((19|20)\\d{2})/0?2/(0?[1-9]|1\\d|2[0-8]))|((((19|20)([13579][26]|[2468][048]|0[48]))|(2000))/0?2/29))$";
        public static final String MONTH = "^(0?[1-9]|1[0-2])$";
        public static final String DAY = "^((0?[1-9])|((1|2)[0-9])|30|31)$";
        public static final String MONEY = "^(0|-?[1-9][0-9]*)$";
        public static final String XML_FILE_NAME = "^([a-zA-Z]+-?)+[a-zA-Z0-9]+\\.[x|X][m|M][l|L]$";
        public static final String CHINESE_LANGUAGE = "[\\u4e00-\\u9fa5]";
        public static final String TWO_CHAR = "[^\\x00-\\xff]";
        public static final String QQ = "[1-9][0-9]{4,}";
        public static final String MAIL_NO = "[1-9]\\d{5}(?!\\d)";
        public static final String IP = "((?:(?:25[0-5]|2[0-4]\\d|[01]?\\d?\\d)\\.){3}(?:25[0-5]|2[0-4]\\d|[01]?\\d?\\d))";
        public static final String NUMBER = "^[0-9]*$";
        public static final String FLOAT = "^-?([1-9]\\d*\\.\\d*|0\\.\\d*[1-9]\\d*|0?\\.0+|0)$";
        public static final String ENGLISH_NUMBER = "^[A-Za-z0-9]+$";
        public static final String MAC = "(([A-Fa-f0-9]{2}:)|([A-Fa-f0-9]{2}-)){5}[A-Fa-f0-9]{2}";

        public static final String ORACLE = "jdbc:oracle:thin:@(?<ip>[0-9.a-zA-Z-_]+):(?<port>[0-9]+):(?<name>[\\w]+)";
        public static final String MYSQL = "jdbc:mysql://(?<ip>[0-9.a-zA-Z-_]+):(?<port>[0-9]+)/(?<name>[\\w]+)(?<param>[\\w\\W]*)";
        public static final String SQL_SERVER = "jdbc:jtds:sqlserver://(?<ip>[0-9.a-zA-Z-_]+):(?<port>[0-9]+)/(?<name>[\\w]+)(?<param>[\\w\\W=]+)";
        public static final String SQL_SERVER2005 = "jdbc:sqlserver://(?<ip>[0-9.a-zA-Z-_]+):(?<port>[0-9]+);DatabaseName=(?<name>[\\w]+)";
        public static final String DB2 = "jdbc:db2://(?<ip>[0-9.a-zA-Z-_]+):(?<port>[0-9]+)/(?<name>[\\w]+)";
        public static final String INFORMIX = "jdbc:informix-sqli://(?<ip>[0-9.a-zA-Z-_]+):(?<port>[0-9]+)/(?<name>[\\w]+)";
        public static final String SYBASE = "jdbc:sybase:Tds:(?<ip>[0-9.a-zA-Z-_]+):(?<port>[0-9]+)/(?<name>[\\w]+)";
    }
}
