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
import java.util.Map;
import java.util.regex.Matcher;
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

    private static final char TEXT_QUALIFIER = '"';
    private static final char CELL_DELIMITER = ';';
    private static final char CARRIAGE_RETURN = '\r';

    private final Collection<Feedback> feedbacks;

    private final char delimiter;

    public StarRatingV1ExportResultConverter(Collection<Feedback> feedbacks, Map<String, String> configuration) {
        this.feedbacks = feedbacks;

        String lDelimiter = configuration.get("delimiter");
        this.delimiter = lDelimiter != null ? lDelimiter.charAt(0) : CELL_DELIMITER;
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
        boolean error = true;
        try {
            final StarRatingV1Fields[] jsonFields = StarRatingV1Fields.values();
            StringBuilder bob = new StringBuilder(1024);

            writer.write('\ufeff'); // BOM for UTF-*
            // Writer header line
            writer.write(convertToLine(jsonFields, null, bob));

            for (Feedback feedback : feedbacks) {
                // Write entry line
                String convertToLine = convertToLine(jsonFields, (JSONObject) feedback.getContent(), bob);
                writer.write(convertToLine);
            }
            writer.flush();
            exportResult.setCSV(sink.getClosingStream());
            error = false;
        } catch (IOException | OXException | JSONException | RuntimeException e) {
            LOG.error("Failed to create CSV stream", e);
        } finally {
            if (error) {
                sink.close();
            }
        }
        return exportResult;
    }

    private String convertToLine(StarRatingV1Fields[] jsonFields, JSONObject object, StringBuilder sb) throws JSONException {
        StringBuilder bob;
        if (null == sb) {
            bob = new StringBuilder(1024);
        } else {
            bob = sb;
            bob.setLength(0);
        }

        if (null == object) {
            // Header line
            for (StarRatingV1Fields token : jsonFields) {
                bob.append(TEXT_QUALIFIER);
                bob.append(sanitize(token.getDisplayName()));
                bob.append(TEXT_QUALIFIER);
                bob.append(this.delimiter);
            }
        } else {
            for (StarRatingV1Fields token : jsonFields) {
                bob.append(TEXT_QUALIFIER);
                String sanitizedLineBreaks = correctLineBreaks(object, token);
                String replaced = useSingleQuotes(sanitizedLineBreaks);
                String sanitizedValue = sanitize(replaced);
                bob.append(sanitizedValue);
                bob.append(TEXT_QUALIFIER);
                bob.append(this.delimiter);
            }
        }
        bob.setCharAt(bob.length() - 1, CARRIAGE_RETURN);
        return bob.toString();
    }

    private String useSingleQuotes(String sanitizedLineBreaks) {
        return sanitizedLineBreaks.replace("\"", "'");
    }

    private final Pattern p = Pattern.compile("\r?\n");

    private String correctLineBreaks(JSONObject object, StarRatingV1Fields token) throws JSONException {
        String content = object.getString(token.name());

        Matcher m = p.matcher(content);
        StringBuffer buffer = new StringBuffer(content.length());
        while (m.find()) {
            m.appendReplacement(buffer, "\r\n");
        }
        m.appendTail(buffer);
        return buffer.toString();
    }

    private String sanitize(String value) {
        int length = value.length();
        if (length <= 0) {
            return value;
        }

        StringBuilder builder = null;

        char firstChar = value.charAt(0);
        if (needsSanitizing(firstChar)) {
            builder = new StringBuilder(length);
            builder.append('\'').append(firstChar);
        }

        for (int i = 1; i < length; i++) {
            char c = value.charAt(i);
            if (null == builder) {
                if (c == '|') {
                    builder = new StringBuilder(length);
                    if (i > 0) {
                        builder.append(value, 0, i);
                    }
                    builder.append("\\").append(c);
                }
            } else {
                if (c == '|') {
                    builder.append("\\").append(c);
                } else {
                    builder.append(c);
                }
            }
        }
        return null == builder ? value : builder.toString();
    }

    private boolean needsSanitizing(char c) {
        switch (c) {
            case '=':
                return true;
            case '+':
                return true;
            case '-':
                return true;
            case '@':
                return true;
            case '|':
                return true;
            default:
                return false;
        }
    }

}
