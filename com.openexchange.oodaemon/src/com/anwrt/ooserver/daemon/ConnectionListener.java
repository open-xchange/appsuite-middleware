
package com.anwrt.ooserver.daemon;

import com.sun.star.io.XStreamListener;
import com.sun.star.lib.uno.helper.ComponentBase;

/**
 * Waits for a connection to stop. When the connection is lost or finished, the OfficeProcess corresponding is notified and added back to
 * the ready process pool. <br>
 * creation : 28 août 07
 *
 * @author <a href="mailto:oodaemon@extraserv.net">Jounayd Id Salah</a>
 */
public class ConnectionListener extends ComponentBase implements XStreamListener {

    private OfficeProcess _officeProcess = null;

    private final String _conDesc;

    private final ProcessPool _processPool;

    public ConnectionListener(final ProcessPool processPool, final OfficeProcess officeProcess, final String conDesc) {
        _processPool = processPool;
        _officeProcess = officeProcess;
        _conDesc = conDesc;
    }

    /**
     * End usage and add back to the ready process pool
     */
    public void clear() {
        if (_officeProcess != null) {
            Logger.info(_conDesc + " disconnects from " + _officeProcess + " (used for  " + Math.round(_officeProcess.getUsageDuration() * 0.001) + "s) ");
            _officeProcess.endUsage();
            new PoolAdderThread(_processPool, _officeProcess).start();
            _officeProcess = null;
        }
    }

    @Override
    public void started() { /* DO NOTHING */
    }

    @Override
    public void closed() {
        clear();
    }

    @Override
    public void terminated() {
        clear();
    }

    @Override
    public void error(final Object obj) {
        clear();
    }

    /**
     * This is a callback method used to inform that the remote bridge has gone down Receives a notification about the connection has been
     * closed.
     *
     * @param source ...
     */
    @Override
    public void disposing(final com.sun.star.lang.EventObject source) {
        /* DO NOTHING */
    }
}
