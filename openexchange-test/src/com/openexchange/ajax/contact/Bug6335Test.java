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

package com.openexchange.ajax.contact;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.TimeZone;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.ajax.ContactTest;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.writer.ContactWriter;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;

/**
 * Tests if bug 6335 appears again in tasks.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Bug6335Test extends ContactTest {

    @Test
    public void testBug6335() throws Exception {

        final Contact contactObj = new Contact();
        contactObj.setSurName("\u001f");
        contactObj.setParentFolderID(contactFolderId);

        final JSONObject jsonObj = new JSONObject();
        final ContactWriter contactWriter = new ContactWriter(TimeZone.getDefault());
        contactWriter.writeContact(contactObj, jsonObj, null);

        cotm.newAction(contactObj);
        AbstractAJAXResponse resp = cotm.getLastResponse();
        
        assertTrue("Invalid character was not detected.", resp.hasError());
        //final OXException.Code code = OXException.Code.INVALID_DATA;
        final OXException exc = resp.getException();
        assertEquals("Wrong exception message.", Category.CATEGORY_USER_INPUT, exc.getCategory());
        assertEquals("Wrong exception message.", 168, exc.getCode());
    }
}
