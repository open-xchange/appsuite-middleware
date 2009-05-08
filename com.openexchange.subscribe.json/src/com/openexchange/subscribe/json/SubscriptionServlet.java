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

import static com.openexchange.subscribe.json.SubscriptionJSONErrorMessages.MISSING_PARAMETER;
import static com.openexchange.subscribe.json.SubscriptionJSONErrorMessages.UNKNOWN_ACTION;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.PermissionServlet;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionExecutionService;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.SubscriptionSourceDiscoveryService;
import com.openexchange.tools.exceptions.LoggingLogic;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link SubscriptionServlet}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class SubscriptionServlet extends AbstractSubscriptionServlet {

    private static final Log LOG = LogFactory.getLog(SubscriptionSourcesServlet.class);
    private static final LoggingLogic LL = LoggingLogic.getLoggingLogic(SubscriptionSourcesServlet.class, LOG);
    
    private static SubscriptionSourceDiscoveryService discovery;
    private static SubscriptionExecutionService executor;
    
    public static void setSubscriptionSourceDiscoveryService(SubscriptionSourceDiscoveryService service) {
        discovery = service;
    }
    
    public static void setSubscriptionExecutionService(SubscriptionExecutionService service) {
        executor = service;
    }
    
    @Override
    protected boolean hasModulePermission(ServerSession session) {
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }
    
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }
    
    protected void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String action = req.getParameter("action");
            if(null == action) {
                MISSING_PARAMETER.throwException("action");
            } else if (action.equals("new")) {
                createSubscription(req, resp);
            } else if (action.equals("update")) {
                updateSubscription(req, resp);
            } else if (action.equals("delete")) {
                deleteSubscriptions(req, resp);
            } else if (action.equals("get")) {
                loadSubscription(req, resp);
            } else if (action.equals("all")) {
                loadAllSubscriptionsInFolder(req, resp);
            } else if (action.equals("list")) {
                listSubscriptions(req, resp);
            } else if (action.equals("refresh")) {
                refreshSubscriptions(req, resp);
            } else {
                UNKNOWN_ACTION.throwException(action);
            }
        } catch (AbstractOXException x) {
            writeOXException(x, resp);
        } catch (Throwable t) {
            writeOXException(wrapThrowable(t), resp);
        }
    }
    
    private void refreshSubscriptions(HttpServletRequest req, HttpServletResponse resp) throws AbstractOXException {
        List<Subscription> subscriptionsToRefresh = new ArrayList<Subscription>(10);
        int contextId = getSessionObject(req).getContextId();
        if(null != req.getParameter("id")) {
            int id = Integer.parseInt(req.getParameter("id"));
            Subscription subscription = loadSubscription(id, contextId, req.getParameter("source"));
            subscriptionsToRefresh.add(subscription);
        }
        if(null != req.getParameter("folder")) {
            int folderId = Integer.parseInt(req.getParameter("folder"));
            FolderObject folder = loadFolder(req, resp, folderId);
            List<Subscription> allSubscriptions = getSubscriptionsInFolder(contextId, folder);
            subscriptionsToRefresh.addAll(allSubscriptions);
        }
        
        executor.executeSubscriptions(subscriptionsToRefresh);
        writeData(1, resp);
    }

    private void listSubscriptions(HttpServletRequest req, HttpServletResponse resp) throws JSONException, IOException {
        JSONArray ids = new JSONArray(getBody(req));
        int contextId = getSessionObject(req).getContextId();
        List<Subscription> subscriptions = new ArrayList<Subscription>(ids.length());
        for(int i = 0, size = ids.length(); i < size; i++) {
            int id = ids.getInt(i);
            SubscribeService subscribeService = discovery.getSource(contextId, id).getSubscribeService();
            Subscription subscription = subscribeService.loadSubscription(contextId, id);
            if(subscription != null) {
                subscriptions.add(subscription);
            }
        }
        String[] basicColumns = getBasicColumns(req);
        Map<String, String[]> dynamicColumns = getDynamicColumns(req);
        List<String> dynamicColumnOrder = getDynamicColumnOrder(req);
        
        writeSubscriptions(subscriptions, basicColumns, dynamicColumns, dynamicColumnOrder, resp);
    }

    private void loadAllSubscriptionsInFolder(HttpServletRequest req, HttpServletResponse resp) throws NumberFormatException, OXException {
        int folderId = Integer.parseInt(req.getParameter("folder"));
        int contextId = getSessionObject(req).getContextId();
        
        FolderObject folder = loadFolder(req, resp, folderId);
        
        List<Subscription> allSubscriptions = getSubscriptionsInFolder(contextId, folder);
        
        String[] basicColumns = getBasicColumns(req);
        Map<String, String[]> dynamicColumns = getDynamicColumns(req);
        List<String> dynamicColumnOrder = getDynamicColumnOrder(req);
        
        writeSubscriptions(allSubscriptions, basicColumns, dynamicColumns, dynamicColumnOrder, resp);
        
    }

    private List<Subscription> getSubscriptionsInFolder(int contextId, FolderObject folder) {
        List<SubscriptionSource> sources = discovery.getSources(folder.getModule());
        List<Subscription> allSubscriptions = new ArrayList<Subscription>(10);
        for (SubscriptionSource subscriptionSource : sources) {
            Collection<Subscription> subscriptions = subscriptionSource.getSubscribeService().loadSubscriptions(contextId, folder.getObjectID());
            allSubscriptions.addAll(subscriptions);
        }
        return allSubscriptions;
    }
    
    private void writeSubscriptions(List<Subscription> allSubscriptions, String[] basicColumns, Map<String, String[]> dynamicColumns, List<String> dynamicColumnOrder, HttpServletResponse resp) {
        JSONArray rows = new JSONArray();
        SubscriptionJSONWriter writer = new SubscriptionJSONWriter();
        for (Subscription subscription : allSubscriptions) {
            JSONArray row = writer.writeArray(subscription, basicColumns, dynamicColumns, dynamicColumnOrder, subscription.getSource().getFormDescription());
            rows.put(row);
        }
        writeData(rows, resp);
    }

    private Map<String, String[]> getDynamicColumns(HttpServletRequest req) {
        List<String> identifiers = getDynamicColumnOrder(req);
        Map<String, String[]> dynamicColumns = new HashMap<String, String[]>();
        for(String identifier : identifiers) {
            String columns = req.getParameter(identifier);
            dynamicColumns.put(identifier, columns.split("\\s*,\\s*"));
        }
        return dynamicColumns;
    }
    
    private static final Set<String> KNOWN_PARAMS = new HashSet<String>() {{
        add("folder");
        add("columns");
        add("session");
        add("action");
    }};
    
    private List<String> getDynamicColumnOrder(HttpServletRequest req) {
        Enumeration parameterNames = req.getParameterNames();
        List<String> dynamicColumnIdentifiers = new ArrayList<String>();
        while(parameterNames.hasMoreElements()) {
            String paramName = (String) parameterNames.nextElement();
            if(!KNOWN_PARAMS.contains(paramName)) {
                dynamicColumnIdentifiers.add(paramName);
            }
        }
        Collections.sort(dynamicColumnIdentifiers, new QueryStringPositionComparator(req.getQueryString()));
        return dynamicColumnIdentifiers;
    }

    private String[] getBasicColumns(HttpServletRequest req) {
        String columns = req.getParameter("columns");
        if(columns == null) {
            return new String[]{"id", "folder", "source"};
        }
        return columns.split("\\s*,\\s*");
    }

    private FolderObject loadFolder(HttpServletRequest req, HttpServletResponse resp, int folderId) throws OXException {
        OXFolderAccess ofa = new OXFolderAccess(getSessionObject(req).getContext());
        return ofa.getFolderObject(folderId);
    }


    private void loadSubscription(HttpServletRequest req, HttpServletResponse resp) throws JSONException {
        int id = Integer.parseInt(req.getParameter("id"));
        String source = req.getParameter("source");
        int contextId = getSessionObject(req).getContextId();
        Subscription subscription = loadSubscription(id, contextId, source);
        writeSubscription(subscription, resp);
    }

    private void writeSubscription(Subscription subscription, HttpServletResponse resp) throws JSONException {
        JSONObject object = new SubscriptionJSONWriter().write(subscription, subscription.getSource().getFormDescription());
        writeData(object, resp);
    }

    private Subscription loadSubscription(int id, int contextId, String source) {
        SubscribeService service = null;
        if(source != null) {
            service = discovery.getSource(source).getSubscribeService();
        } else {
            service = discovery.getSource(contextId, id).getSubscribeService();
        }
        return service.loadSubscription(contextId, id);
    }

    private void deleteSubscriptions(HttpServletRequest req, HttpServletResponse resp) throws JSONException, IOException {
        JSONArray ids = new JSONArray(getBody(req));
        int contextId = getSessionObject(req).getContextId();
        for(int i = 0, size = ids.length(); i < size; i++) {
            int id = ids.getInt(i);
            SubscribeService subscribeService = discovery.getSource(contextId, id).getSubscribeService();
            Subscription subscription = new Subscription();
            subscription.setContextId(contextId);
            subscription.setId(id);
            subscribeService.unsubscribe(subscription);
        }
        writeData(1, resp);
    }

    private void updateSubscription(HttpServletRequest req, HttpServletResponse resp) throws JSONException, IOException {
        ServerSession session = getSessionObject(req);
        Subscription subscription = getSubscription(req, session);
        SubscribeService subscribeService = subscription.getSource().getSubscribeService();
        subscribeService.update(subscription);
        writeData(1, resp);
    }

    private void createSubscription(HttpServletRequest req, HttpServletResponse resp) throws JSONException, IOException {
        ServerSession session = getSessionObject(req);
        Subscription subscription = getSubscription(req, session);
        subscription.setId(-1);
        SubscribeService subscribeService = subscription.getSource().getSubscribeService();
        subscribeService.subscribe(subscription);
        writeId(subscription, resp);
    }

    private void writeId(Subscription subscription, HttpServletResponse resp) {
        writeData(subscription.getId(), resp);
    }

    private Subscription getSubscription(HttpServletRequest req, ServerSession session) throws JSONException, IOException {
        JSONObject object = new JSONObject(getBody(req));
        Subscription subscription = new SubscriptionJSONParser(discovery).parse(object);
        subscription.setContextId(session.getContextId());
        subscription.setUserId(session.getUserId());
        return subscription;
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
