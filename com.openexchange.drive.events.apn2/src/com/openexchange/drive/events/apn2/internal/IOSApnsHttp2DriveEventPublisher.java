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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.drive.events.apn2.internal;

import static com.openexchange.java.Autoboxing.I;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.drive.events.apn2.ApnsHttp2Options;
import com.openexchange.drive.events.apn2.ApnsHttp2Options.AuthType;
import com.openexchange.drive.events.apn2.ApnsHttp2OptionsProvider;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;


/**
 * {@link IOSApnsHttp2DriveEventPublisher}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
public class IOSApnsHttp2DriveEventPublisher extends ApnsHttp2DriveEventPublisher {
    
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IOSApnsHttp2DriveEventPublisher.class);

    /**
     * Initializes a new {@link IOSApnsHttp2DriveEventPublisher}.
     *
     * @param services The tracked OSGi services
     */
    public IOSApnsHttp2DriveEventPublisher(ServiceLookup services) {
        super(services);
    }

    @Override
    protected String getServiceID() {
        return "apn2";
    }

    @Override
    protected ApnsHttp2Options getOptions(int contextId, int userId) {
        LeanConfigurationService configService = services.getService(LeanConfigurationService.class);
        if (null == configService) {
            LOG.warn("Unable to get configuration service to determine options for push via {} for user {} in context {}.", getServiceID(), I(userId), I(contextId));
            return null;
        }
        /*
         * check if push via APNs HTTP/2 is enabled
         */
        if (false == configService.getBooleanProperty(userId, contextId, DriveEventsAPN2IOSProperty.enabled)) {
            LOG.trace("Push via {} is disabled for user {} in context {}.", getServiceID(), I(userId), I(contextId));
            return null;
        }
        AuthType authType = AuthType.authTypeFor(configService.getProperty(userId, contextId, DriveEventsAPN2IOSProperty.authtype));
        if (AuthType.CERTIFICATE.equals(authType)) {
            /*
             * get certificate options via config cascade
             */
            String keystoreName = configService.getProperty(userId, contextId, DriveEventsAPN2IOSProperty.keystore);
            if (Strings.isEmpty(keystoreName)) {
                LOG.info("Missing \"keystore\" APNS HTTP/2 option for drive events for context {}. Ignoring APNS HTTP/2 configuration for drive events.", I(contextId));
                return null;
            }
            LOG.trace("Using configured certificate options for push via {} for user {} in context {}.", getServiceID(), userId, contextId);
            String topic = configService.getProperty(userId, contextId, DriveEventsAPN2IOSProperty.topic);
            String password = configService.getProperty(userId, contextId, DriveEventsAPN2IOSProperty.password);
            boolean production = configService.getBooleanProperty(userId, contextId, DriveEventsAPN2IOSProperty.production);
            return new ApnsHttp2Options(new File(keystoreName), password, production, topic);
        } 
        if (AuthType.JWT.equals(authType)) {
            /*
             * get jwt options via config cascade
             */
            String privateKeyFile = configService.getProperty(userId, contextId, DriveEventsAPN2IOSProperty.privatekey);
            if (Strings.isEmpty(privateKeyFile)) {
                LOG.info("Missing \"privatekey\" APNS HTTP/2 option for drive events for context {}. Ignoring APNS HTTP/2 configuration for drive events.", I(contextId));
                return null;
            }
            LOG.trace("Using configured JWT options for push via {} for user {} in context {}.", getServiceID(), userId, contextId);
            String keyId = configService.getProperty(userId, contextId, DriveEventsAPN2IOSProperty.keyid);
            String teamId = configService.getProperty(userId, contextId, DriveEventsAPN2IOSProperty.teamid);
            String topic = configService.getProperty(userId, contextId, DriveEventsAPN2IOSProperty.topic);
            boolean production = configService.getBooleanProperty(userId, contextId, DriveEventsAPN2IOSProperty.production);
            try {
                return new ApnsHttp2Options(Files.readAllBytes(new File(privateKeyFile).toPath()), keyId, teamId, production, topic);
            } catch (IOException e) {
                LOG.error("Error instantiating APNS HTTP/2 options from {}", privateKeyFile, e);
                return null;
            }
        } 
        /*
         * get options via registered options provider as fallback, otherwise
         */
        ApnsHttp2OptionsProvider optionsProvider = services.getService(ApnsHttp2OptionsProvider.class);
        if (null != optionsProvider) {
            LOG.trace("Using registered fallback options push via {} for user {} in context {}.", getServiceID(), userId, contextId);
            return optionsProvider.getOptions();
        }
        LOG.trace("No valid options available for push via {} for user {} in context {}.", getServiceID(), userId, contextId);
        return null;
    }

}
