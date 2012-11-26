

package com.openexchange.authentication.oxio.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.security.auth.login.LoginException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.authentication.LoginInfo;
import com.openexchange.exception.OXException;

/**
 * This Auth Plugin is used for our demo system which runs in switzerland.
 * It takes the email string and the password string and tries to authenticate with the
 * userpart of the email and the given password against the imap server. The context
 * name and the username is always the same. Both are the userpart of the email address.
 * So the contexts must be created with the context name set to "foo" and loginmappings
 * with "foo" and "foo-1" a user called "foo" and a user called "foo-1". ONLY when these
 * requirements are met, the PLUGIN will work.
 * 
 * @author Manuel Kraft
 */
public class OXIOAuthentication implements AuthenticationService {

    private static final Log LOG = LogFactory.getLog(OXIOAuthentication.class);

    private static String DEFAULTCONTEXT = null;

    private static String IMAP_HOST = null;

    private static int IMAP_PORT;

    private static Properties props;

    private final static String OXIO_AUTH_PROPERTY_FILE = "/opt/open-xchange/etc/oxioauth.properties";

    /**
     * Default constructor.
     * @throws OXException 
     */
    public OXIOAuthentication() throws OXException {
        super();
        if (null == props) {
            initConfig();
        }
        DEFAULTCONTEXT = props.getProperty("DEFAULTCONTEXT");
        IMAP_HOST = props.getProperty("IMAP_HOST");
        IMAP_PORT = Integer.parseInt(props.getProperty("IMAP_PORT"));
    }

    /**
     * {@inheritDoc}
     */
    public Authenticated handleLoginInfo(final LoginInfo loginInfo) throws OXException {

        Session session = null;
        Store imapconnection = null;
        String imap_username = null;

        try {

            final String gui_loginstring = loginInfo.getUsername();

            final String[] gui_loginstring_splitted = split(gui_loginstring);

            final String uid = gui_loginstring_splitted[1];
            final String password = loginInfo.getPassword();

            if ("".equals(uid.trim()) || "".equals(password.trim())) {
                throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
            }

            imap_username = uid;
            session = Session.getDefaultInstance(new Properties(), null);
            session.setDebug(false);

            LOG.debug("Using : " + IMAP_HOST + ":" + IMAP_PORT + " as imap hostname!");
            LOG.debug("Using : " + imap_username + " as imap user!");
            LOG.debug("Using : " + uid + " as ox user!");
            LOG.debug("Using : " + uid + " as context!");

            imapconnection = session.getStore("imap");

            // try to connect with the credentials set above
            imapconnection.connect(IMAP_HOST, IMAP_PORT, imap_username, password);

            final String oxcontext_ = uid;
            final String oxuser_ = uid;

            return new Authenticated() {

                public String getContextInfo() {
                    return oxcontext_;
                }

                public String getUserInfo() {
                    return oxuser_;
                }
            };
        } catch (final AuthenticationFailedException afe) {
            LOG.debug("Imap login failed for user " + imap_username + " on server " + IMAP_HOST, afe);
            throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
        } catch (final NoSuchProviderException e) {
            LOG.error("Error setup initial imap envorinment!", e);
            throw LoginExceptionCodes.COMMUNICATION.create();
        } catch (final MessagingException e) {
            LOG.error("Error setup imap connection!", e);
            throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
        } finally {
            try {
                if (imapconnection != null) {
                    imapconnection.close();
                }
            } catch (final MessagingException e) {
                LOG.error("Error closing imap connection!", e);
                throw LoginExceptionCodes.COMMUNICATION.create();
            }
        }
    }

    /**
     * Splits user name and context.
     * 
     * @param loginInfo combined information seperated by an @ sign.
     * @return a string array with context and user name (in this order).
     * @throws LoginException if no seperator is found.
     */
    private String[] split(final String loginInfo) {
        return split(loginInfo, '@');
    }

    /**
     * Splits user name and context.
     * 
     * @param loginInfo combined information seperated by an @ sign.
     * @param separator for spliting user name and context.
     * @return a string array with context and user name (in this order).
     * @throws LoginException if no seperator is found.
     */
    private String[] split(final String loginInfo, final char separator) {
        final int pos = loginInfo.lastIndexOf(separator);
        final String[] splitted = new String[2];
        if (-1 == pos) {
            splitted[1] = loginInfo;
            splitted[0] = DEFAULTCONTEXT;
        } else {
            splitted[1] = loginInfo.substring(0, pos);
            splitted[0] = loginInfo.substring(pos + 1);
        }
        return splitted;
    }

    private static void initConfig() throws OXException {
        synchronized (OXIOAuthentication.class) {
            if (null == props) {
                final File file = new File(OXIO_AUTH_PROPERTY_FILE);
                if (!file.exists()) {
                    throw com.openexchange.configuration.ConfigurationExceptionCodes.FILE_NOT_FOUND.create(file.getAbsolutePath());
                }
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(file);
                    props = new Properties();
                    props.load(fis);
                } catch (final IOException e) {
                    throw com.openexchange.configuration.ConfigurationExceptionCodes.NOT_READABLE.create(file.getAbsolutePath());
                } finally {
                    if (null != fis) {
                        try {
                            fis.close();
                        } catch (final IOException e) {
                            LOG.error("Error closing file inputstream for file " + OXIO_AUTH_PROPERTY_FILE + " ", e);
                        }
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.authentication.AuthenticationService#handleAutoLoginInfo(com.openexchange.authentication.LoginInfo)
     */
    public Authenticated handleAutoLoginInfo(LoginInfo loginInfo) throws OXException {
        throw LoginExceptionCodes.NOT_SUPPORTED.create(OXIOAuthentication.class.getName());
    }

}
