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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.ajax.chronos.itip;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.List;
import com.openexchange.ajax.chronos.itip.AbstractITipTest.PartStat;
import com.openexchange.testing.httpclient.models.ActionResponse;
import com.openexchange.testing.httpclient.models.Analysis;
import com.openexchange.testing.httpclient.models.AnalysisChange;
import com.openexchange.testing.httpclient.models.AnalyzeResponse;
import com.openexchange.testing.httpclient.models.Attendee;
import com.openexchange.testing.httpclient.models.EventData;

/**
 * {@link ITipAssertion}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 */
public class ITipAssertion {

    private ITipAssertion() {};

    /**
     * Asserts that only one analyze with exact one change was provided in the response
     *
     * @param analyzeResponse The response to check
     * @return The {@link AnalysisChange} provided by the server
     */
    public static AnalysisChange assertSingleChange(AnalyzeResponse analyzeResponse) {
        assertNull("error during analysis: " + analyzeResponse.getError(), analyzeResponse.getCode());
        assertEquals("unexpected analysis number in response", 1, analyzeResponse.getData().size());
        Analysis analysis = analyzeResponse.getData().get(0);
        assertEquals("unexpected number of changes in analysis", 1, analysis.getChanges().size());
        return analysis.getChanges().get(0);
    }

    /**
     * Asserts that the given attendee represented by its mail has the desired participant status
     *
     * @param attendees The attendees of the event
     * @param email The attendee to check represented by its mail
     * @param partStat The participant status of the attendee
     * @return The attendee to check as {@link Attendee} object
     */
    public static Attendee assertAttendeePartStat(List<Attendee> attendees, String email, PartStat partStat) {
        return assertAttendeePartStat(attendees, email, partStat.getStatus());
    }

    /**
     * Asserts that the given attendee represented by its mail has the desired participant status
     *
     * @param attendees The attendees of the event
     * @param email The attendee to check represented by its mail
     * @param expectedPartStat The participant status of the attendee
     * @return The attendee to check as {@link Attendee} object
     */
    public static Attendee assertAttendeePartStat(List<Attendee> attendees, String email, String expectedPartStat) {
        Attendee attendee = extractAttendee(attendees, email);
        assertNotNull(attendee);
        assertEquals(expectedPartStat, attendee.getPartStat());
        return attendee;
    }

    private static Attendee extractAttendee(List<Attendee> attendees, String email) {
        if (null != attendees) {
            for (Attendee attendee : attendees) {
                String uri = attendee.getUri();
                if (null != uri && uri.toLowerCase().contains(email.toLowerCase())) {
                    return attendee;
                }
            }
        }
        return null;
    }

    /**
     * Asserts that the given attendee represented by its mail has the desired participant status
     *
     * @param attendees The attendees of the event
     * @param email The attendee to check represented by its mail
     * @param expectedPartStat The participant status of the attendee as {@link com.openexchange.chronos.ParticipationStatus}
     * @return The attendee to check as {@link com.openexchange.chronos.Attendee} object
     */
    public static com.openexchange.chronos.Attendee assertAttendeePartStat(List<com.openexchange.chronos.Attendee> attendees, String email, com.openexchange.chronos.ParticipationStatus expectedPartStat) {
        com.openexchange.chronos.Attendee matchingAttendee = null;
        if (null != attendees) {
            for (com.openexchange.chronos.Attendee attendee : attendees) {
                String uri = attendee.getUri();
                if (null != uri && uri.toLowerCase().contains(email.toLowerCase())) {
                    matchingAttendee = attendee;
                    break;
                }
            }
        }
        assertNotNull(matchingAttendee);
        assertEquals(expectedPartStat, matchingAttendee.getPartStat());
        return matchingAttendee;
    }

    /**
     * Asserts that exactly one event was handled by the server
     *
     * @param actionResponse The {@link ActionResponse} from the server
     * @return The {@link EventData} of the handled event
     */
    public static EventData assertSingleEvent(ActionResponse actionResponse) {
        return assertSingleEvent(actionResponse, null);
    }

    /**
     * Asserts that exactly one event was handled by the server
     *
     * @param actionResponse The {@link ActionResponse} from the server
     * @param uid The uid the event should have or <code>null</code>
     * @return The {@link EventData} of the handled event
     */
    public static EventData assertSingleEvent(ActionResponse actionResponse, String uid) {
        assertNotNull(actionResponse.getData());
        assertThat("Only one object should have been handled", Integer.valueOf(actionResponse.getData().size()), is(Integer.valueOf(1)));
        EventData eventData = actionResponse.getData().get(0);
        if (null != uid) {
            assertEquals(uid, eventData.getUid());
        }
        return eventData;
    }

    /**
     * Asserts that a single description is given
     *
     * @param change
     * @param descriptionToMatch
     */
    public static void assertSingleDescription(AnalysisChange change, String descriptionToMatch) {
        assertTrue(change.getDiffDescription().size() == 1);
        assertTrue("Description does not contain expected update: (" + change.getDiffDescription().get(0) + ")", change.getDiffDescription().get(0).contains(descriptionToMatch));
    }

}
