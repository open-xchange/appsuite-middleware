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

package com.openexchange.webdav;

import java.io.File;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.BeanFactory;
import com.openexchange.configuration.SystemConfig;
import com.openexchange.database.provider.DBPoolProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.FolderLockManagerImpl;
import com.openexchange.groupware.infostore.WebdavFolderAliases;
import com.openexchange.groupware.infostore.database.impl.InfostoreSecurityImpl;
import com.openexchange.groupware.infostore.facade.impl.EventFiringInfostoreFacadeImpl;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.infostore.paths.impl.PathResolverImpl;
import com.openexchange.groupware.infostore.webdav.EntityLockManagerImpl;
import com.openexchange.groupware.infostore.webdav.InMemoryAliases;
import com.openexchange.groupware.infostore.webdav.InfostoreWebdavFactory;
import com.openexchange.groupware.infostore.webdav.PropertyStoreImpl;
import com.openexchange.groupware.infostore.webdav.TouchInfoitemsWithExpiredLocksListener;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.SessionHolder;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;
import com.openexchange.webdav.action.AbstractAction;
import com.openexchange.webdav.action.DefaultWebdavOptionsAction;
import com.openexchange.webdav.action.OXWebdavMaxUploadSizeAction;
import com.openexchange.webdav.action.OXWebdavPutAction;
import com.openexchange.webdav.action.ServletWebdavRequest;
import com.openexchange.webdav.action.ServletWebdavResponse;
import com.openexchange.webdav.action.WebdavAction;
import com.openexchange.webdav.action.WebdavCopyAction;
import com.openexchange.webdav.action.WebdavDefaultHeaderAction;
import com.openexchange.webdav.action.WebdavDeleteAction;
import com.openexchange.webdav.action.WebdavExistsAction;
import com.openexchange.webdav.action.WebdavGetAction;
import com.openexchange.webdav.action.WebdavHeadAction;
import com.openexchange.webdav.action.WebdavIfAction;
import com.openexchange.webdav.action.WebdavIfMatchAction;
import com.openexchange.webdav.action.WebdavLockAction;
import com.openexchange.webdav.action.WebdavLogAction;
import com.openexchange.webdav.action.WebdavMkcolAction;
import com.openexchange.webdav.action.WebdavMoveAction;
import com.openexchange.webdav.action.WebdavPropfindAction;
import com.openexchange.webdav.action.WebdavProppatchAction;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.action.WebdavRequestCycleAction;
import com.openexchange.webdav.action.WebdavResponse;
import com.openexchange.webdav.action.WebdavTraceAction;
import com.openexchange.webdav.action.WebdavUnlockAction;
import com.openexchange.webdav.action.behaviour.BehaviourLookup;
import com.openexchange.webdav.action.behaviour.RequestSpecificBehaviourRegistry;
import com.openexchange.webdav.action.behaviour.UserAgentBehaviour;
import com.openexchange.webdav.action.ifheader.IgnoreLocksIfHeaderApply;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavMethod;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.xml.spring.SpringParser;

