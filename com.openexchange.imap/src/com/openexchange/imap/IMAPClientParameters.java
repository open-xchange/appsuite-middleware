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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.imap;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import com.openexchange.imap.services.Services;
import com.openexchange.java.Strings;
import com.openexchange.log.LogProperties;
import com.openexchange.session.Session;
import com.openexchange.version.VersionService;
import com.sun.mail.imap.IMAPStore;


/**
 * {@link IMAPClientParameters} - An enumeration for IMAP client parameters passed to IMAP store using <code>"ID"</code> command (if supported)
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum IMAPClientParameters {

    /**
     * The parameter for the client's originating IP address.
     */
    ORIGINATING_IP("x-originating-ip"),
    /**
     * The parameter for the client's session identifier.
     */
    SESSION_ID("x-session-ext-id"),
    /**
     * The parameter for the client's name.
     */
    NAME("name"),
    /**
     * The parameter for the client's version identifier.
     */
    VERSION("xversion"),
    ;

    private final String paramName;

    private IMAPClientParameters(String paramName) {
        this.paramName = paramName;
    }

    /**
     * Gets the parameter name
     *
     * @return The parameter name
     */
    public String getParamName() {
        return paramName;
    }

    // --------------------------------------------------------------------------------------------------------------------

    /**
     * Generates the session information.
     * <pre>
     *  &lt;session-id&gt; + "-" &lt;user-id&gt; + "-" + &lt;context-id&gt; + "-" + &lt;random&gt;
     *
     *  Example:
     *  6ceec6585485458eb27456ad6ec97b62-17-1337-1356782
     * </pre>
     *
     * @param session The user-associated session
     * @return The session information
     */
    public static String generateSessionInformation(Session session) {
        StringBuilder buf = new StringBuilder(64);
        buf.append(session.getSessionID());
        buf.append('-').append(session.getUserId());
        buf.append('-').append(session.getContextId());
        buf.append('-').append(getHashFor(session));
        return buf.toString();
    }

    private static final java.util.concurrent.atomic.AtomicLong NEXT = new java.util.concurrent.atomic.AtomicLong(0);
    private static final byte[] SERVER_ID = intToBytes(com.openexchange.exception.OXException.getServerId());

    private static String getHashFor(Session session) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            long current;
            long next;
            do {
                current = NEXT.get();
                next = current + 1;
                if (next < 0) {
                    next = 0;
                }
            } while (false == NEXT.compareAndSet(current, next));
            md.update(longToBytes(current));
            md.update(SERVER_ID); // Node-unique salt

            return asHex(md.digest(), 8);
        } catch (NoSuchAlgorithmException e) {
            // Ignore
        }

        return new StringBuilder(16).append(session.hashCode()).append(session.getUserId()).append(session.getContextId()).append(System.currentTimeMillis()).toString();
    }

    private static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    /**
     * Turns array of bytes into string representing each byte as unsigned hex number.
     *
     * @param hash Array of bytes to convert to hex-string
     * @return Generated hex string
     */
    private static String asHex(byte[] hash, int len) {
        int length = len <= 0 || len > hash.length ?  hash.length : len;

        char[] buf = new char[length << 1];
        for (int i = 0, x = 0; i < length; i++) {
            buf[x++] = HEX_CHARS[(hash[i] >>> 4) & 0xf];
            buf[x++] = HEX_CHARS[hash[i] & 0xf];
        }
        return new String(buf);
    }

    private static byte[] longToBytes(long value) {
        byte[] result = new byte[8];
        long l = value;
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (l & 0xFF);
            l >>= 8;
        }
        return result;
    }

    private static byte[] intToBytes(int value) {
        return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value };
    }

    /**
     * Generates the session information.
     * <pre>
     *  &lt;session-id&gt; + "-" &lt;user-id&gt; + "-" + &lt;context-id&gt; + "-" + &lt;random&gt;
     *
     *  Example:
     *  6ceec6585485458eb27456ad6ec97b62-17-1337-1356782
     * </pre>
     *
     * @param session The user-associated session
     * @param imapStore The IMAP store
     * @return The session information
     */
    public static String generateSessionInformation(Session session, IMAPStore imapStore) {
        return generateSessionInformation(session);
    }

    private static final class Generator implements com.sun.mail.imap.ExternalIdGenerator {

        private final Session session;

        Generator(Session session) {
            super();
            this.session = session;
        }

        @Override
        public String generateExternalId() {
            String imapSessionId = generateSessionInformation(session);
            LogProperties.put(LogProperties.Name.MAIL_SESSION, imapSessionId);
            return imapSessionId;
        }
    }

    private static final String LOCAL_HOST;
    static {
        String fbHost;
        try {
            fbHost = InetAddress.getLocalHost().getHostAddress();
        } catch (final UnknownHostException e) {
            fbHost = "127.0.0.1";
        }
        LOCAL_HOST = fbHost;
    }

    /**
     * Sets the default client parameters.
     *
     * @param imapStore The IMAP store to connect to
     * @param session The associated Groupware session
     */
    public static void setDefaultClientParameters(IMAPStore imapStore, Session session) {
        // Set generator
        imapStore.setExternalIdGenerator(new Generator(session));

        // Generate & set client parameters
        Map<String, String> clientParams = new LinkedHashMap<String, String>(6);
        String localIp = session.getLocalIp();
        clientParams.put(IMAPClientParameters.ORIGINATING_IP.getParamName(), Strings.isEmpty(localIp) ? LOCAL_HOST : localIp);
        clientParams.put(IMAPClientParameters.NAME.getParamName(), "Open-Xchange");
        clientParams.put(IMAPClientParameters.VERSION.getParamName(), Services.getService(VersionService.class).getVersionString());
        imapStore.setClientParameters(clientParams);
    }

}
