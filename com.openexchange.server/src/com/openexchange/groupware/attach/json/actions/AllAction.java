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

import org.json.JSONException;
import org.json.JSONValue;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.parser.AttachmentParser.UnknownColumnException;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.writer.AttachmentWriter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentField;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AllAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AllAction extends AbstractAttachmentAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AllAction.class);

    /**
     * Initializes a new {@link AllAction}.
     */
    public AllAction(final ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        try {
            require(requestData, AJAXServlet.PARAMETER_FOLDERID, AJAXServlet.PARAMETER_MODULE, AJAXServlet.PARAMETER_ATTACHEDID);
            final int folderId = requireNumber(requestData, AJAXServlet.PARAMETER_FOLDERID);
            final int attachedId = requireNumber(requestData, AJAXServlet.PARAMETER_ATTACHEDID);
            final int moduleId = requireNumber(requestData, AJAXServlet.PARAMETER_MODULE);

            final AttachmentField[] columns = PARSER.getColumns(requestData.getParameterValues(AJAXServlet.PARAMETER_COLUMNS));

            AttachmentField sort = null;
            if (null != requestData.getParameter(AJAXServlet.PARAMETER_SORT)) {
                sort = AttachmentField.get(Integer.parseInt(requestData.getParameter(AJAXServlet.PARAMETER_SORT)));
            }

            int order = AttachmentBase.ASC;
            if ("DESC".equalsIgnoreCase(requestData.getParameter(AJAXServlet.PARAMETER_ORDER))) {
                order = AttachmentBase.DESC;
            }
            final JSONValue jsonValue = all(session, folderId, attachedId, moduleId, columns, sort, order);
            return new AJAXRequestResult(jsonValue, "apiResponse");
        } catch (final RuntimeException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final UnknownColumnException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private JSONValue all(final ServerSession session, final int folderId, final int attachedId, final int moduleId, final AttachmentField[] fields, final AttachmentField sort, final int order) throws OXException {
        SearchIterator<AttachmentMetadata> iter = null;
        boolean rollback = false;
        try {
            ATTACHMENT_BASE.startTransaction();
            rollback = true;

            final Context ctx = session.getContext();
            final User user = session.getUser();
            final UserConfiguration userConfig = session.getUserConfiguration();

            TimedResult<AttachmentMetadata> result;
            if (sort != null) {
                result = ATTACHMENT_BASE.getAttachments(session, folderId, attachedId, moduleId, fields, sort, order, ctx, user, userConfig);
            } else {
                result = ATTACHMENT_BASE.getAttachments(session, folderId, attachedId, moduleId, ctx, user, userConfig);
            }
            iter = result.results();
            final OXJSONWriter w = new OXJSONWriter();
            final AttachmentWriter aWriter = new AttachmentWriter(w);
            aWriter.timedResult(result.sequenceNumber());
            aWriter.writeAttachments(iter, fields, TimeZoneUtils.getTimeZone(user.getTimeZone()));
            aWriter.endTimedResult();
            // w.flush();
            ATTACHMENT_BASE.commit();
            rollback = false;
            return w.getObject();
        } catch (JSONException e) {
            throw new OXException(e);
        } finally {
            if (rollback) {
                rollback();
            }
            try {
                ATTACHMENT_BASE.finish();
            } catch (final OXException e) {
                LOG.error("", e);
            }
            SearchIterators.close(iter);
        }
    }

}
