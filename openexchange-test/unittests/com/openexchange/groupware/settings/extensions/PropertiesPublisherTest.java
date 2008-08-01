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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
package com.openexchange.groupware.settings.extensions;

import junit.framework.TestCase;

import java.util.Properties;
import java.util.List;

import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.settings.SettingException;

import java.util.Arrays;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class PropertiesPublisherTest extends TestCase {

    public void testShouldPublishProperties() {
        Properties p = new Properties();
        p.setProperty("a/certain/path", "value1");
        p.setProperty("another/path", "value2");
        p.setProperty("and/yet/another", "value3");

        List items = publish(p);

        assertEquals(p.size(), items.size());

        for(Object item : items) {
            PreferencesItemService prefItem = (PreferencesItemService) item;
            String[] path = prefItem.getPath();
            assertNotNull(path);
            assertTrue("Invalid Path: "+ Arrays.asList(path).toString(), path.length > 1);
            String firstSegment = path[0];
            if(firstSegment.equals("a")) {
                assertEquals(3, path.length);
                assertEquals("certain", path[1]);
                assertEquals("path", path[2]);
                assertValue(prefItem, "value1");
            } else if (firstSegment.equals("another")) {
                assertEquals(2, path.length);
                assertEquals("path", path[1]);
                assertValue(prefItem, "value2");
            } else if (firstSegment.equals("and")) {
                assertEquals(3, path.length);
                assertEquals("yet", path[1]);
                assertEquals("another", path[2]);
                assertValue(prefItem, "value3");
            } else {
                fail("Invalid Path: "+ Arrays.asList(path).toString());
            }

        }

    }

    public void testShouldParseMultiValues() {
        Properties p = new Properties();
        p.setProperty("multivalue", "[value1, va\\,lue2, value3]");

        List items = publish(p);
        assertEquals(1, items.size());

        PreferencesItemService prefItem = (PreferencesItemService) items.get(0);
        assertMultiValue(prefItem, "value1", "va,lue2", "value3");

    }

    public void testShouldAllowEscaping() {
        Properties p = new Properties();
        p.setProperty("value", "\\[value1\\, v\\alue2, \\\\value3\\]");

        List items = publish(p);
        assertEquals(1, items.size());

        PreferencesItemService prefItem = (PreferencesItemService) items.get(0);
        assertValue(prefItem, "[value1, value2, \\value3]");

    }

    public void testShouldBeUpdateable() {
        Properties p = new Properties();
        p.setProperty("remove", "value1");
        p.setProperty("leaveAsItIs", "static");
        p.setProperty("update", "value2");
        p.setProperty("updateMultiple", "[value3, value4]");
        p.setProperty("updateFromSingleToMulti","value5");
        p.setProperty("updateFromMultiToSingle", "[value6, value7]");

        MockServiceRegistry registry = new MockServiceRegistry();
        PropertiesPublisher publisher = new PropertiesPublisher();
        publisher.setServicePublisher(registry);

        publisher.publish( p );

        Properties update = new Properties();
        update.setProperty("add", "valueUpdate1");
        update.setProperty("update", "valueUpdate2");
        update.setProperty("updateMultiple", "[valueUpdate3, valueUpdate4]");
        update.setProperty("updateFromSingleToMulti","[valueUpdate5, valueUpdate6]");
        update.setProperty("updateFromMultiToSingle", "valueUpdate7");
        update.setProperty("leaveAsItIs", "static");

        registry.clearHistory();
        publisher.publish( update );

        List items = registry.getAllServices(PreferencesItemService.class);
        for(Object service : items) {
            PreferencesItemService prefItem = (PreferencesItemService) service;
            String name = prefItem.getPath()[0];
            if("add".equals(name)) {
                assertValue(prefItem, "valueUpdate1");
            } else if ("update".equals(name)) {
                assertValue(prefItem, "valueUpdate2");
            } else if ("updateMultiple".equals(name)) {
                assertMultiValue(prefItem, "valueUpdate3", "valueUpdate4");
            } else if ("updateFromSingleToMulti".equals(name)) {
                assertMultiValue(prefItem, "valueUpdate5", "valueUpdate6");
            } else if ("updateFromMultiToSingle".equals(name)) {
                assertValue(prefItem, "valueUpdate7");
            } else if ("leaveAsItIs".equals(name)) {
                assertValue(prefItem, "static");
            } else {
                fail("I didn't expect "+name);
            }
        }

        List added = registry.getAdded(PreferencesItemService.class);
        assertEquals(added.toString(), 1, added.size());
        PreferencesItemService addedItem = (PreferencesItemService)added.get(0);
        assertEquals("add", addedItem.getPath()[0]);

        List removed = registry.getRemoved(PreferencesItemService.class);
        assertEquals(removed.toString(), 1, removed.size());
        PreferencesItemService removedItem = (PreferencesItemService)removed.get(0);
        assertEquals("remove", removedItem.getPath()[0]);
    }

    public void testDottedSubpathShouldBePossible() {
        Properties p = new Properties();
        p.setProperty("com.openexchange.myPlugin/a/certain/path", "value1");

        List items = publish(p);

        assertEquals(1, items.size());

        PreferencesItemService item = (PreferencesItemService) items.get(0);

        String[] path = item.getPath();
        assertEquals("com.openexchange.myPlugin", path[0]);
        assertEquals("a", path[1]);
        assertEquals("certain", path[2]);
        assertEquals("path", path[3]);


    }

    private List publish(Properties p) {
        MockServiceRegistry registry = new MockServiceRegistry();
        PropertiesPublisher publisher = new PropertiesPublisher();
        publisher.setServicePublisher(registry);
        publisher.publish(p);

        return registry.getAllServices(PreferencesItemService.class);
    }

    private void assertValue(PreferencesItemService prefItem, String expected) {
        Setting setting = new Setting("",-1,prefItem.getSharedValue());
        try {
            prefItem.getSharedValue().getValue(null,null,null,null,setting);
        } catch (SettingException e) {
            fail(e.toString());
        }
        assertEquals(expected, setting.getSingleValue());
    }

    private void assertMultiValue(PreferencesItemService prefItem, String...values) {
        Setting setting = new Setting("",-1,prefItem.getSharedValue());
        try {
            prefItem.getSharedValue().getValue(null,null,null,null,setting);
        } catch (SettingException e) {
            fail(e.toString());
        }
        Object[] objects = setting.getMultiValue();
        assertNotNull("Expected multivalue", objects);
        assertEquals(values.length, objects.length);
        for(int i = 0; i < objects.length; i++) {
            assertEquals(values[i], objects[i]);
        }
    }

}
