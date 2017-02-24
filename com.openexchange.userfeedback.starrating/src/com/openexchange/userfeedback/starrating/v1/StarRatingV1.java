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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.java.AsciiReader;
import com.openexchange.java.AsciiWriter;
import com.openexchange.java.Streams;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.userfeedback.ExportResultConverter;
import com.openexchange.userfeedback.Feedback;
import com.openexchange.userfeedback.FeedbackMetaData;
import com.openexchange.userfeedback.FeedbackType;
import com.openexchange.userfeedback.starrating.exceptions.StarRatingExceptionCodes;

/**
 * {@link StarRatingV1}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public class StarRatingV1 implements FeedbackType {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(StarRatingV1.class);

    private static final String TYPE = "star-rating-v1";
    private static final String INSERT_SQL = "INSERT INTO star_rating_v1 (data) VALUES (?)";
    private static final String SELECT_SQL = "SELECT id, data FROM star_rating_v1 WHERE id IN (";

    @Override
    public long storeFeedback(JSONObject feedback, Connection con) throws OXException {
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            setBinaryStream(feedback, stmt, 1);
            //            stmt.setObject(1, feedback);
            stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }

            return -1;
        } catch (final SQLException e) {
            throw StarRatingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
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
        } catch (final SQLException | JSONException e) {
            throw StarRatingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public ExportResultConverter getFeedbacks(List<FeedbackMetaData> feedbackMetaData, Connection con) throws OXException {
        if (feedbackMetaData.size() == 0) {
            return ExportResultConverter.EMTPY_CONVERTER;
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
                    JSONObject content = new JSONObject(new AsciiReader(rs.getBinaryStream(2)));
                    current.setContent(content);
                } catch (JSONException e) {
                    LOG.error("Unable to read feedback with id {}. Won't return it.", id, e);
                }
            }
        } catch (final SQLException e) {
            throw StarRatingExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
        return createExportObject(feedbacks.values());
    }

    private ExportResultConverter createExportObject(Collection<Feedback> feedbacks) {
        ExportResultConverter converter = new StarRatingV1ExportResultConverter(feedbacks);
        return converter;
    }

    @Override
    public void deleteFeedbacks(List<Long> ids, Connection con) {
        // TODO Auto-generated method stub
    }

    @Override
    public String getType() {
        return TYPE;
    }

}
