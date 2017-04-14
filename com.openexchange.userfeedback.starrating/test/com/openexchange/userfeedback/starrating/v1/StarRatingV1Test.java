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

package com.openexchange.userfeedback.starrating.v1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.exception.OXException;

/**
 * {@link StarRatingV1Test}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public class StarRatingV1Test {

    // @formatter:off
    private final String wellPreparedFeedbackStr = new String("{ " +
        "\"score\":\"3\","+
        "\"app\":\"app\","+
        "\"entry_point\":\"entry\","+
        "\"comment\": \"s\u00FC\u00DFer die gl\u00F6cken nie klingen !|\u00A7$%&/()=?@\","+
        "\"operating_system\": \"Mac OS X 10.10\","+
        "\"browser\":\"Chrome\","+
        "\"browser_version\": \"77.0\","+
        "\"user_agent\": \"Chrome/55.0.2883.87\","+
        "\"screen_resolution\":\"1600x900\","+
        "\"language\": \"de_de\","+
        "\"client_version\":\"7.8.4 Rev11\""+
        "}");

    // @formatter:off
    private final String limitFeedbackStr = new String("{ " +
        "\"comment\": \"Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.\""+
        "}");

    // @formatter:off
    private final String contentOkButUpperCaseFeedbackStr = new String("{ " +
        "\"score\":\"3\","+
        "\"app\":\"app\","+
        "\"entry_point\":\"entry\","+
        "\"Comment\": \"s\u00FC\u00DFer die gl\u00F6cken nie klingen !|\u00A7$%&/()=?@\","+
        "\"Operating_System\": \"Mac OS X 10.10\","+
        "\"Browser\":\"Chrome\","+
        "\"Browser_version\": \"77.0\","+
        "\"User_agent\": \"Chrome/55.0.2883.87\","+
        "\"Screen_Resolution\":\"1600x900\","+
        "\"Language\": \"de_de\","+
        "\"client_version\":\"7.8.4 Rev11\""+
        "}");

    // @formatter:off
    private final String missingFieldsFeedbackStr = new String("{ " +
        "\"app\":\"app\","+
        "\"entry_point\":\"entry\","+
        "\"browser\":\"Chrome\","+
        "\"browser_version\": \"77.0\","+
        "\"user_agent\": \"Chrome/55.0.2883.87\","+
        "\"language\": \"de_de\","+
        "\"client_version\":\"7.8.4 Rev11\""+
        "}");

    // @formatter:off
    private final String additionalFieldsFeedbackStr = new String("{ " +
        "\"score\":\"3\","+
        "\"app\":\"app\","+
        "\"entry_point\":\"entry\","+
        "\"comment\": \"s\u00FC\u00DFer die gl\u00F6cken nie klingen !|\u00A7$%&/()=?@\","+
        "\"operating_system\": \"Mac OS X 10.10\","+
        "\"browser\":\"Chrome\","+
        "\"schalke\":\"super\","+
        "\"Martin_Schneider\":\"hervorragend\","+
        "\"browser_version\": \"77.0\","+
        "\"user_agent\": \"Chrome/55.0.2883.87\","+
        "\"screen_resolution\":\"1600x900\","+
        "\"language\": \"de_de\","+
        "\"client_version\":\"7.8.4 Rev11\""+
        "}");

    // @formatter:off
    private final String mixedFeedbackStr = new String("{ " +
        "\"app\":\"app\","+
        "\"Comment\": \"s\u00FC\u00DFer die gl\u00F6cken nie klingen !|\u00A7$%&/()=?@\","+
        "\"Operating_System\": \"Mac OS X 10.10\","+
        "\"Browser\":\"Chrome\","+
        "\"Browser_version\": \"77.0\","+
        "\"User_agent\": \"Chrome/55.0.2883.87\","+
        "\"Language\": \"de_de\","+
        "\"server_version\":\"7.8.4 Rev1\","+
        "\"additional_key\":\"remove me\","+
        "\"client_version\":\"7.8.4 Rev11\""+
        "}");
    
    private StarRatingV1 classUnderTest = new StarRatingV1();

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {}

    @Test
    public void testNormalizeFeedback_contentSizeNotChangedAndKeysLowerCased() throws JSONException, OXException {
        JSONObject origin = new JSONObject(wellPreparedFeedbackStr);
        JSONObject normalizeFeedback = (JSONObject) classUnderTest.normalize(origin);
        assertFalse(normalizeFeedback.has("Comment"));
        assertTrue(normalizeFeedback.has("comment"));

        assertFalse(normalizeFeedback.has("Screen_Resolution"));
        assertTrue(normalizeFeedback.has("screen_resolution"));

        assertFalse(normalizeFeedback.has("Entry_Point"));
        assertTrue(normalizeFeedback.has("entry_point"));

        assertEquals(origin.length(), normalizeFeedback.length());
    }

    @Test
    public void testAddRequired_everythingFine_nothingToDo() throws JSONException {
        JSONObject feedback = new JSONObject(wellPreparedFeedbackStr);
        JSONObject addRequired = classUnderTest.addRequired(feedback, StarRatingV1Fields.requiredJsonKeys());

        assertTrue(feedback.equals(addRequired));
    }

    @Test
    public void testAddRequired_upperCaseKeys_addLowerCases() throws JSONException {
        JSONObject feedback = new JSONObject(contentOkButUpperCaseFeedbackStr);

        JSONObject addRequired = classUnderTest.addRequired(feedback, StarRatingV1Fields.requiredJsonKeys());

        assertTrue(addRequired.has("Comment"));
        assertTrue(addRequired.has("comment"));
        assertTrue(addRequired.has("Screen_Resolution"));
        assertTrue(addRequired.has("screen_resolution"));
    }

    @Test
    public void testAddRequired_nothingToAddButAdditionalAvailable_leaveAdditional() throws JSONException {
        JSONObject feedback = new JSONObject(additionalFieldsFeedbackStr);
        JSONObject addRequired = classUnderTest.addRequired(feedback, StarRatingV1Fields.requiredJsonKeys());

        assertTrue(addRequired.has("score"));
        assertFalse(addRequired.has("Score"));
        assertFalse(addRequired.has("Comment"));
        assertTrue(addRequired.has("comment"));
        assertTrue(addRequired.has("Martin_Schneider"));
        assertTrue(addRequired.has("schalke"));
    }

    @Test
    public void testAddRequired_requiredMissing_addRequired() throws JSONException {
        JSONObject feedback = new JSONObject(missingFieldsFeedbackStr);
        JSONObject addRequired = classUnderTest.addRequired(feedback, StarRatingV1Fields.requiredJsonKeys());

        assertTrue(addRequired.has("score"));
        assertFalse(addRequired.has("Score"));
        assertTrue(addRequired.has("comment"));
        assertFalse(addRequired.has("Comment"));
        assertTrue(addRequired.has("operating_system"));
        assertTrue(addRequired.has("screen_resolution"));
    }

    @Test
    public void testAddRequired_keysEmpty_returnOrigin() throws JSONException {
        Set<String> keys = Collections.emptySet();
        JSONObject feedback = new JSONObject(missingFieldsFeedbackStr);

        JSONObject addRequired = classUnderTest.addRequired(feedback, keys);

        assertTrue(feedback.equals(addRequired));
    }

    @Test
    public void testRemoveAdditional_keysEmpty_returnOrigin() throws JSONException {
        Set<String> keys = Collections.emptySet();
        JSONObject feedback = new JSONObject(contentOkButUpperCaseFeedbackStr);

        JSONObject remove = classUnderTest.remove(feedback, keys);

        assertTrue(feedback.equals(remove));
    }

    @Test
    public void testRemoveAdditional_mixedKeys_onlyKeepExpectedLowerCaseKeys() throws JSONException {
        JSONObject feedback = new JSONObject(contentOkButUpperCaseFeedbackStr);

        JSONObject remove = classUnderTest.remove(feedback, StarRatingV1Fields.requiredJsonKeys());

        assertTrue(remove.has("score"));
        assertTrue(remove.has("app"));
        assertTrue(remove.has("entry_point"));
        assertFalse(remove.has("Comment"));
        assertFalse(remove.has("Browser"));
        assertFalse(remove.has("Browser_version"));
    }

    @Test
    public void testRemoveAdditional() throws JSONException {
        Set<String> keys = new HashSet<>(Arrays.asList("score", "server_version", "Entry_Point", "screen_resolution"));
        JSONObject feedback = new JSONObject(contentOkButUpperCaseFeedbackStr);

        JSONObject remove = classUnderTest.remove(feedback, keys);

        assertFalse(remove.has("Screen_Resolution")); // not expected
        assertFalse(remove.has("entry_point")); // not in origin
        assertFalse(remove.has("Comment"));// not expected
        assertTrue(remove.has("score"));
    }

    @Test
    public void testCleanup() throws JSONException {
        JSONObject feedback = new JSONObject(this.mixedFeedbackStr);

        JSONObject remove = (JSONObject) classUnderTest.cleanUpFeedback(feedback, StarRatingV1Fields.requiredJsonKeys());

        assertFalse(remove.has("additional_key"));
        assertTrue(remove.has("score"));
        assertFalse(remove.has("Score"));
        assertTrue(remove.has("screen_resolution"));
        assertFalse(remove.has("Screen_Resolution"));
        assertTrue(remove.has("operating_system"));
        assertTrue(remove.has("user_agent"));
    }
    

    @Test (expected=OXException.class)
    public void testValidateFeedback() throws OXException {
        classUnderTest.prepareAndValidateFeedback(Collections.EMPTY_LIST);

        fail();
    }

    @Test
    public void testEnsureSizeLimits_valueTooBig_limit() throws JSONException {
        JSONObject feedback = new JSONObject(limitFeedbackStr);
        classUnderTest.limit(feedback, StarRatingV1Fields.comment.name(), 15);
        
        String limitedValue = feedback.getString("comment");
        assertTrue(limitedValue.length() <= 15);
        assertEquals(limitedValue, "Lorem ipsum ...");
    }
    
    @Test
    public void testEnsureSizeLimits_limitTooSmall_ignore() throws JSONException {
        JSONObject feedback = new JSONObject(limitFeedbackStr);
        classUnderTest.limit(feedback, StarRatingV1Fields.comment.name(), 1);
        
        String limitedValue = feedback.getString("comment");
        assertEquals(new JSONObject(limitFeedbackStr).getString("comment"), limitedValue);
    }

    @Test
    public void testEnsureSizeLimits_limitNotReached_ignore() throws JSONException {
        JSONObject feedback = new JSONObject(limitFeedbackStr);
        classUnderTest.limit(feedback, StarRatingV1Fields.comment.name(), 10000000);
        
        String limitedValue = feedback.getString("comment");
        assertEquals(new JSONObject(limitFeedbackStr).getString("comment"), limitedValue);
    }
}
