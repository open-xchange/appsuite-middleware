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

package com.openexchange.pop3.config;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import javax.mail.internet.idn.IDNA;
import com.openexchange.exception.OXException;
import com.openexchange.mail.api.IMailProperties;
import com.openexchange.mail.api.MailCapabilities;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.api.UrlInfo;
import com.openexchange.pop3.POP3Capabilities;
import com.openexchange.pop3.POP3ExceptionCode;

/**
 * {@link POP3Config} - The POP3 configuration.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class POP3Config extends MailConfig {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(POP3Config.class);

    private static final String PROTOCOL_POP3_SECURE = "pop3s";

    // private volatile POP3Capabilities pop3Capabilities;

    private int pop3Port;

    private String pop3Server;

    private boolean secure;

    private IPOP3Properties mailProperties;

    private InetAddress pop3ServerAddress;

    private InetSocketAddress pop3ServerSocketAddress;

    /**
     * Default constructor
     */
    public POP3Config() {
        super();
    }

    @Override
    public MailCapabilities getCapabilities() {
        return new POP3Capabilities();
    }

    /**
     * Gets the POP3 port
     *
     * @return the POP3 port
     */
    @Override
    public int getPort() {
        return pop3Port;
    }

    @Override
    public void setPort(final int pop3Port) {
        this.pop3Port = pop3Port;
    }

    /**
     * Gets the POP3 server
     *
     * @return the POP3 server
     */
    @Override
    public String getServer() {
        return pop3Server;
    }

    @Override
    public void setServer(final String pop3Server) {
        this.pop3Server = null == pop3Server ? null : IDNA.toUnicode(pop3Server);
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
    protected void parseServerURL(final UrlInfo urlInfo) {
        pop3Server = urlInfo.getServerURL();
        startTls = urlInfo.isStartTls();
        pop3Port = 110;
        {
            final String[] parsed = parseProtocol(pop3Server);
            if (parsed == null) {
                secure = false;
            } else {
                secure = PROTOCOL_POP3_SECURE.equals(parsed[0]);
                pop3Server = parsed[1];
            }
            final int pos = pop3Server.indexOf(':');
            if (pos > -1) {
                try {
                    pop3Port = Integer.parseInt(pop3Server.substring(pos + 1));
                } catch (final NumberFormatException e) {
                    LOG.error("POP3 port could not be parsed to an integer value. Using fallback value 110", e);
                    pop3Port = 110;
                }
                pop3Server = pop3Server.substring(0, pos);
            }
        }
    }

    /**
     * Gets the internet address of the POP3 server.
     *
     * @return The internet address of the POP3 server.
     * @throws OXException If POP3 server cannot be resolved
     */
    public InetAddress getPOP3ServerAddress() throws OXException {
        if (null == pop3ServerAddress) {
            try {
                pop3ServerAddress = InetAddress.getByName(pop3Server);
            } catch (final UnknownHostException e) {
                throw POP3ExceptionCode.IO_ERROR.create(e, e.getMessage());
            }
        }
        return pop3ServerAddress;
    }

    /**
     * Gets the socket address (internet address + port) of the POP3 server.
     *
     * @return The socket address (internet address + port) of the POP3 server.
     * @throws IMAPException If POP3 server cannot be resolved
     */
    public InetSocketAddress getPOP3ServerSocketAddress() throws OXException {
        if (null == pop3ServerSocketAddress) {
            pop3ServerSocketAddress = new InetSocketAddress(getPOP3ServerAddress(), pop3Port);
        }
        return pop3ServerSocketAddress;
    }

    @Override
    public IMailProperties getMailProperties() {
        return mailProperties;
    }

    @Override
    public void setMailProperties(final IMailProperties mailProperties) {
        this.mailProperties = (IPOP3Properties) mailProperties;
    }

    public IPOP3Properties getPOP3Properties() {
        return mailProperties;
    }

}
