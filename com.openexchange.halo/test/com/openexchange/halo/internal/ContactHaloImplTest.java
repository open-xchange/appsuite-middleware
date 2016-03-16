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

package com.openexchange.halo.internal;

import java.util.List;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.halo.HaloContactImageSource;
import com.openexchange.halo.HaloContactQuery;
import com.openexchange.halo.Picture;
import com.openexchange.halo.internal.ContactHaloImpl;
import com.openexchange.server.MockingServiceLookup;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.SimServerSession;
import com.openexchange.user.UserService;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;

/**
 * {@link ContactHaloImplTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */

public class ContactHaloImplTest {
    // TODO: Tests for regular halo
    
    // Tests for query construction
    
    private MockingServiceLookup services = null;
    private ServerSession session = null;
    private ContactHaloImpl halo = null;
    
    @Before
    public void initialize() {
        services = new MockingServiceLookup();
        UserImpl user = new UserImpl();
        user.setId(42);
        user.setContactId(44);
        
        Context context = new SimContext(23);
        
        session = new SimServerSession(context, user, null);
    
        halo = new ContactHaloImpl(services);
    }
    

    @Test
    public void shouldLoadAUserIfPossible() throws OXException {
        Contact contact = new Contact();
        contact.setInternalUserId(42);
        
        Contact c2 = new Contact();
        c2.setObjectID(44);
        
        UserService users = services.mock(UserService.class);
        
        when(users.getUser(42, session.getContext())).thenReturn(session.getUser());
        
        
        ContactService contacts = services.mock(ContactService.class);
        when(contacts.getUser(session, 42)).thenReturn(c2);
        
        HaloContactQuery query = halo.buildQuery(contact, session);
        
        User user = query.getUser();
        
        assertEquals(user, session.getUser());
    }
    
    @Test
    public void mergedContactsShouldContainTheUserEquivalentContact() throws OXException {
        Contact contact = new Contact();
        contact.setInternalUserId(42);
        
        Contact c2 = new Contact();
        c2.setObjectID(44);
        
        UserService users = services.mock(UserService.class);
        
        when(users.getUser(42, session.getContext())).thenReturn(session.getUser());
        
        
        ContactService contacts = services.mock(ContactService.class);
        when(contacts.getUser(session, 42)).thenReturn(c2);
        
        HaloContactQuery query = halo.buildQuery(contact, session);
        
        List<Contact> mergedContacts = query.getMergedContacts();
        
        assertEquals(mergedContacts.get(0), c2);
    }
    
    @Test
    public void shouldSearchUserByEmail1() throws OXException {
        Contact contact = new Contact();
        contact.setEmail1("francisco.laguna@open-xchange.com");
        
        Contact c2 = new Contact();
        c2.setObjectID(44);
        
        UserService users = services.mock(UserService.class);
        
        when(users.searchUser("francisco.laguna@open-xchange.com", session.getContext(), false)).thenReturn(session.getUser());
        
        
        ContactService contacts = services.mock(ContactService.class);
        when(contacts.getUser(session, 42)).thenReturn(c2);
        
        HaloContactQuery query = halo.buildQuery(contact, session);
        
        User user = query.getUser();
        
        assertEquals(user, session.getUser());
    }
    
    @Test
    public void shouldSearchUserByEmail2() throws OXException {
        Contact contact = new Contact();
        contact.setEmail1("francisco.laguna@open-xchange.com");
        contact.setEmail2("cisco@open-xchange.com");
        
        
        Contact c2 = new Contact();
        c2.setObjectID(44);
        
        UserService users = services.mock(UserService.class);
        
        when(users.searchUser("francisco.laguna@open-xchange.com", session.getContext(), false)).thenReturn(null);
        when(users.searchUser("cisco@open-xchange.com", session.getContext(), false)).thenReturn(session.getUser());
        
        
        ContactService contacts = services.mock(ContactService.class);
        when(contacts.getUser(session, 42)).thenReturn(c2);
        
        HaloContactQuery query = halo.buildQuery(contact, session);
        
        User user = query.getUser();
        
        assertEquals(user, session.getUser());
    }
    
    @Test
    public void shouldSearchUserByEmail3() throws OXException {
        Contact contact = new Contact();
        contact.setEmail1("francisco.laguna@open-xchange.com");
        contact.setEmail2("cisco@open-xchange.com");
        contact.setEmail3("franny@open-xchange.com");
        
        
        Contact c2 = new Contact();
        c2.setObjectID(44);
        
        UserService users = services.mock(UserService.class);
        
        when(users.searchUser("francisco.laguna@open-xchange.com", session.getContext(), false)).thenReturn(null);
        when(users.searchUser("cisco@open-xchange.com", session.getContext(), false)).thenReturn(null);
        when(users.searchUser("franny@open-xchange.com", session.getContext(), false)).thenReturn(session.getUser());
        
        
        ContactService contacts = services.mock(ContactService.class);
        when(contacts.getUser(session, 42)).thenReturn(c2);
        
        HaloContactQuery query = halo.buildQuery(contact, session);
        
        User user = query.getUser();
        
        assertEquals(user, session.getUser());
    }
    
