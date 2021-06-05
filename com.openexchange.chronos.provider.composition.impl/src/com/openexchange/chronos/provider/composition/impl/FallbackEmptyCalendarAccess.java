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

package com.openexchange.chronos.provider.composition.impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.provider.extensions.PersonalAlarmAware;
import com.openexchange.chronos.provider.extensions.WarningsAware;
import com.openexchange.chronos.provider.folder.FallbackFolderCalendarAccess;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.EventID;
import com.openexchange.exception.OXException;

/**
 * {@link FallbackEmptyCalendarAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public class FallbackEmptyCalendarAccess extends FallbackFolderCalendarAccess implements WarningsAware, PersonalAlarmAware {

    private final OXException error;

    /**
     * Initializes a new {@link FallbackEmptyCalendarAccess}.
     *
     * @param account The underlying calendar account
     * @param error The error to include in the accesses' warnings, or <code>null</code> if not defined
     */
    public FallbackEmptyCalendarAccess(CalendarAccount account, OXException error) {
        super(account);
        this.error = error;
    }

    @Override
    public List<OXException> getWarnings() {
        return null == error ? Collections.emptyList() : Collections.singletonList(error);
    }

    @Override
    public CalendarFolder getFolder(String folderId) throws OXException {
        throw CalendarExceptionCodes.FOLDER_NOT_FOUND.create(folderId, error);
    }

    @Override
    public List<CalendarFolder> getVisibleFolders() throws OXException {
        return Collections.emptyList();
    }

    @Override
    public CalendarResult updateAlarms(EventID eventID, List<Alarm> alarms, long clientTimestamp) throws OXException {
        throw unsupportedOperation();
    }

    @Override
    public List<AlarmTrigger> getAlarmTriggers(Set<String> actions) throws OXException {
        return Collections.emptyList();
    }

}

