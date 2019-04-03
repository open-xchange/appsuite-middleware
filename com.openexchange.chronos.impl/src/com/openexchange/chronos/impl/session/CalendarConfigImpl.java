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

package com.openexchange.chronos.impl.session;

import static com.openexchange.java.Autoboxing.I;
import java.util.List;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Available;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.chronos.service.CalendarConfig;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link CalendarConfigImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarConfigImpl implements CalendarConfig {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CalendarConfigImpl.class);

    private final CalendarSession optSession;
    private final ServiceLookup services;
    private final int contextId;

    /**
     * Initializes a new {@link CalendarConfigImpl}.
     *
     * @param session The underlying calendar session
     * @param services A service lookup reference
     */
    public CalendarConfigImpl(CalendarSession session, ServiceLookup services) {
        this(session, session.getContextId(), services);
    }

    /**
     * Initializes a new {@link CalendarConfigImpl}.
     *
     * @param contextId The context identifier
     * @param services A service lookup reference
     */
    public CalendarConfigImpl(int contextId, ServiceLookup services) {
        this(null, contextId, services);
    }

    private CalendarConfigImpl(CalendarSession session, int contextId, ServiceLookup services) {
        super();
        this.contextId = contextId;
        this.services = services;
        this.optSession = session;
    }

    @Override
    public String getDefaultFolderId(int userId) throws OXException {
        return getUserSettings(userId).getDefaultFolderId();
    }

    @Override
    public ParticipationStatus getInitialPartStat(int userId, boolean inPublicFolder) {
        return getUserSettings(userId).getInitialPartStat(inPublicFolder);
    }

    @Override
    public List<Alarm> getDefaultAlarmDate(int userId) throws OXException {
        return getUserSettings(userId).getDefaultAlarmDate();
    }

    @Override
    public List<Alarm> getDefaultAlarmDateTime(int userId) throws OXException {
        return getUserSettings(userId).getDefaultAlarmDateTime();
    }

    @Override
    public Available[] getAvailability(int userId) throws OXException {
        return getUserSettings(userId).getAvailability();
    }

    @Override
    public boolean isResolveGroupAttendees() {
        return getConfigValue("com.openexchange.calendar.resolveGroupAttendees", Boolean.class, Boolean.FALSE).booleanValue();
    }

    @Override
    public int getMinimumSearchPatternLength() throws OXException {
        return ServerConfig.getInt(ServerConfig.Property.MINIMUM_SEARCH_CHARACTERS);
    }

    @Override
    public int getMaxConflictsPerRecurrence() {
        return getConfigValue("com.openexchange.calendar.maxConflictsPerRecurrence", Integer.class, I(5)).intValue();
    }

    @Override
    public int getMaxAttendeesPerConflict() {
        return getConfigValue("com.openexchange.calendar.maxAttendeesPerConflict", Integer.class, I(5)).intValue();
    }

    @Override
    public int getMaxConflicts() {
        return getConfigValue("com.openexchange.calendar.maxConflicts", Integer.class, I(100)).intValue();
    }

    @Override
    public boolean isSkipExternalAttendeeURIChecks() {
        return getConfigValue("com.openexchange.calendar.skipExternalAttendeeURIChecks", Boolean.class, Boolean.FALSE).booleanValue();
    }

    @Override
    public boolean isRestrictAllowedAttendeeChanges() {
        return getConfigValue("com.openexchange.calendar.restrictAllowedAttendeeChanges", Boolean.class, Boolean.TRUE).booleanValue();
    }
    
    @Override
    public boolean isOrganizerChangeAllowed() {
        return getConfigValue("com.openexchange.calendar.allowChangeOfOrganizer", Boolean.class, Boolean.FALSE).booleanValue();
    }

    private CalendarUserSettings getUserSettings(int userId) {
        if (null != optSession) {
            return new CalendarUserSettings(optSession, userId, services);
        }
        return new CalendarUserSettings(contextId, userId, services);
    }

    private <T> T getConfigValue(String property, Class<T> coerceTo, T defaultValue) {
        int userId = null == optSession ? -1 : optSession.getUserId();
        try {
            ConfigView configView = Services.getService(ConfigViewFactory.class, true).getView(userId, contextId);
            return configView.opt(property, coerceTo, defaultValue);
        } catch (OXException e) {
            LOG.warn("Error getting \"{}\", falling back to \"{}\"", property, defaultValue);
            return defaultValue;
        }
    }

}
