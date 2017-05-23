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

package com.openexchange.dav.push.gcm;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.dav.push.DAVPushUtility;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link PushTransportOptions}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.4
 */
public class PushTransportOptions {

    /**
     * Gets the configuration properties of interest.
     *
     * @return The properties of interest
     */
    public static String[] getPropertiesOfInterest() {
        List<String> properties = new ArrayList<String>();
        for (String client : new String[] { DAVPushUtility.CLIENT_CALDAV, DAVPushUtility.CLIENT_CARDDAV }) {
            properties.add("com.openexchange." + client + ".push.davpush.gcm.enabled");
            properties.add("com.openexchange." + client + ".push.davpush.gcm.transportId");
            properties.add("com.openexchange." + client + ".push.davpush.gcm.transportUri");
            properties.add("com.openexchange." + client + ".push.davpush.gcm.applicationId");
            properties.add("com.openexchange." + client + ".push.davpush.gcm.gatewayUrl");
            properties.add("com.openexchange." + client + ".push.davpush.gcm.refreshInterval");
        }
        return properties.toArray(new String[properties.size()]);
    }

    /**
     * Parses the configured and enabled transport options.
     *
     * @param configService The configuration service
     * @return The parsed transport options
     */
    public static List<PushTransportOptions> load(ConfigurationService configService) throws OXException {
        List<PushTransportOptions> options = new ArrayList<PushTransportOptions>(2);
        PushTransportOptions caldavOptions = parse(configService, DAVPushUtility.CLIENT_CALDAV);
        if (null != caldavOptions) {
            options.add(caldavOptions);
        }
        PushTransportOptions carddavOptions = parse(configService, DAVPushUtility.CLIENT_CARDDAV);
        if (null != carddavOptions) {
            options.add(carddavOptions);
        }
        return options;
    }

    private final int refreshInterval;
    private final String applicationID;
    private final String transportURI;
    private final String gatewayUrl;
    private final String transportID;
    private final String clientID;

    /**
     * Initializes a new {@link PushTransportOptions}.
     *
     * @param clientID The internal client identifier
     * @param transportID The internal transport identifier
     * @param transportURI The URL to the push gateway
     * @param gatewayUrl The URL to the push gateway
     * @param applicationID The registered application identifier
     * @param refreshInterval The subscription refresh interval
     */
    public PushTransportOptions(String clientID, String transportID, String transportURI, String gatewayUrl, String applicationID, int refreshInterval) {
        super();
        this.clientID = clientID;
        this.refreshInterval = refreshInterval;
        this.transportURI = transportURI;
        this.applicationID = applicationID;
        this.transportID = transportID;
        this.gatewayUrl = gatewayUrl;
    }

    /**
     * Gets the refreshInterval
     *
     * @return The refreshInterval
     */
    public int getRefreshInterval() {
        return refreshInterval;
    }

    /**
     * Gets the applicationID
     *
     * @return The applicationID
     */
    public String getApplicationID() {
        return applicationID;
    }

    /**
     * Gets the clientID
     *
     * @return The clientID
     */
    public String getClientID() {
        return clientID;
    }

    /**
     * Gets the transportURI
     *
     * @return The transportURI
     */
    public String getTransportURI() {
        return transportURI;
    }

    /**
     * Gets the transportID
     *
     * @return The transportID
     */
    public String getTransportID() {
        return transportID;
    }

    /**
     * Gets the gatewayUrl
     *
     * @return The gatewayUrl
     */
    public String getGatewayUrl() {
        return gatewayUrl;
    }

    private static PushTransportOptions parse(ConfigurationService configService, String client) throws OXException {
        if (false == configService.getBoolProperty("com.openexchange." + client + ".push.davpush.gcm.enabled", false)) {
            return null;
        }
        String transportID = configService.getProperty("com.openexchange." + client + ".push.davpush.gcm.transportId", "davpush-gcm");
        String transportURI = configService.getProperty("com.openexchange." + client + ".push.davpush.gcm.transportUri", "https://push.davpush.com/push?GCM");
        String applicationID = configService.getProperty("com.openexchange." + client + ".push.davpush.gcm.applicationId");
        if (Strings.isEmpty(applicationID)) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create("com.openexchange." + client + ".push.davpush.gcm.applicationId");
        }
        String gatewayUrl = configService.getProperty("com.openexchange." + client + ".push.davpush.gcm.gatewayUrl", "https://push.davpush.com/push/");
        int refreshInterval = configService.getIntProperty("com.openexchange." + client + ".push.davpush.gcm.refreshInterval", 172800);
        return new PushTransportOptions(client, transportID, transportURI, gatewayUrl, applicationID, refreshInterval);
    }

}