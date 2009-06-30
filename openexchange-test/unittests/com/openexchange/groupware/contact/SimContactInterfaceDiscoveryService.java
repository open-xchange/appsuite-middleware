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

package com.openexchange.groupware.contact;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import com.openexchange.api2.OXException;
import com.openexchange.session.Session;


/**
 * {@link SimContactInterfaceDiscoveryService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class SimContactInterfaceDiscoveryService implements ContactInterfaceDiscoveryService {

    private Map<Integer, ContactInterface> registered = new HashMap<Integer, ContactInterface>();
    private ContactInterface defaultContactInterface;
    
    public ContactInterfaceProvider getContactInterfaceProvider(int folderId, int contextId) throws OXException {
        return new StaticContactInterfaceProvider(newContactInterface(folderId, null));
    }

    public boolean hasSpecificContactInterface(int folderId, int contextId) {
        return registered.containsKey(folderId);
    }

    public ContactInterface newContactInterface(int folderId, Session session) throws OXException {
        return (hasSpecificContactInterface(folderId, -1)) ? registered.get(folderId) : newDefaultContactInterface(session);
    }

    public ContactInterface newDefaultContactInterface(Session session) throws OXException {
        return defaultContactInterface;
    }
    
    public void register(ContactInterface contacts, int folderId) {
        this.registered.put(folderId, contacts);
    }
    
    public void setDefaultContactInterface(ContactInterface defaultContactInterface) {
        this.defaultContactInterface = defaultContactInterface;
    }
    
    private static final class StaticContactInterfaceProvider implements ContactInterfaceProvider {
        private ContactInterface contacts;
        
        public StaticContactInterfaceProvider(ContactInterface contacts) {
            this.contacts = contacts;
        }
        
        public ContactInterface newContactInterface(Session session) throws OXException {
            return contacts;
        }
        
    }

    public List<ContactInterfaceProviderRegistration> getRegistrations(int contextId) {
        Set<Entry<Integer,ContactInterface>> entrySet = registered.entrySet();
        List<ContactInterfaceProviderRegistration> registrations = new LinkedList<ContactInterfaceProviderRegistration>();
        for (Entry<Integer, ContactInterface> entry : entrySet) {
           registrations.add(new ContactInterfaceProviderRegistration(entry.getKey(), new StaticContactInterfaceProvider(entry.getValue())));
        }
        
        return registrations;
    }

}
