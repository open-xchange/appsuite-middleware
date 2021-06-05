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

package com.openexchange.contacts.json.mapping;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.contacts.json.actions.IDBasedContactAction;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;

/**
 * {@link ContactMapperTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.1
 */
public class ContactMapperTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
     @Test
     public void testDeserialize_distListWithTwoMembers_markAsDistList() throws OXException, JSONException {
        String json = "{\"distribution_list\":[{\"id\":2345,\"folder_id\":6,\"display_name\":\"Steffen Templin\",\"mail\":\"steffen.templin@premium\",\"mail_field\":1},{\"id\":3,\"folder_id\":6,\"display_name\":\"Marcus Klein\",\"mail\":\"marcus.klein@premium\",\"mail_field\":1}]}";
        Contact contact = ContactMapper.getInstance().deserialize(new JSONObject(json), ContactMapper.getInstance().getAllFields(IDBasedContactAction.VIRTUAL_FIELDS));
        Assert.assertTrue(contact.getMarkAsDistribtuionlist());
        Assert.assertEquals(2, contact.getNumberOfDistributionLists());
    }

    /**
     * Related to bug 42726
     */
     @Test
     public void testDeserialize_distListWithNoMember_markAsDistList() throws OXException, JSONException {
        String json = "{\"distribution_list\":[]}";
        Contact contact = ContactMapper.getInstance().deserialize(new JSONObject(json), ContactMapper.getInstance().getAllFields(IDBasedContactAction.VIRTUAL_FIELDS));
        Assert.assertTrue(contact.getMarkAsDistribtuionlist());
        Assert.assertEquals(0, contact.getNumberOfDistributionLists());
    }

}
