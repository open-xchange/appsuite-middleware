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

package com.openexchange.log.audit.slf4j.osgi;

import java.util.Locale;
import java.util.TimeZone;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.java.Strings;
import com.openexchange.log.audit.AuditLogFilter;
import com.openexchange.log.audit.AuditLogService;
import com.openexchange.log.audit.slf4j.Configuration;
import com.openexchange.log.audit.slf4j.SimpleDateFormatter;
import com.openexchange.log.audit.slf4j.Slf4jAuditLogService;
import com.openexchange.log.audit.slf4j.Slf4jLogLevel;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.NearRegistryServiceTracker;

/**
 * {@link Slf4jAuditLogActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class Slf4jAuditLogActivator extends HousekeepingActivator {

    private ServiceRegistration<AuditLogService> serviceRegistration;
    private Slf4jAuditLogService service;

    private NearRegistryServiceTracker<AuditLogFilter> filters;

    /**
     * Initializes a new {@link Slf4jAuditLogActivator}.
     */
    public Slf4jAuditLogActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { ConfigurationService.class };
    }

    @Override
    protected synchronized void startBundle() throws Exception {
        NearRegistryServiceTracker<AuditLogFilter> filters = new FilterTracker(context);
        this.filters = filters;
        rememberTracker(filters);
        openTrackers();

        Reloadable reloadable = new Reloadable() {

            @Override
            public void reloadConfiguration(ConfigurationService configService) {
                unregisterService();
                registerService();
            }

            @Override
            public Interests getInterests() {
                return Reloadables.interestsForFiles("slf4j-auditlog.properties");
            }
        };

        registerService();
        registerService(Reloadable.class, reloadable);
    }

    @Override
    protected synchronized void stopBundle() throws Exception {
        unregisterService();
        super.stopBundle();
    }

    /**
     * Registers audit logging.
     */
    synchronized void registerService() {
        if (null == serviceRegistration) {
            try {
                ConfigurationService configService = getService(ConfigurationService.class);
                Configuration configuration = buildConfiguration(configService);
                if (configuration.isEnabled()) {
                    Slf4jAuditLogService service = Slf4jAuditLogService.initInstance(configuration, filters);
                    this.service = service;
                    serviceRegistration = context.registerService(AuditLogService.class, service, null);
                }
            } catch (Exception e) {
                org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Slf4jAuditLogActivator.class);
                logger.error("Failed to register {}", AuditLogService.class.getSimpleName(), e);
            }
        }
    }

    /**
     * Unregisters audit logging.
     */
    synchronized void unregisterService() {
        ServiceRegistration<AuditLogService> serviceRegistration = this.serviceRegistration;
        if (null != serviceRegistration) {
            this.serviceRegistration = null;
            serviceRegistration.unregister();
        }

        Slf4jAuditLogService service = this.service;
        if (null != service) {
            this.service = null;
            service.shutDown();
        }
    }

    private Configuration buildConfiguration(ConfigurationService configService) {
        Configuration.Builder builder = new Configuration.Builder();

        builder.enabled(configService.getBoolProperty("com.openexchange.log.audit.slf4j.enabled", false));
        builder.level(Slf4jLogLevel.valueFor(configService.getProperty("com.openexchange.log.audit.slf4j.level", Slf4jLogLevel.INFO.getId())));
        builder.delimiter(Strings.unquote(configService.getProperty("com.openexchange.log.audit.slf4j.delimiter", ", ").trim()));
        builder.includeAttributeNames(configService.getBoolProperty("com.openexchange.log.audit.slf4j.includeAttributeNames", true));

        String datePattern = configService.getProperty("com.openexchange.log.audit.slf4j.date.pattern");
        if (Strings.isNotEmpty(datePattern)) {
            Locale locale = LocaleTools.getLocale(configService.getProperty("com.openexchange.log.audit.slf4j.date.locale", "en_US").trim());
            TimeZone tz = TimeZone.getTimeZone(configService.getProperty("com.openexchange.log.audit.slf4j.date.timezone", "GMT").trim());
            builder.dateFormatter(SimpleDateFormatter.newInstance(datePattern, locale, tz));
        }

        String fileLocation = configService.getProperty("com.openexchange.log.audit.slf4j.file.location");
        if (Strings.isNotEmpty(fileLocation)) {
            int fileSize = Integer.parseInt(configService.getProperty("com.openexchange.log.audit.slf4j.file.size", "2097152").trim());
            int fileCount = Integer.parseInt(configService.getProperty("com.openexchange.log.audit.slf4j.file.count", "99").trim());
            String layoutPattern = Strings.unquote(configService.getProperty("com.openexchange.log.audit.slf4j.file.pattern", "%sanitisedMessage%n").trim());
            builder.fileLocation(fileLocation).fileCount(fileCount).fileLimit(fileSize).fileLayoutPattern(layoutPattern);
        }

        return builder.build();
    }

}
