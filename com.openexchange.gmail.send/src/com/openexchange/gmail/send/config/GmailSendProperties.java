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

package com.openexchange.gmail.send.config;

import static com.openexchange.java.Autoboxing.B;
import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.gmail.send.services.Services;
import com.openexchange.java.Strings;
import com.openexchange.mail.api.AbstractProtocolProperties;
import com.openexchange.mail.transport.config.ITransportProperties;
import com.openexchange.mail.transport.config.TransportProperties;

/**
 * {@link GmailSendProperties}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class GmailSendProperties extends AbstractProtocolProperties implements IGmailSendProperties {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GmailSendProperties.class);

    private static final GmailSendProperties instance = new GmailSendProperties();

    /**
     * Gets the singleton instance of {@link GmailSendProperties}
     *
     * @return The singleton instance of {@link GmailSendProperties}
     */
    public static GmailSendProperties getInstance() {
        return instance;
    }

    /*
     * Fields for global properties
     */

    private final ITransportProperties transportProperties;
    private int timeout;
    private int connectionTimeout;
    private boolean logTransport;

    /**
     * Initializes a new {@link GmailSendProperties}
     */
    private GmailSendProperties() {
        super();
        transportProperties = TransportProperties.getInstance();
    }

    @Override
    protected void loadProperties0() throws OXException {
        StringBuilder logBuilder = new StringBuilder(1024);
        List<Object> args = new ArrayList<Object>(32);
        String lineSeparator = Strings.getLineSeparator();

        logBuilder.append("{}Loading global Gmail Send properties...{}");
        args.add(lineSeparator);
        args.add(lineSeparator);

        ConfigurationService configuration = Services.getService(ConfigurationService.class);
        {
            final String tmp = configuration.getProperty("com.openexchange.gmail.send.logTransport", "false").trim();
            logTransport = Boolean.parseBoolean(tmp);
            logBuilder.append("    Log transport: {}{}");
            args.add(B(logTransport));
            args.add(lineSeparator);
        }

        {
            final String timeoutStr = configuration.getProperty("com.openexchange.gmail.send.timeout", "5000").trim();
            try {
                timeout = Integer.parseInt(timeoutStr);
                logBuilder.append("    Gmail Send Timeout: {}{}");
                args.add(I(timeout));
                args.add(lineSeparator);
            } catch (NumberFormatException e) {
                timeout = 5000;
                logBuilder.append("    Gmail Send Timeout: Invalid value \"{}\". Setting to fallback {}{}");
                args.add(timeoutStr);
                args.add(I(timeout));
                args.add(lineSeparator);

            }
        }

        {
            final String conTimeoutStr = configuration.getProperty("com.openexchange.gmail.send.connectionTimeout", "3000").trim();
            try {
                connectionTimeout = Integer.parseInt(conTimeoutStr);
                logBuilder.append("    Gmail Send Connection Timeout: {}{}");
                args.add(I(connectionTimeout));
                args.add(lineSeparator);
            } catch (NumberFormatException e) {
                connectionTimeout = 3000;
                logBuilder.append("    Gmail Send Connection Timeout: Invalid value \"{}\". Setting to fallback {}{}");
                args.add(conTimeoutStr);
                args.add(I(connectionTimeout));
                args.add(lineSeparator);

            }
        }

        logBuilder.append("Global Gmail Send properties successfully loaded!");
        LOG.info(logBuilder.toString(), args.toArray(new Object[args.size()]));
    }

    @Override
    protected void resetFields() {
        timeout = 0;
        connectionTimeout = 0;
        logTransport = false;
    }

    @Override
    public boolean isLogTransport() {
        return logTransport;
    }

    @Override
    public int getTimeout() {
        return timeout;
    }

    @Override
    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    @Override
    public int getReferencedPartLimit() {
        return transportProperties.getReferencedPartLimit();
    }

}
