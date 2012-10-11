
package com.openexchange.control.console;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXPrincipal;
import javax.management.remote.JMXServiceURL;
import javax.security.auth.Subject;
import org.apache.commons.codec.binary.Base64;
import com.openexchange.control.console.internal.ConsoleException;

/**
 * {@link AbstractJMXHandler} - This class contains the JMX stuff for the command line tools
 *
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public abstract class AbstractJMXHandler {

    protected static final class AbstractConsoleJMXAuthenticator implements JMXAuthenticator {

        private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(AbstractConsoleJMXAuthenticator.class));

        private static volatile Charset US_ASCII;

        private final String[] credentials;

        public AbstractConsoleJMXAuthenticator(final String[] credentials) {
            super();
            this.credentials = new String[credentials.length];
            System.arraycopy(credentials, 0, this.credentials, 0, credentials.length);
        }

        private static Charset getUSASCII() {
            if (US_ASCII == null) {
                synchronized (AbstractConsoleJMXAuthenticator.class) {
                    if (US_ASCII == null) {
                        US_ASCII = Charset.forName("US-ASCII");
                    }
                }
            }
            return US_ASCII;
        }

        private static String makeSHAPasswd(final String raw) {
            MessageDigest md;

            try {
                md = MessageDigest.getInstance("SHA-1");
            } catch (final NoSuchAlgorithmException e) {
                LOG.error(e.getMessage(), e);
                return raw;
            }

            final byte[] salt = {};

            md.reset();
            try {
                md.update(raw.getBytes("UTF-8"));
            } catch (final UnsupportedEncodingException e) {
                /*
                 * Cannot occur
                 */
                LOG.error(e.getMessage(), e);
            }
            md.update(salt);

            final String ret = getUSASCII().decode(ByteBuffer.wrap(Base64.encodeBase64(md.digest()))).toString();

            return ret;
        }

        @Override
        public Subject authenticate(final Object credentials) {
            if (!(credentials instanceof String[])) {
                if (credentials == null) {
                    throw new SecurityException("Credentials required");
                }
                throw new SecurityException("Credentials should be String[]");
            }
            final String[] creds = (String[]) credentials;
            if (creds.length != 2) {
                throw new SecurityException("Credentials should have 2 elements");
            }
            /*
             * Perform authentication
             */
            final String username = creds[0];
            final String password = creds[1];
            if ((this.credentials[0].equals(username)) && (this.credentials[1].equals(makeSHAPasswd(password)))) {
                return new Subject(true, Collections.singleton(new JMXPrincipal(username)), Collections.EMPTY_SET, Collections.EMPTY_SET);
            }
            throw new SecurityException("Invalid credentials");

        }

    }

    protected static final String DEFAULT_HOST = "localhost";

    protected final static int DEFAULT_PORT = 9999;

    private JMXConnector jmxConnector;

    private MBeanServerConnection mBeanServerConnection;

    private ObjectName objectName;

    protected final void close() throws ConsoleException {
        try {
            if (jmxConnector != null) {
                jmxConnector.close();
            }
        } catch (final Exception exc) {
            throw new ConsoleException(exc);
        }
    }

    protected MBeanServerConnection getMBeanServerConnection() {
        return mBeanServerConnection;
    }

    protected ObjectName getObjectName() {
        return objectName;
    }

    protected final void initJMX(final String jmxHost, final int jmxPort, final String jmxLogin, final String jmxPassword) throws IOException, MalformedObjectNameException, NullPointerException {
        final JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + jmxHost + ":" + jmxPort + "/server");

        final Map<String, Object> environment;
        if (jmxLogin == null || jmxPassword == null) {
            environment = null;
        } else {
            environment = new HashMap<String, Object>(1);
            environment.put(JMXConnectorServer.AUTHENTICATOR, new AbstractConsoleJMXAuthenticator(new String[] { jmxLogin, jmxPassword }));
        }

        jmxConnector = JMXConnectorFactory.connect(url, environment);

        mBeanServerConnection = jmxConnector.getMBeanServerConnection();

        objectName = ObjectName.getInstance("com.openexchange.control", "name", "Control");
    }

}
