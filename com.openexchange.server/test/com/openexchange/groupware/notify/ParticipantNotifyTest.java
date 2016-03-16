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
