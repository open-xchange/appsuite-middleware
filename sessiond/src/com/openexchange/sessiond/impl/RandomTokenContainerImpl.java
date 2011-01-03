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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.sessiond.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import com.openexchange.session.RandomTokenContainer;
import com.openexchange.session.Session;
import com.openexchange.session.SessionSpecificContainerRetrievalService.CleanUp;
import com.openexchange.session.SessionSpecificContainerRetrievalService.Lifecycle;


/**
 * {@link RandomTokenContainerImpl}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class RandomTokenContainerImpl<T> implements RandomTokenContainer<T> {

    protected CleanUp<T> cleanUp;
    protected Lifecycle lifecycle;
    protected String name;
    
    protected ConcurrentHashMap<String, T> delegate = new ConcurrentHashMap<String, T>();
    protected ConcurrentHashMap<String, List<String>> tokensPerSession = new ConcurrentHashMap<String, List<String>>();
    
    public RandomTokenContainerImpl(String name, Lifecycle lifecycle, CleanUp<T> cleanUp) {
        this.name = name;
        this.lifecycle = lifecycle;
        this.cleanUp = cleanUp;
    }
    
    public T get(String token) {
        return delegate.get(token);
    }

    public String rememberForSession(Session session, T value) {
        while(true) {
            String token = UUIDSessionIdGenerator.randomUUID();
            T original = delegate.putIfAbsent(token, value);
            if(original == null) {
                associate(session, token);
                return token;
            }
            // Try again.
        }
    }


    private void associate(Session session, String token) {
        String sessionID = session.getSessionID();
        List<String> tokenList = tokensPerSession.get(sessionID);
        if(tokenList == null) {
            tokenList = new CopyOnWriteArrayList<String>();
            List<String> other = tokensPerSession.putIfAbsent(sessionID, tokenList);
            if(other != null) {
                tokenList = other;
            }
        }
        
        tokenList.add(token);
    }

    public T remove(String token) {
        T removed = delegate.remove(token);
        if(removed != null && cleanUp != null) {
            cleanUp.clean(removed);
        }
        return removed;
    }
    
    public void clear(CleanUp<T> overridingCleanUp) {
        if(overridingCleanUp == null) {
            overridingCleanUp = this.cleanUp;
        }
        Collection<T> values = delegate.values();
        
        Iterator<T> iterator = values.iterator();
        while(iterator.hasNext()) {
            T value = iterator.next();
            if(overridingCleanUp != null) {
                overridingCleanUp.clean(value);
            }
            iterator.remove();
        }
        
    }

    public void removeForSession(Session session) {
        List<String> list = tokensPerSession.remove(session.getSessionID());
        if(list == null) {
            return;
        }
        for (String token : list) {
            remove(token);
        }
    }

    public Lifecycle getLifecycle() {
        return lifecycle;
    }

    
    

}
