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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.groupware.calendar;

import com.openexchange.api2.OXException;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import java.util.ArrayList;

/**
 * CachedCalendarIterator
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */
public class CachedCalendarIterator implements SearchIterator {
    
    private ArrayList<CalendarDataObject> list;
    private SearchIterator non_cached_iterator;
    private boolean cache = false;
    private int counter = 0;
    private boolean closed = false;
    
    public CachedCalendarIterator(SearchIterator non_cached_iterator) throws SearchIteratorException, OXException {
        list = new ArrayList<CalendarDataObject>(16);
        this.non_cached_iterator = non_cached_iterator;        
        cache = ServerConfig.getBoolean(Property.PrefetchEnabled);
        if (cache) {
            fillCachedResultSet();
        }
    }
    
    public boolean hasNext() {
        if (!cache) {
            return non_cached_iterator.hasNext();
        } else {
            if (list.size() > 0 && counter < list.size()) {
                return true;
            }
        }
        return false;
    }
    
    public Object next() throws SearchIteratorException, OXException {
        if (!cache) {
            return non_cached_iterator.next();
        } else {
            if (hasNext()) {
                return list.get(counter++);
            }
        }
        return null;
    }
    
    public void close() throws SearchIteratorException {
        if (closed) {
            return;
        }
        closed = true;
        non_cached_iterator.close();
    }
    
    public int size() {
        return non_cached_iterator.size();
    }
    
    public boolean hasSize() {
        return non_cached_iterator.hasSize();
    }
    
    private final void fillCachedResultSet() throws SearchIteratorException, OXException {
        try {
            while (non_cached_iterator.hasNext()) {
                list.add((CalendarDataObject)non_cached_iterator.next());
            }
        } finally {
            close();
        }
    }
    
}
