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
import com.openexchange.groupware.attach.AttachmentBatch;
import com.openexchange.groupware.attach.AttachmentEvent;
import com.openexchange.groupware.attach.AttachmentListener;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.session.Session;
import com.openexchange.tx.AbstractUndoable;
import com.openexchange.tx.UndoableAction;
import com.openexchange.user.User;

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
            event.setBatch(att.getAttachmentBatch());

            try {
                for(final AttachmentListener listener : listeners) {
                    final long mod = listener.attached(event);
                    if (mod > ts) {
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
            if (ids == null) {
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
        private AttachmentBatch attachmentBatch;


        public AttachmentEventImpl(final AttachmentMetadata m, final int folderId, final int attachedId, final int moduleId, final User user, final UserConfiguration userConfig, final Session session, final Context ctx, final DBProvider provider, final AttachmentBase base) {
            this(folderId,attachedId,moduleId,user,userConfig,session,ctx,provider,base);
            this.attachment = m;
        }

        public void close() {
            if (writeCon != null) {
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
            if (attachment != null) {
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

        @Override
        public Session getSession() {
            return session;
        }

        public void setBatch(AttachmentBatch batch) {
            this.attachmentBatch = batch;
        }

        @Override
        public AttachmentBatch getAttachmentBatch() {
            return attachmentBatch;
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
