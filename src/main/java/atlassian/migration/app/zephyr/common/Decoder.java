package atlassian.migration.app.zephyr.common;

@FunctionalInterface
public interface Decoder {

    String decode(byte[] encodedData) throws Exception;
}
