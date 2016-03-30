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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.exception.OXException;
import com.openexchange.session.RandomTokenContainer;
import com.openexchange.session.Session;
import com.openexchange.session.SessionScopedContainer;
import com.openexchange.session.SessionSpecificContainerRetrievalService;
import com.openexchange.sessiond.event.SessiondEventListener;

/**
 * {@link SessiondSessionSpecificRetrievalService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SessiondSessionSpecificRetrievalService implements SessionSpecificContainerRetrievalService, SessiondEventListener {

    private final ConcurrentHashMap<String, SessionScopedContainerImpl<?>> containers = new ConcurrentHashMap<String, SessionScopedContainerImpl<?>>();
    private final ConcurrentHashMap<String, RandomTokenContainerImpl<?>> randomTokenContainer = new ConcurrentHashMap<String, RandomTokenContainerImpl<?>>();

    @Override
    public <T> SessionScopedContainer<T> getContainer(String name, Lifecycle lifecycle, InitialValueFactory<T> initial, CleanUp<T> cleanUp) {
        if (containers.containsKey(name)) {
            return (SessionScopedContainer<T>) containers.get(name);
        }
        if(lifecycle == null) {
            lifecycle = DEFAULT_LIFECYCLE;
        }
        SessionScopedContainerImpl<T> newValue = new SessionScopedContainerImpl<T>(name, lifecycle, initial, cleanUp, this);
        SessionScopedContainerImpl<?> other = containers.putIfAbsent(name, newValue);
        if (other != null) {
            return (SessionScopedContainer<T>) other;
        }
        return newValue;
    }

    @Override
    public void destroyContainer(String name, CleanUp cleanUp) {
        SessionScopedContainerImpl<?> removed = containers.remove(name);
        if (removed != null) {
            removed.clear(cleanUp);
        }
    }

    public void handleLifecycleChange(Session session, Lifecycle newLifecycle) {
        for (SessionScopedContainerImpl<?> container : containers.values()) {
            if (container.getLifecycle().includes(newLifecycle)) {
                container.remove(session);
            }
        }
        for(RandomTokenContainerImpl<?> container : randomTokenContainer.values()) {
            if(container.getLifecycle().includes(newLifecycle)) {
                container.removeForSession(session);
            }
        }
    }

    @Override
    public <T> RandomTokenContainer<T> getRandomTokenContainer(String name, Lifecycle lifecycle, CleanUp<T> cleanUp) {
        if (randomTokenContainer.containsKey(name)) {
            return (RandomTokenContainer<T>) randomTokenContainer.get(name);
        }
        if(lifecycle == null) {
            lifecycle = DEFAULT_LIFECYCLE;
        }
        RandomTokenContainerImpl<T> container = new RandomTokenContainerImpl<T>(name, lifecycle, cleanUp);
        RandomTokenContainerImpl<?> other = randomTokenContainer.putIfAbsent(name, container);
        if(other != null) {
            return (RandomTokenContainer<T>) other;
        }
        return container;
    }

    @Override
    public void destroyRandomTokenContainer(String name, CleanUp cleanUp) {
        RandomTokenContainerImpl<?> container = randomTokenContainer.get(name);
        container.clear(cleanUp);
    }

    // Event Handling

    @Override
    public void handleContainerRemoval(Map<String, Session> sessions) {
        for (Session session : sessions.values()) {
            handleLifecycleChange(session, Lifecycle.TERMINATE);
        }
    }

    @Override
    public void handleError(OXException error) {
        // IGNORE
    }

    @Override
    public void handleSessionDataRemoval(Map<String, Session> sessions) {
        for (Session session : sessions.values()) {
            handleLifecycleChange(session, Lifecycle.HIBERNATE);
        }
    }

    @Override
    public void handleSessionReactivation(Session session) {
        // IGNORE
    }

    @Override
    public void handleSessionRemoval(Session session) {
        handleLifecycleChange(session, Lifecycle.TERMINATE);
    }





}
