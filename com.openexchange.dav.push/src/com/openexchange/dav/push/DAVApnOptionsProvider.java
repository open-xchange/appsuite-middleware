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

package com.openexchange.dav.push;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.pns.transport.apn.ApnOptionsPerClient;
import com.openexchange.pns.transport.apn.ApnOptionsProvider;

/**
 * {@link DAVApnOptionsProvider}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.4
 */
public class DAVApnOptionsProvider implements ApnOptionsProvider {

    public static String[] getPropertiesOfInterest() {
        List<String> properties = new ArrayList<String>();
        for (String client : new String[] { DAVPushUtility.CLIENT_CALDAV, DAVPushUtility.CLIENT_CARDDAV }) {
            properties.add("com.openexchange." + client + ".push.apsd.enabled");
            properties.add("com.openexchange." + client + ".push.apsd.bundleId");
            properties.add("com.openexchange." + client + ".push.apsd.keystore");
            properties.add("com.openexchange." + client + ".push.apsd.password");
            properties.add("com.openexchange." + client + ".push.apsd.production");
        }
        return properties.toArray(new String[properties.size()]);
    }

    private final Map<String, DAVApnOptions> options;

    public DAVApnOptionsProvider(ConfigurationService configService) throws OXException {
        super();
        this.options = new HashMap<String, DAVApnOptions>(2);
        DAVApnOptions caldavOptions = parseApnOptions(configService, DAVPushUtility.CLIENT_CALDAV);
        if (null != caldavOptions) {
            options.put(DAVPushUtility.CLIENT_CALDAV, caldavOptions);
        }
        DAVApnOptions carddavOptions = parseApnOptions(configService, DAVPushUtility.CLIENT_CARDDAV);
        if (null != carddavOptions) {
            options.put(DAVPushUtility.CLIENT_CARDDAV, carddavOptions);
        }
    }

    @Override
    public DAVApnOptions getOptions(String client) {
        return options.get(client);
    }

    @Override
    public Collection<ApnOptionsPerClient> getAvailableOptions() {
        Collection<ApnOptionsPerClient> availableOptions = new ArrayList<ApnOptionsPerClient>(options.size());
        for (Map.Entry<String, DAVApnOptions> entry : options.entrySet()) {
            availableOptions.add(new ApnOptionsPerClient(entry.getKey(), entry.getValue()));
        }
        return availableOptions;
    }

    private static DAVApnOptions parseApnOptions(ConfigurationService configService, String client) throws OXException {
        if (false == configService.getBoolProperty("com.openexchange." + client + ".push.apsd.enabled", false)) {
            return null;
        }
        String bundleId = configService.getProperty("com.openexchange." + client + ".push.apsd.bundleId");
        if (Strings.isEmpty(bundleId)) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create("com.openexchange." + client + ".push.apsd.bundleId");
        }
        String keystore = configService.getProperty("com.openexchange." + client + ".push.apsd.keystore");
        if (Strings.isEmpty(keystore)) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create("com.openexchange." + client + ".push.apsd.keystore");
        }
        String password = configService.getProperty("com.openexchange." + client + ".push.apsd.password");
        if (Strings.isEmpty(password)) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create("com.openexchange." + client + ".push.apsd.password");
        }
        boolean production = configService.getBoolProperty("com.openexchange." + client + ".push.apsd.production", true);
        int refreshInterval = configService.getIntProperty("com.openexchange." + client + ".push.apsd.production", 172800);
        return new DAVApnOptions(bundleId, keystore, password, production, refreshInterval);
    }

}
