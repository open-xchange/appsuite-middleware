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

package com.openexchange.mailaccount.internal;

import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.transport.config.TransportConfig;
import com.openexchange.mail.transport.config.TransportProperties;
import com.openexchange.mailaccount.MailAccount;

/**
 * {@link AbstractMailAccount} - Abstract mail account.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractMailAccount implements MailAccount {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(AbstractMailAccount.class);

    private static final long serialVersionUID = -641194838598605274L;

    protected int id;

    protected String login;

    protected String password;

    protected String mailServer;

    protected int mailPort;

    protected String mailProtocol;

    protected boolean mailSecure;

    protected String transportServer;

    protected int transportPort;

    protected String transportProtocol;

    protected boolean transportSecure;

    protected String name;

    protected String primaryAddress;

    protected int userId;

    protected String spamHandler;

    protected String trash;

    protected String sent;

    protected String drafts;

    protected String spam;

    protected String confirmedSpam;

    protected String confirmedHam;

    private String mailServerUrl;

    private String transportServerUrl;

    /**
     * Initializes a new {@link AbstractMailAccount}.
     */
    protected AbstractMailAccount() {
        super();
        transportPort = 25;
        mailPort = 143;
        transportProtocol = TransportProperties.getInstance().getDefaultTransportProvider();
        mailProtocol = MailProperties.getInstance().getDefaultMailProvider();
    }

    public int getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getMailServer() {
        return mailServer;
    }

    public int getMailPort() {
        return mailPort;
    }

    public String getMailProtocol() {
        return mailProtocol;
    }

    public boolean isMailSecure() {
        return mailSecure;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getPrimaryAddress() {
        return primaryAddress;
    }

    public String getTransportServer() {
        return transportServer;
    }

    public int getTransportPort() {
        return transportPort;
    }

    public String getTransportProtocol() {
        return transportProtocol;
    }

    public boolean isTransportSecure() {
        return transportSecure;
    }

    public int getUserId() {
        return userId;
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
        this.primaryAddress = primaryAddress;
    }

    /**
     * Sets the user ID.
     * 
     * @param userId The user ID
     */
    public void setUserId(final int userId) {
        this.userId = userId;
    }

    public String getDrafts() {
        return drafts;
    }

    public String getSent() {
        return sent;
    }

    public String getSpam() {
        return spam;
    }

    public String getTrash() {
        return trash;
    }

    public String getConfirmedHam() {
        return confirmedHam;
    }

    public String getConfirmedSpam() {
        return confirmedSpam;
    }

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
     * Sets whether to establish a secure connection to mail server.
     * 
     * @param mailSecure Whether to establish a secure connection to mail server
     */
    public void setMailSecure(final boolean mailSecure) {
        mailServerUrl = null;
        this.mailSecure = mailSecure;
    }

    /**
     * Sets the transport server name.
     * 
     * @param transportServer The transport server name to set
     */
    public void setTransportServer(final String transportServer) {
        transportServerUrl = null;
        this.transportServer = transportServer;
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
     * Parses specified mail server URL.
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
     * Parses specified transport server URL.
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
        if (null != transportServerUrl) {
            return transportServerUrl;
        }
        if (null == transportServer) {
            return null;
        }
        final StringBuilder sb = new StringBuilder(32);
        sb.append(transportProtocol);
        if (transportSecure) {
            sb.append('s');
        }
        return transportServerUrl = sb.append("://").append(transportServer).append(':').append(transportPort).toString();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(128);
        sb.append(" id=").append(getId()).append(" user=").append(getUserId());
        sb.append("\nname=").append(getName()).append(" primary-address=").append(getPrimaryAddress());
        sb.append("\nmail-server=").append(generateMailServerURL()).append(" transport-server=").append(generateTransportServerURL());
        return sb.toString();
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
