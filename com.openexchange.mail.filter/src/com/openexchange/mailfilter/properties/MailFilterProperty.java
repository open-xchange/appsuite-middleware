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

package com.openexchange.mailfilter.properties;

/**
 * {@link MailFilterProperty}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum MailFilterProperty {

    /**
     * Specify which sieve server should be used. Two options are allowed here:
     * user : use the imap server setting stored for user in the database
     * global : use the sieve server given in this config file for all users
     */
    SIEVE_LOGIN_TYPE("user", MailFilterProperty.EMPTY),

    /**
     * Specify which sieve credentials should be use. Two options are allowed here:
     * "session" : login name and password are used from the current session
     * "session-full-login" : full login (incl. context part) name and password are used from the current session
     * "imapLogin" : the login name is taken from the field imapLogin of the current
     * user the password is taken from the current session
     * "mail" : use the primary mail address of the user and the password from the
     * session
     */
    SIEVE_CREDSRC("session", MailFilterProperty.EMPTY),

    /**
     * To override the sieve server defaults specify a value for the sieve server here
     */
    SIEVE_SERVER("localhost", MailFilterProperty.EMPTY),

    /**
     * Specify the SIEVE port
     * -----------------------------------------------------------
     * NOTE: 2000 is the deprecated port number for SIEVE (now assigned to some Cisco SCCP protocol by the IANA)
     * 4190 is the new one used with most recent Linux and IMAP implementations.
     * Please check your system's default port defined at /etc/services.
     * -----------------------------------------------------------
     */
    SIEVE_PORT(4190, MailFilterProperty.EMPTY),

    /**
     * If you want the script to be generated with another script name change it here.
     * Note that the mail filter bundle will then leave the old script with the old
     * script name behind, and doesn't delete it
     */
    SCRIPT_NAME("Open-Xchange", MailFilterProperty.EMPTY),

    /**
     * Define the charset encoding to use for authentication to sieve server
     */
    SIEVE_AUTH_ENC("UTF-8", MailFilterProperty.EMPTY),

    /**
     * Define the regex which recognizes servers with incorrect sieve TLS implementation
     */
    NON_RFC_COMPLIANT_TLS_REGEX("^Cyrus.*v([0-1]\\.[0-9].*|2\\.[0-2].*|2\\.3\\.[0-9]|2\\.3\\.[0-9][^0-9].*)$", MailFilterProperty.EMPTY),

    /**
     * Whether to use TLS if available
     */
    TLS(true, MailFilterProperty.EMPTY),

    /**
     * Specify here if vacation messages should only be sent to specific domains
     * If multiple domains are given, they should be separated by ","
     * e.g. VACATION_DOMAINS=testdomain.com,example.com
     */
    VACATION_DOMAINS(MailFilterProperty.EMPTY, MailFilterProperty.EMPTY),

    /**
     * Specifies when the connection should time out (value in milliseconds)
     */
    connectionTimeout(30000),

    /**
     * Set the password source; meaning which source is taken to determine a user's
     * password to login into mail filter system. If 'session' is set, then user's individual
     * system's password is taken. If 'global' is set, then the value specified through
     * property 'com.openexchange.mail.filter.masterPassword' is taken.
     * Currently known values: session and global
     */
    passwordSource("session"),

    /**
     * The master password for mail/transport server. Only takes effect when property
     * "com.openexchange.mail.filter.passwordSource" is set to "global"
     */
    masterPassword,

    /**
     * <p>This property defines if mailbox names shall be UTF-7 encoded as specified in
     * RFC2060; section 5.1.3. "Mailbox International Naming Convention".
     * Default is "false"; meaning no UTF-7 encoding is performed.</p>
     * 
     * <p>Set to "true" for those Cyrus IMAP server versions that do NOT support
     * "sieve_utf8fileinto" property (e.g. lower than v2.3.11) Set to "true" for those
     * Cyrus IMAP server versions that support "sieve_utf8fileinto" property having that
     * property set to "0". Thus moving mails with the 'fileinto' command will properly
     * work for mailbox names that contain non-ascii characters</p>
     */
    useUTF7FolderEncoding(false),

    /**
     * Enable punycode encoding for the username used in authentication
     * against the managesieve server
     */
    punycode(false),

    /**
     * Interpret SIEVE Response Codes, see https://tools.ietf.org/html/rfc5804#section-1.3
     * in most cases, this option must be kept to false
     */
    useSIEVEResponseCodes(false),

    /**
     * Specify a comma-separated list of domains (wild-card syntax supported) that are allowed
     * for redirect rules
     */
    redirectWhitelist,

    /**
     * <p>Specifies the preferred SASL authentication mechanism.
     * An empty value falls-back to "PLAIN"</p>
     * 
     * <p>Known values: GSSAPI, XOAUTH2, OAUTHBEARER</p>
     * 
     * <p>Default is empty (which results in "PLAIN" being used).</p>
     */
    preferredSaslMech,
    ;

    private static final String EMPTY = "";
    private static final String PREFIX = "com.openexchange.mail.filter.";
    private final String fqn;
    private final Object defaultValue;

    /**
     * Initialises a new {@link MailFilterProperty}.
     */
    private MailFilterProperty() {
        this(EMPTY);
    }

    /**
     * Initialises a new {@link MailFilterProperty}.
     * 
     * @param defaultValue The default value of the property
     */
    private MailFilterProperty(Object defaultValue) {
        this(defaultValue, PREFIX);
    }

    /**
     * Initialises a new {@link MailFilterProperty}.
     * 
     * @param defaultValue The default value of the property
     * @param fqn The fully qualified name of the property
     */
    private MailFilterProperty(Object defaultValue, String fqn) {
        this.defaultValue = defaultValue;
        this.fqn = fqn;
    }

    /**
     * Returns the fully qualified name of the property
     *
     * @return the fully qualified name of the property
     */
    public String getFQPropertyName() {
        return fqn + name();
    }

    /**
     * Returns the default value of this property
     *
     * @return the default value of this property
     */
    public <T extends Object> T getDefaultValue(Class<T> cls) {
        if (defaultValue.getClass().isAssignableFrom(cls)) {
            return cls.cast(defaultValue);
        }
        throw new IllegalArgumentException("The object cannot be converted to the specified type '" + cls.getCanonicalName() + "'");
    }
}
