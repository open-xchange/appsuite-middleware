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

package com.openexchange.userfeedback;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.userfeedback.export.ExportResultConverter;
import com.openexchange.userfeedback.fields.GenericUserFeedbackExportFields;
import com.openexchange.userfeedback.fields.UserFeedbackField;

/**
 * {@link AbstractJSONFeedbackTypeTest}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.4
 */
public class AbstractJSONFeedbackTypeTest {

    // @formatter:off
    private final String wellPreparedFeedbackStr = new String("{ " +
        "\"app\":\"app\","+
        "\"entry_point\":\"entry\","+
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
        "\"operating_system\": \"Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.\""+
        "}");

    // @formatter:off
    private final String contentOkButUpperCaseFeedbackStr = new String("{ " +
        "\"score\":\"3\","+
        "\"app\":\"app\","+
        "\"user\":\"bla\","+
        "\"entry_point\":\"entry\","+
        "\"Operating_System\": \"Mac OS X 10.10\","+
        "\"Browser\":\"Chrome\","+
        "\"Browser_version\": \"77.0\","+
        "\"User_agent\": \"Chrome/55.0.2883.87\","+
        "\"Screen_Resolution\":\"1600x900\","+
        "\"Language\": \"de_de\","+
        "\"client_version\":\"7.8.4 Rev11\""+
        "}");

    private final String normalizedFeedbackWithAdditionalFields = new String("{ " +
        "\"score\":\"3\","+
        "\"app\":\"app\","+
        "\"user\":\"bla\","+
        "\"comment\":\"mycomment\","+
        "\"entry_point\":\"entry\","+
        "\"operating_system\": \"Mac OS X 10.10\","+
        "\"browser\":\"Chrome\","+
        "\"browser_version\": \"77.0\","+
        "\"user_agent\": \"Chrome/55.0.2883.87\","+
        "\"screen_resolution\":\"1600x900\","+
        "\"language\": \"de_de\","+
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
        "\"Operating_System\": \"Mac OS X 10.10\","+
        "\"Browser\":\"Chrome\","+
        "\"Browser_version\": \"77.0\","+
        "\"User_agent\": \"Chrome/55.0.2883.87\","+
        "\"Language\": \"de_de\","+
        "\"server_version\":\"7.8.4 Rev1\","+
        "\"additional_key\":\"remove me\","+
        "\"client_version\":\"7.8.4 Rev11\""+
        "}");
    
    private AbstractJSONFeedbackType classUnderTest = new AbstractJSONFeedbackType() {
        
        @Override
        public String getType() {
            return null;
        }
        
        @Override
        public ExportResultConverter getFeedbacks(List<FeedbackMetaData> metaDataList, Connection con, Map<String, String> configuration) throws OXException {
            return null;
        }
        
        @Override
        public ExportResultConverter getFeedbacks(List<FeedbackMetaData> metaDataList, Connection con) throws OXException {
            return null;
        }
        
        @Override
        public void deleteFeedbacks(List<Long> ids, Connection con) throws OXException {
        }
        
        @Override
        protected void validate(JSONObject jsonFeedback) throws OXException {
        }
        
        @Override
        public long storeFeedbackInternal(JSONObject jsonFeedback, Connection con) throws OXException {
            return 0;
        }
        
        @Override
        protected List<UserFeedbackField> getRequiredFields() throws OXException {
            return null;
        }
    };

    @Test
    public void testAddRequired_everythingFine_nothingToDo() throws JSONException {
        JSONObject feedback = new JSONObject(wellPreparedFeedbackStr);
        JSONObject addRequired = classUnderTest.addRequired(feedback, GenericUserFeedbackExportFields.ALL_TYPE_UNSPECIFIC_FIELDS);

        assertTrue(feedback.equals(addRequired));
    }

    @Test
    public void testAddRequired_upperCaseKeys_addLowerCases() throws JSONException {
        JSONObject feedback = new JSONObject(contentOkButUpperCaseFeedbackStr);

        JSONObject addRequired = classUnderTest.addRequired(feedback, GenericUserFeedbackExportFields.ALL_TYPE_UNSPECIFIC_FIELDS);

        assertTrue(addRequired.has("Screen_Resolution"));
        assertTrue(addRequired.has("screen_resolution"));
    }

    @Test
    public void testAddRequired_nothingToAddButAdditionalAvailable_leaveAdditional() throws JSONException {
        JSONObject feedback = new JSONObject(additionalFieldsFeedbackStr);
        JSONObject addRequired = classUnderTest.addRequired(feedback, GenericUserFeedbackExportFields.ALL_TYPE_UNSPECIFIC_FIELDS);

        assertTrue(addRequired.has("score"));
        assertFalse(addRequired.has("Score"));
        assertTrue(addRequired.has("Martin_Schneider"));
        assertTrue(addRequired.has("schalke"));
    }

