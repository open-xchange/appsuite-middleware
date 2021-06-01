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

package com.openexchange.ajax.infostore.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.JSONException;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.groupware.container.FolderObject;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class AnotherCreateAndDeleteInfostoreTest extends AbstractInfostoreTest {

    public AnotherCreateAndDeleteInfostoreTest() {
        super();
    }

    @Test
    public void testCreatingOneItem() throws OXException, IOException, JSONException {
        FolderObject folder = generateInfostoreFolder("InfostoreCreateDeleteTest" + System.currentTimeMillis());
        ftm.insertFolderOnServer(folder);

        File expected = new DefaultFile();
        expected.setCreated(new Date());
        expected.setFolderId(String.valueOf(folder.getObjectID()));
        expected.setTitle("InfostoreCreateDeleteTest Item");
        expected.setLastModified(new Date());
        final Map<String, Object> meta = new LinkedHashMap<String, Object>(2);
        meta.put("customField0012", "value0012");
        meta.put("customField0013", Integer.valueOf(2));
        expected.setMeta(meta);

        itm.newAction(expected);
        {
            OXException exception = itm.getLastResponse().getException();
            if (null != exception) {
                fail("Creating an entry should work, but failed with an unexpected exception: " + exception.getMessage());
            }
        }

        File actual = itm.getAction(expected.getId());
        assertEquals("Name should be the same", expected.getTitle(), actual.getTitle());

        final Map<String, Object> actualMeta = actual.getMeta();
        assertTrue("Meta not available, but should", actualMeta != meta && !actualMeta.isEmpty());

        Object actualValue = actualMeta.get("customField0012");
        assertNotNull("Unexpected meta value", actualValue);
        assertEquals("Unexpected meta value", "value0012", actualValue.toString());

        actualValue = actualMeta.get("customField0013");
        assertNotNull("Unexpected meta value", actualValue);
        assertEquals("Unexpected meta value", "2", actualValue.toString());

        itm.deleteAction(expected);
        assertFalse("Deleting an entry should work", itm.getLastResponse().hasError());
    }

}
