/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.zmal.config;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.internet.IDNA;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.mortbay.log.Log;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.api.IMailProperties;
import com.openexchange.mail.api.MailCapabilities;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.session.Session;
import com.openexchange.tools.net.URIDefaults;
import com.openexchange.tools.net.URIParser;
import com.openexchange.zmal.Services;
import com.openexchange.zmal.ZmalCapabilities;
import com.openexchange.zmal.ZmalException;
import com.openexchange.zmal.utils.Preauth;

/**
 * {@link ZmalConfig} - The Zimbra mail configuration.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ZmalConfig extends MailConfig {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.loggerFor(ZmalConfig.class);

    private static final String PROTOCOL_ZMAL_SECURE = "zmals";

    /**
     * The preauth information object.
     */
    public static final class PreauthInfo {
        public final long expires;
        public final long timestamp;
        public final String preauth;

        /**
         * Initializes a new {@link PreauthInfo}.
         * 
         * @param preauth The computed computed
         * @param timestamp The time stamp
         * @param expires The expires
         */
        public PreauthInfo(String preauth, long timestamp, long expires) {
            super();
            this.preauth = preauth;
            this.timestamp = timestamp;
            this.expires = expires;
        }
    }

    // private final int accountId;

    private volatile ZmalCapabilities zmalCapabilities;

    private volatile Map<String, String> capabilities;

    private int port;

    private String server;

    private boolean secure;

    private IZmalProperties mailProperties;

    private InetAddress zmalServerAddress;

    private InetSocketAddress zmalServerSocketAddress;

    private final Map<String, Object> params;

    private PreauthInfo preauth;

    /**
     * Default constructor
     *
     * @param accountId The account identifier
     */
    public ZmalConfig(final int accountId) {
        super();
        this.accountId = accountId;
        params = new NonBlockingHashMap<String, Object>(4);
    }
    
    /**
     * Gets the preauth
     *
     * @return The preauth
     */
    public PreauthInfo getPreauth() {
        return preauth;
    }

    /**
     * Sets specified parameter. If value is <code>null</code> a remove is performed.
     *
     * @param name The name
     * @param value The value
     */
    public void setParameter(final String name, final Object value) {
        if (null == value) {
            params.remove(name);
        } else {
            params.put(name, value);
        }
    }

    /**
     * Gets the named parameter.
     *
     * @param name The name
     * @param clazz The parameter's type
     * @return The value
     */
    public <V> V getParameter(final String name, final Class<? extends V> clazz) {
        return clazz.cast(params.get(name));
    }

    @Override
    public MailCapabilities getCapabilities() {
        final ZmalCapabilities capabilities = zmalCapabilities;
        return capabilities == null ? MailCapabilities.EMPTY_CAPS : capabilities;
    }

    /**
     * Gets the Zimbra MAL capabilities.
     *
     * @return The Zimbra MAL capabilities
     */
    public ZmalCapabilities getZmalCapabilities() {
        return zmalCapabilities;
    }

    /**
     * Gets the (unmodifiable) map view of the Zimbra MAL capabilities.
     *
     * @return The (unmodifiable) map containing Zimbra MAL capabilities
     */
    public Map<String, String> asMap() {
        return capabilities;
    }

    /**
     * Gets the imapPort
     *
     * @return the imapPort
     */
    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void setPort(final int imapPort) {
        this.port = imapPort;
    }

    /**
     * Gets the imapServer
     *
     * @return the imapServer
     */
    @Override
    public String getServer() {
        return server;
    }

    @Override
    public void setServer(final String imapServer) {
        this.server = null == imapServer ? null : IDNA.toUnicode(imapServer);
    }

    /**
     * Initializes Zimbra mail server's capabilities if not done, yet
     */
    public void initializeCapabilities() {
        ZmalCapabilities zmalCapabilities = this.zmalCapabilities;
        if (zmalCapabilities == null) {
            synchronized (this) {
                zmalCapabilities = this.zmalCapabilities;
                if (zmalCapabilities == null) {
                    zmalCapabilities = new ZmalCapabilities();
                    zmalCapabilities.setACL(true);
                    zmalCapabilities.setHasSubscription(true);
                    zmalCapabilities.setSort(true);
                    zmalCapabilities.setThreadReferences(true);
                    this.zmalCapabilities = zmalCapabilities;
                    return;
                }
            }
        }
    }

    /**
     * Checks if Zimbra mail sort is configured and corresponding capability is available.
     *
     * @return <code>true</code> if Zimbra mail sort is configured and corresponding capability is available; otherwise <code>false</code>
     */
    public boolean isZmalSort() {
        final ZmalCapabilities capabilities = zmalCapabilities;
        return (capabilities != null) ? (capabilities.hasSort()) : false;
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    public void setSecure(final boolean secure) {
        this.secure = secure;
    }

    @Override
    protected void parseServerURL(final String serverURL) throws OXException {
        final URI uri;
        try {
            uri = URIParser.parse(serverURL, URIDefaults.IMAP);
        } catch (final URISyntaxException e) {
            throw ZmalException.Code.URI_PARSE_FAILED.create(e, serverURL);
        }
        secure = PROTOCOL_ZMAL_SECURE.equals(uri.getScheme());
        server = uri.getHost();
        port = uri.getPort();
    }

    @Override
    protected boolean doCustomParsing(MailAccount account, Session session) throws OXException {
        if (MailAccount.DEFAULT_ID != account.getId()) {
            throw MailExceptionCode.CONFIG_ERROR.create("Zimbra mail account must be the primary one.");
        }
        // Gather needed data
        try {
            /*-
             * COMCAST_USERNAME
             * COMCAST_TIMESTAMP
             * COMCAST_EXPIRES
             */
            final long timestamp = ((Long) session.getParameter("COMCAST_USERNAME")).longValue();
            final long expires = ((Long) session.getParameter("COMCAST_EXPIRES")).longValue();
            final String userName = (String) session.getParameter("COMCAST_USERNAME");
            final ConfigurationService service = Services.getService(ConfigurationService.class);
            final String key = service.getProperty("com.openexchange.zmal.preauth.key");
            // Preauth information
            final Map<String, String> preauthParams = new HashMap<String, String>(4);
            preauthParams.put("account", login);
            preauthParams.put("by", "name"); // needs to be part of hmac
            preauthParams.put("timestamp", Long.toString(timestamp));
            preauthParams.put("expires", Long.toString(expires));
            String preauth = Preauth.computePreAuth(preauthParams, key);
            this.preauth = new PreauthInfo(preauth, timestamp, expires);
        } catch (final Exception e) {
            Log.warn("Unable to generate Zimbra preauth.", e);
            this.preauth = null;
        }
        
        // TODO: Delete
        // this.password = "password";
        
        return true;
    }

    private String computePreAuth(final String username, final String by, final long expires, final long ts, final String key, Properties properties) {
        StringBuilder ret = new StringBuilder();
        String input = username + "|" + by + "|" + expires + "|" + ts;
        try {
            String hmacSha = (String) properties.get("hmacSha");
            Mac mac = Mac.getInstance(hmacSha);
            SecretKeySpec secret = new SecretKeySpec(key.getBytes(), hmacSha);
            mac.init(secret);
            byte[] digest = mac.doFinal(input.getBytes());
            for (byte b : digest) {
                String tokenByte = String.format("%02x", b);
                ret.append(tokenByte);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return ret.toString();
    }

    /**
     * Gets the internet address of the Zimbra mail server.
     *
     * @return The internet address of the Zimbra mail server.
     * @throws OXException If Zimbra mail server cannot be resolved
     */
    public InetAddress getZmalServerAddress() throws OXException {
        if (null == zmalServerAddress) {
            try {
                zmalServerAddress = InetAddress.getByName(server);
                // TODO: Touch address for proper equality check?
                // imapServerAddress.toString();
            } catch (final UnknownHostException e) {
                throw ZmalException.Code.IO_ERROR.create(e, e.getMessage());
            }
        }
        return zmalServerAddress;
    }

    /**
     * Gets the socket address (internet address + port) of the Zimbra mail server.
     *
     * @return The socket address (internet address + port) of the Zimbra mail server.
     * @throws OXException If Zimbra mail server cannot be resolved
     */
    public InetSocketAddress getZmalServerSocketAddress() throws OXException {
        if (null == zmalServerSocketAddress) {
            zmalServerSocketAddress = new InetSocketAddress(getZmalServerAddress(), port);
        }
        return zmalServerSocketAddress;
    }

    @Override
    public IMailProperties getMailProperties() {
        return mailProperties;
    }

    public IZmalProperties getZmalProperties() {
        return mailProperties;
    }

    @Override
    public void setMailProperties(final IMailProperties mailProperties) {
        this.mailProperties = (IZmalProperties) mailProperties;
    }

}
