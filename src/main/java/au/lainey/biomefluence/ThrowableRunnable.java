package au.lainey.biomefluence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public interface ThrowableRunnable<T extends Throwable> {
    Logger LOGGER = LoggerFactory.getLogger(Biomefluence.MOD_ID);

    void run() throws T;

    static <T extends Exception> void wrap(ThrowableRunnable<T> runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            LOGGER.error("Exception caught", e);
        }
    }
}
