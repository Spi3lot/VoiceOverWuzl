package server;

/**
 * @author : Emilio Zottel (4AHIF)
 * @since : 08.11.2022, Di.
 **/
@FunctionalInterface
public interface RunnableWithException {

    void run() throws Exception;

}
