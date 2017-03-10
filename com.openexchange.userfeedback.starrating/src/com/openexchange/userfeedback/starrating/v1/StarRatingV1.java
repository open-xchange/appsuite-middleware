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

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.common.hash.Hashing;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.java.AsciiReader;
import com.openexchange.java.AsciiWriter;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.userfeedback.AbstractFeedbackType;
import com.openexchange.userfeedback.ExportResultConverter;
import com.openexchange.userfeedback.Feedback;
import com.openexchange.userfeedback.FeedbackMetaData;
import com.openexchange.userfeedback.exception.FeedbackExceptionCodes;

/**
 * {@link StarRatingV1}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public class StarRatingV1 extends AbstractFeedbackType {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(StarRatingV1.class);

    private static final String TYPE = "star-rating-v1";
    private static final String INSERT_SQL = "INSERT INTO star_rating_v1 (data) VALUES (?)";
    private static final String SELECT_SQL = "SELECT id, data FROM star_rating_v1 WHERE id IN (";
    private static final String DELETE_SQL = "DELETE FROM star_rating_v1 WHERE id = ?";

    @Override
    public long storeFeedbackInternal(Object feedback, Connection con) throws OXException {
        if (!(feedback instanceof JSONObject)) {
            throw FeedbackExceptionCodes.INVALID_DATA_TYPE.create("JSONObject");
        }
        JSONObject jsonFeedback = (JSONObject) feedback;

        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            setBinaryStream(jsonFeedback, stmt, 1);
            stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }

            return -1;
        } catch (final SQLException e) {
            throw FeedbackExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public Object validateFeedback(Object feedback) throws OXException {
        if (!(feedback instanceof JSONObject)) {
            throw FeedbackExceptionCodes.INVALID_DATA_TYPE.create("JSONObject");
        }

        JSONObject jsonFeedback = normalizeFeedback((JSONObject) feedback);
        return cleanUpFeedback(jsonFeedback);
    }

    /**
     * Aligns the feedback to store (provided via the jsonFeedback parameter) against the JSON keys expected by the implementation
     *
     * @param jsonFeedback The JSON object provided by the client
     * @return {@link JSONObject} that is aligned to be stored
     */
    protected Object cleanUpFeedback(JSONObject jsonFeedback) {
        JSONObject returnFeedback = new JSONObject(jsonFeedback);

        Set<String> keys = new HashSet<String>(StarRatingV1JsonFields.requiredJsonKeys());
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
    protected JSONObject addRequired(final JSONObject feedback, Set<String> keys) {
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
    protected JSONObject remove(final JSONObject feedback, Set<String> expectedKeys) {
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
    protected JSONObject normalizeFeedback(JSONObject feedback) {
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

    private void setBinaryStream(JSONObject jObject, PreparedStatement stmt, int... positions) throws OXException {
        try {
            JSONObject json = null != jObject ? jObject : new JSONObject(0);
            ByteArrayOutputStream buf = Streams.newByteArrayOutputStream(65536);
            json.write(new AsciiWriter(buf), true);

            if (positions.length == 1) {
                stmt.setBinaryStream(positions[0], Streams.asInputStream(buf));
            } else {
                byte[] data = buf.toByteArray();
                buf = null; // might help GC
                for (int pos : positions) {
                    stmt.setBytes(pos, data);
                }
            }
        } catch (final SQLException | JSONException e) {
            throw FeedbackExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public ExportResultConverter getFeedbacks(List<FeedbackMetaData> feedbackMetaData, Connection con) throws OXException {
        if (feedbackMetaData.size() == 0) {
            return createExportObject(Collections.<Feedback> emptyList());
        }

        ResultSet rs = null;
        PreparedStatement stmt = null;
        Map<Long, Feedback> feedbacks = new HashMap<>();
        try {
            String sql = Databases.getIN(SELECT_SQL, feedbackMetaData.size());
            stmt = con.prepareStatement(sql);
            int x = 1;
            for (FeedbackMetaData meta : feedbackMetaData) {
                stmt.setLong(x++, meta.getTypeId());
                feedbacks.put(meta.getTypeId(), new Feedback(meta, null));
            }
            rs = stmt.executeQuery();

            while (rs.next()) {
                long id = rs.getLong(1);
                try {
                    Feedback current = feedbacks.get(id);
                    enrichContent(new AsciiReader(rs.getBinaryStream(2)), current);
                } catch (JSONException e) {
                    LOG.error("Unable to read feedback with id {}. Won't return it.", id, e);
                }
            }
        } catch (final SQLException e) {
            throw FeedbackExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
        return createExportObject(feedbacks.values());
    }

    /**
     * Adds required export fields to content.
     * 
     * @param asciiReader The initial content
     * @param current The feedback object to update
     * @throws JSONException
     */
    private void enrichContent(AsciiReader asciiReader, Feedback current) throws JSONException {
        JSONObject content = new JSONObject(asciiReader);
        content.put("date", new Date(current.getDate())); // add date to content for easy export
        String userContextTupel = current.getCtxId() + ":" + current.getUserId();
        String hashedTupel = Hashing.md5().hashString(userContextTupel, StandardCharsets.UTF_8).toString();
        content.put("user", hashedTupel);
        current.setContent(content);
    }

    private ExportResultConverter createExportObject(Collection<Feedback> feedbacks) {
        ExportResultConverter converter = new StarRatingV1ExportResultConverter(feedbacks);
        return converter;
    }

    @Override
    public void deleteFeedbacks(List<Long> ids, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(DELETE_SQL);
            for (Long l : ids) {
                stmt.setLong(1, l.longValue());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw FeedbackExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

    @Override
    public String getType() {
        return TYPE;
    }
}
