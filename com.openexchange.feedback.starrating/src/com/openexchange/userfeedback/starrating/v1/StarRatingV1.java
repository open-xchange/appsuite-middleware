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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.userfeedback.ExportType;
import com.openexchange.userfeedback.FeedbackType;
import com.openexchange.java.Charsets;

/**
 * {@link StarRatingV1}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public class StarRatingV1 implements FeedbackType {

    private static final List<String> DISPLAY_FIELDS = new ArrayList<>();

    static {
        for(JSONField field: JSONField.values()) {
            DISPLAY_FIELDS.add(field.getDisplayName());
        }
    }

    private static final String TYPE = "star-rating-v1";
    private static final String INSERT_SQL = "INSERT INTO star_rating_v1 (data) VALUES (?)";
    private static final String SELECT_SQL = "SELECT data FROM star_rating_v1 WHERE id IN (";

    private static final Pattern PATTERN_QUOTE = Pattern.compile("\"", Pattern.LITERAL);
    public static final char CELL_DELIMITER = ',';
    public static final char ROW_DELIMITER = '\n';


    @Override
    public long storeFeedback(Object feedback, Connection con) throws SQLException {

        PreparedStatement stmt = con.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
        ResultSet rs = null;
        try {
            stmt.setObject(1, feedback);
            stmt.executeUpdate();
            rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }

            return -1;
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }


    @Override
    public Object getFeedbacks(List<Long> ids, Connection con, ExportType type) throws SQLException {
        if(ids.size()==0){
            return null;
        }

        String sql = Databases.getIN(SELECT_SQL, ids.size());
        PreparedStatement stmt = con.prepareStatement(sql);
        ResultSet rs = null;
        try {
            int x=1;
            for(Long id: ids){
                stmt.setLong(x++, id);
            }
            ResultSet resultSet = stmt.executeQuery();
            switch(type){
             case CSV:
                 return convertResultsToCSVStream(resultSet);
             case RAW:
             default:
                 List<Object> result = new ArrayList<>(ids.size());
                 while(resultSet.next()){
                     result.add(resultSet.getObject(1));
                 }
                 return result;
            }

        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
    }


    @SuppressWarnings("resource")
    private Object convertResultsToCSVStream(ResultSet resultSet) throws SQLException {

        ThresholdFileHolder sink = new ThresholdFileHolder();
        OutputStreamWriter writer = new OutputStreamWriter(sink.asOutputStream(), Charsets.UTF_8);
        try {
            writer.write(convertToLine(DISPLAY_FIELDS));
            while (resultSet.next()) {
                JSONObject current = (JSONObject) resultSet.getObject(1);
                try {
                    writer.write(convertToLine(convertToList(current)));
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
            writer.flush();
            return sink.getClosingStream();
        } catch (IOException e) {
            sink.close();
        } catch (OXException e) {
            sink.close();
            e.printStackTrace();
        }

        return null;
    }

    private static String convertToLine(final List<String> line) {
        StringBuilder bob = new StringBuilder(1024);
        for (String token : line) {
            bob.append('"');
            bob.append(PATTERN_QUOTE.matcher(token).replaceAll("\"\""));
            bob.append('"');
            bob.append(CELL_DELIMITER);
        }
        bob.setCharAt(bob.length() - 1, ROW_DELIMITER);
        return bob.toString();
    }

    private static List<String> convertToList(final JSONObject json) throws JSONException {
        final List<String> l = new LinkedList<String>();
        for (JSONField field: JSONField.values()) {
            l.add(json.getString(field.name()));
        }
        return l;
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
