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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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
import java.sql.Connection;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.compat.Appointment2Event;
import com.openexchange.chronos.impl.StorageOperation;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.chronos.service.CalendarConfig;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.tools.oxfolder.OXFolderAccess;

/**
 * {@link CalendarConfigImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarConfigImpl implements CalendarConfig {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CalendarConfigImpl.class);

    private final CalendarSession session;

    /**
     * Initializes a new {@link CalendarConfigImpl}.
     *
     * @param session The underlying calendar session
     */
    public CalendarConfigImpl(CalendarSession session) {
        super();
        this.session = session;
    }

    /**
     * Gets a value indicating whether newly added group attendees should be resolved to their individual members, without preserving the
     * group reference, or not.
     *
     * @return <code>true</code> if group attendees should be resolved, <code>false</code>, otherwise
     */
    @Override
    public boolean isResolveGroupAttendees() {
        return getConfigValue("com.openexchange.chronos.resolveGroupAttendees", Boolean.class, Boolean.FALSE);
    }

    /**
     * Gets the identifier of a specific user's default personal calendar folder.
     *
     * @param userID The identifier of the user to retrieve the default calendar identifier for
     * @return The default calendar folder identifier
     */
    @Override
    public int getDefaultFolderID(int userID) throws OXException {
        return getFolderAccess().getDefaultFolderID(userID, FolderObject.CALENDAR);
    }

    /**
     * Gets the initial participation status to use for new events in a specific folder.
     *
     * @param folderType The folder type where the new event is located in
     * @param userID The identifier of the user to get the participation status for
     * @return The initial participation status, or {@link ParticipationStatus#NEEDS_ACTION} if not defined
     */
    @Override
    public ParticipationStatus getInitialPartStat(Type folderType, int userID) {
        Integer defaultStatus = null;
        try {
            if (PublicType.getInstance().equals(folderType)) {
                defaultStatus = getUserSettings().getDefaultStatusPublic(session.getContext().getContextId(), userID);
            } else {
                defaultStatus = getUserSettings().getDefaultStatusPrivate(session.getContext().getContextId(), userID);
            }
        } catch (OXException e) {
            LOG.warn("Error getting default participation status for user {}, type {}, falling back to \"{}\"",
                I(userID), folderType, ParticipationStatus.NEEDS_ACTION);
        }
        return null != defaultStatus ? Appointment2Event.getParticipationStatus(defaultStatus.intValue()) : ParticipationStatus.NEEDS_ACTION;
    }

    /**
     * Gets the configured minimum search pattern length.
     *
     * @return The minimum search pattern length, or <code>0</code> for no limitation
     */
    @Override
    public int getMinimumSearchPatternLength() throws OXException {
        return ServerConfig.getInt(ServerConfig.Property.MINIMUM_SEARCH_CHARACTERS);
    }

    /**
     * Gets a value indicating whether the legacy stack (with the entry point <code>com.openexchange.api2.AppointmentSQLInterface</code>)
     * stack should be used or not.
     *
     * @return <code>true</code> if the legacy stack should be used, <code>false</code>, otherwise
     */
    @Override
    public boolean isUseLegacyStack() {
        return getConfigValue("com.openexchange.chronos.useLegacyStack", Boolean.class, Boolean.FALSE);
    }

    /**
     * Gets the configured limit for the maximum calculated occurrences when expanding event series.
     *
     * @return The recurrence calculation limit
     */
    @Override
    public int getRecurrenceCalculationLimit() {
        return getConfigValue("com.openexchange.chronos.recurrenceCalculationLimit", Integer.class, I(1000)).intValue();
    }

    /**
     * Gets a value indicating whether <i>old</i> event series can be ignored when fetching events from the storage or not, i.e. series
     * where the recurrence calculation limit kicks in prior the actually requested timeframe.
     *
     * @return <code>true</code> if old event series can be ignored, <code>false</code>, otherwise
     */
    @Override
    public boolean isIgnoreSeriesPastCalculationLimit() {
        return getConfigValue("com.openexchange.chronos.ignoreSeriesPastCalculationLimit", Boolean.class, Boolean.FALSE);
    }

    /**
     * Gets the configured maximum number of conflicts between two recurring event series.
     *
     * @return The maximum conflicts per recurrence
     */
    @Override
    public int getMaxConflictsPerRecurrence() {
        return getConfigValue("com.openexchange.chronos.maxConflictsPerRecurrence", Integer.class, I(5)).intValue();
    }

    /**
     * Gets the configured maximum number of attendees to indicate per conflict.
     *
     * @return The the maximum number of attendees to indicate per conflict
     */
    @Override
    public int getMaxAttendeesPerConflict() {
        return getConfigValue("com.openexchange.chronos.maxAttendeesPerConflict", Integer.class, I(5)).intValue();
    }

    /**
     * Gets the overall maximum number of conflicts to return.
     *
     * @return The the maximum number of conflicts to return
     */
    @Override
    public int getMaxConflicts() {
        return getConfigValue("com.openexchange.chronos.maxConflicts", Integer.class, I(999)).intValue();
    }

    private OXFolderAccess getFolderAccess() {
        Connection connection = optConnection();
        return null != connection ? new OXFolderAccess(connection, session.getContext()) : new OXFolderAccess(session.getContext());
    }

    private ServerUserSetting getUserSettings() {
        Connection connection = optConnection();
        return null != connection ? ServerUserSetting.getInstance(connection) : ServerUserSetting.getInstance();
    }

    private Connection optConnection() {
        return session.get(StorageOperation.PARAM_CONNECTION, Connection.class, null);
    }

    private <T> T getConfigValue(String property, Class<T> coerceTo, T defaultValue) {
        try {
            ConfigView configView = Services.getService(ConfigViewFactory.class, true).getView(session.getUser().getId(), session.getContext().getContextId());
            return configView.opt(property, coerceTo, defaultValue);
        } catch (OXException e) {
            LOG.warn("Error getting \"{}\", falling back to \"{}\"", property, defaultValue);
            return defaultValue;
        }
    }

}
