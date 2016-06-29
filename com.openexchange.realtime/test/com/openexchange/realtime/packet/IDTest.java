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

package com.openexchange.realtime.packet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.lang.reflect.Field;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * {@link IDTest}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class IDTest {

    @BeforeClass
    public static void setUp() {
        ID.ID_MANAGER_REF.set(new IDManager());
    }

    @AfterClass
    public static void tearDown() {
        ID.ID_MANAGER_REF.set(null);
    }

    @Test
    public void testIDString() {
        ID newID = new ID("ox.some.component://some.user@context/resource");
        assertEquals("ox", newID.getProtocol());
        assertEquals("some.component", newID.getComponent());
        assertEquals("some.user", newID.getUser());
        assertEquals("context", newID.getContext());
        assertEquals("resource", newID.getResource());
        ID clonedID = new ID(newID.toString());
        assertEquals(newID, clonedID);
    }

    @Test
    public void testSyntheticIDFromToString() {
        String original = "synthetic.office://operations@premium/66499.62446";
        ID realEntity = new ID(original);
        assertEquals("synthetic", realEntity.getProtocol());
        assertEquals("office", realEntity.getComponent());
        assertEquals("operations", realEntity.getUser());
        assertEquals("premium", realEntity.getContext());
        assertEquals("66499.62446", realEntity.getResource());
        assertEquals(original, realEntity.toString());
        ID clonedID = new ID(realEntity.toString());
        assertEquals(realEntity, clonedID);
    }

    @Test
    public void testSyntheticIDToGeneralString() {
        String original = "synthetic.office://operations@premium/66499.62446";
        ID syntheticEntity = new ID(original);
        assertEquals("office://operations@premium", syntheticEntity.toGeneralForm().toString());
        ID clonedID = new ID(syntheticEntity.toString());
        assertEquals(syntheticEntity, clonedID);
    }

    @Test
    public void testStringConstructor() {
        String idString = "ox.some.component://some.body@context/762d2d9b-a949-418a-ac11-645a5b05038f";
        ID id = new ID(idString);
        assertEquals("some.component", id.getComponent());
        assertEquals("some.body", id.getUser());
        assertEquals("context", id.getContext());
        assertEquals("762d2d9b-a949-418a-ac11-645a5b05038f", id.getResource());
        ID clonedID = new ID(id.toString());
        assertEquals(id, clonedID);
    }

    @Test
    public void testRealIDFromToString() {
        String original = "ox://francisco.laguna@premium/20d39asd9da93249f009d";
        ID realEntity = new ID(original);
        assertEquals("ox", realEntity.getProtocol());
        assertNull(realEntity.getComponent());
        assertEquals("francisco.laguna", realEntity.getUser());
        assertEquals("premium", realEntity.getContext());
        assertEquals("20d39asd9da93249f009d", realEntity.getResource());
        assertEquals(original, realEntity.toString());
        ID clonedID = new ID(realEntity.toString());
        assertEquals(realEntity, clonedID);
    }

    @Test
    public void testRealIDToGeneralString() {
        String original = "ox://francisco.laguna@premium/20d39asd9da93249f009d";
        ID realEntity = new ID(original);
        assertEquals("francisco.laguna@premium", realEntity.toGeneralForm().toString());
        ID clonedID = new ID(realEntity.toString());
        assertEquals(realEntity, clonedID);
    }

    @Test
    public void testCallIDFromToString() {
        String original = "call://356c4ad6a4af46948f9703217a1f5a2d@internal";
        ID callEntity = new ID(original);
        assertEquals("call", callEntity.getProtocol());
        assertNull(callEntity.getComponent());
        assertEquals("356c4ad6a4af46948f9703217a1f5a2d", callEntity.getUser());
        assertEquals("internal", callEntity.getContext());
        assertNull(callEntity.getResource());
        assertEquals(original, callEntity.toString());
        ID clonedID = new ID(callEntity.toString());
        assertEquals(callEntity, clonedID);
    }

    @Test
    public void testDefaultContext() {
        ID newID = new ID("ox.component://user/resource", "context");
        assertEquals("ox", newID.getProtocol());
        assertEquals("component", newID.getComponent());
        assertEquals("user", newID.getUser());
        assertEquals("context", newID.getContext());
        assertEquals("resource", newID.getResource());
        ID clonedID = new ID(newID.toString());
        assertEquals(newID, clonedID);
    }

    @Test
    public void testIDStringObligatory() {
        ID newID = new ID("user@context");
        assertNull(newID.getProtocol());
        assertEquals("user", newID.getUser());
        assertEquals("context", newID.getContext());
        assertNull(newID.getResource());
        ID clonedID = new ID(newID.toString());
        assertEquals(newID, clonedID);
    }

    /**
     * ID creation has to fail with an IllegalArgumentException if user or context are missing from the String constructor.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIDWithDefaultContext() {
        new ID("thorben");
    }

    @Test
    public void testToString() throws Exception {
        String original = "ox://francisco.laguna@premium/20d39asd9da93249f009d";
        ID id = new ID(original);
        assertFalse(representationInSync(id));
        id.toString();
        assertTrue(representationInSync(id));

        id.setComponent("changedComponent");
        assertFalse(representationInSync(id));
        id.toString();
        assertTrue(representationInSync(id));

        id.setContext("changedContext");
        assertFalse(representationInSync(id));
        id.toString();
        assertTrue(representationInSync(id));

        id.setProtocol("changedProtocol");
        assertFalse(representationInSync(id));
        id.toString();
        assertTrue(representationInSync(id));

        id.setResource("changedResource");
        assertFalse(representationInSync(id));
        id.toString();
        assertTrue(representationInSync(id));

        id.setUser("changedUser");
        assertFalse(representationInSync(id));
        id.toString();
        assertTrue(representationInSync(id));

        assertEquals("changedProtocol.changedComponent://changedUser@changedContext/changedResource", id.toString());
    }

    private boolean representationInSync(ID id) throws Exception {
        Field loopMap = ID.class.getDeclaredField("cachedStringRepresentation");
        loopMap.setAccessible(true);
        return loopMap.get(id) != null;
    }
}
