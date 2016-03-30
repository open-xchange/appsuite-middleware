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

package com.openexchange.halo.contacts;

import static org.junit.Assert.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.halo.HaloContactQuery;
import com.openexchange.halo.Picture;
import com.openexchange.server.MockingServiceLookup;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.SimServerSession;
import static org.mockito.Mockito.*;


/**
 * {@link ContactDataSourceTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ContactDataSourceTest {
    // TODO: Test regular halo lookup
    private MockingServiceLookup services = null;
    private ServerSession session = null;
    private ContactDataSource dataSource = null;
    
    @Before
    public void initialize() {
        services = new MockingServiceLookup();
        UserImpl user = new UserImpl();
        user.setId(42);
        user.setContactId(44);
        
        Context context = new SimContext(23);
        
        session = new SimServerSession(context, user, null);
    
        dataSource = new ContactDataSource(services);
    }
    
    // Picture Halo
    
    @Test
    public void shouldTakeAPictureFromTheMergedContacts() throws OXException, IOException {
        HaloContactQuery query = new HaloContactQuery();
        
        Contact c = new Contact();
        c.setImage1(new byte[]{1,2,3});
        c.setImageContentType("image/jpeg");
        
        query.setMergedContacts(Arrays.asList(c));
        
        Picture picture = dataSource.getPicture(query, session);
        
        assertNotNull(picture);
        InputStream stream = picture.getFileHolder().getStream();
        assertEquals(1, stream.read());
        assertEquals(2, stream.read());
        assertEquals(3, stream.read());
        assertEquals(-1, stream.read());
        
        stream.close();
    }
    
    @Test
    public void shouldPreferGlobalAddressBook() throws IOException, OXException {
        HaloContactQuery query = new HaloContactQuery();
        
        Contact c = new Contact();
        c.setImage1(new byte[]{1,2,3});
        c.setImageContentType("image/jpeg");
        c.setParentFolderID(6); // This is the global address folder, and should be preferred
        
        Contact c2 = new Contact();
        c2.setImage1(new byte[]{3,2,1});
        c2.setImageContentType("image/jpeg");
        c2.setParentFolderID(37);
        
        query.setMergedContacts(Arrays.asList(c2, c));
        
        Picture picture = dataSource.getPicture(query, session);
        
        assertNotNull(picture);
        InputStream stream = picture.getFileHolder().getStream();
        assertEquals(1, stream.read());
        assertEquals(2, stream.read());
        assertEquals(3, stream.read());
        assertEquals(-1, stream.read());
        
        stream.close();
        
    }
    
    @Test
    public void shouldPreferMoreRecentLastModified() throws OXException, IOException {
        HaloContactQuery query = new HaloContactQuery();
        
        Contact c = new Contact();
        c.setImage1(new byte[]{1,2,3});
        c.setImageContentType("image/jpeg");
        c.setParentFolderID(37); 
        c.setLastModified(new Date(10));
        
        
        Contact c2 = new Contact();
        c2.setImage1(new byte[]{3,2,1});
        c2.setImageContentType("image/jpeg");
        c2.setParentFolderID(37);
        c2.setLastModified(new Date(5));
        
        query.setMergedContacts(Arrays.asList(c2, c));
        
        Picture picture = dataSource.getPicture(query, session);
        
        assertNotNull(picture);
        InputStream stream = picture.getFileHolder().getStream();
        assertEquals(1, stream.read());
        assertEquals(2, stream.read());
        assertEquals(3, stream.read());
        assertEquals(-1, stream.read());
        
        stream.close();
    }
    
    @Test
    public void shouldTryToReloadContacts() throws OXException, IOException {
        HaloContactQuery query = new HaloContactQuery();
        
        Contact c = new Contact();
        c.setObjectID(12);
        c.setParentFolderID(37);
        
        Contact c2 = new Contact();
        c2.setObjectID(12);
        c2.setParentFolderID(37);
        c2.setImage1(new byte[]{1,2,3});
        c2.setImageContentType("image/jpeg");
        
        query.setMergedContacts(Arrays.asList(c));
        
        ContactService cs = services.mock(ContactService.class);
        when(cs.getContact(session, "37", "12")).thenReturn(c2);
        
        Picture picture = dataSource.getPicture(query, session);
        
        assertNotNull(picture);
        InputStream stream = picture.getFileHolder().getStream();
        assertEquals(1, stream.read());
        assertEquals(2, stream.read());
        assertEquals(3, stream.read());
        assertEquals(-1, stream.read());
        
        stream.close();
    }
    
    @Test
    public void shouldFallBackToOwnSearch() throws IOException, OXException {
        HaloContactQuery query = new HaloContactQuery();
        
        Contact c = new Contact();
        c.setObjectID(12);
        c.setParentFolderID(37);
        c.setEmail1("email1");
        c.setEmail2("email2");
        c.setEmail3("email3");
        
        Contact c2 = new Contact();
        c2.setObjectID(12);
        c2.setParentFolderID(37);
        c2.setImage1(new byte[]{1,2,3});
        c2.setImageContentType("image/jpeg");
        
        query.setContact(c);
        query.setMergedContacts(Arrays.asList(c));
        
        ContactService cs = services.mock(ContactService.class);
        when(cs.getContact(session, "37", "12")).thenReturn(c);
        
        when(cs.searchContacts(eq(session), searchFor("email1"), (ContactField[]) any())).thenReturn(SearchIteratorAdapter.createArrayIterator(new Contact[]{c}));
        when(cs.searchContacts(eq(session), searchFor("email2"), (ContactField[]) any())).thenReturn(SearchIteratorAdapter.createArrayIterator(new Contact[]{c}));
        when(cs.searchContacts(eq(session), searchFor("email3"), (ContactField[]) any())).thenReturn(SearchIteratorAdapter.createArrayIterator(new Contact[]{c, c2}));
        
        
        Picture picture = dataSource.getPicture(query, session);
        
        assertNotNull(picture);
        InputStream stream = picture.getFileHolder().getStream();
        assertEquals(1, stream.read());
        assertEquals(2, stream.read());
        assertEquals(3, stream.read());
        assertEquals(-1, stream.read());
        
        stream.close();
    }

    private ContactSearchObject searchFor(final String address) {
        return argThat(new BaseMatcher<ContactSearchObject>() {

            @Override
            public boolean matches(Object item) {
                if (!(item instanceof ContactSearchObject)) {
                    return false;
                }
                
                ContactSearchObject cso = (ContactSearchObject) item;
                if (!cso.isOrSearch()) {
                    return false;
                }
                return cso.getEmail1().equals(address) && cso.getEmail2().equals(address) && cso.getEmail3().equals(address);
            }

            @Override
            public void describeTo(Description description) {
            }
            
            
        });
    }
    
}
