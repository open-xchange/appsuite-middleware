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
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.SessionHolder;
import com.openexchange.webdav.action.AbstractAction;
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
import com.openexchange.webdav.action.WebdavOptionsAction;
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

    public static enum Action {
        UNLOCK, PROPPATCH, PROPFIND, OPTIONS, MOVE, MKCOL, LOCK, COPY, DELETE, GET, HEAD, PUT, TRACE
    }

    private final InfostoreWebdavFactory factory;

    private final Protocol protocol = new Protocol();

    private final Map<Action, WebdavAction> actions = new EnumMap<Action, WebdavAction>(Action.class);

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

        unlock = prepare(new WebdavUnlockAction(), true, true, new WebdavIfAction(0, false, false));
        propPatch = prepare(new WebdavProppatchAction(protocol), true, true, new WebdavExistsAction(), new WebdavIfAction(0, true, false));
        propFind = prepare(new WebdavPropfindAction(protocol), true, true, new WebdavExistsAction(), new WebdavIfAction(0, false, false));
        options = prepare(new WebdavOptionsAction(), true, true, new WebdavIfAction(0, false, false));
        move = prepare(new WebdavMoveAction(infoFactory), true, true, new WebdavExistsAction(), new WebdavIfAction(0, true, true));
        mkcol = prepare(new WebdavMkcolAction(), true, true, new WebdavIfAction(0, true, false));
        lock = prepare(new WebdavLockAction(), true, true, new WebdavIfAction(0, true, false));
        copy = prepare(new WebdavCopyAction(infoFactory), true, true, new WebdavExistsAction(), new WebdavIfAction(0, false, true));
        delete = prepare(new WebdavDeleteAction(), true, true, new WebdavExistsAction(), new WebdavIfAction(0, true, false));
        get = prepare(new WebdavGetAction(), true, false, new WebdavExistsAction(), new WebdavIfAction(0, false, false));
        head = prepare(new WebdavHeadAction(), true, true, new WebdavExistsAction(), new WebdavIfAction(0, false, false));

        final OXWebdavPutAction oxWebdavPut = new OXWebdavPutAction();
        final OXWebdavMaxUploadSizeAction oxWebdavMaxUploadSize = new OXWebdavMaxUploadSizeAction(this);
        put = prepare(oxWebdavPut, false, true, new WebdavIfAction(0, false, false), oxWebdavMaxUploadSize);
        trace = prepare(new WebdavTraceAction(), true, true, new WebdavIfAction(0, false, false));

        actions.put(Action.UNLOCK, unlock);
        actions.put(Action.PROPPATCH, propPatch);
        actions.put(Action.PROPFIND, propFind);
        actions.put(Action.OPTIONS, options);
        actions.put(Action.MOVE, move);
        actions.put(Action.MKCOL, mkcol);
        actions.put(Action.LOCK, lock);
        actions.put(Action.COPY, copy);
        actions.put(Action.DELETE, delete);
        actions.put(Action.GET, get);
        actions.put(Action.HEAD, head);
        actions.put(Action.PUT, put);
        actions.put(Action.TRACE, trace);

        makeLockNullTolerant();

        loadRequestSpecificBehaviourRegistry();
    }

    private static volatile Action[] NULL_TOLERANT_ACTIONS;

    private void makeLockNullTolerant() {
        // Single-check-idiom to initialize constant
        Action[] tmp = NULL_TOLERANT_ACTIONS;
        if (null == tmp) {
            NULL_TOLERANT_ACTIONS = tmp = new Action[] { Action.OPTIONS, Action.LOCK, Action.MKCOL, Action.PUT };
        }
        for (final Action action : tmp) {
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
        } catch (final OXException e) {
            LOG.error("Can't add default overrides", e);
        }
        registry.log();

        BehaviourLookup.getInstance().setRegistry(registry);
    }

    private WebdavAction prepare(final AbstractAction action, final boolean logBody, final boolean logResponse, final AbstractAction... additionals) {
        final WebdavLogAction logAction = new WebdavLogAction();
        logAction.setLogRequestBody(logBody);
        logAction.setLogResponseBody(logResponse);

        final AbstractAction lifeCycle = new WebdavRequestCycleAction();
        final AbstractAction defaultHeader = new WebdavDefaultHeaderAction();
        final AbstractAction ifMatch = new WebdavIfMatchAction();

        lifeCycle.setNext(logAction);
        logAction.setNext(defaultHeader);
        defaultHeader.setNext(ifMatch);

        AbstractAction a = ifMatch;

        for (final AbstractAction a2 : additionals) {
            a.setNext(a2);
            a = a2;
        }

        a.setNext(action);

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

    public void doIt(final HttpServletRequest req, final HttpServletResponse resp, final Action action, final ServerSession sess) {
        try {
            final WebdavRequest webdavRequest = new ServletWebdavRequest(factory, req);
            final WebdavResponse webdavResponse = new ServletWebdavResponse(resp);

            session.set(sess);
            BehaviourLookup.getInstance().setRequest(webdavRequest);
            LOG.debug("Executing {}", action);
            actions.get(action).perform(webdavRequest, webdavResponse);
        } catch (final WebdavProtocolException x) {
            resp.setStatus(x.getStatus());
        } catch (final NullPointerException x) {
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
