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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.realtime.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.RealtimeExceptionCodes;
import com.openexchange.realtime.ResourceRegistry;
import com.openexchange.realtime.packet.ID;


/**
 * {@link ResourceRegistryImpl}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ResourceRegistryImpl implements ResourceRegistry {
    
    private final EventAdmin eventAdmin;
    
    private final Set<ID> ids;
    
    public ResourceRegistryImpl(EventAdmin eventAdmin) {
        super();
        this.eventAdmin = eventAdmin;
        ids = Collections.synchronizedSet(new HashSet<ID>());
    }

    @Override
    public boolean register(ID id) throws OXException {
        if (id == null) {
            throw new IllegalArgumentException("id was null.");
        }
        
        String resource = id.getResource();
        if (isInvalid(resource)) {
            throw RealtimeExceptionCodes.INVALID_ID.create();
        }
        
        if (ids.add(id)) {
            Event event = new Event(TOPIC_REGISTERED, Collections.singletonMap(ID_PROPERTY, id));
            eventAdmin.postEvent(event);
            return true;
        }
        
        return false;
    }

    @Override
    public boolean unregister(ID id) throws OXException {
        if (id == null) {
            throw new IllegalArgumentException("id was null.");
        }
        
        String resource = id.getResource();
        if (isInvalid(resource)) {
            throw RealtimeExceptionCodes.INVALID_ID.create();
        }
        
        if (ids.remove(id)) {
            Event event = new Event(TOPIC_UNREGISTERED, Collections.singletonMap(ID_PROPERTY, id));
            eventAdmin.postEvent(event);
            return true;
        }
        
        return false;
    }

    @Override
    public boolean contains(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("id was null.");
        }

        return ids.contains(id);
    }

    @Override
    public void clear() {
        Set<ID> eventIDs = new HashSet<ID>(ids);
        ids.clear();
        for (ID id : eventIDs) {
            Event event = new Event(TOPIC_UNREGISTERED, Collections.singletonMap(ID_PROPERTY, id));
            eventAdmin.postEvent(event);
        }
    }
    
    private static boolean isInvalid(String resource) {
        if (resource == null || resource.isEmpty()) {
            return true;
        }
        
        return false;
    }
}
