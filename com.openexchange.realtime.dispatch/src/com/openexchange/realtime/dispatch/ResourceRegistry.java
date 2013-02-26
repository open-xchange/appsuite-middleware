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

package com.openexchange.realtime.dispatch;

import com.openexchange.exception.OXException;
import com.openexchange.realtime.packet.ID;


/**
 * {@link ResourceRegistry}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public interface ResourceRegistry {
    
    /**
     * The topic for events that notify about resource registrations.
     */
    String TOPIC_REGISTERED = "com/openexchange/realtime/RESOURCE_REGISTERED";
    
    /**
     * The topic for events that notify about resource unregistrations.
     */
    String TOPIC_UNREGISTERED = "com/openexchange/realtime/RESOURCE_UNREGISTERED";
    
    /**
     * The key to receive the affected ID from an events properties.
     */
    String ID_PROPERTY = "com.openexchange.realtime.ID";
    
    /**
     * Registers a resource at the registry, so that the result of 
     * {@link #contains(ID)} returns <code>true</code>.<br>
     * <br>
     * The given {@link ID} must contain a valid resource identifier.
     *
     * @return <code>false</code> if the id was already registered, otherwise <code>true</code>.
     * @param id The {@link ID} that identifies the resource.
     * @throws OXException
     */
    boolean register(ID id) throws OXException;
    
    /**
     * Removes a resource from the registry. A subsequent call of 
     * {@link #contains(ID)} will return <code>false</code>.<br>
     * <br>
     * The given {@link ID} must contain a valid resource identifier.
     *
     * @return <code>false</code> if the registry did not contain the given id. Otherwise <code>true</code>.
     * @param id The {@link ID} that identifies the resource.
     * @throws OXException
     */
    boolean unregister(ID id) throws OXException;
    
    /**
     * Returns <code>true</code> if the given ID was registered at this registry.
     * Otherwise <code>false</code> is returned.
     *
     * @param id The {@link ID} that identifies the resource.
     */
    boolean contains(ID id);
    
    /**
     * Clears the whole registry.
     */
    void clear();

}
