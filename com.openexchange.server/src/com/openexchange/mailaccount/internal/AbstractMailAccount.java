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

package com.openexchange.mailaccount.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.mail.internet.idn.IDNA;
import com.openexchange.exception.OXException;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.transport.config.TransportProperties;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.mailaccount.TransportAuth;
import com.openexchange.tools.net.URIDefaults;
import com.openexchange.tools.net.URIParser;
import com.openexchange.tools.net.URITools;

/**
 * {@link AbstractMailAccount} - Abstract mail account.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractMailAccount implements MailAccount {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractMailAccount.class);

    private static final long serialVersionUID = -641194838598605274L;

    protected int id;
    protected String login;
    protected String password;
    protected String transportLogin;
    protected String transportPassword;
    protected String mailServer;
    protected int mailPort;
    protected String mailProtocol;
    protected boolean mailSecure;
    protected TransportAuth transportAuth;
    protected String transportServer;
    protected int transportPort;
    protected String transportProtocol;
    protected boolean transportSecure;
    protected String name;
    protected String primaryAddress;
    protected String personal;
    protected String replyTo;
    protected int userId;
    protected String spamHandler;
    protected String trash;
    protected String archive;
    protected String sent;
    protected String drafts;
    protected String spam;
    protected String confirmedSpam;
    protected String confirmedHam;
    protected String mailServerUrl;
    protected String transportServerUrl;
    protected boolean unifiedINBOXEnabled;
    protected String trashFullname;
    protected String archiveFullname;
    protected String sentFullname;
    protected String draftsFullname;
    protected String spamFullname;
    protected String confirmedSpamFullname;
    protected String confirmedHamFullname;
    protected Map<String, String> properties;
    protected Map<String, String> transportProperties;
    protected boolean mailStartTls;
    protected boolean transportStartTls;
    protected int mailOAuthId;
    protected int transportOAuthId;
    protected String rootFolder;

    /**
     * Initializes a new {@link AbstractMailAccount}.
     */
    protected AbstractMailAccount() {
        super();
        properties = new HashMap<>(4);
        transportAuth = TransportAuth.MAIL;
        transportProperties = new HashMap<>(4);
        transportPort = 25;
        mailPort = 143;
        final String transportProvider = TransportProperties.getInstance().getDefaultTransportProvider();
        transportProtocol = transportProvider == null ? "smtp" : transportProvider;
        final String mailProvider = MailProperties.getInstance().getDefaultMailProvider();
        mailProtocol = mailProvider == null ? "imap" : mailProvider;
        mailOAuthId = -1;
        transportOAuthId = -1;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public boolean isMailAccount() {
        return true;
    }

    @Override
    public String getLogin() {
        return login;
    }

    @Override
    public String getMailServer() {
        return mailServer;
    }

    @Override
    public int getMailPort() {
        return mailPort;
    }

    @Override
    public String getMailProtocol() {
        return mailProtocol;
    }

    @Override
    public boolean isMailSecure() {
        return mailSecure;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getPrimaryAddress() {
        return primaryAddress;
    }

    @Override
    public String getPersonal() {
        return personal;
    }

    @Override
    public String getReplyTo() {
        if (com.openexchange.java.Strings.isEmpty(replyTo)) {
            return properties.get("replyto");
        }
        return replyTo;
    }

    @Override
    public TransportAuth getTransportAuth() {
        return transportAuth;
    }

    @Override
    public String getTransportServer() {
        return transportServer;
    }

    @Override
    public int getTransportPort() {
        return transportPort;
    }

    @Override
    public String getTransportProtocol() {
        return transportProtocol;
    }

    @Override
    public boolean isTransportSecure() {
        return transportSecure;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public String getRootFolder() {
        return rootFolder;
    }

    /**
     * Sets the account ID.
     *
     * @param id The account ID
     */
    public void setId(final int id) {
        this.id = id;
    }

    /**
     * Sets the login.
     *
     * @param login The login
     */
    public void setLogin(final String login) {
        this.login = login;
    }

    /**
     * Sets the password.
     *
     * @param password The password
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * Sets the account name.
     *
     * @param name The account name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Sets the primary email address.
     *
     * @param primaryAddress The primary email address
     */
    public void setPrimaryAddress(final String primaryAddress) {
        this.primaryAddress = primaryAddress == null ? primaryAddress : IDNA.toIDN(primaryAddress);;
    }

    /**
     * Sets the personal.
     *
     * @param personal The personal
     */
    public void setPersonal(final String personal) {
        this.personal = personal;
    }

    /**
     * Sets the reply-to address
     *
     * @param replyTo The reply-to address
     */
    public void setReplyTo(final String replyTo) {
        this.replyTo = replyTo;
        if (com.openexchange.java.Strings.isEmpty(replyTo)) {
            properties.remove("replyto");
        } else {
            properties.put("replyto", replyTo);
        }
    }

    /**
     * Sets the user ID.
     *
     * @param userId The user ID
     */
    public void setUserId(final int userId) {
        this.userId = userId;
    }

    @Override
    public String getDrafts() {
        return drafts;
    }

    @Override
    public String getSent() {
        return sent;
    }

    @Override
    public String getSpam() {
        return spam;
    }

    @Override
    public String getArchive() {
        return archive;
    }

    @Override
    public String getTrash() {
        return trash;
    }

    @Override
    public String getConfirmedHam() {
        return confirmedHam;
    }

    @Override
    public String getConfirmedSpam() {
        return confirmedSpam;
    }

    @Override
    public String getSpamHandler() {
        return spamHandler;
    }

    /**
     * Sets the default trash folder's name.
     *
     * @param trash The default trash folder's name
     */
    public void setTrash(final String trash) {
        this.trash = trash;
    }

    /**
     * Sets the default archive folder's name.
     *
     * @param trash The default archive folder's name
     */
    public void setArchive(String archive) {
        this.archive = archive;
    }

    /**
     * Sets the default sent folder's name.
     *
     * @param trash The default sent folder's name
     */
    public void setSent(final String sent) {
        this.sent = sent;
    }

    /**
     * Sets the default drafts folder's name.
     *
     * @param trash The default drafts folder's name
     */
    public void setDrafts(final String drafts) {
        this.drafts = drafts;
    }

    /**
     * Sets the default spam folder's name.
     *
     * @param trash The default spam folder's name
     */
    public void setSpam(final String spam) {
        this.spam = spam;
    }

    /**
     * Sets the default confirmed-spam folder's name.
     *
     * @param trash The default confirmed-spam folder's name
     */
    public void setConfirmedSpam(final String confirmedSpam) {
        this.confirmedSpam = confirmedSpam;
    }

    /**
     * Sets the default confirmed-ham folder's name.
     *
     * @param trash The default confirmed-ham folder's name
     */
    public void setConfirmedHam(final String confirmedHam) {
        this.confirmedHam = confirmedHam;
    }

    /**
     * Sets the spam handler name.
     *
     * @param trash The spam handler name
     */
    public void setSpamHandler(final String spamHandler) {
        this.spamHandler = spamHandler;
    }

    /**
     * Sets the mail server name.
     *
     * @param mailServer The mail server name to set
     */
    public void setMailServer(final String mailServer) {
        mailServerUrl = null;
        this.mailServer = mailServer == null ? null : IDNA.toUnicode(mailServer);
    }

    /**
     * Sets the mail server port.
     *
     * @param mailPort The mail server port to set
     */
    public void setMailPort(final int mailPort) {
        mailServerUrl = null;
        this.mailPort = mailPort;
    }

    /**
     * Sets the mail server protocol.
     *
     * @param mailProtocol The mail server protocol to set
     */
    public void setMailProtocol(final String mailProtocol) {
        mailServerUrl = null;
        this.mailProtocol = mailProtocol;
    }

    /**
     * Sets whether to establish a secure connection to mail server.
     *
     * @param mailSecure Whether to establish a secure connection to mail server
     */
    public void setMailSecure(final boolean mailSecure) {
        mailServerUrl = null;
        this.mailSecure = mailSecure;
    }

    /**
     * Sets the transport authentication information
     *
     * @param transportAuth The transport authentication information to set
     */
    public void setTransportAuth(TransportAuth transportAuth) {
        this.transportAuth = transportAuth;
    }

    /**
     * Sets the transport server name.
     *
     * @param transportServer The transport server name to set
     */
    public void setTransportServer(final String transportServer) {
        transportServerUrl = null;
        this.transportServer = transportServer == null ? null : IDNA.toUnicode(transportServer);
    }

    /**
     * Sets the transport server port.
     *
     * @param transportPort The transport server port to set
     */
    public void setTransportPort(final int transportPort) {
        transportServerUrl = null;
        this.transportPort = transportPort;
    }

    /**
     * Sets the transport server protocol
     *
     * @param transportProtocol The transport server protocol to set
     */
    public void setTransportProtocol(final String transportProtocol) {
        transportServerUrl = null;
        this.transportProtocol = transportProtocol;
    }

    /**
     * Sets whether to establish a secure connection to transport server.
     *
     * @param transportSecure Whether to establish a secure connection to transport server
     */
    public void setTransportSecure(final boolean transportSecure) {
        transportServerUrl = null;
        this.transportSecure = transportSecure;
    }

    /**
     * Sets the identifier for the root folder
     *
     * @param rootFolder The root folder identifier to set
     */
    public void setRootFolder(String rootFolder) {
        this.rootFolder = rootFolder;
    }

    @Override
    public String generateMailServerURL() {
        if (null != mailServerUrl) {
            return mailServerUrl;
        }
        if (com.openexchange.java.Strings.isEmpty(mailServer)) {
            return null;
        }
        final String protocol = mailSecure ? mailProtocol + 's' : mailProtocol;
        try {
            return mailServerUrl = URITools.generateURI(protocol, IDNA.toASCII(mailServer), mailPort).toString();
        } catch (final URISyntaxException e) {
            LOG.error("", e);
            // Old implementation is not capable of handling IPv6 addresses.
            final StringBuilder sb = new StringBuilder(32);
            sb.append(mailProtocol);
            if (mailSecure) {
                sb.append('s');
            }
            return mailServerUrl = sb.append("://").append(mailServer).append(':').append(mailPort).toString();
        }
    }

    /**
     * Parses specified mail server URL.
     *
     * @param mailServerURL The mail server URL to parse
     * @throws OXException if parsing the URL fails.
     */
    public void parseMailServerURL(final String mailServerURL) throws OXException {
        try {
            setMailServer(URIParser.parse(IDNA.toASCII(mailServerURL), URIDefaults.IMAP));
        } catch (final URISyntaxException e) {
            throw MailAccountExceptionCodes.URI_PARSE_FAILED.create(e, mailServerURL);
        }
    }

    public void setMailServer(final URI mailServer) {
        if (null == mailServer) {
            // Parse like old parser to prevent problems.
            setMailServer("");
        } else {
            final String protocol = mailServer.getScheme();
            if (protocol.endsWith("s")) {
                setMailSecure(true);
                setMailProtocol(protocol.substring(0, protocol.length() - 1));
            } else {
                setMailSecure(false);
                setMailProtocol(protocol);
            }
            setMailServer(URITools.getHost(mailServer));
            setMailPort(mailServer.getPort());
        }
    }

    /**
     * Parses specified transport server URL.
     *
     * @param mailServerURL The transport server URL to parse
     * @throws OXException if parsing the URL fails.
     */
    public void parseTransportServerURL(final String transportServerURL) throws OXException {
        if (null == transportServerURL) {
            setTransportServer((String) null);
            return;
        }
        try {
            setTransportServer(URIParser.parse(IDNA.toASCII(transportServerURL), URIDefaults.SMTP));
        } catch (final URISyntaxException e) {
            setTransportServer((String) null);
            return;
            //throw MailAccountExceptionFactory.getInstance().create(MailAccountExceptionMessages.URI_PARSE_FAILED, e, transportServerURL);
        }
    }

    public void setTransportServer(final URI transportServer) {
        if (null == transportServer) {
            // Parse like old parser to prevent problems.
            setTransportServer("");
        } else {
            final String protocol = transportServer.getScheme();
            if (protocol.endsWith("s")) {
                setTransportSecure(true);
                setTransportProtocol(protocol.substring(0, protocol.length() - 1));
            } else {
                setTransportSecure(false);
                setTransportProtocol(protocol);
            }
            setTransportServer(URITools.getHost(transportServer));
            setTransportPort(transportServer.getPort());
        }
    }

    @Override
    public String generateTransportServerURL() {
        if (null != transportServerUrl) {
            return transportServerUrl;
        }
        if (com.openexchange.java.Strings.isEmpty(transportServer)) {
            return null;
        }
        final String protocol = transportSecure ? transportProtocol + 's' : transportProtocol;
        try {
            return transportServerUrl = URITools.generateURI(protocol, IDNA.toASCII(transportServer), transportPort).toString();
        } catch (final URISyntaxException e) {
            LOG.error("", e);
            // Old implementation is not capable of handling IPv6 addresses.
            final StringBuilder sb = new StringBuilder(32);
            sb.append(transportProtocol);
            if (transportSecure) {
                sb.append('s');
            }
            return transportServerUrl = sb.append("://").append(transportServer).append(':').append(transportPort).toString();
        }
    }

    /**
     * Gets the optional transport login.
     *
     * @return The optional transport login
     */
    @Override
    public String getTransportLogin() {
        if (null == transportLogin) {
            return getLogin();
        }
        return transportLogin;
    }

    /**
     * Gets the optional transport password.
     *
     * @return The optional transport password
     */
    @Override
    public String getTransportPassword() {
        if (null == transportPassword) {
            return getPassword();
        }
        return transportPassword;
    }

    /**
     * Sets the optional transport login.
     *
     * @param transportLogin The optional transport login
     */
    public void setTransportLogin(final String transportLogin) {
        this.transportLogin = transportLogin;
    }

    /**
     * Sets the optional transport password.
     *
     * @param transportLogin The optional transport password
     */
    public void setTransportPassword(final String transportPassword) {
        this.transportPassword = transportPassword;
    }

    @Override
    public boolean isUnifiedINBOXEnabled() {
        return unifiedINBOXEnabled;
    }

    /**
     * Sets if this mail account is enabled for Unified Mail.
     *
     * @param unifiedINBOXEnabled <code>true</code> if this mail account is enabled for Unified Mail; otherwise <code>false</code>
     */
    public void setUnifiedINBOXEnabled(final boolean unifiedINBOXEnabled) {
        this.unifiedINBOXEnabled = unifiedINBOXEnabled;
    }

    @Override
    public String getTrashFullname() {
        return trashFullname;
    }

    /**
     * Sets the trash full name
     *
     * @param trashFullname The trash full name to set
     */
    public void setTrashFullname(final String trashFullname) {
        this.trashFullname = trashFullname;
    }

    @Override
    public String getArchiveFullname() {
        return archiveFullname;
    }

    /**
     * Sets the archive full name
     *
     * @param trashFullname The archive full name to set
     */
    public void setArchiveFullname(String archiveFullname) {
        this.archiveFullname = archiveFullname;
    }

    @Override
    public String getSentFullname() {
        return sentFullname;
    }

    /**
     * Sets the sent full name
     *
     * @param sentFullname The sent full name to set
     */
    public void setSentFullname(final String sentFullname) {
        this.sentFullname = sentFullname;
    }

    @Override
    public String getDraftsFullname() {
        return draftsFullname;
    }

    /**
     * Sets the drafts full name
     *
     * @param draftsFullname The drafts full name to set
     */
    public void setDraftsFullname(final String draftsFullname) {
        this.draftsFullname = draftsFullname;
    }

    @Override
    public String getSpamFullname() {
        return spamFullname;
    }

    /**
     * Sets the spam full name
     *
     * @param spamFullname The spam full name to set
     */
    public void setSpamFullname(final String spamFullname) {
        this.spamFullname = spamFullname;
    }

    @Override
    public String getConfirmedSpamFullname() {
        return confirmedSpamFullname;
    }

    /**
     * Sets the confirmed-spam full name
     *
     * @param confirmedSpamFullname The confirmed-spam full name to set
     */
    public void setConfirmedSpamFullname(final String confirmedSpamFullname) {
        this.confirmedSpamFullname = confirmedSpamFullname;
    }

    @Override
    public String getConfirmedHamFullname() {
        return confirmedHamFullname;
    }

    /**
     * Sets the confirmed-ham full name
     *
     * @param confirmedHamFullname The confirmed-ham full name to set
     */
    public void setConfirmedHamFullname(final String confirmedHamFullname) {
        this.confirmedHamFullname = confirmedHamFullname;
    }

    public void setMailStartTls(boolean mailStartTls) {
        this.mailStartTls = mailStartTls;
    }

    public void setTransportStartTls(boolean transportStartTls) {
        this.transportStartTls = transportStartTls;
    }

    @Override
    public boolean isMailStartTls() {
        return mailStartTls;
    }

    @Override
    public boolean isMailOAuthAble() {
        return mailOAuthId >= 0;
    }

    @Override
    public int getMailOAuthId() {
        return mailOAuthId < 0 ? -1 : mailOAuthId;
    }

    /**
     * Sets the identifier of the associated OAuth account for mail server
     *
     * @param mailOauthId The OAuth account identifier or <code>-1</code> to signal none
     */
    public void setMailOAuthId(int mailOauthId) {
        this.mailOAuthId = mailOauthId < 0 ? -1 : mailOauthId;
    }

    @Override
    public boolean isTransportStartTls() {
        return transportStartTls;
    }

    @Override
    public boolean isTransportOAuthAble() {
        return transportOAuthId >= 0;
    }

    @Override
    public int getTransportOAuthId() {
        return transportOAuthId < 0 ? -1 : transportOAuthId;
    }

    /**
     * Sets the identifier of the associated OAuth account for transport server
     *
     * @param transportOAuthId The OAuth account identifier or <code>-1</code> to signal none
     */
    public void setTransportOAuthId(int transportOAuthId) {
        this.transportOAuthId = transportOAuthId < 0 ? -1 : transportOAuthId;
    }

    @Override
    public Map<String, String> getProperties() {
        if (properties.isEmpty()) {
            return Collections.emptyMap();
        }
        final Map<String, String> clone = new HashMap<>(properties.size());
        clone.putAll(properties);
        if (null != replyTo) {
            clone.put("replyto", replyTo);
        }
        return clone;
    }

    /**
     * Sets the properties.
     *
     * @param properties The properties to set
     */
    public void setProperties(final Map<String, String> properties) {
        if (null == properties) {
            this.properties = new HashMap<>(4);
        } else if (properties.isEmpty()) {
            this.properties = new HashMap<>(4);
        } else {
            for (final Map.Entry<String, String> e : properties.entrySet()) {
                if ("replyto".equals(e.getKey())) {
                    replyTo = e.getValue();
                    break;
                }
            }
            this.properties = new HashMap<>(properties.size());
            this.properties.putAll(properties);
        }
    }

    @Override
    public void addProperty(final String name, final String value) {
        if (properties.isEmpty()) {
            properties = new HashMap<>(4);
        }
        if ("replyto".equals(name)) {
            replyTo = value;
        }
        properties.put(name, value);
    }

    @Override
    public Map<String, String> getTransportProperties() {
        if (transportProperties.isEmpty()) {
            return Collections.emptyMap();
        }
        return new HashMap<>(transportProperties);
    }

    /**
     * Sets the transport properties
     *
     * @param properties The transport properties to set
     */
    public void setTransportProperties(final Map<String, String> transportProperties) {
        if (null == transportProperties) {
            this.transportProperties = new HashMap<>(4);
        } else if (transportProperties.isEmpty()) {
            this.transportProperties = new HashMap<>(4);
        } else {
            this.transportProperties = new HashMap<>(transportProperties);
        }
    }

    @Override
    public void addTransportProperty(final String name, final String value) {
        if (transportProperties.isEmpty()) {
            transportProperties = new HashMap<>(4);
        }
        transportProperties.put(name, value);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(128);
        sb.append(" id=").append(getId()).append(" user=").append(getUserId());
        sb.append("\nname=").append(getName()).append(" primary-address=").append(getPrimaryAddress());
        sb.append("\nmail-server=").append(generateMailServerURL()).append(" transport-server=").append(generateTransportServerURL());
        return sb.toString();
    }

}
