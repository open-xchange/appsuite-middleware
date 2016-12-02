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

import java.util.Collections;
import java.util.List;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ConfigurationExceptionCodes;
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
        return new String[] {
            "com.openexchange.davpush.gcm.enabled",
            "com.openexchange.davpush.gcm.transportId",
            "com.openexchange.davpush.gcm.transportUri",
            "com.openexchange.davpush.gcm.applicationId",
            "com.openexchange.davpush.gcm.gatewayUrl",
            "com.openexchange.davpush.gcm.refreshInterval",
        };
    }

    /**
     * Parses the configured and enabled transport options.
     *
     * @param configService The configuration service
     * @return The parsed transport options
     */
    public static List<PushTransportOptions> load(ConfigurationService configService) throws OXException {
        if (false == configService.getBoolProperty("com.openexchange.davpush.gcm.enabled", false)) {
            return null;
        }
        String transportURI = configService.getProperty("com.openexchange.davpush.gcm.transportUri", "https://push.davpush.com/push?GCM");
        String gatewayUrl = configService.getProperty("com.openexchange.davpush.gcm.gatewayUrl", "https://push.davpush.com/push/");
        String applicationID = configService.getProperty("com.openexchange.davpush.gcm.applicationId");
        if (Strings.isEmpty(applicationID)) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create("com.openexchange.davpush.gcm.applicationId");
        }
        String transportID = configService.getProperty("com.openexchange.davpush.gcm.transportId", "davpush-gcm");
        int refreshInterval = configService.getIntProperty("com.openexchange.davpush.gcm.refreshInterval", 172800);
        return Collections.singletonList(new PushTransportOptions(transportID, transportURI, gatewayUrl, applicationID, refreshInterval));
    }

    private final int refreshInterval;
    private final String applicationID;
    private final String transportURI;
    private final String gatewayUrl;
    private final String transportID;

    /**
     * Initializes a new {@link PushTransportOptions}.
     *
     * @param transportID The internal transport identifier
     * @param transportURI The URL to the push gateway
     * @param gatewayUrl The URL to the push gateway
     * @param applicationID The registered application identifier
     * @param refreshInterval The subscription refresh interval
     */
    public PushTransportOptions(String transportID, String transportURI, String gatewayUrl, String applicationID, int refreshInterval) {
        super();
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

}