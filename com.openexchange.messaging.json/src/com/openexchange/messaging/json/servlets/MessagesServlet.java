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

package com.openexchange.messaging.json.servlets;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import com.google.common.collect.ImmutableList;
import com.openexchange.ajax.MultipleAdapterServletNew;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingContent;
import com.openexchange.messaging.MessagingMessageAccess;
import com.openexchange.messaging.json.BinaryContentDumper;
import com.openexchange.messaging.json.MessagingContentDumper;
import com.openexchange.messaging.json.actions.messages.MessagingActionFactory;
import com.openexchange.messaging.json.actions.messages.MessagingRequestData;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MessagesServlet}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MessagesServlet extends MultipleAdapterServletNew {

    private static final long serialVersionUID = 2342329232570692011L;

    private static final Object RESOLVE = "resolve";

    private final List<MessagingContentDumper> dumpers = ImmutableList.of(new BinaryContentDumper()); // Add more as needed

    public MessagesServlet() {
        super(MessagingActionFactory.INSTANCE);
    }

    @Override
    protected boolean hasModulePermission(final ServerSession session) {
        return true;
    }

    @Override
    protected boolean handleIndividually(final String action, final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException, OXException {
        if (RESOLVE.equals(action)) {
            final ServerSession session = getSessionObject(req);
            final AJAXRequestData requestData = parseRequest(req, false, FileUploadBase.isMultipartContent(new ServletRequestContext(req)), session, resp);
            final MessagingRequestData request = MessagingActionFactory.INSTANCE.wrapRequest(requestData, session);

            try {
                final MessagingMessageAccess messageAccess = request.getMessageAccess(session.getUserId(), session.getContextId());

                final MessagingContent content = messageAccess.resolveContent(request.getFolderId(), request.getId(), request.getReferenceId());

                //TODO: Set Content-Type Header
                for (final MessagingContentDumper dumper : dumpers) {
                    if (dumper.handles(content)) {
                        dumper.dump(content, resp.getOutputStream());
                    }
                }

            } catch (OXException e) {
                throw new ServletException(e);
            }

            return true;
        }
        return super.handleIndividually(action, req, resp);
    }

}