/**
 * {@link InfostorePerformer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public final class InfostorePerformer implements SessionHolder {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InfostorePerformer.class);

    private static final InfostorePerformer INSTANCE = new InfostorePerformer();

    /**
     * Gets the instance of {@link InfostorePerformer}.
     *
     * @return The instance of {@link InfostorePerformer}.
     */
    public static InfostorePerformer getInstance() {
        return INSTANCE;
    }

    public static enum Acti1on {
        UNLOCK, PROPPATCH, PROPFIND, OPTIONS, MOVE, MKCOL, LOCK, COPY, DELETE, GET, HEAD, PUT, TRACE
    }

    private final InfostoreWebdavFactory factory;

    private final Protocol protocol = new Protocol();

    private final Map<WebdavMethod, WebdavAction> actions = new EnumMap<WebdavMethod, WebdavAction>(WebdavMethod.class);

    private final ThreadLocal<ServerSession> session = new ThreadLocal<ServerSession>();

    private InfostorePerformer() {

        WebdavAction unlock;
        WebdavAction propPatch;
        WebdavAction propFind;
        WebdavAction options;
        WebdavAction move;
        WebdavAction mkcol;
        WebdavAction lock;
        WebdavAction copy;
        WebdavAction delete;
        WebdavAction get;
        WebdavAction head;
        WebdavAction put;
        WebdavAction trace;

        final WebdavFolderAliases aliases = new InMemoryAliases();
        final Locale locale = new Locale("en", "US");
        aliases.registerNameWithIDAndParent(
            FolderObject.getFolderString(FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID, locale),
            FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID,
            FolderObject.SYSTEM_INFOSTORE_FOLDER_ID);
        aliases.registerNameWithIDAndParent(
            FolderObject.getFolderString(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID, locale),
            FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID,
            FolderObject.SYSTEM_INFOSTORE_FOLDER_ID);


        final InfostoreWebdavFactory infoFactory = new InfostoreWebdavFactory();
        final InfostoreFacadeImpl database = new EventFiringInfostoreFacadeImpl();
        infoFactory.setDatabase(database);
        infoFactory.setFolderLockManager(new FolderLockManagerImpl());
        infoFactory.setFolderProperties(new PropertyStoreImpl("oxfolder_property"));

        final EntityLockManagerImpl infoLockManager = new EntityLockManagerImpl("infostore_lock");
        infoLockManager.addExpiryListener(new TouchInfoitemsWithExpiredLocksListener(this, database));

        infoFactory.setInfoLockManager(infoLockManager);


        infoFactory.setLockNullLockManager(new EntityLockManagerImpl("lock_null_lock"));
        infoFactory.setInfoProperties(new PropertyStoreImpl("infostore_property"));
        infoFactory.setProvider(new DBPoolProvider());
        final PathResolverImpl resolver = new PathResolverImpl(infoFactory.getDatabase());
        resolver.setAliases(aliases);
        infoFactory.setResolver(resolver);
        infoFactory.setSecurity(new InfostoreSecurityImpl());
        infoFactory.setSessionHolder(this);
        infoFactory.setAliases(aliases);
        this.factory = infoFactory;

        unlock = prepare(new WebdavUnlockAction(), true, true, true, new WebdavIfAction(0, false, false));
        propPatch = prepare(new WebdavProppatchAction(protocol), true, true, true, new WebdavExistsAction(), new WebdavIfAction(0, true, false));
        propFind = prepare(new WebdavPropfindAction(protocol), true, true, true, new WebdavExistsAction(), new WebdavIfAction(0, false, false));
        options = prepare(new DefaultWebdavOptionsAction(), true, true, false, new WebdavIfAction(0, false, false));
        move = prepare(new WebdavMoveAction(infoFactory), true, true, true, new WebdavExistsAction(), new WebdavIfAction(0, true, true));
        mkcol = prepare(new WebdavMkcolAction(), true, true, true, new WebdavIfAction(0, true, false));
        lock = prepare(new WebdavLockAction(), true, true, true, new WebdavIfAction(0, false, false));
        copy = prepare(new WebdavCopyAction(infoFactory), true, true, true, new WebdavExistsAction(), new WebdavIfAction(0, false, true));
        delete = prepare(new WebdavDeleteAction(), true, true, true, new WebdavExistsAction(), new WebdavIfAction(0, true, false));
        get = prepare(new WebdavGetAction(), true, false, false, new WebdavExistsAction(), new WebdavIfAction(0, false, false), new WebdavIfMatchAction(HttpServletResponse.SC_NOT_MODIFIED));
        head = prepare(new WebdavHeadAction(), true, true, false, new WebdavExistsAction(), new WebdavIfAction(0, false, false), new WebdavIfMatchAction(HttpServletResponse.SC_NOT_MODIFIED));

        final OXWebdavPutAction oxWebdavPut = new OXWebdavPutAction();
        final OXWebdavMaxUploadSizeAction oxWebdavMaxUploadSize = new OXWebdavMaxUploadSizeAction(this);
        put = prepare(oxWebdavPut, false, true, true, new WebdavIfAction(0, true, false), oxWebdavMaxUploadSize);
        trace = prepare(new WebdavTraceAction(), true, true, true, new WebdavIfAction(0, false, false));

        actions.put(WebdavMethod.UNLOCK, unlock);
        actions.put(WebdavMethod.PROPPATCH, propPatch);
        actions.put(WebdavMethod.PROPFIND, propFind);
        actions.put(WebdavMethod.OPTIONS, options);
        actions.put(WebdavMethod.MOVE, move);
        actions.put(WebdavMethod.MKCOL, mkcol);
        actions.put(WebdavMethod.LOCK, lock);
        actions.put(WebdavMethod.COPY, copy);
        actions.put(WebdavMethod.DELETE, delete);
        actions.put(WebdavMethod.GET, get);
        actions.put(WebdavMethod.HEAD, head);
        actions.put(WebdavMethod.PUT, put);
        actions.put(WebdavMethod.TRACE, trace);

        makeLockNullTolerant();

        loadRequestSpecificBehaviourRegistry();
    }

    private void makeLockNullTolerant() {
        WebdavMethod[] nullTolerantActions = new WebdavMethod[] { WebdavMethod.OPTIONS, WebdavMethod.LOCK, WebdavMethod.MKCOL, WebdavMethod.PUT };
        for (final WebdavMethod action : nullTolerantActions) {
            WebdavAction webdavAction = actions.get(action);
            while (webdavAction != null) {
                if (webdavAction instanceof WebdavExistsAction) {
                    ((WebdavExistsAction) webdavAction).setTolerateLockNull(true);
                    webdavAction = null;
                } else if (webdavAction instanceof AbstractAction) {
                    webdavAction = ((AbstractAction) webdavAction).getNext();
                } else {
                    webdavAction = null;
                }
            }
        }
    }

    private void loadRequestSpecificBehaviourRegistry() {
        final String beanPath = SystemConfig.getProperty(SystemConfig.Property.WebdavOverrides);
        RequestSpecificBehaviourRegistry registry;
        if (beanPath != null && new File(beanPath).exists()) {
                final SpringParser springParser = ServerServiceRegistry.getInstance().getService(SpringParser.class);
                final BeanFactory beanfactory = springParser.parseFile(beanPath, InfostorePerformer.class.getClassLoader());
                registry = (RequestSpecificBehaviourRegistry) beanfactory.getBean("registry");
        } else {
             registry = new RequestSpecificBehaviourRegistry();
        }
        try {
            registry.add(new UserAgentBehaviour("Microsoft Data Access Internet Publishing Provider DAV", new IgnoreLocksIfHeaderApply()));
        } catch (OXException e) {
            LOG.error("Can't add default overrides", e);
        }
        registry.log();

        BehaviourLookup.getInstance().setRegistry(registry);
    }

    /**
     * Prepares a new {@link WebdavRequestCycleAction} for a concrete WebDAV action, injecting appropriate actions for logging, default
     * response headers and if-header matching implicitly. Further actions are added as needed, as well as finally the action itself.
     *
     * @param action The action to prepare
     * @param logBody <code>true</code> to log the request body, <code>false</code>, otherwise
     * @param logResponse <code>true</code> to log the response, <code>false</code>, otherwise
     * @param ifMatch <code>true</code> to do <code>"If-Match"</code> / <code>If-None-Match</code> header validation, <code>false</code>, otherwise
     * @param additionals Additional actions to include
     * @return The prepared WebDAV action
     */
    private WebdavAction prepare(AbstractAction action, boolean logBody, boolean logResponse, boolean ifMatch, AbstractAction... additionals) {
        /*
         * initialize surrounding request lifecycle
         */
        WebdavRequestCycleAction lifeCycle = new WebdavRequestCycleAction();
        /*
         * add log action
         */
        WebdavLogAction logAction = new WebdavLogAction(logBody, logResponse);
        lifeCycle.setNext(logAction);
        /*
         * add default header action
         */
        WebdavDefaultHeaderAction defaultHeader = new WebdavDefaultHeaderAction();
        logAction.setNext(defaultHeader);
        AbstractAction previousAction = defaultHeader;
        /*
         * add if-match action
         */
        if (ifMatch) {
            WebdavIfMatchAction ifMatchAction = new WebdavIfMatchAction();
            previousAction.setNext(ifMatchAction);
            previousAction = ifMatchAction;
        }
        /*
         * add additional actions
         */
        for (AbstractAction nextAction : additionals) {
            previousAction.setNext(nextAction);
            previousAction = nextAction;
        }
        /*
         * add action itself
         */
        previousAction.setNext(action);
        return lifeCycle;
    }

    @Override
    public ServerSession getSessionObject() {
        sessionNotNull();
        return session.get();
    }

    private void sessionNotNull() {
        if (session.get() == null) {
            final IllegalStateException exc = new IllegalStateException();
            LOG.error("No session found in Session holder", exc.fillInStackTrace());
        }
    }

    @Override
    public Context getContext() {
        return session.get().getContext();
    }

    @Override
    public User getUser() {
        return session.get().getUser();
    }

    public void doIt(final HttpServletRequest req, final HttpServletResponse resp, final WebdavMethod action, final ServerSession sess) {
        try {
            final WebdavRequest webdavRequest = new ServletWebdavRequest(factory, req);
            final WebdavResponse webdavResponse = new ServletWebdavResponse(resp);

            session.set(sess);
            BehaviourLookup.getInstance().setRequest(webdavRequest);
            LOG.debug("Executing {}", action);
            actions.get(action).perform(webdavRequest, webdavResponse);
        } catch (WebdavProtocolException x) {
            resp.setStatus(x.getStatus());
        } catch (NullPointerException x) {
            LOG.error("Null reference detected.", x);
        } finally {
            BehaviourLookup.getInstance().unsetRequest();
            session.set(null);
        }
    }

    public InfostoreWebdavFactory getFactory() {
        return factory;
    }
}
