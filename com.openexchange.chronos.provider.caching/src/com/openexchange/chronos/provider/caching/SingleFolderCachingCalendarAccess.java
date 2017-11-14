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

package com.openexchange.chronos.provider.caching;

import java.util.Collections;
import java.util.List;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 *
 * The {@link SingleFolderCachingCalendarAccess} is a default implementation for single folder usage that only requires to do the {@link Event} retrieval by implementing {@link #getAllEvents()}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public abstract class SingleFolderCachingCalendarAccess extends CachingCalendarAccess {

    /** The constant folder identifier for cached single folder calendar accesses */
    public static final String FOLDER_ID = "0";

    protected final CalendarFolder folder;

    /**
     * Initializes a new {@link SingleFolderCachingCalendarAccess}.
     *
     * @param session The user session
     * @param account The user calendar account
     * @param parameters The calendar parameters (for the given request)
     * @throws OXException
     */
    protected SingleFolderCachingCalendarAccess(Session session, CalendarAccount account, CalendarParameters parameters, CalendarFolder folder) throws OXException {
        super(session, account, parameters);
        this.folder = folder;
    }

    @Override
    public final CalendarFolder getFolder(String folderId) throws OXException {
        checkFolderId(folderId);
        return folder;
    }

    @Override
    public List<CalendarFolder> getVisibleFolders() throws OXException {
        return Collections.singletonList(folder);
    }

    @Override
    protected final long getRefreshInterval(String folderId) throws OXException {
        checkFolderId(folderId);
        return getRefreshInterval();
    }

    protected abstract long getRefreshInterval() throws OXException;

    @Override
    public final String updateFolder(String folderId, CalendarFolder folder, long clientTimestamp) throws OXException {
        checkFolderId(folderId);
        return updateFolder(folder, clientTimestamp);
    }

    protected abstract String updateFolder(CalendarFolder folder, long clientTimestamp) throws OXException;

    @Override
    public void handleExceptions(String folderId, OXException e) {
        try {
            checkFolderId(folderId);
        } catch (OXException e1) {
            return;
        }
        handleExceptions(e);
    }

    public abstract void handleExceptions(OXException e);

    /**
     * Returns a list of {@link Event}s by querying the underlying calendar and some meta information encapsulated within an {@link ExternalCalendarResult}
     *
     * @return {@link ExternalCalendarResult} containing the external events associated with the calendar account
     * @throws OXException
     */
    public abstract ExternalCalendarResult getAllEvents() throws OXException;

    @Override
    public final ExternalCalendarResult getAllEvents(String folderId) throws OXException {
        checkFolderId(folderId);
        ExternalCalendarResult externalCalendarResult = getAllEvents();
        if (!externalCalendarResult.isUpdated()) {
            return externalCalendarResult;
        }
        for (Event event : externalCalendarResult.getEvents()) {
            event.setFolderId(this.folder.getId());
        }
        return externalCalendarResult;
    }

    
    protected String checkFolderId(String folderId) throws OXException {
        if (false == folder.getId().equals(folderId)) {
            throw OXException.notFound(folderId);
        }
        return folderId;
    }
}
