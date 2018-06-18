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

package com.openexchange.chronos.storage.rdb.migration;

import static com.openexchange.java.Autoboxing.I;
import java.util.Set;
import java.util.TreeSet;
import com.openexchange.chronos.exception.ProblemSeverity;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link MigrationConfig}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class MigrationConfig {

    private final ServiceLookup services;
    private final int batchSize;
    private final int maxTombstoneAgeInMonths;
    private final Set<String> nonSevereSeverities;
    private final boolean uncommitted;
    private final boolean intermediateCommits;

    /**
     * Initializes a new {@link MigrationConfig}.
     *
     * @param services A service lookup reference
     */
    public MigrationConfig(ServiceLookup services) {
        super();
        this.services = services;
        ConfigurationService configService = services.getService(ConfigurationService.class);
        batchSize = configService.getIntProperty("com.openexchange.calendar.migration.batchSize", 500);
        maxTombstoneAgeInMonths = configService.getIntProperty("com.openexchange.calendar.migration.maxTombstoneAgeInMonths", 12);
        nonSevereSeverities = getNonSevereSeverities(ProblemSeverity.MAJOR);
        uncommitted = configService.getBoolProperty("com.openexchange.calendar.migration.uncommitted", false);
        intermediateCommits = configService.getBoolProperty("com.openexchange.calendar.migration.intermediateCommits", false);
    }

    /**
     * Optionally gets an entity resolver for the supplied context.
     *
     * @param contextId The identifier of the context to get the entity resolver for
     * @return The entity resolver, or <code>null</code> if not available
     */
    public EntityResolver optEntityResolver(int contextId) {
        CalendarUtilities calendarUtilities = services.getOptionalService(CalendarUtilities.class);
        if (null != calendarUtilities) {
            try {
                return calendarUtilities.getEntityResolver(contextId);
            } catch (OXException e) {
                org.slf4j.LoggerFactory.getLogger(MigrationConfig.class).warn(
                    "Error getting entity resolver for context {}: {}", I(contextId), e.getMessage(), e);
            }
        }
        return null;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public int getMaxTombstoneAgeInMonths() {
        return maxTombstoneAgeInMonths;
    }

    public boolean isUncommitted() {
        return uncommitted;
    }

    public boolean isIntermediateCommits() {
        return intermediateCommits;
    }

    protected ServiceLookup getServiceLookup() {
        return services;
    }

    /**
     * Gets a value indicating whether the supplied migration warning represents an issue with a severity above the configured problem
     * severity threshold or not.
     *
     * @param warning The warning to check
     * @return <code>true</code> in case of a problem with a higher severity than the configured threshold, <code>false</code>, otherwise
     */
    public boolean isSevere(OXException warning) {
        if (0 < nonSevereSeverities.size() && "CAL-1990".equals(warning.getErrorCode())) {
            String severityProperty = warning.getProperty(ProblemSeverity.class.getName());
            if (null != severityProperty) {
                return false == nonSevereSeverities.contains(severityProperty);
            }
            Object[] logArgs = warning.getLogArgs();
            if (null != logArgs && 2 < logArgs.length && null != logArgs[2] && nonSevereSeverities.contains(String.valueOf(logArgs[2]))) {
                return false;
            }
        }
        return true;
    }

    private static Set<String> getNonSevereSeverities(ProblemSeverity severityThreshold) {
        Set<String> allowedSeverities = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        for (ProblemSeverity severity : ProblemSeverity.values()) {
            if (severity.equals(severityThreshold)) {
                break;
            }
            allowedSeverities.add(severity.name());
        }
        return allowedSeverities;
    }

    @Override
    public String toString() {
        return "MigrationConfig [batchSize=" + batchSize + ", maxTombstoneAgeInMonths=" + maxTombstoneAgeInMonths + ", uncommitted=" + uncommitted + "]";
    }

}

