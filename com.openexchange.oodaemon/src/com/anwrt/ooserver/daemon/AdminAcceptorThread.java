
package com.anwrt.ooserver.daemon;

import java.util.Timer;
import com.sun.star.bridge.BridgeExistsException;
import com.sun.star.connection.AlreadyAcceptingException;
import com.sun.star.connection.ConnectionSetupException;
import com.sun.star.connection.XAcceptor;
import com.sun.star.connection.XConnection;
import com.sun.star.uno.UnoRuntime;

/**
 * Accepts connections from admin daemon instances. <br>
 * creation : 23 août 07
 *
 * @author <a href="mailto:oodaemon@extraserv.net">Jounayd Id Salah</a>
 */
public class AdminAcceptorThread extends Thread {

    private final Daemon _daemon;

    private final String _acceptString;

    private XAcceptor _acceptor;

    private Timer _shutdownThread = null;

    /** displayed tag used for logging */
    public static final String ADMIN_LOGGER_TAG = "#ADMIN# ";

    /**
     * Creates an acceptor to further accept connections from admin daemon instances
     *
     * @param daemon the current daemon
     * @param shutdownThread
     * @param acceptString the admin acceptor string (usually passed by the configuration file)
     */
    public AdminAcceptorThread(final Daemon daemon, final Timer shutdownThread, final String acceptString) {
        super();
        _daemon = daemon;
        _acceptString = acceptString;
        _shutdownThread = shutdownThread;

        try {
            final Object acceptorObj =
                daemon.getInitialContext().getServiceManager().createInstanceWithContext(
                    "com.sun.star.connection.Acceptor",
                    daemon.getInitialContext());
            _acceptor = (XAcceptor) UnoRuntime.queryInterface(XAcceptor.class, acceptorObj);
        } catch (final Exception ex) {
            Logger.error(ADMIN_LOGGER_TAG + "Error getting acceptor");
            Logger.debug(ex);
        }
    }

    /**
     * Accept connections
     *
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        Logger.info(ADMIN_LOGGER_TAG + "started");
        while (true) {
            try {
                final XConnection connexion = _acceptor.accept(_acceptString);
                if (connexion == null) {
                    break;
                }
                Logger.debug(ADMIN_LOGGER_TAG + "Accepted admin connection from " + Daemon.extractContactInfo(connexion.getDescription()));
                _daemon.getBridgeFactory().createBridge("", "urp", connexion, new AdminInstanceProvider(_daemon, _shutdownThread));
            } catch (final AlreadyAcceptingException ex) // TODO this must be taken into account for the admin acceptor to work the best manner
            {
                Logger.error(ADMIN_LOGGER_TAG + "Already accepting connection");
                Logger.debug(ex);
            } catch (final ConnectionSetupException ex) {
                if (_daemon.isShutdowned()) {
                    break;
                } else {
                    Logger.error(ADMIN_LOGGER_TAG + ex);
                    Logger.debug(ex);
                }
            } catch (final BridgeExistsException ex) {
                Logger.error(ADMIN_LOGGER_TAG + "Bridge already exists");
                Logger.debug(ex);
            } catch (final com.sun.star.lang.IllegalArgumentException ex) {
                Logger.error(ADMIN_LOGGER_TAG + ex);
                Logger.debug(ex);
            }
        }
        Logger.info(ADMIN_LOGGER_TAG + "terminating");
    }

    /**
     * Called to stop the admin thread
     */
    public void cancel() {
        _acceptor.stopAccepting();
    }
}
