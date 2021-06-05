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

import java.sql.Connection;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.tools.validate.ParameterValidator;
import com.openexchange.userfeedback.exception.FeedbackExceptionCodes;
import com.openexchange.userfeedback.fields.UserFeedbackField;

/**
 * Abstract class that supports handling of feedback in JSON format
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public abstract class AbstractJSONFeedbackType implements FeedbackType {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractJSONFeedbackType.class);

    protected static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Stores the well prepared feedback within the dedicated feedback mode tables.
     *
     * @param feedback The feedback {@link JSONObject} to validate
     * @param con The write connection to the global db
     * @return The id of the newly created entry or -1
     * @throws OXException
     */
    protected abstract long storeFeedbackInternal(JSONObject jsonFeedback, Connection con) throws OXException;

    /**
     * Semantic, feedback mode specific check of the feedback content.
     * Validates the normalized ({@link #normalize(JSONObject)}) and cleaned ({@link #cleanUp(JSONObject)}) feedback content.
     * 
     * @param jsonFeedback The feedback {@link JSONObject} to validate
     * @throws OXException
     */
    protected abstract void validate(JSONObject jsonFeedback) throws OXException;

    /**
     * Returns the feedback mode specific fields that are required to be provided by the client.
     *
     * @return List of {@link UserFeedbackField} that should be set within the provided JSON
     * @throws OXException
     */
    protected abstract List<UserFeedbackField> getRequiredFields() throws OXException;

    /**
     * {@inheritDoc}
     */
    @Override
    public long storeFeedback(Object feedback, Connection con) throws OXException {
        JSONObject jsonFeedback = getFeedback(feedback);

        JSONObject validatedFeedback = prepareAndValidateFeedback(jsonFeedback);
        return storeFeedbackInternal(validatedFeedback, con);
    }

    /**
     * Returns the updated and ready-to-persist feedback as {@link JSONObject}
     * 
     * @param feedback The feedback to prepare and validate
     * @return ready-to-persist {@link JSONObject}
     * @throws OXException
     */
    protected final JSONObject prepareAndValidateFeedback(JSONObject jsonFeedback) throws OXException {
        ParameterValidator.checkJSON(jsonFeedback);

        JSONObject normalizedFeedback = normalize(jsonFeedback);
        JSONObject cleanUpFeedback = cleanUpFeedback(normalizedFeedback, getRequiredFields());

        validate(cleanUpFeedback);

        return cleanUpFeedback;
    }

    /**
     * Returns the feedback contained in 'feedback' in JSON format
     * 
     * @param feedback {@link Object} with the feedback
     * @return {@link JSONObject} with the feedback.
     * @throws OXException in case the parameter feedback is not from type {@link JSONObject}
     */
    private final JSONObject getFeedback(Object feedback) throws OXException {
        if (!(feedback instanceof JSONObject)) {
            throw FeedbackExceptionCodes.INVALID_DATA_TYPE.create("JSONObject");
        }
        JSONObject jsonFeedback = (JSONObject) feedback;
        return jsonFeedback;
    }

    /**
     * Ensures a normalized representation of the feedback {@link JSONObject} for upcoming cleanups (e. g. lower case keys in JSON)
     * 
     * @param feedback The feedback {@link JSONObject} to normalize
     * @return JSONObject The normalized feedback
     */
    protected final JSONObject normalize(JSONObject jsonFeedback) {
        Iterator<?> jsonKeys = jsonFeedback.keys();
        JSONObject processed = new JSONObject(jsonFeedback.length());
        while (jsonKeys.hasNext()) {
            try {
                String unnormalizedKey = (String) jsonKeys.next();
                String value = jsonFeedback.getString(unnormalizedKey);
                String key = unnormalizedKey.toLowerCase();
                processed.put(key, value);
            } catch (JSONException e) {
                LOG.warn("Error while updating json keys.", e);
            }
        }
        return processed;
    }

    /**
     * Aligns the feedback to store (provided via the jsonFeedback parameter) against the JSON keys provided within the given Set<UserFeedbackField>
     *
     * @param jsonFeedback The {@link JSONObject} provided by the client
     * @param keys The keys that are expected within the {@link JSONObject}
     * @return {@link JSONObject} that is aligned to be stored
     */
    protected final JSONObject cleanUpFeedback(JSONObject jsonFeedback, List<UserFeedbackField> keys) {
        JSONObject returnFeedback = new JSONObject(jsonFeedback);

        JSONObject removeAdditional = remove(returnFeedback, keys);
        JSONObject cleanedFeedback = addRequired(removeAdditional, keys);

        JSONObject ensureSizeLimits = ensureSizeLimits(cleanedFeedback, keys);

        return ensureSizeLimits;
    }

    /**
     * Removes JSON entries from provided object that aren't expected. Expected keys are defined by the 'keys' parameter). If keys parameter is empty the origin object will be returned.<br>
     * <br>
     * <b>Caution:</> this check is case sensitive. Having 'comment' in keys parameter will remove 'Comment' from provided {@link JSONObject} as it is not expected.
     *
     * @param feedback The provided feedback that will be adapted
     * @param expectedKeys The keys that are expected
     */
    protected final JSONObject remove(final JSONObject feedback, final List<UserFeedbackField> expectedKeys) {
        if ((expectedKeys == null) || (expectedKeys.isEmpty())) {
            return feedback;
        }

        JSONObject processed = new JSONObject(feedback);
        Iterator<?> jsonKeys = feedback.keys();
        while (jsonKeys.hasNext()) {
            String key = (String) jsonKeys.next();
            if (!expectedKeys.stream().anyMatch(x -> x.getName().equalsIgnoreCase(key))) {
                LOG.debug("An unknown key '{}' has been provided. It will be removed before persisting.", key);
                processed.remove(key);
                continue;
            }
        }
        for (UserFeedbackField key : expectedKeys) {
            if (!key.isProvidedByClient() && processed.has(key.getName())) {
                processed.remove(key.getName());
            }
        }
        return processed;
    }

    /**
     * Enhances the given JSON by dummy entries for every missing key defined in the parameter list. If keys parameter is empty the origin object will be returned.<br>
     * <br>
     * <b>Caution:</> this check is case sensitive. Having 'comment' in keys parameter will add it even 'Comment' is available within the provided {@link JSONObject}.
     *
     * @param feedback The provided feedback that will be adapted.
     * @param keys The keys that are expected within the object
     */
    protected final JSONObject addRequired(final JSONObject feedback, final List<UserFeedbackField> keys) {
        if ((keys == null) || (keys.isEmpty())) {
            return feedback;
        }

        JSONObject processed = new JSONObject(feedback);
        for (UserFeedbackField key : keys) {
            String name = key.getName();
            if (Strings.isEmpty(name) || feedback.has(name) || !key.isProvidedByClient()) {
                continue;
            }
            LOG.debug("Desired key {} not contained within the request. It will be stored as empty.", name);
            try {
                processed.put(name, "");
            } catch (JSONException e) {
                LOG.error("Error while adding new key.", e);
            }
        }
        return processed;
    }

    /**
     * Limits the data column to have at most 21000 UTF-8 characters as the blob column is able to take 65535 bytes and an UTF-8 character can be up to 3 bytes.
     * Therefore values will be cut off after defined lengths.
     *
     * @param jsonFeedback The feedback
     * @param keys The keys that are expected within the feedback
     */
    protected JSONObject ensureSizeLimits(JSONObject jsonFeedback, List<UserFeedbackField> keys) {
        JSONObject limitedFeedback = new JSONObject(jsonFeedback);

        for (UserFeedbackField field : keys) {
            limit(limitedFeedback, field);
        }
        return limitedFeedback;
    }

    /**
     * Limits the provided {@link JSONObject} for the given {@link UserFeedbackField} if available.
     * 
     * @param feedback The feedback that might contain the {@link UserFeedbackField}
     * @param field The field holding the limit information
     */
    protected void limit(JSONObject feedback, UserFeedbackField field) {
        String key = field.getName();
        int allowed = field.getStorageSize();
        if ((feedback == null) || !field.isProvidedByClient() || (Strings.isEmpty(key)) || (allowed <= 10) || !feedback.has(key)) {
            return;
        }
        try {
            String value = feedback.getString(field.getName());
            if (value.length() > allowed) {
                String limitedValue = StringUtils.substring(value, 0, allowed - 4).concat(" ...");
                feedback.put(key, limitedValue);
            }
        } catch (JSONException e) {
            LOG.warn("Unable to limit json value.", e);
        }
    }
}
