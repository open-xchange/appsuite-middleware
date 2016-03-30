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

package com.openexchange.sessiond.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
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
    protected ConcurrentHashMap<String, Queue<String>> tokensPerSession = new ConcurrentHashMap<String, Queue<String>>();

    public RandomTokenContainerImpl(final String name, final Lifecycle lifecycle, final CleanUp<T> cleanUp) {
        this.name = name;
        this.lifecycle = lifecycle;
        this.cleanUp = cleanUp;
    }

    @Override
    public T get(final String token) {
        return delegate.get(token);
    }

    @Override
    public String rememberForSession(final Session session, final T value) {
        while(true) {
            final String token = UUIDSessionIdGenerator.randomUUID();
            final T original = delegate.putIfAbsent(token, value);
            if(original == null) {
                associate(session, token);
                return token;
            }
            // Try again.
        }
    }


    private void associate(final Session session, final String token) {
        final String sessionID = session.getSessionID();
        Queue<String> tokenList = tokensPerSession.get(sessionID);
        if(tokenList == null) {
            final Queue<String> tmp = new ConcurrentLinkedQueue<String>();
            tokenList = tokensPerSession.putIfAbsent(sessionID, tmp);
            if(tokenList == null) {
                tokenList = tmp;
            }
        }
        tokenList.add(token);
    }

    @Override
    public T remove(final String token) {
        final T removed = delegate.remove(token);
        if(removed != null && cleanUp != null) {
            cleanUp.clean(removed);
        }
        return removed;
    }

    public void clear(final CleanUp<T> overridingCleanUp) {
        final CleanUp<T> cleanUp = overridingCleanUp == null ? this.cleanUp : overridingCleanUp;
        final Collection<T> values = delegate.values();
        final Iterator<T> iterator = values.iterator();
        while(iterator.hasNext()) {
            final T value = iterator.next();
            if(cleanUp != null) {
                cleanUp.clean(value);
            }
            iterator.remove();
        }
    }

    public void removeForSession(final Session session) {
        final Queue<String> list = tokensPerSession.remove(session.getSessionID());
        if(list == null) {
            return;
        }
        for (final String token : list) {
            remove(token);
        }
    }

    public Lifecycle getLifecycle() {
        return lifecycle;
    }




}
