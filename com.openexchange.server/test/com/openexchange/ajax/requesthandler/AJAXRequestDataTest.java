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

package com.openexchange.ajax.requesthandler;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.mail.MailFolderImpl;
import com.openexchange.group.Group;

/**
 * {@link AJAXRequestDataTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
public class AJAXRequestDataTest {

    private Group group = new Group();

    @Before
    public void setUp() {
        group.setDisplayName("theDisplayName");
        group.setIdentifier(1111);
        group.setLastModified(new Date(1111111111L));
        group.setSimpleName("theSimpleName");
    }

     @Test
     public void testGetDataFromObject() throws OXException {
        AJAXRequestData requestData = new AJAXRequestData(group);
        Group data = requestData.getData(Group.class);

        assertNotNull(data);
        assertTrue(data.getDisplayName().equals("theDisplayName"));
        assertTrue(data.getIdentifier() == 1111);
        assertTrue(data.getLastModified().equals(new Date(1111111111L)));
    }

    @Test(expected = OXException.class)
     public void testGetDataFromObject_wrongClassDefinition_throwsException() throws OXException {
        AJAXRequestData requestData = new AJAXRequestData(group);

        requestData.getData(MailFolderImpl.class);
    }

     @Test
     public void testGetDataFromObject_classDefNull_returnNull() throws OXException {
        AJAXRequestData requestData = new AJAXRequestData(group);

        Group data = requestData.getData(null);

        assertNull(data);
    }

     @Test
     public void testGetDataFromObject_dataNull_returnNull() throws OXException {
        AJAXRequestData requestData = new AJAXRequestData();

        Group data = requestData.getData(Group.class);

        assertNull(data);
    }

     @Test
     public void testGetDataFromJSON_mostSet_returnPOJO() throws OXException {
        String groupJson = "{\"identifier\":1111,\"identifierSet\":true,\"simpleName\":\"theSimpleName\",\"simpleNameSet\":true,\"member\":[],\"memberSet\":false,\"displayName\":\"theDisplayName\",\"displayNameSet\":true,\"lastModified\":1111111111,\"lastModifiedSet\":true}";

        AJAXRequestData requestData = new AJAXRequestData(groupJson);
        Group data = requestData.getData(Group.class);

        assertNotNull(data);
        assertTrue(data.getDisplayName().equals("theDisplayName"));
        assertTrue(data.getIdentifier() == 1111);
        assertTrue(data.getLastModified().equals(new Date(1111111111L)));
        assertTrue(data.getSimpleName().equals("theSimpleName"));
        assertTrue(data.getMember().length == 0);
        assertTrue(data.isDisplayNameSet());
        assertTrue(data.isIdentifierSet());
        assertTrue(data.isLastModifiedSet());
        assertFalse(data.isMemberSet());
        assertTrue(data.isSimpleNameSet());
    }

     @Test
     public void testGetDataFromJSON_onlyLastModifiedSet_returnPOJO() throws OXException {
        String groupJson = "{\"lastModified\":1111111111}";

        AJAXRequestData requestData = new AJAXRequestData(groupJson);
        Group data = requestData.getData(Group.class);

        assertNotNull(data);
        assertNull(data.getDisplayName());
        assertTrue(data.getIdentifier() == -1);
        assertTrue(data.getLastModified().equals(new Date(1111111111L)));
        assertNull(data.getSimpleName());
        assertTrue(data.getMember().length == 0);

        assertFalse(data.isDisplayNameSet());
        assertFalse(data.isIdentifierSet());
        assertTrue(data.isLastModifiedSet());
        assertFalse(data.isMemberSet());
        assertFalse(data.isSimpleNameSet());
    }
}
