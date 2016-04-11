package kamon.agent.util.log;

import org.pmw.tinylog.Configurator;
import org.pmw.tinylog.Logger;
import org.pmw.tinylog.labelers.TimestampLabeler;
import org.pmw.tinylog.policies.SizePolicy;
import org.pmw.tinylog.policies.StartupPolicy;
import org.pmw.tinylog.writers.ConsoleWriter;
import org.pmw.tinylog.writers.RollingFileWriter;

import java.util.Locale;
import java.util.function.Supplier;

/**
 * Lazy Logger implementing {@link Logger}, which supports lazy evaluation of messages.<br>
 * The message to be logged must be inside a {@link Supplier} which will be evaluated only if the level of debug is enabled.
 */
public class LazyLogger {

    static {
        Configurator.currentConfig()
                .locale(Locale.US)
                .writingThread("main", 1)
                .formatPattern("{date:HH:mm:ss.SSS} [{thread}] {level}:{message}")
                .writer(new ConsoleWriter())
                .addWriter(new RollingFileWriter("log.log", 10, new TimestampLabeler(), new StartupPolicy(), new SizePolicy(10 * 1024)))
                .activate();
    }

    private LazyLogger(){}

    public static void trace(final Supplier<String> msg) {
        org.pmw.tinylog.Logger.trace(msg.get());
    }

    public static void trace(final Supplier<String> msg, Throwable t) {
        org.pmw.tinylog.Logger.trace(msg.get(), t);
    }

    public static void debug(final Supplier<String> msg) {
        org.pmw.tinylog.Logger.debug(msg.get());
    }

    public static void debug(final Object source, final Supplier<String> msg, final Throwable t) {
        org.pmw.tinylog.Logger.debug(msg.get(),t);
    }

    public static void info(final Supplier<String> msg) {
        org.pmw.tinylog.Logger.info(msg.get());
    }

    public static void info(final Supplier<String> msg, final Throwable t) {
        org.pmw.tinylog.Logger.info(msg.get(),t);
    }

    public static void warn(final Supplier<String> msg) {
        org.pmw.tinylog.Logger.warn(msg.get());
    }

    public static void warn(final Supplier<String> msg, final Throwable t) {
        org.pmw.tinylog.Logger.warn(msg.get(), t);
    }

    public static void error(final Supplier<String> msg) {
        org.pmw.tinylog.Logger.error(msg.get());
    }

    public static void error(final Supplier<String> msg, final Throwable t) {
        org.pmw.tinylog.Logger.warn(msg.get(),t);
    }
}
