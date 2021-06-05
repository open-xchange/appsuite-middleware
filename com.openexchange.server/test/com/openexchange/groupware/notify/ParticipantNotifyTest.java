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

package com.openexchange.groupware.notify;

import org.junit.Assert;
import org.junit.Test;
import com.openexchange.groupware.container.UserParticipant;


/**
 * Unit tests for {@link ParticipantNotify}
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
public class ParticipantNotifyTest {

    /**
     * The first user participant
     */
    private UserParticipant userParticipant1 = new UserParticipant(1);

    /**
     * The second user participant
     */
    private UserParticipant userParticipant2 = new UserParticipant(2);

    /**
     * The third user participant
     */
    private UserParticipant userParticipant3 = new UserParticipant(3);

    /**
     * The user that is not included in user array
     */
    private UserParticipant notIncludedUser = new UserParticipant(4);

    /**
     * All users
     */
    private UserParticipant[] userParticipants = new UserParticipant[] { userParticipant1, userParticipant2, userParticipant3 };

     @Test
     public void testContains_toSearchNull_ReturnFalse() {
        boolean containsUser = ParticipantNotify.contains(null, userParticipants);

        Assert.assertFalse(containsUser);
    }

     @Test
     public void testContains_ArrayToSearchWithinNull_ReturnFalse() {
        boolean containsUser = ParticipantNotify.contains(userParticipant1, null);

        Assert.assertFalse(containsUser);
    }

     @Test
     public void testContains_UserNotIncludedInSearchArray_ReturnTrue() {
        boolean containsUser = ParticipantNotify.contains(notIncludedUser, userParticipants);

        Assert.assertFalse(containsUser);
    }

     @Test
     public void testContains_UserIncludedInSearchArray_ReturnTrue() {
        boolean containsUser = ParticipantNotify.contains(userParticipant3, userParticipants);

        Assert.assertTrue(containsUser);
    }


}
