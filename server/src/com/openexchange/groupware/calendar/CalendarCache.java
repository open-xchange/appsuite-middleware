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

import com.openexchange.cache.Configuration;
import com.openexchange.configuration.ConfigurationInit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;



/**
 * CalendarCache
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */

public class CalendarCache {
    
    private static CalendarCache instance;
    private static final String CACHE_NAME = "CalendarCache";
    private static final Log LOG = LogFactory.getLog(CalendarCache.class);
    
    private JCS jcs;
    
    private CalendarCache() {
        try {
            ConfigurationInit.init();
            Configuration.load();
            try {
                jcs = JCS.getInstance(CACHE_NAME);
            } catch (CacheException ce) {
                LOG.error("CalendarCache could not be initialized!", ce);
            }
        } catch (Exception e) {
            LOG.error("CalendarCache could not be initialized!", e);
        }
    }
    
    public synchronized static CalendarCache getInstance() {
        if (instance == null) {
            instance = new CalendarCache();
        }
        return instance;
    }
    
    public synchronized void add(final Object key, final String groupKey, final Object o) throws CacheException {
        jcs.putInGroup(key, groupKey, o);
    }
    
    public Object get(final Object key, final String groupKey) throws CacheException {
        return jcs.getFromGroup(key, groupKey);
    }
    
    public synchronized void clear() throws CacheException {
        jcs.clear();
    }
    
    public synchronized void invalidateGroup(int cid) {
        jcs.invalidateGroup(CalendarFolderObject.createGroupKeyFromContextID(cid));
    }
    
    public static final boolean isInitialized() {
        return instance != null;
    }
    
}
