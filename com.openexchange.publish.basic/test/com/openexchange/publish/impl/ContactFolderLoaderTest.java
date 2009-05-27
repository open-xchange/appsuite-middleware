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

package com.openexchange.publish.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.api2.ContactSQLFactory;
import com.openexchange.api2.ContactSQLInterface;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.publish.Publication;
import com.openexchange.publish.PublicationException;
import com.openexchange.publish.services.SimContactSQLInterface;
import com.openexchange.session.Session;
import junit.framework.TestCase;


/**
 * {@link ContactFolderLoaderTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class ContactFolderLoaderTest extends TestCase {
    private ContactSQLFactory contactSQLFactory;
    private ContactFolderLoader contactLoader;
    private int cid;
    private int folderId;
    private int id1;
    private int id2;
    private Publication publication;

    public void setUp() {
        final SimContactSQLInterface contacts = new SimContactSQLInterface();
        
        cid = 1;
        folderId = 12;
        id1 = 1337;
        id2 = 1338;

        publication = new Publication();
        publication.setEntityId(String.valueOf(folderId));
        publication.setContext(new SimContext(cid));
        
        contacts.simulateContact(cid, folderId, id1, "Hans");
        contacts.simulateContact(cid, folderId, id2, "Peter");
        
            
        contactSQLFactory = new ContactSQLFactory() {

            public ContactSQLInterface create(Session session) throws AbstractOXException {
                return contacts;
            }
            
        };
        
        contactLoader = new ContactFolderLoader(contactSQLFactory);
    }
    
    public void testLoadFolder() throws PublicationException {
        Collection<? extends Object> collection = contactLoader.load(publication);
        
        assertNotNull("Collection was null", collection);
        
        assertEquals("Folder should contain two contacts", 2, collection.size());
        Set<Integer> expectedIds = new HashSet<Integer>(Arrays.asList(id1, id2));
        for (Object object : collection) {
            ContactObject contact = (ContactObject) object;
            assertTrue("Did not expect: "+contact.getObjectID(), expectedIds.remove(contact.getObjectID()));
        }
    }
    
}
