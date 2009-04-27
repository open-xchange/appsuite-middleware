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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.mailaccount;

import java.io.Serializable;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.transport.config.TransportConfig;

/**
 * {@link MailAccountDescription} - Container object describing a mail account to insert/update.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccountDescription implements Serializable {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(MailAccountDescription.class);

    private static final long serialVersionUID = -2443656355399068302L;

    private int id;

    private String login;

    private String password;

    private String transportLogin;

    private String transportPassword;

    private String name;

    private String primaryAddress;

    private String spamHandler;

    private String trash;

    private String sent;

    private String drafts;

    private String spam;

    private String confirmedSpam;

    private String confirmedHam;

    private boolean defaultFlag;

    private String mailServer;

    private int mailPort;

    private String mailProtocol;

    private boolean mailSecure;

    private String transportServer;

    private int transportPort;

    private String transportProtocol;

    private boolean transportSecure;

    private String mailServerUrl;

    private String transportUrl;

    /**
     * Initializes a new {@link MailAccountDescription}.
     */
    public MailAccountDescription() {
        super();
        transportPort = 25;
        mailPort = 143;
        transportProtocol = "smtp";
        mailProtocol = "imap";
    }

    /**
     * Gets the ID.
     * 
     * @return The ID
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the login.
     * 
     * @return The login
     */
    public String getLogin() {
        return login;
    }

    /**
     * Gets the account name.
     * 
     * @return The account name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets clear, non-encrypted password.
     * 
     * @return The clear, non-encrypted password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the primary email address.
     * 
     * @return The primary email address
     */
    public String getPrimaryAddress() {
        return primaryAddress;
    }

    /**
     * Gets the ID
     * 
     * @return The ID
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
     * Sets the clear-text password (which is stored encrypted).
     * 
     * @param password The clear-text password
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * Gets the mail server name.
     * <p>
     * The mail server name can either be a machine name, such as "<code>java.sun.com</code>", or a textual representation of its IP
     * address.
     * 
     * @return The mail server name
     */
    public String getMailServer() {
        return mailServer;
    }

    /**
     * Gets the mail server port.
     * 
     * @return The mail server port
     */
    public int getMailPort() {
        return mailPort;
    }

    /**
     * Gets the mail server protocol.
     * 
     * @return The mail server protocol
     */
    public String getMailProtocol() {
        return mailProtocol;
    }

    /**
     * Checks if a secure connection to mail server shall be established.
     * 
     * @return <code>true</code> if a secure connection to mail server shall be established; otherwise <code>false</code>
     */
    public boolean isMailSecure() {
        return mailSecure;
    }

    /**
     * Sets the mail server name.
     * 
     * @param mailServer The mail server name to set
     */
    public void setMailServer(final String mailServer) {
        mailServerUrl = null;
        this.mailServer = mailServer;
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
     * Sets if a secure connection to mail server shall be established.
     * 
     * @param mailSecure <code>true</code> if a secure connection to mail server shall be established; otherwise <code>false</code>
     */
    public void setMailSecure(final boolean mailSecure) {
        mailServerUrl = null;
        this.mailSecure = mailSecure;
    }

    /**
     * Gets the transport server name.
     * <p>
     * The transport server name can either be a machine name, such as "<code>java.sun.com</code>", or a textual representation of its IP
     * address.
     * 
     * @return The transport server name
     */
    public String getTransportServer() {
        return transportServer;
    }

    /**
     * Gets the transport server port.
     * 
     * @return The transport server port
     */
    public int getTransportPort() {
        return transportPort;
    }

    /**
     * Gets the transport server protocol.
     * 
     * @return The transport server protocol
     */
    public String getTransportProtocol() {
        return transportProtocol;
    }

    /**
     * Checks if a secure connection to transport server shall be established.
     * 
     * @return <code>true</code> if a secure connection to transport server shall be established; otherwise <code>false</code>
     */
    public boolean isTransportSecure() {
        return transportSecure;
    }

    /**
     * Sets the transport server name.
     * 
     * @param transportServer The transport server name to set
     */
    public void setTransportServer(final String transportServer) {
        transportUrl = null;
        this.transportServer = transportServer;
    }

    /**
     * Sets the transport server port
     * 
     * @param transportPort The transport server port to set
     */
    public void setTransportPort(final int transportPort) {
        transportUrl = null;
        this.transportPort = transportPort;
    }

    /**
     * Sets the transport server protocol
     * 
     * @param transportProtocol The transport server protocol to set
     */
    public void setTransportProtocol(final String transportProtocol) {
        transportUrl = null;
        this.transportProtocol = transportProtocol;
    }

    /**
     * Sets if a secure connection to transport server shall be established.
     * 
     * @param mailSecure <code>true</code> if a secure connection to transport server shall be established; otherwise <code>false</code>
     */
    public void setTransportSecure(final boolean transportSecure) {
        transportUrl = null;
        this.transportSecure = transportSecure;
    }

    /**
     * Generates the mail server URL.
     * 
     * @return The generated mail server URL
     */
    public String generateMailServerURL() {
        if (null != mailServerUrl) {
            return mailServerUrl;
        }
        if (null == mailServer) {
            return null;
        }
        final StringBuilder sb = new StringBuilder(32);
        sb.append(mailProtocol);
        if (mailSecure) {
            sb.append('s');
        }
        return mailServerUrl = sb.append("://").append(mailServer).append(':').append(mailPort).toString();
    }

    /**
     * Parses specified mail server URL
     * 
     * @param mailServerURL The mail server URL to parse
     */
    public void parseMailServerURL(final String mailServerURL) {
        if (null == mailServerURL) {
            setMailServer(null);
            return;
        }
        final String[] tmp = MailConfig.parseProtocol(mailServerURL);
        final String prot;
        final Object[] parsed;
        if (tmp != null) {
            prot = tmp[0];
            parsed = parseServerAndPort(tmp[1], getMailPort());
        } else {
            prot = getMailProtocol();
            parsed = parseServerAndPort(mailServerURL, getMailPort());
        }
        if (prot.endsWith("s")) {
            setMailSecure(true);
            setMailProtocol(prot.substring(0, prot.length() - 1));
        } else {
            setMailProtocol(prot);
        }
        setMailServer(parsed[0].toString());
        setMailPort(((Integer) parsed[1]).intValue());
    }

    /**
     * Parses specified transport server URL
     * 
     * @param mailServerURL The transport server URL to parse
     */
    public void parseTransportServerURL(final String transportServerURL) {
        if (null == transportServerURL) {
            setTransportServer(null);
            return;
        }
        final String[] tmp = TransportConfig.parseProtocol(transportServerURL);
        final String prot;
        final Object[] parsed;
        if (tmp != null) {
            prot = tmp[0];
            parsed = parseServerAndPort(tmp[1], getTransportPort());
        } else {
            prot = getTransportProtocol();
            parsed = parseServerAndPort(transportServerURL, getTransportPort());
        }
        if (prot.endsWith("s")) {
            setTransportSecure(true);
            setTransportProtocol(prot.substring(0, prot.length() - 1));
        } else {
            setTransportProtocol(prot);
        }
        setTransportServer(parsed[0].toString());
        setTransportPort(((Integer) parsed[1]).intValue());
    }

    public String generateTransportServerURL() {
        if (null != transportUrl) {
            return transportUrl;
        }
        if (null == transportServer) {
            return null;
        }
        final StringBuilder sb = new StringBuilder(32);
        sb.append(transportProtocol);
        if (transportSecure) {
            sb.append('s');
        }
        return transportUrl = sb.append("://").append(transportServer).append(':').append(transportPort).toString();
    }

    /**
     * Sets the account name.
     * 
     * @param name The account name.
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
        this.primaryAddress = primaryAddress;
    }

    /**
     * Gets the name for default drafts folder.
     * 
     * @return The name for default drafts folder
     */
    public String getDrafts() {
        return drafts;
    }

    /**
     * Gets the name for default sent folder.
     * 
     * @return The name for default sent folder
     */
    public String getSent() {
        return sent;
    }

    /**
     * Gets the name for default spam folder.
     * 
     * @return The name for default spam folder
     */
    public String getSpam() {
        return spam;
    }

    /**
     * Gets the name for default trash folder.
     * 
     * @return The name for default trash folder
     */
    public String getTrash() {
        return trash;
    }

    /**
     * Gets the name for default confirmed-ham folder.
     * 
     * @return The name for default confirmed-ham folder
     */
    public String getConfirmedHam() {
        return confirmedHam;
    }

    /**
     * Gets the name for default confirmed-spam folder.
     * 
     * @return The name for default confirmed-spam folder
     */
    public String getConfirmedSpam() {
        return confirmedSpam;
    }

    /**
     * Gets the spam handler name.
     * 
     * @return The spam handler name
     */
    public String getSpamHandler() {
        return spamHandler;
    }

    /**
     * Sets the name for default trash folder.
     * 
     * @param trash The name for default trash folder
     */
    public void setTrash(final String trash) {
        this.trash = trash;
    }

    /**
     * Sets the name for default sent folder.
     * 
     * @param sent The name for default sent folder
     */
    public void setSent(final String sent) {
        this.sent = sent;
    }

    /**
     * Sets the name for default drafts folder.
     * 
     * @param drafts The name for default drafts folder
     */
    public void setDrafts(final String drafts) {
        this.drafts = drafts;
    }

    /**
     * Sets the name for default spam folder.
     * 
     * @param spam The name for spam trash folder
     */
    public void setSpam(final String spam) {
        this.spam = spam;
    }

    /**
     * Sets the name for default confirmed-spam folder.
     * 
     * @param confirmedSpam The name for default confirmed-spam folder
     */
    public void setConfirmedSpam(final String confirmedSpam) {
        this.confirmedSpam = confirmedSpam;
    }

    /**
     * Sets the name for default confirmed-ham folder.
     * 
     * @param confirmedHam The name for default confirmed-ham folder
     */
    public void setConfirmedHam(final String confirmedHam) {
        this.confirmedHam = confirmedHam;
    }

    /**
     * Sets the spam handler name.
     * 
     * @param spamHandler The spam handler name
     */
    public void setSpamHandler(final String spamHandler) {
        this.spamHandler = spamHandler;
    }

    /**
     * Checks if mail account denotes the default mail account.
     * 
     * @return <code>true</code> if mail account denotes the default mail account; otherwise <code>false</code>
     */
    public boolean isDefaultFlag() {
        return defaultFlag;
    }

    /**
     * Sets whether mail account denotes the default mail account.
     * 
     * @param defaultFlag <code>true</code> if mail account denotes the default mail account; otherwise <code>false</code>
     */
    public void setDefaultFlag(final boolean defaultFlag) {
        this.defaultFlag = defaultFlag;
    }

    /**
     * Gets the optional transport login.
     * <p>
     * <b>NOTE</b>:&nbsp;{@link #getLogin()} is returned if no separate transport login is available.
     * 
     * @return The optional transport login
     */
    public String getTransportLogin() {
        if (null == transportLogin) {
            return getLogin();
        }
        return transportLogin;
    }

    /**
     * Gets the optional transport password.
     * <p>
     * <b>NOTE</b>:&nbsp;{@link #getPassword()} is returned if no separate transport password is available.
     * 
     * @return The optional transport password
     */
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

    private static Object[] parseServerAndPort(final String server, final int defaultPort) {
        final int pos = server.indexOf(':');
        if (pos == -1) {
            return new Object[] { server, Integer.valueOf(defaultPort) };
        }
        int port;
        try {
            port = Integer.parseInt(server.substring(pos + 1));
        } catch (final NumberFormatException e) {
            LOG.warn("Unable to parse port out of URL: " + server + ". Using default port instead.", e);
            port = defaultPort;
        }
        return new Object[] { server.subSequence(0, pos), Integer.valueOf(port) };
    }

}
