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

package com.openexchange.webdav;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;

import com.openexchange.configuration.SystemConfig;
import com.openexchange.groupware.impl.FolderLockManagerImpl;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.infostore.paths.impl.PathResolverImpl;
import com.openexchange.groupware.infostore.webdav.EntityLockManagerImpl;
import com.openexchange.groupware.infostore.webdav.InfostoreWebdavFactory;
import com.openexchange.groupware.infostore.webdav.PropertyStoreImpl;
import com.openexchange.groupware.tx.AlwaysWriteConnectionProvider;
import com.openexchange.groupware.tx.DBPoolProvider;
import com.openexchange.session.Session;
import com.openexchange.sessiond.impl.SessionHolder;
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
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavException;

public class InfostorePerformer implements SessionHolder {
	
	private static  final Log LOG = LogFactory.getLog(InfostorePerformer.class);
	
	private static final InfostorePerformer INSTANCE = new InfostorePerformer();
	
	public static final InfostorePerformer getInstance(){
		return INSTANCE;
	}
	
	public static enum Action {
		UNLOCK,
		PROPPATCH,
		PROPFIND,
		OPTIONS,
		MOVE,
		MKCOL,
		LOCK,
		COPY,
		DELETE,
		GET,
		HEAD,
		PUT,
		TRACE
	}
	
	
	private  InfostoreWebdavFactory factory;
	private  Protocol protocol = new Protocol();
	
	private Map<Action, WebdavAction> actions = new EnumMap<Action, WebdavAction>(Action.class);
	
	private ThreadLocal<Session> session = new ThreadLocal<Session>();
	
	private InfostorePerformer(){
		
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
		
		final InfostoreWebdavFactory infoFactory = new InfostoreWebdavFactory();
		infoFactory.setDatabase(new InfostoreFacadeImpl());
		infoFactory.setFolderLockManager(new FolderLockManagerImpl());
		infoFactory.setFolderProperties(new PropertyStoreImpl("oxfolder_property"));
		infoFactory.setInfoLockManager(new EntityLockManagerImpl("infostore_lock"));
		infoFactory.setLockNullLockManager(new EntityLockManagerImpl("lock_null_lock"));
		infoFactory.setInfoProperties(new PropertyStoreImpl("infostore_property"));
		infoFactory.setProvider(new AlwaysWriteConnectionProvider(new DBPoolProvider()));
		infoFactory.setResolver(new PathResolverImpl(infoFactory.getDatabase()));
		infoFactory.setSessionHolder(this);
		this.factory = infoFactory;
		
		
		unlock = prepare(new WebdavUnlockAction(), true, true, new WebdavIfAction(0,false,false));
		propPatch = prepare(new WebdavProppatchAction(protocol), true, true, new WebdavExistsAction(), new WebdavIfAction(0,true,false) );
		propFind = prepare(new WebdavPropfindAction(), true, true, new WebdavExistsAction(), new WebdavIfAction(0,false,false));
		options = prepare(new WebdavOptionsAction(), true, true,new WebdavIfAction(0,false,false));
		move = prepare(new WebdavMoveAction(infoFactory), true, true, new WebdavExistsAction(), new WebdavIfAction(0,true,true));
		mkcol = prepare(new WebdavMkcolAction(), true, true, new WebdavIfAction(0,true,false));
		lock = prepare(new WebdavLockAction(), true, true, new WebdavIfAction(0,true,false));
		copy = prepare(new WebdavCopyAction(infoFactory), true, true, new WebdavExistsAction(), new WebdavIfAction(0,false,true));
		delete = prepare(new WebdavDeleteAction(), true, true, new WebdavExistsAction(), new WebdavIfAction(0,true,false));
		get = prepare(new WebdavGetAction(), true, false, new WebdavExistsAction(), new WebdavIfAction(0,false,false));
		head = prepare(new WebdavHeadAction(),true, true, new WebdavExistsAction(), new WebdavIfAction(0,false,false));
		
		OXWebdavPutAction oxWebdavPut = new OXWebdavPutAction();
		oxWebdavPut.setSessionHolder(this); 
		
		OXWebdavMaxUploadSizeAction oxWebdavMaxUploadSize = new OXWebdavMaxUploadSizeAction();
		oxWebdavMaxUploadSize.setSessionHolder(this);
		
		put = prepare(oxWebdavPut, false, true, new WebdavIfAction(0,true,false),oxWebdavMaxUploadSize);
		trace = prepare(new WebdavTraceAction(), true, true, new WebdavIfAction(0,false,false));
		
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
		
		loadRequestSpecificBehaviourRegistry();
	}
	
	private void loadRequestSpecificBehaviourRegistry() {
		String beanPath = SystemConfig.getProperty(SystemConfig.Property.WebdavOverrides);
		if (beanPath != null && new File(beanPath).exists()) {
			try {
				XmlBeanFactory beanfactory = new XmlBeanFactory( new FileSystemResource( new File(beanPath) ) );
				RequestSpecificBehaviourRegistry registry = (RequestSpecificBehaviourRegistry) beanfactory.getBean("registry");
				registry.log();
			} catch (Exception x) {
				LOG.error("Can't load webdav overrides", x);
			}
		} else {
			LOG.info("No WebDAV overrides configured in SystemConfig");
		}
	}

	private final WebdavAction prepare(final AbstractAction action, final boolean logBody, final boolean logResponse, final AbstractAction...additionals) {
		final WebdavLogAction logAction = new WebdavLogAction();
		logAction.setLogRequestBody(logBody);
		logAction.setLogResponseBody(logResponse);
		
		
		final AbstractAction lifeCycle = new WebdavRequestCycleAction();
		final AbstractAction defaultHeader = new WebdavDefaultHeaderAction();
		final AbstractAction ifMatch = new WebdavIfMatchAction();
		
		if(logAction.isEnabled()) {
			lifeCycle.setNext(logAction);
			logAction.setNext(defaultHeader);
		} else {
			lifeCycle.setNext(defaultHeader);
		}
		defaultHeader.setNext(ifMatch);
		
		AbstractAction a = ifMatch;
		
		for(AbstractAction  a2 : additionals) {
			a.setNext(a2);
			a = a2;
		}
		
		a.setNext(action);
		
		return lifeCycle;
	}
	
	public Session getSessionObject() {
		return session.get();
	}
	
	public final void doIt(HttpServletRequest req, HttpServletResponse resp, Action action, Session sess) throws ServletException, IOException {
		try {
			WebdavRequest webdavRequest = new ServletWebdavRequest(factory, req);
			WebdavResponse webdavResponse = new ServletWebdavResponse(resp);
			
			session.set(sess);
			BehaviourLookup.getInstance().setRequest(webdavRequest);
			if (LOG.isDebugEnabled()) {
				LOG.debug("Executing "+action);
			}
			actions.get(action).perform(webdavRequest, webdavResponse);
		} catch (WebdavException x) {
			resp.setStatus(x.getStatus());
		} catch (NullPointerException x) {
			x.printStackTrace();
		} finally {
			BehaviourLookup.getInstance().unsetRequest();
			session.set(null);
		}
	}
	
	public InfostoreWebdavFactory getFactory(){
		return factory;
	}
}
