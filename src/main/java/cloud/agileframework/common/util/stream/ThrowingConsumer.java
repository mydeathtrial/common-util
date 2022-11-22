package cloud.agileframework.common.util.stream;

import java.util.function.Consumer;

@FunctionalInterface
public interface ThrowingConsumer<T> extends Consumer<T> {
    default void accept(T input) {
        try {
            this.acceptThrows(input);
        } catch (AssertionError | RuntimeException var3) {
            throw var3;
        } catch (Throwable var4) {
            throw new RuntimeException(var4);
        }
    }

    void acceptThrows(T var1) throws Throwable;
}