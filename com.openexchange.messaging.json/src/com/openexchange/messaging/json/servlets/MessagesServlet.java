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

package com.openexchange.messaging.json.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
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

    /**
     *
     */
    private static final long serialVersionUID = 2342329232570692011L;

    private static final Object RESOLVE = "resolve";
    private final List<MessagingContentDumper> dumpers = new ArrayList<MessagingContentDumper>(1) {{
        add(new BinaryContentDumper());
        // Add more as needed
    }};

    public MessagesServlet() {
        super(MessagingActionFactory.INSTANCE);
    }

    @Override
    protected boolean hasModulePermission(final ServerSession session) {
        return true;
    }

    @Override
    protected boolean handleIndividually(final String action, final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException, OXException {
        if(RESOLVE.equals(action)) {
            final ServerSession session = getSessionObject(req);
            final AJAXRequestData requestData = parseRequest(req, false, FileUploadBase.isMultipartContent(new ServletRequestContext(req)), session, resp);
            final MessagingRequestData request = MessagingActionFactory.INSTANCE.wrapRequest(requestData, session);

            try {
                final MessagingMessageAccess messageAccess = request.getMessageAccess(session.getUserId(), session.getContextId());

                final MessagingContent content = messageAccess.resolveContent(request.getFolderId(), request.getId(), request.getReferenceId());

                //TODO: Set Content-Type Header
                for(final MessagingContentDumper dumper : dumpers) {
                    if(dumper.handles(content)) {
                        dumper.dump(content, resp.getOutputStream());
                    }
                }


            } catch (final OXException e) {
                throw new ServletException(e);
            }

            return true;
        }
        return super.handleIndividually(action, req, resp);
    }

}
