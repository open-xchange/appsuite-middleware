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

package com.openexchange.calendar;

import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.CalendarFolderObject;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.oxfolder.OXFolderAccess;

/**
 * {@link FolderSearchAppointmentIterator}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class FolderSearchAppointmentIterator implements SearchIterator<CalendarDataObject> {

    private final SearchIterator<CalendarDataObject> delegate;

    private final CalendarFolderObject cfo;

    private CalendarDataObject next;

    private final int userId;

    private final OXFolderAccess folderAccess;

    private final Set<Integer> searchFolder;

    /**
     * Initializes a new {@link FolderSearchAppointmentIterator}.
     *
     * @param cci
     * @param searchFolder
     */
    public FolderSearchAppointmentIterator(SearchIterator<CalendarDataObject> cci, CalendarFolderObject cfo, Set<Integer> searchFolder, int userId, OXFolderAccess folderAccess) {
        super();
        this.delegate = cci;
        this.cfo = cfo;
        this.searchFolder = searchFolder;
        this.userId = userId;
        this.folderAccess = folderAccess;
        try {
            next = innerNext();
        } catch (OXException e) {
            next = null;
        }
    }

    @Override
    public boolean hasNext() throws OXException {
        return next != null;
    }

    @Override
    public CalendarDataObject next() throws OXException {
        CalendarDataObject retval = next;
        next = innerNext();
        return retval;
    }

    @Override
    public void close() {
        SearchIterators.close(delegate);
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException("Size can not be determined.");
    }

    @Override
    public boolean hasWarnings() {
        return delegate.hasWarnings();
    }

    @Override
    public void addWarning(OXException warning) {
        delegate.addWarning(warning);
    }

    @Override
    public OXException[] getWarnings() {
        return delegate.getWarnings();
    }

    private CalendarDataObject innerNext() throws OXException {
        while (delegate.hasNext()) {
            CalendarDataObject candidate = delegate.next();
            if (isVisible(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private boolean isVisible(CalendarDataObject candidate) throws OXException {
        int folderId = candidate.getParentFolderID();
        if (!searchFolder.contains(folderId)) {
            return false;
        }

        if (cfo.getPrivateFolders().contains(folderId)) {
            for (UserParticipant up : candidate.getUsers()) {
                if (up.getIdentifier() == userId) {
                    return true;
                }
            }
        }

        if (cfo.getSharedReadableAll().contains(folderId)) {
            if (!candidate.getPrivateFlag()) {
                int owner = folderAccess.getFolderOwner(folderId);
                for (UserParticipant up : candidate.getUsers()) {
                    if ((up.getIdentifier() == owner)) {
                        return true;
                    }
                }
            }
        }

        if (cfo.getSharedReadableOwn().contains(folderId)) {
            if (candidate.getCreatedBy() == userId && !candidate.getPrivateFlag()) {
                int owner = folderAccess.getFolderOwner(folderId);
                for (UserParticipant up : candidate.getUsers()) {
                    if (up.getIdentifier() == owner) {
                        return true;
                    }
                }
            }
        }

        if (cfo.getPublicReadableAll().contains(folderId)) {
            return true;
        }

        if (cfo.getPublicReadableOwn().contains(folderId)) {
            if (candidate.getCreatedBy() == userId) {
                return true;
            }
        }

        return false;
    }

}
