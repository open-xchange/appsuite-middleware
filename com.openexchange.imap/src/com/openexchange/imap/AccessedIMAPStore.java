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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.imap;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Quota;
import javax.mail.Session;
import javax.mail.URLName;
import javax.mail.event.ConnectionListener;
import javax.mail.event.FolderListener;
import javax.mail.event.StoreListener;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.MailAccount;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link AccessedAccessedIMAPStore} - The {@link AccessedIMAPStore} extended by {@link #getImapAccess()}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AccessedIMAPStore extends IMAPStore {

    private final AtomicReference<IMAPStore> imapStoreRef;

    private final IMAPAccess imapAccess;

    /**
     * Initializes a new {@link AccessedAccessedIMAPStore}.
     *
     * @param imapAccess The associated IMAP access
     * @param imapStore The IMAP store
     * @param imapSession The IMAP session with which the store was created
     */
    public AccessedIMAPStore(final IMAPAccess imapAccess, final IMAPStore imapStore, final Session imapSession) {
        super(imapSession, imapStore.getURLName());
        this.imapAccess = imapAccess;
        this.imapStoreRef = new AtomicReference<IMAPStore>(imapStore);
    }

    private IMAPStore imapStore() {
        return imapStoreRef.get();
    }

    /**
     * Drops & returns the underlying IMAP store.
     *
     * @return The IMAP store
     */
    public IMAPStore dropAndGetImapStore() {
        return imapStoreRef.getAndSet(null);
    }

    /**
     * Whether to notify about recent messages. Notification is enabled if both conditions are met:<br>
     * It's the primary account's IMAP store <b>AND</b> notify-recent has been enabled by configuration.
     *
     * @return <code>true</code> to notify about recent messages; otherwise <code>false</code>
     */
    public boolean notifyRecent() {
        return MailAccount.DEFAULT_ID == imapAccess.getAccountId() && imapAccess.getIMAPConfig().getIMAPProperties().notifyRecent();
    }

    /**
     * Gets the IMAP access
     *
     * @return The IMAP access
     */
    public IMAPAccess getImapAccess() {
        return imapAccess;
    }

    @Override
    public int hashCode() {
        return imapStore().hashCode();
    }

    @Override
    public void connect() throws MessagingException {
        imapStore().connect();
    }

    @Override
    public boolean equals(final Object obj) {
        return imapStore().equals(obj);
    }

    @Override
    public void connect(final String host, final String user, final String password) throws MessagingException {
        imapStore().connect(host, user, password);
    }

    @Override
    public void connect(final String user, final String password) throws MessagingException {
        imapStore().connect(user, password);
    }

    @Override
    public void addStoreListener(final StoreListener l) {
        imapStore().addStoreListener(l);
    }

    @Override
    public void removeStoreListener(final StoreListener l) {
        imapStore().removeStoreListener(l);
    }

    @Override
    public void connect(final String host, final int port, final String user, final String password) throws MessagingException {
        imapStore().connect(host, port, user, password);
    }

    @Override
    public void addFolderListener(final FolderListener l) {
        imapStore().addFolderListener(l);
    }

    @Override
    public void removeFolderListener(final FolderListener l) {
        imapStore().removeFolderListener(l);
    }

    @Override
    public URLName getURLName() {
        return imapStore().getURLName();
    }

    @Override
    public void addConnectionListener(final ConnectionListener l) {
        imapStore().addConnectionListener(l);
    }

    @Override
    public void removeConnectionListener(final ConnectionListener l) {
        imapStore().removeConnectionListener(l);
    }

    @Override
    public String toString() {
        return imapStore().toString();
    }

    @Override
    public void setUsername(final String user) {
        imapStore().setUsername(user);
    }

    @Override
    public void setPassword(final String password) {
        imapStore().setPassword(password);
    }

    @Override
    public boolean hasCapability(final String capability) throws MessagingException {
        return imapStore().hasCapability(capability);
    }

    @Override
    public Map getCapabilities() throws MessagingException {
        return imapStore().getCapabilities();
    }

    @Override
    public String getGreeting() throws MessagingException {
        return imapStore().getGreeting();
    }

    @Override
    public boolean isConnected() {
        return imapStore().isConnected();
    }

    @Override
    public void close() throws MessagingException {
        final IMAPStore imapStore = imapStore();
        if (null == imapStore) {
            return;
        }
        imapStore.close();
    }

    @Override
    public Folder getDefaultFolder() throws MessagingException {
        return imapStore().getDefaultFolder();
    }

    @Override
    public Folder getFolder(final String name) throws MessagingException {
        try {
            return imapStore().getFolder(name);
        } catch (final IllegalStateException e) {
            if (!"Not connected".equals(e.getMessage())) {
                throw e;
            }
            try {
                imapStoreRef.set(imapAccess.connectIMAPStore(0));
            } catch (final OXException ignore) {
                // Cannot occur since not borrowed from cache
            }
            return imapStore().getFolder(name);
        }
    }

    @Override
    public Folder getFolder(final URLName url) throws MessagingException {
        return imapStore().getFolder(url);
    }

    @Override
    public Folder[] getPersonalNamespaces() throws MessagingException {
        return imapStore().getPersonalNamespaces();
    }

    @Override
    public Folder[] getUserNamespaces(final String user) throws MessagingException {
        return imapStore().getUserNamespaces(user);
    }

    @Override
    public Folder[] getSharedNamespaces() throws MessagingException {
        return imapStore().getSharedNamespaces();
    }

    @Override
    public Quota[] getQuota(final String root) throws MessagingException {
        return imapStore().getQuota(root);
    }

    @Override
    public void setQuota(final Quota quota) throws MessagingException {
        imapStore().setQuota(quota);
    }

    @Override
    public void handleResponse(final Response r) {
        imapStore().handleResponse(r);
    }

    @Override
    public void idle() throws MessagingException {
        imapStore().idle();
    }

}