    @Test
    public void shouldSearchContactByEmail1() throws OXException {
        Contact contact = new Contact();
        contact.setEmail1("francisco.laguna@open-xchange.com");
        contact.setEmail2("cisco@open-xchange.com");
        contact.setEmail3("franny@open-xchange.com");
        
        
        Contact c2 = new Contact();
        c2.setObjectID(44);
        
        Contact c3 = new Contact();
        c3.setObjectID(45);
        
        Contact c4 = new Contact();
        c4.setObjectID(46);
        
        UserService users = services.mock(UserService.class);
        
        when(users.searchUser("francisco.laguna@open-xchange.com", session.getContext(), false)).thenReturn(null);
        when(users.searchUser("cisco@open-xchange.com", session.getContext(), false)).thenReturn(null);
        when(users.searchUser("franny@open-xchange.com", session.getContext(), false)).thenReturn(null);
        
        
        ContactService contacts = services.mock(ContactService.class);
        when(contacts.searchContacts(eq(session), argThat(new BaseMatcher<ContactSearchObject>() {

            @Override
            public boolean matches(Object item) {
                if (! (item instanceof ContactSearchObject)) {
                    return false;
                }
                
                ContactSearchObject cso = (ContactSearchObject) item;
                
                return cso.getEmail1().equals("francisco.laguna@open-xchange.com") && cso.getEmail2().equals("francisco.laguna@open-xchange.com") && cso.getEmail3().equals("francisco.laguna@open-xchange.com");
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Mail addresses didn't match");
            }
        }))).thenReturn(SearchIteratorAdapter.createArrayIterator(new Contact[]{c2, c3, c4}));
        
        HaloContactQuery query = halo.buildQuery(contact, session);
        
        List<Contact> mergedContacts = query.getMergedContacts();
        
        assertEquals(mergedContacts.get(0), c2);
        assertEquals(mergedContacts.get(1), c3);
        assertEquals(mergedContacts.get(2), c4);
        
    }
    
    @Test
    public void shouldMergeAllSearchResults() throws OXException {
        Contact contact = new Contact();
        contact.setEmail1("francisco.laguna@open-xchange.com");
        contact.setEmail2("cisco@open-xchange.com");
        contact.setEmail3("franny@open-xchange.com");
        
        
        Contact c2 = new Contact();
        c2.setObjectID(44);
        c2.setGivenName("given_name");
        
        Contact c3 = new Contact();
        c3.setObjectID(45);
        c3.setMiddleName("middle_name");;
        
        Contact c4 = new Contact();
        c4.setObjectID(46);
        c4.setSurName("sur_name");;
        
        UserService users = services.mock(UserService.class);
        
        when(users.searchUser("francisco.laguna@open-xchange.com", session.getContext(), false)).thenReturn(null);
        when(users.searchUser("cisco@open-xchange.com", session.getContext(), false)).thenReturn(null);
        when(users.searchUser("franny@open-xchange.com", session.getContext(), false)).thenReturn(null);
        
        
        ContactService contacts = services.mock(ContactService.class);
        when(contacts.searchContacts(eq(session), argThat(new BaseMatcher<ContactSearchObject>() {

            @Override
            public boolean matches(Object item) {
                if (! (item instanceof ContactSearchObject)) {
                    return false;
                }
                
                ContactSearchObject cso = (ContactSearchObject) item;
                
                return cso.getEmail1().equals("francisco.laguna@open-xchange.com") && cso.getEmail2().equals("francisco.laguna@open-xchange.com") && cso.getEmail3().equals("francisco.laguna@open-xchange.com");
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Mail addresses didn't match");
            }
        }))).thenReturn(SearchIteratorAdapter.createArrayIterator(new Contact[]{c2, c3, c4}));
        
        HaloContactQuery query = halo.buildQuery(contact, session);
        
        Contact merged = query.getContact();
        
        assertEquals(merged.getEmail1(), "francisco.laguna@open-xchange.com");
        assertEquals(merged.getEmail2(), "cisco@open-xchange.com");
        assertEquals(merged.getEmail3(), "franny@open-xchange.com");
        assertEquals(merged.getGivenName(), "given_name");
        assertEquals(merged.getMiddleName(), "middle_name");
        assertEquals(merged.getSurName(), "sur_name");
    }
    
    // Tests for image halo
    private Contact setUpQueryables() throws OXException {
        Contact contact = new Contact();
        contact.setInternalUserId(42);
        
        Contact c2 = new Contact();
        c2.setObjectID(44);
        
        UserService users = services.mock(UserService.class);
        
        when(users.getUser(42, session.getContext())).thenReturn(session.getUser());
        
        
        ContactService contacts = services.mock(ContactService.class);
        when(contacts.getUser(session, 42)).thenReturn(c2);
        
        return contact;
    }
    
