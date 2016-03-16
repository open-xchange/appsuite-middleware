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

package com.openexchange.publish.json;

import static com.openexchange.json.JSONAssertion.assertValidates;
import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.i18n.Translator;
import com.openexchange.json.JSONAssertion;
import com.openexchange.publish.PublicationTarget;
import com.openexchange.publish.interfaces.UserSpecificPublicationTarget;

/**
 * {@link PublicationTargetWriterTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class PublicationTargetWriterTest extends TestCase {

    private PublicationTarget target;

    @Override
    public void setUp() {
        target = new PublicationTarget();

        target.setId("com.openexchange.publish.test1");
        target.setDisplayName("Test 1 PubTarget");
        target.setIcon("http://example.invalid/icon.png");
        target.setModule("contacts");

        target.setFormDescription(new DynamicFormDescription());
    }

    public void testWriteObject() throws JSONException {
        JSONObject object = new PublicationTargetWriter(Translator.EMPTY).write(target, null, null);

        JSONAssertion assertion = new JSONAssertion().isObject()
            .hasKey("id").withValue("com.openexchange.publish.test1")
            .hasKey("displayName").withValue("Test 1 PubTarget")
            .hasKey("icon").withValue("http://example.invalid/icon.png")
            .hasKey("module").withValue("contacts")
            .hasKey("formDescription").withValueArray()
       .hasNoMoreKeys();


        assertValidates(assertion, object);
    }

    public void testWriteArray() throws JSONException, OXException {
        JSONArray array = new PublicationTargetWriter(Translator.EMPTY).writeArray(target, new String[]{"id", "displayName", "icon", "module"}, null, null);

        JSONAssertion assertion = new JSONAssertion().isArray().withValues(target.getId(), target.getDisplayName(), target.getIcon(), target.getModule());

        assertValidates(assertion, array);
    }

    public void testUnknownColumn() throws JSONException {
        try {
            new PublicationTargetWriter(Translator.EMPTY).writeArray(target, new String[]{"id", "unkownColumn"}, null, null);
            fail("Expected exception");
        } catch (OXException e) {
            // Hooray!
        }
    }

    public void testWriteUserSpecificForm() throws JSONException {
        TestTarget target = new TestTarget();
        target.setId("com.openexchange.publish.test1");
        target.setDisplayName("Test 1 PubTarget");
        target.setIcon("http://example.invalid/icon.png");
        target.setModule("contacts");

        JSONObject object = new PublicationTargetWriter(Translator.EMPTY).write(target, null, null);

        JSONArray array = object.getJSONArray("formDescription");
        assertEquals(1, array.length());

    }

    public void testWriteUserSpecificFormInArray() throws OXException, JSONException {
        TestTarget target = new TestTarget();
        target.setId("com.openexchange.publish.test1");
        target.setDisplayName("Test 1 PubTarget");
        target.setIcon("http://example.invalid/icon.png");
        target.setModule("contacts");

        JSONArray array = new PublicationTargetWriter(Translator.EMPTY).writeArray(target, new String[]{"formDescription"}, null, null);

        JSONArray formDescription = array.getJSONArray(0);
        assertEquals(1, formDescription.length());


    }


    private static final class TestTarget extends PublicationTarget implements UserSpecificPublicationTarget {

        @Override
        public DynamicFormDescription getUserSpecificDescription(User user, UserPermissionBits permissionBits) {
            return new DynamicFormDescription().add(FormElement.input("userSpecific", "User Specific"));
        }

    }
}
