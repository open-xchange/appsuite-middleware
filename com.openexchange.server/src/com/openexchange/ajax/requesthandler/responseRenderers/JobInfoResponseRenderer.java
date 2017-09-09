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

package com.openexchange.ajax.requesthandler.responseRenderers;

import static com.openexchange.ajax.fields.ResponseFields.DATA;
import static com.openexchange.tools.servlet.http.Tools.getWriterFrom;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.ResponseRenderer;
import com.openexchange.ajax.requesthandler.jobqueue.JobInfo;
import com.openexchange.ajax.requesthandler.jobqueue.JobQueueExceptionCodes;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.util.UUIDs;
import com.openexchange.tools.session.ServerSession;

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
            resp.setContentType("text/javascript");

            JSONObject jData = new JSONObject(2);
            jData.put("job", UUIDs.getUnformattedString(jobInfo.getId()));

            JSONObject jResponse = new JSONObject(2);
            jResponse.put(DATA, jData);

            OXException warning = JobQueueExceptionCodes.LONG_RUNNING_OPERATION.create(session.getUserId(), session.getContextId());
            Locale locale = localeFrom(session);
            ResponseWriter.addException(jResponse, warning, locale, false);
            ResponseWriter.addWarning(jResponse, warning, locale);

            try {
                jResponse.write(writer, false);
                // Successfully written...
                return true;
            } catch (final JSONException e) {
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
