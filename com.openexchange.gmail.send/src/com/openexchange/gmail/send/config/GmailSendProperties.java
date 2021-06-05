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
