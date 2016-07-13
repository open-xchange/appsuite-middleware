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

package com.openexchange.ajax.requesthandler;

import static org.junit.Assert.assertFalse;
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