    @Test
    public void testAddRequired_requiredMissing_addRequired() throws JSONException {
        JSONObject feedback = new JSONObject(missingFieldsFeedbackStr);
        JSONObject addRequired = classUnderTest.addRequired(feedback, GenericUserFeedbackExportFields.ALL_TYPE_UNSPECIFIC_FIELDS);

        assertFalse(addRequired.has("score"));
        assertFalse(addRequired.has("Score"));
        assertTrue(addRequired.has("operating_system"));
        assertTrue(addRequired.has("screen_resolution"));
    }

    @Test
    public void testAddRequired_keysEmpty_returnOrigin() throws JSONException {
        JSONObject feedback = new JSONObject(missingFieldsFeedbackStr);

        JSONObject addRequired = classUnderTest.addRequired(feedback, new ArrayList<>());

        assertTrue(feedback.equals(addRequired));
    }

    @Test
    public void testNormalizeFeedback_contentSizeNotChangedAndKeysLowerCased() throws JSONException {
        JSONObject origin = new JSONObject(wellPreparedFeedbackStr);
        JSONObject normalizeFeedback = JSONObject.class.cast(classUnderTest.normalize(origin));
        assertFalse(normalizeFeedback.has("Screen_Resolution"));
        assertTrue(normalizeFeedback.has("screen_resolution"));

        assertFalse(normalizeFeedback.has("Entry_Point"));
        assertTrue(normalizeFeedback.has("entry_point"));

        assertEquals(origin.length(), normalizeFeedback.length());
    }


    @Test
    public void testRemoveAdditional_keysEmpty_returnOrigin() throws JSONException {
        JSONObject feedback = new JSONObject(contentOkButUpperCaseFeedbackStr);

        JSONObject remove = classUnderTest.remove(feedback, new ArrayList<>());

        assertTrue(feedback.equals(remove));
    }

    @Test
    public void testRemoveAdditional_mixedKeys_onlyKeepExpectedLowerCaseKeys() throws JSONException {
        JSONObject feedback = new JSONObject(contentOkButUpperCaseFeedbackStr);

        JSONObject remove = classUnderTest.remove(feedback, GenericUserFeedbackExportFields.ALL_TYPE_UNSPECIFIC_FIELDS);

        assertFalse(remove.has("score"));
        assertTrue(remove.has("app"));
        assertTrue(remove.has("entry_point"));
        assertTrue(remove.has("Browser"));
        assertTrue(remove.has("Browser_version"));
    }

    @Test
    public void testRemoveAdditional() throws JSONException {
        JSONObject feedback = new JSONObject(normalizedFeedbackWithAdditionalFields);

        JSONObject remove = classUnderTest.remove(feedback, GenericUserFeedbackExportFields.ALL_TYPE_UNSPECIFIC_FIELDS);

        assertTrue(remove.has(GenericUserFeedbackExportFields.ENTRY_POINT.getName()));
        assertFalse(remove.has(GenericUserFeedbackExportFields.USER.getName())); // only for export
        assertFalse(remove.has("comment"));// not expected
        assertTrue(remove.has(GenericUserFeedbackExportFields.APP.getName()));
        assertTrue(remove.has(GenericUserFeedbackExportFields.SCREEN_RESOLUTION.getName()));
    }

    @Test
    public void testCleanup() throws JSONException {
        JSONObject feedback = new JSONObject(this.mixedFeedbackStr);

        JSONObject cleaned = classUnderTest.cleanUpFeedback(feedback, GenericUserFeedbackExportFields.ALL_TYPE_UNSPECIFIC_FIELDS);

        assertFalse(cleaned.has("additional_key"));
        assertFalse(cleaned.has("score"));
        assertFalse(cleaned.has("Score"));
        assertTrue(cleaned.has("screen_resolution"));
        assertFalse(cleaned.has("Screen_Resolution"));
        assertTrue(cleaned.has("operating_system"));
        assertTrue(cleaned.has("user_agent"));
    }
    

    @Test (expected=OXException.class)
    public void testValidateFeedback() throws OXException {
        classUnderTest.prepareAndValidateFeedback(new JSONObject());

        fail();
    }

    @Test
    public void testEnsureSizeLimits_fieldNotAvailable_return() throws JSONException {
        JSONObject feedback = new JSONObject(limitFeedbackStr);
        classUnderTest.limit(feedback, GenericUserFeedbackExportFields.USER_AGENT);

        String limitedValue = feedback.optString(GenericUserFeedbackExportFields.USER_AGENT.getName());
        assertTrue(Strings.isEmpty(limitedValue));
    }

    @Test
    public void testEnsureSizeLimits_fieldExceeds_limit() throws JSONException {
        JSONObject feedback = new JSONObject(limitFeedbackStr);
        classUnderTest.limit(feedback, GenericUserFeedbackExportFields.OPERATING_SYSTEM);

        String limitedValue = feedback.getString(GenericUserFeedbackExportFields.OPERATING_SYSTEM.getName());
        assertTrue(limitedValue.length() <= GenericUserFeedbackExportFields.OPERATING_SYSTEM.getStorageSize());
    }
}
