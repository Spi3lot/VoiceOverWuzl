package client;

/**
 * @author Emilio Zottel (4AHIF)
 * @since 08.11.2022, Di.
 */
@FunctionalInterface
public interface BiFunctionWithException<T, U, R> {

    R apply(T t, U u) throws Exception;

}
