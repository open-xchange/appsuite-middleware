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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.outlook.updater;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.TemplateService;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.webdav.OXServlet;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class UpdaterXMLServlet extends OXServlet {

    private static final long serialVersionUID = -7945036709270719526L;

    private static final Log LOG = LogFactory.getLog(UpdaterXMLServlet.class);

    private static final String TEMPLATE_NAME = "";
    
    private static TemplateService templateService;
    
    private static MailAccountStorageService mailAccountStorageService;

    private ServerSession session;

    public static void setTemplateService(TemplateService templateService) {
        UpdaterXMLServlet.templateService = templateService;
    }

    public static void setMailAccountStorageService(MailAccountStorageService service) {
        UpdaterXMLServlet.mailAccountStorageService = service;
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            getServerSession(req);
            OXTemplate template = templateService.loadTemplate(TEMPLATE_NAME);
            ParameterCollector collector = new ParameterCollector(session, mailAccountStorageService.getDefaultMailAccount(session.getUserId(), session.getContextId()));
            template.process(collector.getParameters(), resp.getWriter());
        } catch (AbstractOXException e) {
            LOG.error(e.getMessage(), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            resp.setContentType("text/html");
        }
    }

    private void getServerSession(HttpServletRequest req) throws ContextException {
        session = new ServerSessionAdapter(getSession(req));
    }

    @Override
    protected void decrementRequests() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void incrementRequests() {
        // TODO Auto-generated method stub

    }

}
