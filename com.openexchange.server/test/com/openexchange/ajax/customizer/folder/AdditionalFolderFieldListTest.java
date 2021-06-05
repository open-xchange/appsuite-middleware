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

package com.openexchange.ajax.customizer.folder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * {@link AdditionalFolderFieldListTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class AdditionalFolderFieldListTest {

    private SimFolderField field;
    private SimFolderField field2;
    private AdditionalFolderFieldList fields;

    @Before
    public void setUp() {
        field = new SimFolderField();
        field.setColumnId(12);
        field.setColumnName("someField");
        field.setJsonValue("jsonValue");
        field.setValue("someValue");

        field2 = new SimFolderField();
        field2.setColumnId(13);
        field2.setColumnName("someOtherField");
        field2.setJsonValue("otherJsonValue");
        field2.setValue("someOtherValue");

        fields = new AdditionalFolderFieldList();
        fields.addField(field);
        fields.addField(field2);

    }

    @Test
    public void testLookup() {
        assertEquals(field, fields.get(12));
        assertEquals(field2, fields.get(13));
        assertEquals(field, fields.get("someField"));
        assertEquals(field2, fields.get("someOtherField"));

    }

    @Test
    public void testColumnIDCollision() {
        // First come first serve
        SimFolderField collision = new SimFolderField();
        collision.setColumnId(field.getColumnID());
        fields.addField(collision);

        assertEquals(field, fields.get(12));
    }

    @Test
    public void testColumnNameCollision() {
        // First come first serve
        SimFolderField collision = new SimFolderField();
        collision.setColumnName(field.getColumnName());
        fields.addField(collision);

        assertEquals(field, fields.get("someField"));
    }

    @Test
    public void testKnows() {
        assertTrue(fields.knows(12));
        assertTrue(fields.knows("someField"));
        assertFalse(fields.knows(23));
        assertFalse(fields.knows("someUnknownField"));
    }

    @Test
    public void testRemove() {
        fields.remove(12);
        assertFalse(fields.knows(12));
    }

    @Test
    public void testNullField() {
        AdditionalFolderField field3 = fields.get(23);
        assertNotNull(field3);
        assertEquals(null, field3.getValue(null, null));
        assertEquals(null, field3.renderJSON(null, null));
    }

}
