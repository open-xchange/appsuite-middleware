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
import java.util.Collection;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.userfeedback.ExportResult;
import com.openexchange.userfeedback.ExportResultConverter;
import com.openexchange.userfeedback.ExportType;
import com.openexchange.userfeedback.Feedback;

/**
 * {@link StarRatingV1ExportResultConverter}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public class StarRatingV1ExportResultConverter implements ExportResultConverter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(StarRatingV1ExportResultConverter.class);

    private static final Pattern PATTERN_QUOTE = Pattern.compile("\"", Pattern.LITERAL);
    private static final char CELL_DELIMITER = ',';
    private static final char ROW_DELIMITER = '\n';

    private Collection<Feedback> feedbacks;

    public StarRatingV1ExportResultConverter(Collection<Feedback> feedbacks) {
        this.feedbacks = feedbacks;
    }

    @Override
    public ExportResult get(ExportType type) {
        switch (type) {
            case CSV:
                return createCsvStream();
            case RAW:
            default:
                return createRaw();
        }
    }

    private ExportResult createRaw() {
        StarRatingV1ExportResult exportResult = new StarRatingV1ExportResult();
        JSONArray result = new JSONArray(feedbacks.size());
        for (Feedback feedback : feedbacks) {
            JSONObject current = (JSONObject) feedback.getContent();
            result.put(current);
        }
        exportResult.setRAW(result);
        return exportResult;
    }

    private ExportResult createCsvStream() {
        StarRatingV1ExportResult exportResult = new StarRatingV1ExportResult();
        ThresholdFileHolder sink = new ThresholdFileHolder();
        OutputStreamWriter writer = new OutputStreamWriter(sink.asOutputStream(), Charsets.UTF_8);
        try {
            final StarRatingV1Fields[] jsonFields = StarRatingV1Fields.values();
            writer.write(convertToLine(jsonFields, null));

            for (Feedback feedback : feedbacks) {
                writer.write(convertToLine(jsonFields, (JSONObject) feedback.getContent()));
            }
            writer.flush();
            exportResult.setCSV(sink.getClosingStream());
        } catch (final IOException | OXException | JSONException e) {
            sink.close();
        }
        return exportResult;
    }

    private String convertToLine(StarRatingV1Fields[] jsonFields, JSONObject object) throws JSONException {
        boolean isHeader = object == null;
        StringBuilder bob = new StringBuilder(1024);
        for (StarRatingV1Fields token : jsonFields) {
            bob.append('"');
            if (isHeader) {
                bob.append(PATTERN_QUOTE.matcher(token.getDisplayName()).replaceAll("\"\""));
            } else {
                bob.append(PATTERN_QUOTE.matcher(object.getString(token.name())).replaceAll("\"\""));
            }
            bob.append('"');
            bob.append(CELL_DELIMITER);
        }
        bob.setCharAt(bob.length() - 1, ROW_DELIMITER);

        return bob.toString();
    }
}
