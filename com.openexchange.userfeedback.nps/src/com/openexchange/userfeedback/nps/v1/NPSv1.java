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

package com.openexchange.userfeedback.nps.v1;

import static com.openexchange.java.Autoboxing.L;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.common.hash.Hashing;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.java.AsciiReader;
import com.openexchange.java.AsciiWriter;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.userfeedback.AbstractJSONFeedbackType;
import com.openexchange.userfeedback.Feedback;
import com.openexchange.userfeedback.FeedbackMetaData;
import com.openexchange.userfeedback.exception.FeedbackExceptionCodes;
import com.openexchange.userfeedback.export.ExportResultConverter;
import com.openexchange.userfeedback.fields.UserFeedbackField;
import com.openexchange.userfeedback.nps.exception.NPSExceptionCodes;

/**
 * 
 * {@link NPSv1}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.4
 */
public class NPSv1 extends AbstractJSONFeedbackType {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(NPSv1.class);

    private static final String TYPE = "nps-v1";
    private static final String INSERT_SQL = "INSERT INTO feedback_nps_v1 (data) VALUES (?)";
    private static final String SELECT_SQL = "SELECT id, data FROM feedback_nps_v1 WHERE id IN (";
    private static final String DELETE_SQL = "DELETE FROM feedback_nps_v1 WHERE id = ?";

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validate(JSONObject jsonFeedback) throws OXException {
        if (!jsonFeedback.has(NPSv1ExportFields.SCORE.getName())) {
            throw NPSExceptionCodes.PARAMETER_MISSING.create(NPSv1ExportFields.SCORE.getName());
        }

        try {
            String score = jsonFeedback.getString(NPSv1ExportFields.SCORE.getName());
            if (Strings.isEmpty(score)) {
                throw NPSExceptionCodes.INVALID_TYPE.create(score);
            }
            long scoreInt = Long.valueOf(score).longValue();
            if (scoreInt < Integer.MIN_VALUE || scoreInt > Integer.MAX_VALUE) {
                throw NPSExceptionCodes.INVALID_VALUE.create(NPSv1ExportFields.SCORE.getDisplayName(), L(scoreInt));
            }
            String questionId = jsonFeedback.optString(NPSv1ExportFields.QUESTION_ID.getName());
            if (Strings.isEmpty(questionId)) {
                return;
            }
            long questionIdInt = Long.valueOf(questionId).longValue();
            if (questionIdInt < 0 || questionIdInt > 3) {
                throw NPSExceptionCodes.INVALID_VALUE.create(NPSv1ExportFields.QUESTION_ID.getDisplayName(), L(questionIdInt));
            }
        } catch (JSONException e) {
            LOG.error("Unable to retrieve 'score' from feedback.", e);
            throw NPSExceptionCodes.PARAMETER_MISSING.create(NPSv1ExportFields.SCORE.getName());
        } catch (NumberFormatException e) {
            LOG.error("Unable to parse 'score' value from feedback.", e);
            throw NPSExceptionCodes.BAD_PARAMETER.create(NPSv1ExportFields.SCORE.getName());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long storeFeedbackInternal(JSONObject jsonFeedback, Connection con) throws OXException {
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
        } catch (SQLException e) {
            throw FeedbackExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
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
        } catch (SQLException | JSONException e) {
            throw FeedbackExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExportResultConverter getFeedbacks(List<FeedbackMetaData> feedbackMetaData, Connection con) throws OXException {
        return getFeedbacks(feedbackMetaData, con, Collections.<String, String> emptyMap());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExportResultConverter getFeedbacks(List<FeedbackMetaData> feedbackMetaData, Connection con, Map<String, String> configuration) throws OXException {
        if (feedbackMetaData == null || feedbackMetaData.size() == 0) {
            return new NPSv1ExportResultConverter(Collections.<Feedback> emptyList(), configuration);
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
                feedbacks.put(L(meta.getTypeId()), Feedback.builder(meta).build());
            }
            rs = stmt.executeQuery();

            while (rs.next()) {
                long id = rs.getLong(1);
                try {
                    Feedback current = feedbacks.get(L(id));
                    enrichContent(new AsciiReader(rs.getBinaryStream(2)), current);
                } catch (JSONException e) {
                    LOG.error("Unable to read feedback with id {}. Won't return it.", L(id), e);
                }
            }
            SortedSet<Feedback> sorted = sort(feedbacks.values());
            return new NPSv1ExportResultConverter(sorted, configuration);
        } catch (SQLException e) {
            throw FeedbackExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Sorts the given collection of {@link Feedback}s based on the date
     * 
     * @param collection {@link Collection} to sort
     * @return {@link SortedSet} of {@link Feedback}
     */
    private SortedSet<Feedback> sort(Collection<Feedback> collection) {
        Comparator<Feedback> comparator = new Comparator<Feedback>() {

            @Override
            public int compare(Feedback o1, Feedback o2) {
                if (o1.getDate() < o2.getDate()) {
                    return -1;
                }
                return 1;

            }
        };
        SortedSet<Feedback> sorted = new TreeSet<Feedback>(comparator);
        sorted.addAll(collection);
        return sorted;
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

        Instant ofEpochMilli = Instant.ofEpochMilli(current.getDate());
        LocalDateTime date = LocalDateTime.ofInstant(ofEpochMilli, ZoneId.of("Z"));
        content.put("date", date.format(dateTimeFormatter));

        String userContextTupel = current.getCtxId() + ":" + current.getUserId();
        @SuppressWarnings("deprecation") String hashedTupel = Hashing.md5().hashString(userContextTupel, StandardCharsets.UTF_8).toString();
        content.put("user", hashedTupel);
        content.put("server_version", current.getServerVersion());
        content.put("client_version", current.getUiVersion());
        current.setContent(content);
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
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    protected List<UserFeedbackField> getRequiredFields() throws OXException {
        return NPSv1ExportFields.getFieldsRequiredByClient();
    }

}
