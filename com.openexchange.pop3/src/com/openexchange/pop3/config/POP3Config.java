/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
        requireTls = urlInfo.isRequireStartTls();
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
                } catch (NumberFormatException e) {
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
            } catch (UnknownHostException e) {
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
