/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.halo.contacts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import com.openexchange.contact.ContactID;
import com.openexchange.contact.picture.ContactPicture;
import com.openexchange.contact.provider.composition.IDBasedContactsAccess;
import com.openexchange.contact.provider.composition.IDBasedContactsAccessFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.groupware.ldap.UserImpl;
import com.openexchange.halo.HaloContactQuery;
import com.openexchange.server.MockingServiceLookup;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.SimServerSession;

/**
 * {@link ContactDataSourceTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
@RunWith(MockitoJUnitRunner.class)
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
        HaloContactQuery.Builder query = HaloContactQuery.builder();

        Contact c = new Contact();
        c.setImage1(new byte[] { 1, 2, 3 });
        c.setImageContentType("image/jpeg");
        c.setLastModified(new Date());

        query.withMergedContacts(Arrays.asList(c));

        ContactPicture picture = dataSource.getPicture(query.build(), session);

        assertNotNull(picture.getFileHolder());
        InputStream stream = picture.getFileHolder().getStream();
        assertEquals(1, stream.read());
        assertEquals(2, stream.read());
        assertEquals(3, stream.read());
        assertEquals(-1, stream.read());

        stream.close();
    }

    @Test
    public void shouldPreferGlobalAddressBook() throws IOException, OXException {
        HaloContactQuery.Builder query = HaloContactQuery.builder();

        Contact c = new Contact();
        c.setImage1(new byte[] { 1, 2, 3 });
        c.setImageContentType("image/jpeg");
        c.setParentFolderID(6); // This is the global address folder, and should be preferred
        c.setLastModified(new Date());

        Contact c2 = new Contact();
        c2.setImage1(new byte[] { 3, 2, 1 });
        c2.setImageContentType("image/jpeg");
        c2.setParentFolderID(37);
        c2.setLastModified(new Date());

        query.withMergedContacts(Arrays.asList(c2, c));

        ContactPicture picture = dataSource.getPicture(query.build(), session);

        assertNotNull(picture.getFileHolder());
        InputStream stream = picture.getFileHolder().getStream();
        assertEquals(1, stream.read());
        assertEquals(2, stream.read());
        assertEquals(3, stream.read());
        assertEquals(-1, stream.read());

        stream.close();

    }

    @Test
    public void shouldPreferMoreRecentLastModified() throws OXException, IOException {
        HaloContactQuery.Builder query = HaloContactQuery.builder();

        Contact c = new Contact();
        c.setImage1(new byte[] { 1, 2, 3 });
        c.setImageContentType("image/jpeg");
        c.setParentFolderID(37);
        c.setLastModified(new Date(10));

        Contact c2 = new Contact();
        c2.setImage1(new byte[] { 3, 2, 1 });
        c2.setImageContentType("image/jpeg");
        c2.setParentFolderID(37);
        c2.setLastModified(new Date(5));

        query.withMergedContacts(Arrays.asList(c2, c));

        ContactPicture picture = dataSource.getPicture(query.build(), session);

        assertNotNull(picture.getFileHolder());
        InputStream stream = picture.getFileHolder().getStream();
        assertEquals(1, stream.read());
        assertEquals(2, stream.read());
        assertEquals(3, stream.read());
        assertEquals(-1, stream.read());

        stream.close();
    }

    @Test
    public void shouldTryToReloadContacts() throws OXException, IOException {
        HaloContactQuery.Builder query = HaloContactQuery.builder();

        Contact c = new Contact();
        c.setObjectID(12);
        c.setParentFolderID(37);
        c.setLastModified(new Date());

        Contact c2 = new Contact();
        c2.setObjectID(12);
        c2.setParentFolderID(37);
        c2.setImage1(new byte[] { 1, 2, 3 });
        c2.setImageContentType("image/jpeg");
        c2.setLastModified(new Date());

        query.withMergedContacts(Arrays.asList(c));

        IDBasedContactsAccessFactory af = services.mock(IDBasedContactsAccessFactory.class);
        IDBasedContactsAccess ca = Mockito.mock(IDBasedContactsAccess.class);
        when(af.createAccess(session)).thenReturn(ca);
        when(ca.getContact(new ContactID("37", "12"))).thenReturn(c2);

        ContactPicture picture = dataSource.getPicture(query.build(), session);

        assertNotNull(picture.getFileHolder());
        InputStream stream = picture.getFileHolder().getStream();
        assertEquals(1, stream.read());
        assertEquals(2, stream.read());
        assertEquals(3, stream.read());
        assertEquals(-1, stream.read());

        stream.close();
    }

}
