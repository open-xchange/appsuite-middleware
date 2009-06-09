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

package com.openexchange.subscribe.json;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.ajax.PermissionServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;
import com.openexchange.tools.exceptions.LoggingLogic;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;
import static com.openexchange.subscribe.json.SubscriptionJSONErrorMessages.*;


/**
 * {@link SubscriptionSourcesServlet}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class SubscriptionSourcesServlet extends AbstractSubscriptionServlet {

    public static final int CLASS_ID = 1;
    
    private static final long serialVersionUID = 6645557914497380571L;
    private static final Log LOG = LogFactory.getLog(SubscriptionSourcesServlet.class);
    private static final LoggingLogic LL = LoggingLogic.getLoggingLogic(SubscriptionSourcesServlet.class, LOG);
    
    
    private static SubscriptionSourceDiscoveryService discoverer = null;
    private static SubscriptionSourceJSONWriterInterface writer = null;
    
    public static void setSubscriptionSourceDiscoveryService(SubscriptionSourceDiscoveryService service) {
        discoverer = service;
    }

    public static void setSubscriptionSourceJSONWriter(SubscriptionSourceJSONWriterInterface service) {
        writer = service;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }

    protected void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String action = req.getParameter("action");
            if(null == action) {
                MISSING_PARAMETER.throwException("action");
            } else if(action.equals("listSources") || action.equals("all")) {
                listSources(req, resp);
            } else if (action.equals("getSource") || action.equals("get")) {
                getSource(req, resp);
            } else {
                UNKNOWN_ACTION.throwException(action);
            }
        } catch (AbstractOXException x) {
            writeOXException(x, resp);
        } catch (Throwable t) {
            writeOXException(wrapThrowable(t), resp);
        }
    }
    
    
    protected void listSources(HttpServletRequest req, HttpServletResponse resp) throws AbstractOXException  {
        int module = getModule(req, resp);
        List<SubscriptionSource> sources = discoverer.getSources(module);
        String[] columns = getColumns(req);
        JSONArray json = writer.writeJSONArray(sources, columns);
        writeData(json, resp);
    }
    
    
    private String[] getColumns(HttpServletRequest req) {
        String columns = req.getParameter("columns");
        if(columns == null) {
            return new String[]{"id", "displayName", "module", "icon",  "formDescription"};
        }
        return columns.split("\\s*,\\s*"); 
    }

    protected void getSource(HttpServletRequest req, HttpServletResponse resp) throws AbstractOXException {
        String identifier = req.getParameter("id");
        if(identifier == null) {
            MISSING_PARAMETER.throwException("id");
        }
        SubscriptionSource source = discoverer.getSource(identifier);
        JSONObject data = writer.writeJSON(source);
        writeData(data, resp);
    }
    
    protected int getModule(HttpServletRequest req, HttpServletResponse resp) throws AbstractOXException {
        String moduleAsString = req.getParameter("module");
        if(moduleAsString == null) {
            return -1;
        }
        if(moduleAsString.equals("contacts")) {
            return FolderObject.CONTACT;
        } else if (moduleAsString.equals("calendar")) {
            return FolderObject.CALENDAR;
        } else if (moduleAsString.equals("tasks")) {
            return FolderObject.TASK;
        } else if (moduleAsString.equals("infostore")) {
            return FolderObject.INFOSTORE;
        }
        return -1;
    }


    @Override
    protected Log getLog() {
        return LOG;
    }
    
    @Override
    protected LoggingLogic getLoggingLogic() {
        return LL;
    }
    
}
