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

package com.openexchange.ajax.requesthandler.responseRenderers;

import static com.openexchange.ajax.fields.ResponseFields.DATA;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.servlet.http.Tools.getWriterFrom;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.ResponseRenderer;
import com.openexchange.ajax.requesthandler.jobqueue.JobInfo;
import com.openexchange.ajax.requesthandler.jobqueue.JobQueueExceptionCodes;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

/**
 * {@link JobInfoResponseRenderer}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class JobInfoResponseRenderer implements ResponseRenderer {

    /**
     * The logger constant.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(JobInfoResponseRenderer.class);

    /**
     * Initializes a new {@link JobInfoResponseRenderer}.
     */
    public JobInfoResponseRenderer() {
        super();
    }

    /**
     * Gets the locale for given server session
     *
     * @param session The server session
     * @return The locale
     */
    private static Locale localeFrom(final ServerSession session) {
        if (null == session) {
            return Locale.US;
        }
        User user = session.getUser();
        if (user == null) {
            return Locale.US;
        }

        return user.getLocale();
    }

    @Override
    public int getRanking() {
        return 0;
    }

    @Override
    public boolean handles(final AJAXRequestData request, final AJAXRequestResult result) {
        return result.getResultObject() instanceof JobInfo;
    }

    @Override
    public void write(final AJAXRequestData request, final AJAXRequestResult result, final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        JobInfo jobInfo = (JobInfo) result.getResultObject();

        // Try to obtain writer instance and then output response
        PrintWriter writer = getWriterFrom(resp);
        if (null != writer) {
            writeResponse(jobInfo, writer, req, resp, request.getSession());
        }
    }

    private static boolean writeResponse(JobInfo jobInfo, PrintWriter writer, HttpServletRequest req, HttpServletResponse resp, ServerSession session) throws IOException {
        try {
            resp.setStatus(HttpServletResponse.SC_ACCEPTED);
            AJAXServlet.setDefaultContentType(resp);

            JSONObject jData = new JSONObject(2);
            jData.put("job", UUIDs.getUnformattedString(jobInfo.getId()));

            JSONObject jResponse = new JSONObject(2);
            jResponse.put(DATA, jData);

            OXException warning = JobQueueExceptionCodes.LONG_RUNNING_OPERATION.create(I(session.getUserId()), I(session.getContextId()));
            Locale locale = localeFrom(session);
            ResponseWriter.addException(jResponse, warning, locale, false);
            ResponseWriter.addWarning(jResponse, warning, locale);

            try {
                jResponse.write(writer, false);
                // Successfully written...
                return true;
            } catch (JSONException e) {
                if (e.getCause() instanceof IOException) {
                    /*
                     * Throw proper I/O error since a serious socket error could been occurred which prevents further communication. Just
                     * throwing a JSON error possibly hides this fact by trying to write to/read from a broken socket connection.
                     */
                    throw (IOException) e.getCause();
                }
                /*
                 * Just re-throw JSON error probably caused by a JSON syntax error.
                 */
                throw e;
            }
        } catch (JSONException e) {
            LOG.error("", e);
            try {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "A JSON error occurred: " + e.getMessage());
            } catch (IOException ioe) {
                LOG.error("", ioe);
            }
        }
        return false;
    }

}
