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
import com.openexchange.chronos.impl.AbstractStorageOperation;
import com.openexchange.chronos.impl.StorageOperation;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.chronos.service.CalendarConfig;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSessionAdapter;

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

    @Override
    public boolean isResolveGroupAttendees() {
        return getConfigValue("com.openexchange.chronos.resolveGroupAttendees", Boolean.class, Boolean.FALSE).booleanValue();
    }

    @Override
    public String getDefaultFolderID(int userID) throws OXException {
        return String.valueOf(getFolderAccess().getDefaultFolderID(userID, FolderObject.CALENDAR));
    }

    @Override
    public ParticipationStatus getInitialPartStat(int userID, boolean inPublicFolder) {
        Integer defaultStatus = null;
        try {
            if (inPublicFolder) {
                defaultStatus = getUserSettings().getDefaultStatusPublic(session.getContextId(), userID);
            } else {
                defaultStatus = getUserSettings().getDefaultStatusPrivate(session.getContextId(), userID);
            }
        } catch (OXException e) {
            LOG.warn("Error getting default participation status for user {}, falling back to \"{}\"",
                I(userID), ParticipationStatus.NEEDS_ACTION);
        }
        return null != defaultStatus ? Appointment2Event.getParticipationStatus(defaultStatus.intValue()) : ParticipationStatus.NEEDS_ACTION;
    }

    @Override
    public int getMinimumSearchPatternLength() throws OXException {
        return ServerConfig.getInt(ServerConfig.Property.MINIMUM_SEARCH_CHARACTERS);
    }

    @Override
    public boolean isUseLegacyStack() {
        return getConfigValue("com.openexchange.chronos.useLegacyStack", Boolean.class, Boolean.FALSE).booleanValue();
    }

    @Override
    public boolean isUseLegacyStorage() {
        return isUseLegacyStack() || getConfigValue("com.openexchange.chronos.useLegacyStorage", Boolean.class, Boolean.TRUE).booleanValue();
    }

    @Override
    public boolean isReplayToLegacyStorage() {
        return false == isUseLegacyStack() && getConfigValue("com.openexchange.chronos.replayToLegacyStorage", Boolean.class, Boolean.FALSE).booleanValue();
    }

    @Override
    public int getRecurrenceCalculationLimit() {
        return getConfigValue("com.openexchange.chronos.recurrenceCalculationLimit", Integer.class, I(1000)).intValue();
    }

    @Override
    public boolean isIgnoreSeriesPastCalculationLimit() {
        return getConfigValue("com.openexchange.chronos.ignoreSeriesPastCalculationLimit", Boolean.class, Boolean.FALSE).booleanValue();
    }

    @Override
    public int getMaxConflictsPerRecurrence() {
        return getConfigValue("com.openexchange.chronos.maxConflictsPerRecurrence", Integer.class, I(5)).intValue();
    }

    @Override
    public int getMaxAttendeesPerConflict() {
        return getConfigValue("com.openexchange.chronos.maxAttendeesPerConflict", Integer.class, I(5)).intValue();
    }

    @Override
    public int getMaxConflicts() {
        return getConfigValue("com.openexchange.chronos.maxConflicts", Integer.class, I(999)).intValue();
    }

    @Override
    public boolean isSkipExternalAttendeeURIChecks() {
        return getConfigValue("com.openexchange.chronos.skipExternalAttendeeURIChecks", Boolean.class, Boolean.FALSE).booleanValue();
    }

    @Override
    public boolean isCollectEmailAddresses() {
        try {
            return getUserConfiguration().isCollectEmailAddresses();
        } catch (OXException e) {
            LOG.warn("Error getting user configuration to query if collection of e-mail addresses is enabled, assuming \"false\"");
            return false;
        }
    }

    private OXFolderAccess getFolderAccess() throws OXException {
        Connection connection = optConnection();
        Context context = ServerSessionAdapter.valueOf(session.getSession()).getContext();
        return null != connection ? new OXFolderAccess(connection, context) : new OXFolderAccess(context);
    }

    private ServerUserSetting getUserSettings() {
        Connection connection = optConnection();
        return null != connection ? ServerUserSetting.getInstance(connection) : ServerUserSetting.getInstance();
    }

    private Connection optConnection() {
        return session.get(AbstractStorageOperation.PARAM_CONNECTION, Connection.class, null);
    }

    private <T> T getConfigValue(String property, Class<T> coerceTo, T defaultValue) {
        try {
            ConfigView configView = Services.getService(ConfigViewFactory.class, true).getView(session.getUserId(), session.getContextId());
            return configView.opt(property, coerceTo, defaultValue);
        } catch (OXException e) {
            LOG.warn("Error getting \"{}\", falling back to \"{}\"", property, defaultValue);
            return defaultValue;
        }
    }

    private UserConfiguration getUserConfiguration() throws OXException {
        return ServerSessionAdapter.valueOf(session.getSession()).getUserConfiguration();
    }

}
