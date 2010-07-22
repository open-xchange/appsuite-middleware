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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;

/**
 * Goes through the results of another SearchIterator and anonymizes every appointment that is private,
 * unless it belongs to the user given.
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class AnonymizingIterator implements SearchIterator<CalendarDataObject> {

    private SearchIterator<CalendarDataObject> delegate;
    private int userID;

    public AnonymizingIterator(SearchIterator<CalendarDataObject> delegate, int userID) {
        super();
        this.delegate = delegate;
        this.userID = userID;
    }

    public void addWarning(AbstractOXException warning) {
        delegate.addWarning(warning);
    }

    public void close() throws SearchIteratorException {
        delegate.close();
    }

    public AbstractOXException[] getWarnings() {
        return delegate.getWarnings();
    }

    public boolean hasNext() {
        return delegate.hasNext();
    }

    public boolean hasSize() {
        return delegate.hasSize();
    }

    public boolean hasWarnings() {
        return delegate.hasWarnings();
    }

    public CalendarDataObject next() throws SearchIteratorException, OXException {
        CalendarDataObject app = delegate.next();
        if (!app.getPrivateFlag())
            return app;
        if(app.getPrivateFlag() && app.getCreatedBy() == userID)
            return app;

        CalendarDataObject anonymized = app.clone();
        anonymized.setTitle("Private");
        anonymized.removeAlarm();
        anonymized.removeCategories();
        anonymized.removeConfirm();
        anonymized.removeConfirmMessage();
        anonymized.removeLabel();
        anonymized.removeLocation();
        anonymized.removeNote();
        anonymized.removeNotification();
        anonymized.removeParticipants();
        anonymized.removeShownAs();
        anonymized.removeUsers();
        return anonymized;
    }

    public int size() {
        return delegate.size();
    }
}
