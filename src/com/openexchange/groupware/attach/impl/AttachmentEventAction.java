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

package com.openexchange.groupware.attach.impl;

import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.UserConfiguration;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentEvent;
import com.openexchange.groupware.attach.AttachmentListener;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tx.AbstractUndoable;
import com.openexchange.groupware.tx.DBProvider;
import com.openexchange.groupware.tx.UndoableAction;

public abstract class AttachmentEventAction extends AbstractUndoable implements
		UndoableAction {
	
	private List<AttachmentMetadata> attachments;
	private Context ctx;
	private User user;
	private UserConfiguration userConfig;
	private List<AttachmentListener> listeners;
	private AttachmentBase source;
	private DBProvider provider;
	private long ts;


	protected void fireAttached(List<AttachmentMetadata> attachments, List<AttachmentMetadata> processed, User user, UserConfiguration userConfig, Context ctx, DBProvider provider) throws Exception {
		long ts = 0;
		for(AttachmentMetadata att : attachments) {
			AttachmentEventImpl event = new AttachmentEventImpl(att,att.getFolderId(),att.getAttachedId(),att.getModuleId(),user,userConfig,ctx,provider,source);
			
			try {
				for(AttachmentListener listener : listeners) {
					long mod = listener.attached(event);
					if(mod > ts)
						ts = mod;
				}	
			} finally {
				event.close();
			}
			processed.add(att);
		}
		this.ts = ts;
	}
	
	protected void fireDetached(List<AttachmentMetadata> m, User user, UserConfiguration userConfig, Context ctx, DBProvider provider) throws Exception {
		
		Map<AttachmentAddress, Set<Integer>> collector = new HashMap<AttachmentAddress, Set<Integer>>();
		for(AttachmentMetadata attachment : m) {
			AttachmentAddress addr = new AttachmentAddress(attachment.getModuleId(), attachment.getFolderId(), attachment.getAttachedId());
			Set<Integer> ids = collector.get(addr);
			if(ids == null) {
				ids = new HashSet<Integer>();
				collector.put(addr, ids);
			}
			ids.add(attachment.getId());
		}
		for(Map.Entry<AttachmentAddress, Set<Integer>> entry : collector.entrySet()) {
			long ts = 0;
			AttachmentAddress addr = entry.getKey();
			Set<Integer> ids = entry.getValue();
			int[] idsArr = new int[ids.size()];
			int i = 0;
			for(Integer id : ids) { idsArr[i++] = id; }
			AttachmentEventImpl event = new AttachmentEventImpl(idsArr,addr.folder,addr.attached,addr.module,user,userConfig,ctx,provider,source);
			try {
				for(AttachmentListener listener : listeners) {
					long mod = listener.detached(event);
					if (mod > ts) {
						ts = mod;
					}
				}	
			} finally {
				event.close();
			}
			this.ts = ts;
		}
	}

	public void setAttachments(List<AttachmentMetadata> attachments) {
		this.attachments = attachments;
	}
	
	public List<AttachmentMetadata> getAttachments(){
		return attachments;
	}

	public void setContext(Context context) {
		this.ctx = context;
	}
	
	public Context getContext(){
		return ctx;
	}

	public void setUser(User user) {
		this.user = user;
	}
	
	public User getUser(){
		return user;
	}

	public void setUserConfiguration(UserConfiguration userConfig) {
		this.userConfig = userConfig;
	}
	
	public UserConfiguration getUserConfiguration(){
		return userConfig;
	}

	public void setProvider(DBProvider provider) {
		this.provider = provider;
	}
	
	public DBProvider getProvider(){
		return provider;
	}

	public void setAttachmentListeners(List<AttachmentListener> listeners) {
		this.listeners = listeners;
	}
	
	public void setSource(AttachmentBase attachmentBase) {
		this.source = attachmentBase;
	}
	
	public long getTimestamp(){
		return ts;
	}
	
	protected static final class AttachmentEventImpl implements AttachmentEvent {

		private AttachmentMetadata attachment;
		private int folderId;
		private int attachedId;
		private int moduleId;
		private User user;
		private Context ctx;
		private DBProvider provider;
		private Connection writeCon;
		private AttachmentBase base;
		private int[] detached = new int[0];
		private UserConfiguration userConfig;

		
		public AttachmentEventImpl(AttachmentMetadata m, int folderId, int attachedId, int moduleId, User user, UserConfiguration userConfig, Context ctx, DBProvider provider, AttachmentBase base) {
			this(folderId,attachedId,moduleId,user,userConfig,ctx,provider,base);
			this.attachment = m;
		}

		public void close() {
			if(writeCon != null)
				provider.releaseWriteConnection(ctx, writeCon);
			writeCon = null;
		}

		public AttachmentEventImpl(int folderId, int attachedId, int moduleId, User user,UserConfiguration userConfig, Context ctx, DBProvider provider, AttachmentBase base) {
			this.folderId = folderId;
			this.attachedId = attachedId;
			this.moduleId = moduleId;
			this.user = user;
			this.ctx = ctx;
			this.provider = provider;
			this.base = base;
			this.userConfig = userConfig;
		}
		
		public AttachmentEventImpl(int[] ids, int folderId, int attachedId, int moduleId, User user, UserConfiguration userConfig, Context ctx, DBProvider provider, AttachmentBase base) {
			this(folderId, attachedId, moduleId, user,userConfig, ctx, provider, base);
			this.detached = (int[]) ids.clone();
		}

		public int[] getDetached() {
			return (int[])detached.clone();
		}

		public AttachmentMetadata getAttachment() {
			return attachment;
		}

		public InputStream getAttachedFile() throws OXException {
			if(attachment != null) {
				return base.getAttachedFile(folderId, attachedId, moduleId, attachment.getId(),	ctx, user, userConfig);
			}
			return null;
		}

		public AttachmentBase getSource() {
			return base;
		}

		public Connection getWriteConnection() throws OXException {
			if(writeCon == null)
				writeCon = provider.getWriteConnection(ctx);
			return writeCon;
		}

		public Context getContext() {
			return ctx;
		}

		public User getUser() {
			return user;
		}
		
		public UserConfiguration getUserConfig(){
			return userConfig;
		}

		public int getFolderId() {
			return folderId;
		}

		public int getAttachedId() {
			return attachedId;
		}

		public int getModuleId() {
			return moduleId;
		}
		
	}
	
	protected static final class AttachmentAddress {
		public int attached;
		public int folder;
		public int module;

		public AttachmentAddress(int module, int folder, int attached) {
			this.module = module;
			this.folder = folder;
			this.attached = attached;
		}
		
		public int hashCode(){
			return module+folder+attached;
		}
		
		public boolean equals(Object o) {
			if (o instanceof AttachmentAddress) {
				AttachmentAddress attAddr = (AttachmentAddress) o;
				return module == attAddr.module && folder == attAddr.folder && attached == attAddr.attached;
			}
			return false;
		}
	}

}
