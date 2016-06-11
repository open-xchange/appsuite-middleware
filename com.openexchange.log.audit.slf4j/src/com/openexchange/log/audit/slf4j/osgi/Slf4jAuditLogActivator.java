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

package com.openexchange.log.audit.slf4j.osgi;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.osgi.framework.ServiceRegistration;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Reloadable;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.java.Strings;
import com.openexchange.log.audit.AuditLogFilter;
import com.openexchange.log.audit.AuditLogService;
import com.openexchange.log.audit.slf4j.Slf4jAuditLogService;
import com.openexchange.log.audit.slf4j.Configuration;
import com.openexchange.log.audit.slf4j.SimpleDateFormatter;
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

    private volatile NearRegistryServiceTracker<AuditLogFilter> filters;

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
    protected void startBundle() throws Exception {
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
            public Map<String, String[]> getConfigFileNames() {
                Map<String, String[]> m = new HashMap<String, String[]>(2);
                m.put("slf4j-auditlog.properties", new String[] { "all properties in file" });
                return m;
            }
        };

        registerService();
        registerService(Reloadable.class, reloadable);
    }

    @Override
    protected void stopBundle() throws Exception {
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
            String layoutPattern = Strings.unquote(configService.getProperty("com.openexchange.log.audit.slf4j.file.pattern", "%message%n").trim());
            builder.fileLocation(fileLocation).fileCount(fileCount).fileLimit(fileSize).fileLayoutPattern(layoutPattern);
        }

        return builder.build();
    }

}
