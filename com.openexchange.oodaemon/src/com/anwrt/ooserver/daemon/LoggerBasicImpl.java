
package com.anwrt.ooserver.daemon;

/**
 * Basic Logger implementation that throw everything to standard outputs.<br>
 * Use it as an example if you want to make something more inventive. <br>
 * creation : 28 août 07
 *
 * @author <a href="mailto:oodaemon@extraserv.net">Jounayd Id Salah</a>
 */
public class LoggerBasicImpl extends Logger {

    protected static final String TAG_INFO = "[ info  ]";

    protected static final String TAG_WARNING = "[ warn  ]";

    protected static final String TAG_ERROR = "[ ERROR ]";

    protected static final String TAG_FATAL = "[ FATAL ]";

    protected static final String TAG_DEBUG = "[ debug ]";

    protected static String getMessageString(final String tag, final String msg) {
        return tag + " " + msg;
    }

    @Override
    protected void infoImpl(final String msg) {
        System.out.println(getMessageString(TAG_INFO, msg));
    }

    @Override
    protected void warningImpl(final String msg) {
        if (level >= WARNING)
            System.out.println(getMessageString(TAG_WARNING, msg));
    }

    @Override
    protected void debugImpl(final String msg) {
        System.out.println(getMessageString(TAG_DEBUG, msg));
    }

    @Override
    protected void debugImpl(final String msg, final Exception ex) {
        debugImpl(msg);
        debugImpl(ex);
    }

    @Override
    protected void debugImpl(final Exception ex) {
        exception(ex);
    }

    @Override
    protected void detailedDebugImpl(final Exception ex) {
        exception(ex);
    }

    @Override
    protected void errorImpl(final String msg) {
        System.err.println(getMessageString(TAG_ERROR, msg));
    }

    @Override
    protected void fatalErrorImpl(final String msg) {
        System.err.println(getMessageString(TAG_FATAL, msg));
    }

    @Override
    protected void fatalErrorImpl(final String msg, final Exception ex) {
        System.err.println(getMessageString(TAG_FATAL, msg));
        exception(ex);
    }

    protected static void exception(final Exception ex) {
        ex.printStackTrace();
    }
}
