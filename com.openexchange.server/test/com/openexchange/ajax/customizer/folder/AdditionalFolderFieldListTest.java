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

package com.openexchange.ajax.customizer.folder;

import junit.framework.TestCase;


/**
 * {@link AdditionalFolderFieldListTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class AdditionalFolderFieldListTest extends TestCase {

    private SimFolderField field;
    private SimFolderField field2;
    private AdditionalFolderFieldList fields;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
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

    public void testLookup() {
        assertEquals(field, fields.get(12));
        assertEquals(field2, fields.get(13));
        assertEquals(field, fields.get("someField"));
        assertEquals(field2, fields.get("someOtherField"));

    }

    public void testColumnIDCollision() {
        // First come first serve
        SimFolderField collision = new SimFolderField();
        collision.setColumnId(field.getColumnID());
        fields.addField(collision);

        assertEquals(field, fields.get(12));
    }

    public void testColumnNameCollision() {
        // First come first serve
        SimFolderField collision = new SimFolderField();
        collision.setColumnName(field.getColumnName());
        fields.addField(collision);

        assertEquals(field, fields.get("someField"));
    }

    public void testKnows() {
        assertTrue(fields.knows(12));
        assertTrue(fields.knows("someField"));
        assertFalse(fields.knows(23));
        assertFalse(fields.knows("someUnknownField"));
    }

    public void testRemove() {
        fields.remove(12);
        assertFalse(fields.knows(12));
    }

    public void testNullField() {
        AdditionalFolderField field3 = fields.get(23);
        assertNotNull(field3);
        assertEquals(null, field3.getValue(null, null));
        assertEquals(null, field3.renderJSON(null, null));
    }

}
