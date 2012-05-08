
package com.openexchange.custom.dynamicnet.impl;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.authentication.LoginInfo;
import com.openexchange.exception.OXException;

public class DynamicNetAuthentication implements AuthenticationService {

    private static final Log LOG = LogFactory.getLog(DynamicNetAuthentication.class);
    private static String DEFAULTCONTEXT = "defaultcontext";

    // NOT NEEDED CURRENTLY
    // private static Properties props;
    // private final static String DN_AUTH_PROPERTY_FILE = "/opt/open-xchange/etc/authplugin.properties";

    /**
     * Default constructor.
     */
    public DynamicNetAuthentication() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Authenticated handleLoginInfo(final LoginInfo loginInfo)
    throws OXException {

        Session session = null;
        Store imapconnection = null;
        String imap_username = null;
        String imaphostname = null;
        String context = null;
        try {

            // CURRENTLY NOT NEEDED!!!
            //			if (props == null) {
            //				initConfig();
            //			}

            // user will login with :
            // web1p1@alpha
            // Admin will login with:
            // web1@alpha

            final String[] splitted = split(loginInfo.getUsername());

            imaphostname = splitted[0]; // hostname of the plesk
            // and the imap server.
            // Example: alpha
            if(imaphostname.equals(DEFAULTCONTEXT)){
                LOG.debug("No @<SERVER> was specified at login!This is mandatory!");
                throw LoginExceptionCodes.INVALID_CREDENTIALS.create(); // LOGIN WITHOUT HOSTNAME IS NOT PERMITTED!
            }else{
                if(imaphostname.indexOf(".")==-1){
                    // keine domain angegeben, wir setzen imap server hostname zusammen
                    context = imaphostname;
                    imaphostname = imaphostname+".ibone.ch";
                    LOG.debug("Created FQDN as imap server: "+imaphostname);
                }else{
                    context = imaphostname;
                    LOG.debug("Using FQDN from login as imap server: "+imaphostname);
                }
            }


            final String uid = splitted[1]; // username for user or admin.
            // Example: web1p1
            final String password = loginInfo.getPassword();
            if ("".equals(uid.trim()) || "".equals(password.trim())) {
                throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
            }

            final Pattern p = Pattern.compile("(web(?:[0-9]+))(p(?:[0-9]+))?");
            final Matcher m = p.matcher(uid);


            String customer = null;
            String ox_username = null;

            if(!m.matches()){
                // illegal login string
                LOG.debug("Invalid login entered! "+uid);
                throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
            }else{
                customer = m.group(1);
                ox_username = m.group(2);
                if(ox_username==null){
                    // admin login
                    ox_username = customer;
                    imap_username = ox_username;
                }else{
                    imap_username = customer+ox_username;
                }
            }


            session = Session.getDefaultInstance(new Properties(), null);
            session.setDebug(false);

            LOG.debug("Using : " + imaphostname + " as imap hostname!");
            LOG.debug("Using : " + imap_username + " as imap user!");
            LOG.debug("Using : " + ox_username + " as ox user!");
            LOG.debug("Using : " + customer + " as customer!");

            imapconnection = session.getStore("imap");

            // try to connect with the credentials set above
            imapconnection.connect(imaphostname,143,imap_username,password);

            final String oxcontext_ = customer+"_"+context;// context_or_domain;  ITS THE web1_alpha
            final String oxuser_ =	ox_username;

            return new Authenticated() {
                @Override
                public String getContextInfo() {
                    return oxcontext_;
                }

                @Override
                public String getUserInfo() {
                    return oxuser_;
                }
            };
            //		} catch (ConfigurationException e) {
            //			LOG.error("Error reading auth plugin config!", e);
            //			throw LoginExceptionCodes..COMMUNICATION, e);
        }catch (final AuthenticationFailedException afe){
            LOG.debug("Imap login failed for user "+imap_username+" on server "+imaphostname, afe);
            throw LoginExceptionCodes.INVALID_CREDENTIALS.create(afe);
        } catch (final NoSuchProviderException e) {
            LOG.error("Error setup initial imap envorinment!", e);
            throw LoginExceptionCodes.COMMUNICATION.create(e);
        } catch (final MessagingException e) {
            LOG.error("Error setup imap connection!", e);
            throw LoginExceptionCodes.INVALID_CREDENTIALS.create(e);
        } finally {
            try {
                if (imapconnection != null) {
                    imapconnection.close();
                }
            } catch (final MessagingException e) {
                LOG.error("Error closing imap connection!", e);
                throw LoginExceptionCodes.COMMUNICATION.create(e);
            }
        }
    }
    /*
     * CURRENTLY NOT NEEDED
	private static void initConfig() throws ConfigurationException {
		synchronized (DynamicNetAuthentication.class) {
			if (null == props) {
				final File file = new File(DN_AUTH_PROPERTY_FILE);
				if (!file.exists()) {
					throw new ConfigurationException(
							com.openexchange.configuration.ConfigurationException.Code.FILE_NOT_FOUND,
							file.getAbsolutePath());
				}
				FileInputStream fis = null;
				try {
					fis = new FileInputStream(file);
					props = new Properties();
					props.load(fis);
				} catch (IOException e) {
					throw new ConfigurationException(
							com.openexchange.configuration.ConfigurationException.Code.NOT_READABLE,
							file.getAbsolutePath());
				} finally {
					try {
						fis.close();
					} catch (IOException e) {
						LOG.error("Error closing file inputstream for file "
								+ DN_AUTH_PROPERTY_FILE + " ", e);
					}
				}
			}
		}
	}
     */

    /**
     * Splits user name and context.
     * 
     * @param loginInfo
     * combined information seperated by an @ sign.
     * @return a string array with context and user name (in this order).
     * @throws OXException
     *             if no seperator is found.
     */
    private String[] split(final String loginInfo) throws OXException {
        return split(loginInfo, '@');
    }

    /**
     * Splits user name and context.
     * 
     * @param loginInfo
     * combined information seperated by an @ sign.
     * @param separator
     *            for spliting user name and context.
     * @return a string array with context and user name (in this order).
     * @throws OXException
     *             if no seperator is found.
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

}
