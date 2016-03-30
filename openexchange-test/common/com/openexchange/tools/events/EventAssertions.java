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
package com.openexchange.tools.events;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import com.openexchange.event.CommonEvent;
import com.openexchange.groupware.container.CommonObject;


/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class EventAssertions {
    /**
     * Checks whether the last event that was triggered was a delete event for the given Object.
     * @param type The Class of the Object that was deleted
     * @param parentFolderID The folder that contained the object
     * @param objectID The ID of the deleted object
     */
    public static void assertDeleteEvent(final Class type, final int parentFolderID, final int objectID) {
         assertEvent(type, CommonEvent.DELETE, parentFolderID, objectID, false);
    }

    /**
     * Checks whether the last event that was triggered was a modification event for the given object.
     * This doesn't require that the original Object be part of the event.
     * @param type The Class of the Object that was modified
     * @param parentFolderID The folder that now contains the object (the new folder id)
     * @param objectID  The ID of the object
     * @return
     */
    public static <T> T assertModificationEvent(final Class<T> type, final int parentFolderID, final int objectID) {
        return assertEvent(type, CommonEvent.UPDATE, parentFolderID, objectID, false);
    }


    /**
     * Checks whether the last event that was triggered was a modification event for the given object.
     * Checks also that the original object is present in the event.
     * @param type The Class of the Object that was modified
     * @param parentFolderID The folder that now contains the object (the new folder id)
     * @param objectID  The ID of the object, must be present in both the new and the old object.
     * @return
     */
    public static <T> T assertModificationEventWithOldObject(final Class<T> type, final int parentFolderID, final int objectID) {
        return assertEvent(type, CommonEvent.UPDATE, parentFolderID, objectID, true);
    }

    public static <T> T assertEvent(final Class<T> type, final int action, final int parentFolderID, final int objectID, final boolean checkForOldObject) {
        final TestEventAdmin events = TestEventAdmin.getInstance();

        final CommonEvent event = events.getNewest();
        final CommonObject commonObject = (CommonObject) event.getActionObj();

        assertNotNull(commonObject);
        assertTrue(type.isAssignableFrom(commonObject.getClass()));
        assertEquals(action, event.getAction());
        assertEquals(parentFolderID, commonObject.getParentFolderID());
        assertEquals(objectID, commonObject.getObjectID());

        if(checkForOldObject) {
            final CommonObject oldObject = (CommonObject) event.getOldObj();
            assertTrue("Old Object was not set in the event", oldObject != null);
            assertEquals(objectID, oldObject.getObjectID());
        }

        return (T) commonObject;
    }

    public static void assertEquals(int expected, int  other) {
        if(expected != other) {
            fail("Expected: "+expected+" Got: "+other);
        }
    }
}