    @Test 
    public void shouldRetrievePicturesFromAnImageSourceUsingAQuery() throws OXException {
        Contact contact = setUpQueryables();
        HaloContactImageSource is = mock(HaloContactImageSource.class);
        Picture p = new Picture();
        
        when(is.isAvailable(session)).thenReturn(true);
        when(is.getPriority()).thenReturn(1000);
        when(is.getPicture(isQueryForCurrentUser(), eq(session))).thenReturn(p);
        
        halo.addContactImageSource(is);
        
        Picture returned = halo.getPicture(contact, session);
    
        assertEquals(p, returned);
    }
    
    @Test
    public void shouldPreferHigherPriority() throws OXException {
        Contact contact = setUpQueryables();
        HaloContactImageSource is = mock(HaloContactImageSource.class);
        HaloContactImageSource is2 = mock(HaloContactImageSource.class);

        Picture p = new Picture();
        Picture p2 = new Picture();
        
        
        when(is.isAvailable(session)).thenReturn(true);
        when(is.getPriority()).thenReturn(1000);
        when(is.getPicture(isQueryForCurrentUser(), eq(session))).thenReturn(p);
        
        when(is2.isAvailable(session)).thenReturn(true);
        when(is2.getPriority()).thenReturn(2000);
        when(is2.getPicture(isQueryForCurrentUser(), eq(session))).thenReturn(p2);
        
        halo.addContactImageSource(is);
        halo.addContactImageSource(is2);
        
        Picture returned = halo.getPicture(contact, session);
    
        assertEquals(p2, returned);
    }
    
    @Test
    public void shouldContinueToLowerPriorities() throws OXException {
        Contact contact = setUpQueryables();
        HaloContactImageSource is = mock(HaloContactImageSource.class);
        HaloContactImageSource is2 = mock(HaloContactImageSource.class);

        Picture p = new Picture();
        
        
        when(is.isAvailable(session)).thenReturn(true);
        when(is.getPriority()).thenReturn(1000);
        when(is.getPicture(isQueryForCurrentUser(), eq(session))).thenReturn(p);
        
        when(is2.isAvailable(session)).thenReturn(true);
        when(is2.getPriority()).thenReturn(2000);
        when(is2.getPicture(isQueryForCurrentUser(), eq(session))).thenReturn(null);
        
        halo.addContactImageSource(is);
        halo.addContactImageSource(is2);
        
        Picture returned = halo.getPicture(contact, session);
    
        assertEquals(p, returned);
    }
    
    @Test
    public void shouldSkipUnavailableSources() throws OXException {
        Contact contact = setUpQueryables();
        HaloContactImageSource is = mock(HaloContactImageSource.class);
        HaloContactImageSource is2 = mock(HaloContactImageSource.class);

        Picture p = new Picture();
        
        
        when(is.isAvailable(session)).thenReturn(true);
        when(is.getPriority()).thenReturn(1000);
        when(is.getPicture(isQueryForCurrentUser(), eq(session))).thenReturn(p);
        
        when(is2.isAvailable(session)).thenReturn(false);
        
        halo.addContactImageSource(is);
        halo.addContactImageSource(is2);
        
        Picture returned = halo.getPicture(contact, session);
    
        assertEquals(p, returned);
    }
    
    @Test
    public void shouldModifyETag() throws OXException {
        Contact contact = setUpQueryables();
        HaloContactImageSource is = mock(HaloContactImageSource.class);
        Picture p = new Picture();
        p.setEtag("etag");
        
        when(is.isAvailable(session)).thenReturn(true);
        when(is.getPriority()).thenReturn(1000);
        when(is.getPicture(isQueryForCurrentUser(), eq(session))).thenReturn(p);
        
        halo.addContactImageSource(is);
        
        Picture returned = halo.getPicture(contact, session);
    
        assertEquals(p, returned);
        
        assertEquals(is.getClass().getName() + "://etag", p.getEtag());
    }
    
    @Test
    public void shouldReturnNullWhenNoPictureCouldBeFound() throws OXException {
        Contact contact = setUpQueryables();
        HaloContactImageSource is = mock(HaloContactImageSource.class);
        
        when(is.isAvailable(session)).thenReturn(true);
        when(is.getPriority()).thenReturn(1000);
        when(is.getPicture(isQueryForCurrentUser(), eq(session))).thenReturn(null);
        
        halo.addContactImageSource(is);
        
        Picture returned = halo.getPicture(contact, session);
    
        assertTrue(returned == null);
    }
    
    private HaloContactQuery isQueryForCurrentUser() {
        return argThat(new BaseMatcher<HaloContactQuery>() {
            
            private Description description;

            @Override
            public boolean matches(Object item) {
                if (!(item instanceof HaloContactQuery)) {
                    description.appendText("item not of class HaloContactQuery");
                    return false;
                }
                
                HaloContactQuery query = (HaloContactQuery) item;
                User user = query.getUser();
                if (user == null) {
                    description.appendText("User was null");
                    return false;
                }
                
                if (user.getId() != session.getUserId()) {
                    description.appendText("User ID was " + user.getId() + " expected " + session.getUserId());
                    return false;
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                this.description = description;
            }
        });
    }
    
}
