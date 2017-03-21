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

package com.openexchange.userfeedback;

import java.sql.Connection;
import java.util.Iterator;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.userfeedback.exception.FeedbackExceptionCodes;

/**
 * {@link AbstractFeedbackType}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public abstract class AbstractFeedbackType implements FeedbackType {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractFeedbackType.class);

    @Override
    public long storeFeedback(Object feedback, Connection con) throws OXException {
        Object validatedFeedback = validateFeedback(feedback);
        return storeFeedbackInternal(validatedFeedback, con);
    }

    public abstract long storeFeedbackInternal(Object feedback, Connection con) throws OXException;

    /**
     * Returns the updated and ready-to-persist feedback object
     * 
     * @param feedback
     * @return ready-to-persist object
     * @throws OXException
     */
    public final Object validateFeedback(Object feedback) throws OXException {
        if (!(feedback instanceof JSONObject)) {
            throw FeedbackExceptionCodes.INVALID_DATA_TYPE.create("JSONObject");
        }

        JSONObject jsonFeedback = normalizeFeedback((JSONObject) feedback);
        return cleanUpFeedback(jsonFeedback);
    }

    /**
     * Delegate cleanup to implementation. Ensure the implementation also calls {@link #cleanUpFeedback(JSONObject, Set)} to have normalized and cleaned feedback content
     * 
     * @param jsonFeedback
     * @return The feedback
     * @throws OXException
     */
    public abstract JSONObject cleanUpFeedback(JSONObject jsonFeedback) throws OXException;

    /**
     * Aligns the feedback to store (provided via the jsonFeedback parameter) against the JSON keys provided within the given Set<String>
     *
     * @param jsonFeedback The JSON object provided by the client
     * @return {@link JSONObject} that is aligned to be stored
     */
    protected final JSONObject cleanUpFeedback(JSONObject jsonFeedback, Set<String> keys) {
        JSONObject returnFeedback = new JSONObject(jsonFeedback);

        JSONObject removeAdditional = remove(returnFeedback, keys);
        JSONObject cleanedFeedback = addRequired(removeAdditional, keys);
        return cleanedFeedback;
    }

    /**
     * Enhances the given JSON by dummy entries for every missing key defined in the parameter list. If keys parameter is empty the origin object will be returned.<br>
     * <br>
     * <b>Caution:</> this check is case sensitive. Having 'comment' in keys parameter will add it even 'Comment' is available within the provided {@link JSONObject}.
     *
     * @param feedback The provided feedback that will be adapted.
     * @param keys The keys that should be available within the object
     */
    protected final JSONObject addRequired(final JSONObject feedback, Set<String> keys) {
        if ((keys == null) || (keys.isEmpty())) {
            return feedback;
        }

        JSONObject processed = new JSONObject(feedback);
        for (String key : keys) {
            if (feedback.has(key)) {
                continue;
            }
            LOG.info("Desired key {} not contained within the request. They will be stored as empty.", Strings.concat(",", keys));
            try {
                processed.put(key, "");
            } catch (JSONException e) {
                LOG.error("Error while adding new key.", e);
            }
        }
        return processed;
    }

    /**
     * Removes JSON entries from provided object that aren't expected. Expected keys are defined by the 'keys' parameter). If keys parameter is empty the origin object will be returned.<br>
     * <br>
     * <b>Caution:</> this check is case sensitive. Having 'comment' in keys parameter will remove 'Comment' from provided {@link JSONObject} as it is not expected.
     *
     * @param feedback The provided feedback that will be adapted
     * @param expectedKeys The keys that are expected
     */
    protected final JSONObject remove(final JSONObject feedback, Set<String> expectedKeys) {
        if ((expectedKeys == null) || (expectedKeys.isEmpty())) {
            return feedback;
        }

        JSONObject processed = new JSONObject(feedback);
        Iterator<?> jsonKeys = feedback.keys();
        while (jsonKeys.hasNext()) {
            String key = (String) jsonKeys.next();
            if (!expectedKeys.contains(key)) {
                LOG.warn("An unknown key '{}' has been provided. It will be removed before persisting.", key);
                processed.remove(key);
                continue;
            }
            expectedKeys.remove(key);
        }
        return processed;
    }

    /**
     * Ensures that the provided feedback only has lower case keys!
     *
     * @param feedback The feedback that should be normalized
     * @return {@link JSONObject} with lower case keys
     */
    protected final JSONObject normalizeFeedback(JSONObject feedback) {
        Iterator<?> jsonKeys = feedback.keys();
        JSONObject processed = new JSONObject(feedback.length());
        while (jsonKeys.hasNext()) {
            try {
                String unnormalizedKey = (String) jsonKeys.next();
                String value = feedback.getString(unnormalizedKey);
                String key = unnormalizedKey.toLowerCase();
                processed.put(key, value);
            } catch (JSONException e) {
                LOG.warn("Error while updating json keys.", e);
            }
        }
        return processed;
    }
}
