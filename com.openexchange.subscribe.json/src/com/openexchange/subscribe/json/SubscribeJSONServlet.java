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
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.PermissionServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.session.Session;
import com.openexchange.subscribe.SubscribeService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionHandler;
import com.openexchange.subscribe.ExternalSubscription;
import com.openexchange.subscribe.ExternalSubscriptionHandler;
import com.openexchange.subscribe.ExternalSubscriptionService;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class SubscribeJSONServlet extends PermissionServlet {

    private static final long serialVersionUID = 1L;

    private static final Log LOG = LogFactory.getLog(SubscribeJSONServlet.class);

    private static final int POST = 0;

    private static final int GET = 1;

    private static final int PUT = 3;

    private static final String SUBSCRIBE_ACTION = "subscribe";

    private static final String UNSUBSCRIBE_ACTION = "unsubscribe";
    
    private static final String CLEAR_ACTION = "clear";

    private static final String SUBSCRIBE_EXTERNAL = "subscribeExternal";

    private static final String LOAD_ACTION = "load";

    private static final String LOAD_EXTERNAL = "loadExternal";

    private static final String REFRESH_ACTION = "refresh";

    private static SubscribeService subscribeService;

    private static SubscriptionHandler subscriptionHandler;

    private static ExternalSubscriptionService externalSubscribeService;

    private static ExternalSubscriptionHandler externalSubscriptionHandler;

    public static void setSubscribeService(final SubscribeService subscribeService) {
        SubscribeJSONServlet.subscribeService = subscribeService;
    }

    public static void setSubscriptionHandler(SubscriptionHandler subscriptionHandler) {
        SubscribeJSONServlet.subscriptionHandler = subscriptionHandler;
    }

    public static void setExternalSubscribeService(ExternalSubscriptionService service) {
        SubscribeJSONServlet.externalSubscribeService = service;
    }

    public static void setExternalSubscriptionHandler(ExternalSubscriptionHandler service) {
        SubscribeJSONServlet.externalSubscriptionHandler = service;
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        perform(req, resp, POST);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        perform(req, resp, GET);
    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        perform(req, resp, PUT);
    }

    private void perform(final HttpServletRequest req, final HttpServletResponse resp, final int method) throws ServletException, IOException {
        resp.setContentType(CONTENTTYPE_JAVASCRIPT);
        /*
         * The magic spell to disable caching
         */
        Tools.disableCaching(resp);
        try {
            final Response response = doAction(req, method);
            ResponseWriter.write(response, resp.getWriter());
        } catch (final AbstractOXException e) {
            LOG.error("perform", e);
            final Response response = new Response();
            response.setException(e);
            final PrintWriter writer = resp.getWriter();
            try {
                ResponseWriter.write(response, writer);
            } catch (final JSONException e1) {
                final ServletException se = new ServletException(e1);
                se.initCause(e1);
                throw se;
            }
            writer.flush();
        } catch (final JSONException e) {
            LOG.error("perform", e);
        }
    }

    private Response doAction(final HttpServletRequest req, final int method) throws AbstractOXException, JSONException, IOException {
        switch (method) {
        case PUT:
        case POST:
            return writeAction(req);
        default:
            return readAction(req);
        }
    }

    private Response readAction(final HttpServletRequest req) throws JSONException {
        final Response response = new Response();

        final String action = req.getParameter("action");

        final Session session = getSessionObject(req);

        if (action.equals(LOAD_ACTION)) {
            response.setData(load(session));
        } else if (action.equals(LOAD_EXTERNAL)) {
            response.setData(loadExternal(session, req.getParameter("service")));
        }

        return response;
    }

    private JSONArray load(final Session session) throws JSONException {
        final JSONArray retval = new JSONArray();

        final Collection<Subscription> subscriptions = subscribeService.loadForUser(session.getContextId(), session.getUserId());
        for (final Subscription subscription : subscriptions) {
            retval.put(getJSONObject(subscription, session));
        }

        return retval;
    }
    
    private JSONObject loadExternal(final Session session, String externalService) throws JSONException {
        return getJSONObject(externalSubscribeService.getSubscriptionForUser(session.getContextId(), session.getUserId(), externalService), session);
    }

    private Response writeAction(final HttpServletRequest req) throws JSONException, IOException {
        final Response response = new Response();
        final Session session = getSessionObject(req);

        final String action = req.getParameter("action");
        if (action.equals(SUBSCRIBE_ACTION)) {
            final JSONObject objectToSubscribe = new JSONObject(getBody(req));
            final Subscription subscription = getSubscription(objectToSubscribe, session);
            subscribe(subscription);
        } else if (action.equals(UNSUBSCRIBE_ACTION)) {
            final JSONObject objectToSubscribe = new JSONObject(getBody(req));
            final Subscription subscription = getSubscription(objectToSubscribe, session);
            unsubscribe(subscription);
        } else if (action.equals(REFRESH_ACTION)) {
            int folderId = Integer.parseInt(req.getParameter("folderId"));
            refresh(session, folderId);
        } else if (action.equals(SUBSCRIBE_EXTERNAL)) {
            final JSONObject objectToSubscribe = new JSONObject(getBody(req));
            final ExternalSubscription subscription = getXingSubscription(objectToSubscribe, session);
            saveExternalSubscription(subscription);
        } else if (action.equals(CLEAR_ACTION)) {
            clearAllSubscriptions(session);
        }

        response.setData(1);
        return response;
    }

    private void refresh(Session session, int folderId) {
        Collection<Subscription> subscriptions = subscribeService.load(session.getContextId(), folderId);
        for (Subscription subscription : subscriptions) {
            subscriptionHandler.handleSubscription(subscription);
        }
        for(String service : externalSubscriptionHandler.getServices()) {
            ExternalSubscription subscriptionForUser = externalSubscribeService.getSubscriptionForUser(session.getContextId(), session.getUserId(), service);
            if (subscriptionForUser != null && subscriptionForUser.getTargetFolder() == folderId) {
                externalSubscriptionHandler.handleSubscription(subscriptionForUser);
            }
        }
    }

    private Subscription getSubscription(final JSONObject objectToSubscribe, final Session session) throws JSONException {
        final Subscription subscription = new Subscription();
        subscription.setContextId(session.getContextId());
        subscription.setUserId(session.getUserId());
        subscription.setFolderId(objectToSubscribe.getInt("folder"));
        //subscription.setUrl(objectToSubscribe.optString("url"));
        subscription.setLastUpdate(new Date(0));

        return subscription;
    }

    private ExternalSubscription getXingSubscription(final JSONObject objectToSubscribe, final Session session) throws JSONException {
        ExternalSubscription subscription = new ExternalSubscription();
        subscription.setContextId(session.getContextId());
        subscription.setUserId(session.getUserId());
        subscription.setTargetFolder(resolveFolder(objectToSubscribe.getString("folder"), session));
        subscription.setUserName(objectToSubscribe.getString("user"));
        subscription.setPassword(objectToSubscribe.getString("password"));
        subscription.setExternalService(objectToSubscribe.getString("service"));
        return subscription;
    }

    private JSONObject getJSONObject(final Subscription subscription, Session session) throws JSONException {
        final JSONObject subscriptionObject = new JSONObject();
        subscriptionObject.put("folder", subscription.getFolderId());
        subscriptionObject.put("last_update", subscription.getLastUpdate().getTime());
        //subscriptionObject.put("url", subscription.getUrl());
        return subscriptionObject;
    }
    
    private JSONObject getJSONObject(final ExternalSubscription subscription, Session session) throws JSONException {
        final JSONObject subscriptionObject = new JSONObject();
        
        if(subscription == null) {
            subscriptionObject.put("folder", "");
            subscriptionObject.put("user", "");
            subscriptionObject.put("password", "");
            subscriptionObject.put("service", "");
            return subscriptionObject;
        }
        
        subscriptionObject.put("folder", getName(subscription.getTargetFolder(), session));
        subscriptionObject.put("user", subscription.getUserName());
        subscriptionObject.put("password", subscription.getPassword());
        subscriptionObject.put("service", subscription.getExternalService());
        return subscriptionObject;
    }

    private void subscribe(final Subscription subscription) {
        subscribeService.subscribe(subscription);
    }

    private void unsubscribe(final Subscription subscription) {
        subscribeService.unsubscribe(subscription);
    }

    private void clearAllSubscriptions(Session session) {
        Collection<Subscription> subscriptionsForUser = subscribeService.loadForUser(session.getContextId(), session.getUserId());
        for (Subscription subscription : subscriptionsForUser) {
            subscribeService.unsubscribe(subscription);
        }
    }
    
    private void saveExternalSubscription(final ExternalSubscription subscription) {
        externalSubscribeService.saveSubscription(subscription);
    }

    private int resolveFolder(String fname, Session session) {
        try {
            Context ctx = ContextStorage.getStorageContext(session.getContextId());
            User user = UserStorage.getStorageUser(session.getUserId(), ctx);
            UserConfiguration userConfig = UserConfigurationStorage.getInstance().getUserConfiguration(session.getUserId(), ctx);
            
            final SearchIterator iter = OXFolderIteratorSQL.getAllVisibleFoldersIteratorOfModule(session.getUserId(), user
                .getGroups(), userConfig.getAccessibleModules(), FolderObject.CONTACT, ctx);

           while(iter.hasNext()) {
               FolderObject folder = (FolderObject) iter.next();
               if(folder.getFolderName().equals(fname)) {
                   return folder.getObjectID();
               }
           }
        } catch (Exception x) {
            x.printStackTrace();
        }
        return 0;
    }
    
    private String getName(int folderId, Session session) {
        try {
            Context ctx = ContextStorage.getStorageContext(session.getContextId());
            return FolderObject.loadFolderObjectFromDB(folderId, ctx).getFolderName();
        } catch (Exception x) {
            return "";
        }
    }

    @Override
    protected boolean hasModulePermission(ServerSession session) {
        // TODO Auto-generated method stub
        return false;
    }

}
