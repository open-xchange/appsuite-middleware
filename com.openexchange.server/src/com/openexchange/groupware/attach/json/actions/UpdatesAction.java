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

package com.openexchange.groupware.attach.json.actions;

import org.json.JSONValue;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.parser.AttachmentParser.UnknownColumnException;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.writer.AttachmentWriter;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentField;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link UpdatesAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.GET, name = "updates", description = "Get updated attachments.", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "folder", description = "Object ID of the folder, whose contents are queried."),
    @Parameter(name = "attached", description = "Object ID of the object to which the attachments are attached."),
    @Parameter(name = "module", description = "Module ID (as per Attachment object) of the attached object."),
    @Parameter(name = "columns", description = "A comma-separated list of columns to return. Each column is specified by a numeric column identifier. Column identifiers for attachments are defined in Common object data (with only id, created_by and creation_date available) and Attachment object."),
    @Parameter(name = "sort", optional=true, description = "The identifier of a column which determines the sort order of the response. If this parameter is specified, then the parameter order must be also specified."),
    @Parameter(name = "order", optional=true, description = "\"asc\" if the response entires should be sorted in the ascending order, \"desc\" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified."),
    @Parameter(name = "timestamp", description = "Timestamp of the last update of the requested infoitems."),
    @Parameter(name = "ignore", optional=true, description = "Which kinds of updates should be ignored. Currently, the only valid value \u2013 \"deleted\" \u2013 causes deleted object IDs not to be returned.")
}, responseDescription = "Response with timestamp: An array with new and deleted attachments for the specified object. New attachments are represented by arrays. The elements of each array contain the information specified by the corresponding identifiers in the columns parameter. Deleted attachments (should the ignore parameter be ever implemented) would be identified by their object IDs as plain numbers, without being part of a nested array.")
public final class UpdatesAction extends AbstractAttachmentAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UpdatesAction.class);

    /**
     * Initializes a new {@link UpdatesAction}.
     */
    public UpdatesAction(final ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        try {
            require(
                requestData,
                AJAXServlet.PARAMETER_FOLDERID,
                AJAXServlet.PARAMETER_MODULE,
                AJAXServlet.PARAMETER_ATTACHEDID,
                AJAXServlet.PARAMETER_TIMESTAMP);
            final int folderId = requireNumber(requestData, AJAXServlet.PARAMETER_FOLDERID);
            final int attachedId = requireNumber(requestData, AJAXServlet.PARAMETER_ATTACHEDID);
            final int moduleId = requireNumber(requestData, AJAXServlet.PARAMETER_MODULE);

            long timestamp = -1;
            try {
                timestamp = Long.parseLong(requestData.getParameter(AJAXServlet.PARAMETER_TIMESTAMP).trim());
            } catch (final NumberFormatException nfe) {
                throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(
                    AJAXServlet.PARAMETER_TIMESTAMP,
                    requestData.getParameter(AJAXServlet.PARAMETER_TIMESTAMP));
            }

            final AttachmentField[] columns = PARSER.getColumns(requestData.getParameterValues(AJAXServlet.PARAMETER_COLUMNS));

            AttachmentField sort = null;
            if (null != requestData.getParameter(AJAXServlet.PARAMETER_SORT)) {
                sort = AttachmentField.get(Integer.parseInt(requestData.getParameter(AJAXServlet.PARAMETER_SORT)));
            }

            int order = AttachmentBase.ASC;
            if ("DESC".equalsIgnoreCase(requestData.getParameter(AJAXServlet.PARAMETER_ORDER))) {
                order = AttachmentBase.DESC;
            }

            final String delete = requestData.getParameter(AJAXServlet.PARAMETER_IGNORE);

            final String timeZoneId = requestData.getParameter(AJAXServlet.PARAMETER_TIMEZONE);

            return new AJAXRequestResult(updates(
                session,
                folderId,
                attachedId,
                moduleId,
                timestamp,
                "deleted".equals(delete),
                columns,
                sort,
                order,
                timeZoneId), "apiResponse");
        } catch (final RuntimeException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final UnknownColumnException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private JSONValue updates(final ServerSession session, final int folderId, final int attachedId, final int moduleId, final long ts, final boolean ignoreDeleted, final AttachmentField[] fields, final AttachmentField sort, final int order, final String timeZoneId) throws OXException {

        SearchIterator<AttachmentMetadata> iter = null;
        SearchIterator<AttachmentMetadata> iter2 = null;

        try {
            ATTACHMENT_BASE.startTransaction();

            final Context ctx = session.getContext();
            final User user = session.getUser();
            final UserConfiguration userConfig = session.getUserConfiguration();

            Delta<AttachmentMetadata> delta;
            if (sort != null) {
                delta =
                    ATTACHMENT_BASE.getDelta(session, folderId, attachedId, moduleId, ts, ignoreDeleted, fields, sort, order, ctx, user, userConfig);
            } else {
                delta = ATTACHMENT_BASE.getDelta(session, folderId, attachedId, moduleId, ts, ignoreDeleted, ctx, user, userConfig);
            }
            iter = delta.results();
            iter2 = delta.getDeleted();

            final OXJSONWriter w = new OXJSONWriter();
            final AttachmentWriter aWriter = new AttachmentWriter(w);
            aWriter.timedResult(delta.sequenceNumber());
            aWriter.writeDelta(
                iter,
                iter2,
                fields,
                ignoreDeleted,
                null == timeZoneId ? TimeZoneUtils.getTimeZone(user.getTimeZone()) : TimeZoneUtils.getTimeZone(timeZoneId));
            aWriter.endTimedResult();
            // w.flush();
            ATTACHMENT_BASE.commit();

            return w.getObject();
        } catch (final Throwable t) {
            rollback();
            if (t instanceof OXException) {
                throw (OXException) t;
            }
            throw new OXException(t);
        } finally {
            try {
                ATTACHMENT_BASE.finish();
            } catch (final OXException e) {
                LOG.error("", e);
            }
            SearchIterators.close(iter);
            SearchIterators.close(iter2);
        }
    }

}
