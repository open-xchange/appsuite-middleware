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

package com.openexchange.groupware.attach.json.actions;

import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONValue;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.parser.AttachmentParser.UnknownColumnException;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.writer.AttachmentWriter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentField;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

/**
 * {@link ListAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ListAction extends AbstractAttachmentAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ListAction.class);

    /**
     * Initializes a new {@link ListAction}.
     */
    public ListAction(final ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        try {
            require(requestData, AJAXServlet.PARAMETER_FOLDERID, AJAXServlet.PARAMETER_MODULE, AJAXServlet.PARAMETER_ATTACHEDID);
            final int folderId = requireNumber(requestData, AJAXServlet.PARAMETER_FOLDERID);
            final int attachedId = requireNumber(requestData, AJAXServlet.PARAMETER_ATTACHEDID);
            final int moduleId = requireNumber(requestData, AJAXServlet.PARAMETER_MODULE);

            final JSONArray idsArray = (JSONArray) requestData.requireData();

            final int[] ids = new int[idsArray.length()];
            for (int i = 0; i < idsArray.length(); i++) {
                try {
                    ids[i] = idsArray.getInt(i);
                } catch (JSONException e) {
                    final String string = idsArray.getString(i);
                    try {
                        ids[i] = Integer.parseInt(string);
                    } catch (NumberFormatException e1) {
                        throw AjaxExceptionCodes.INVALID_PARAMETER.create(string);
                    }
                }
            }
            final AttachmentField[] columns = PARSER.getColumns(requestData.getParameterValues(AJAXServlet.PARAMETER_COLUMNS));
            String timeZoneId = requestData.getParameter(AJAXServlet.PARAMETER_TIMEZONE);
            final JSONValue jsonValue = list(session, folderId, attachedId, moduleId, ids, columns, timeZoneId);
            return new AJAXRequestResult(jsonValue, "apiResponse");
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (UnknownColumnException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private JSONValue list(final ServerSession session, final int folderId, final int attachedId, final int moduleId, final int[] ids, final AttachmentField[] fields, String timeZoneId) throws OXException {

        SearchIterator<AttachmentMetadata> iter = null;
        final User user = session.getUser();
        final TimeZone tz;
        if (null == timeZoneId) {
            tz = TimeZoneUtils.getTimeZone(user.getTimeZone());
        } else {
            tz = TimeZoneUtils.getTimeZone(timeZoneId);
        }
        try {
            ATTACHMENT_BASE.startTransaction();

            final Context ctx = session.getContext();
            final UserConfiguration userConfig = session.getUserConfiguration();
            final TimedResult<AttachmentMetadata> result =
                ATTACHMENT_BASE.getAttachments(session, folderId, attachedId, moduleId, ids, fields, ctx, user, userConfig);

            iter = result.results();

            final OXJSONWriter w = new OXJSONWriter();
            final AttachmentWriter aWriter = new AttachmentWriter(w);
            aWriter.timedResult(result.sequenceNumber());
            aWriter.writeAttachments(iter, fields, tz);
            aWriter.endTimedResult();
            // w.flush();

            ATTACHMENT_BASE.commit();

            return w.getObject();
        } catch (Throwable t) {
            rollback();
            if (t instanceof OXException) {
                throw (OXException) t;
            }
            throw new OXException(t);
        } finally {
            try {
                ATTACHMENT_BASE.finish();
            } catch (OXException e) {
                LOG.error("", e);
            }

            SearchIterators.close(iter);
        }
    }

}
