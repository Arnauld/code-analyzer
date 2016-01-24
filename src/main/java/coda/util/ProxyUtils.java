package coda.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.stream.Stream;

/**
 * @author <a href="http://twitter.com/aloyer">@aloyer</a>
 */
public class ProxyUtils {
    public static <T> T broadcast(Class<T> clazz, T main, T... others) {
        return clazz.cast(Proxy.newProxyInstance(ProxyUtils.class.getClassLoader(),
                new Class[]{clazz},
                (proxy, method, args) -> {
                    Object ret = method.invoke(main, args);
                    Stream.of(others)
                            .forEach((l) -> {
                                try {
                                    method.invoke(l, args);
                                } catch (IllegalAccessException | InvocationTargetException e) {
                                    throw new ProxyDispatchException("Invocation exception on " + l, e);
                                }
                            });
                    return ret;
                }));
    }

    private static class ProxyDispatchException extends RuntimeException {
        public ProxyDispatchException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
