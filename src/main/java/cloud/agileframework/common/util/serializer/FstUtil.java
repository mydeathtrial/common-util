package cloud.agileframework.common.util.serializer;

import org.nustaq.serialization.FSTConfiguration;

/**
 * @author 佟盟
 * 日期 2021-08-14 19:20
 * 描述 Fst序列化工具
 * @version 1.0
 * @since 1.0
 */
public final class FstUtil {
	private FstUtil() {
	}

	private static final FSTConfiguration CONF = FSTConfiguration.createDefaultConfiguration();

	public static byte[] serialize(Object object) {
		return CONF.asByteArray(object);
	}

	public static Object deserialize(byte[] bytes) {
		return CONF.asObject(bytes);
	}
}
