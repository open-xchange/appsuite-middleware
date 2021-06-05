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

package com.openexchange.test.common.tools.events;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import com.openexchange.event.CommonEvent;
import com.openexchange.groupware.container.CommonObject;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class EventAssertions {

    /**
     * Checks whether the last event that was triggered was a delete event for the given Object.
     * 
     * @param type The Class of the Object that was deleted
     * @param parentFolderID The folder that contained the object
     * @param objectID The ID of the deleted object
     */
    public static void assertDeleteEvent(final Class<?> type, final int parentFolderID, final int objectID) {
        assertEvent(type, CommonEvent.DELETE, parentFolderID, objectID, false);
    }

    /**
     * Checks whether the last event that was triggered was a modification event for the given object.
     * This doesn't require that the original Object be part of the event.
     * 
     * @param type The Class of the Object that was modified
     * @param parentFolderID The folder that now contains the object (the new folder id)
     * @param objectID The ID of the object
     * @return
     */
    public static <T> T assertModificationEvent(final Class<T> type, final int parentFolderID, final int objectID) {
        return assertEvent(type, CommonEvent.UPDATE, parentFolderID, objectID, false);
    }

    /**
     * Checks whether the last event that was triggered was a modification event for the given object.
     * Checks also that the original object is present in the event.
     * 
     * @param type The Class of the Object that was modified
     * @param parentFolderID The folder that now contains the object (the new folder id)
     * @param objectID The ID of the object, must be present in both the new and the old object.
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

        if (checkForOldObject) {
            final CommonObject oldObject = (CommonObject) event.getOldObj();
            assertTrue("Old Object was not set in the event", oldObject != null);
            assertEquals(objectID, oldObject.getObjectID());
        }

        return (T) commonObject;
    }

    public static void assertEquals(int expected, int other) {
        if (expected != other) {
            fail("Expected: " + expected + " Got: " + other);
        }
    }
}
