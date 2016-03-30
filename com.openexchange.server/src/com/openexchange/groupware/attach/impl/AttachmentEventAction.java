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

package com.openexchange.groupware.attach.impl;

import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentEvent;
import com.openexchange.groupware.attach.AttachmentListener;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.session.Session;
import com.openexchange.tx.AbstractUndoable;
import com.openexchange.tx.UndoableAction;

public abstract class AttachmentEventAction extends AbstractUndoable implements
        UndoableAction {

    private List<AttachmentMetadata> attachments;
    private Session session;
    private Context ctx;
    private User user;
    private UserConfiguration userConfig;
    private List<AttachmentListener> listeners;
    private AttachmentBase source;
    private DBProvider provider;
    private long ts;


    protected void fireAttached(final List<AttachmentMetadata> attachments, final List<AttachmentMetadata> processed, final User user, final UserConfiguration userConfig, final Session session, final Context ctx, final DBProvider provider) throws Exception {
        long ts = 0;
        for(final AttachmentMetadata att : attachments) {
            final AttachmentEventImpl event = new AttachmentEventImpl(att,att.getFolderId(),att.getAttachedId(),att.getModuleId(),user,userConfig,session,ctx,provider,source);

            try {
                for(final AttachmentListener listener : listeners) {
                    final long mod = listener.attached(event);
                    if(mod > ts) {
                        ts = mod;
                    }
                }
            } finally {
                event.close();
            }
            processed.add(att);
        }
        this.ts = ts;
    }

    protected void fireDetached(final List<AttachmentMetadata> m, final User user, final UserConfiguration userConfig, final Session session, final Context ctx, final DBProvider provider) throws Exception {

        final Map<AttachmentAddress, Set<Integer>> collector = new HashMap<AttachmentAddress, Set<Integer>>();
        for(final AttachmentMetadata attachment : m) {
            final AttachmentAddress addr = new AttachmentAddress(attachment.getModuleId(), attachment.getFolderId(), attachment.getAttachedId());
            Set<Integer> ids = collector.get(addr);
            if(ids == null) {
                ids = new HashSet<Integer>();
                collector.put(addr, ids);
            }
            ids.add(Integer.valueOf(attachment.getId()));
        }
        for(final Map.Entry<AttachmentAddress, Set<Integer>> entry : collector.entrySet()) {
            long ts = 0;
            final AttachmentAddress addr = entry.getKey();
            final Set<Integer> ids = entry.getValue();
            final int[] idsArr = new int[ids.size()];
            int i = 0;
            for(final Integer id : ids) { idsArr[i++] = id.intValue(); }
            final AttachmentEventImpl event = new AttachmentEventImpl(idsArr,addr.folder,addr.attached,addr.module,user,userConfig,session,ctx,provider,source);
            try {
                for(final AttachmentListener listener : listeners) {
                    final long mod = listener.detached(event);
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

    public void setAttachments(final List<AttachmentMetadata> attachments) {
        this.attachments = attachments;
    }

    public List<AttachmentMetadata> getAttachments(){
        return attachments;
    }

    public void setSession(final Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }

    public void setContext(final Context context) {
        this.ctx = context;
    }

    public Context getContext(){
        return ctx;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public User getUser(){
        return user;
    }

    public void setUserConfiguration(final UserConfiguration userConfig) {
        this.userConfig = userConfig;
    }

    public UserConfiguration getUserConfiguration(){
        return userConfig;
    }

    public void setProvider(final DBProvider provider) {
        this.provider = provider;
    }

    public DBProvider getProvider(){
        return provider;
    }

    public void setAttachmentListeners(final List<AttachmentListener> listeners) {
        this.listeners = listeners;
    }

    public void setSource(final AttachmentBase attachmentBase) {
        this.source = attachmentBase;
    }

    public long getTimestamp(){
        return ts;
    }

    protected static final class AttachmentEventImpl implements AttachmentEvent {

        private AttachmentMetadata attachment;
        private final int folderId;
        private final int attachedId;
        private final int moduleId;
        private final User user;
        private final Session session;
        private final Context ctx;
        private final DBProvider provider;
        private Connection writeCon;
        private final AttachmentBase base;
        private int[] detached = new int[0];
        private final UserConfiguration userConfig;


        public AttachmentEventImpl(final AttachmentMetadata m, final int folderId, final int attachedId, final int moduleId, final User user, final UserConfiguration userConfig, final Session session, final Context ctx, final DBProvider provider, final AttachmentBase base) {
            this(folderId,attachedId,moduleId,user,userConfig,session,ctx,provider,base);
            this.attachment = m;
        }

        public void close() {
            if(writeCon != null) {
                provider.releaseWriteConnection(ctx, writeCon);
            }
            writeCon = null;
        }

        public AttachmentEventImpl(final int folderId, final int attachedId, final int moduleId, final User user,final UserConfiguration userConfig, final Session session, final Context ctx, final DBProvider provider, final AttachmentBase base) {
            this.folderId = folderId;
            this.attachedId = attachedId;
            this.moduleId = moduleId;
            this.user = user;
            this.session = session;
            this.ctx = ctx;
            this.provider = provider;
            this.base = base;
            this.userConfig = userConfig;
        }

        public AttachmentEventImpl(final int[] ids, final int folderId, final int attachedId, final int moduleId, final User user, final UserConfiguration userConfig, final Session session, final Context ctx, final DBProvider provider, final AttachmentBase base) {
            this(folderId, attachedId, moduleId, user,userConfig, session, ctx, provider, base);
            this.detached = ids.clone();
        }

        @Override
        public int[] getDetached() {
            return detached.clone();
        }

        @Override
        public AttachmentMetadata getAttachment() {
            return attachment;
        }

        @Override
        public InputStream getAttachedFile() throws OXException {
            if(attachment != null) {
                return base.getAttachedFile(session, folderId, attachedId, moduleId, attachment.getId(),    ctx, user, userConfig);
            }
            return null;
        }

        @Override
        public AttachmentBase getSource() {
            return base;
        }

        @Override
        public Connection getWriteConnection() throws OXException {
            if (writeCon == null) {
                writeCon = provider.getWriteConnection(ctx);
            }
            return writeCon;
        }

        @Override
        public Context getContext() {
            return ctx;
        }

        @Override
        public User getUser() {
            return user;
        }

        @Override
        public UserConfiguration getUserConfig(){
            return userConfig;
        }

        @Override
        public int getFolderId() {
            return folderId;
        }

        @Override
        public int getAttachedId() {
            return attachedId;
        }

        @Override
        public int getModuleId() {
            return moduleId;
        }

        /* (non-Javadoc)
         * @see com.openexchange.groupware.attach.AttachmentEvent#getSession()
         */
        @Override
        public Session getSession() {
            return session;
        }

    }

    protected static final class AttachmentAddress {
        public int attached;
        public int folder;
        public int module;

        public AttachmentAddress(final int module, final int folder, final int attached) {
            this.module = module;
            this.folder = folder;
            this.attached = attached;
        }

        @Override
        public int hashCode(){
            return module+folder+attached;
        }

        @Override
        public boolean equals(final Object o) {
            if (o instanceof AttachmentAddress) {
                final AttachmentAddress attAddr = (AttachmentAddress) o;
                return module == attAddr.module && folder == attAddr.folder && attached == attAddr.attached;
            }
            return false;
        }
    }

}
