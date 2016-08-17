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

package com.openexchange.contacts.json.mapping;

import static org.junit.Assert.*;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.contacts.json.actions.ContactAction;
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
    public void setUp() throws Exception {}

    @Test
    public void testDeserialize_distListWithTwoMembers_markAsDistList() throws OXException, JSONException {
        String json = "{\"distribution_list\":[{\"id\":2345,\"folder_id\":6,\"display_name\":\"Steffen Templin\",\"mail\":\"steffen.templin@premium\",\"mail_field\":1},{\"id\":3,\"folder_id\":6,\"display_name\":\"Marcus Klein\",\"mail\":\"marcus.klein@premium\",\"mail_field\":1}]}";
        Contact contact = ContactMapper.getInstance().deserialize(new JSONObject(json), ContactMapper.getInstance().getAllFields(ContactAction.VIRTUAL_FIELDS));
        Assert.assertTrue(contact.getMarkAsDistribtuionlist());
        Assert.assertEquals(2, contact.getNumberOfDistributionLists());
    }

    /**
     * Related to bug 42726
     */
    @Test
    public void testDeserialize_distListWithNoMember_markAsDistList() throws OXException, JSONException {
        String json = "{\"distribution_list\":[]}";
        Contact contact = ContactMapper.getInstance().deserialize(new JSONObject(json), ContactMapper.getInstance().getAllFields(ContactAction.VIRTUAL_FIELDS));
        Assert.assertTrue(contact.getMarkAsDistribtuionlist());
        Assert.assertEquals(0, contact.getNumberOfDistributionLists());
    }

}
