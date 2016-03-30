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

package com.openexchange.imap.dataobjects;

import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Quota;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.event.ConnectionListener;
import javax.mail.event.FolderListener;
import javax.mail.event.MessageChangedListener;
import javax.mail.event.MessageCountListener;
import javax.mail.search.SearchTerm;
import com.sun.mail.iap.Response;
import com.sun.mail.imap.ACL;
import com.sun.mail.imap.AppendUID;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.Rights;

/**
 * {@link ExtendedIMAPFolder} - Extends {@link IMAPFolder} by possibility to trigger listeners.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ExtendedIMAPFolder extends IMAPFolder {

    private final IMAPFolder imapFolder;

    /**
     * Initializes a new {@link ExtendedIMAPFolder}.
     *
     * @param imapFolder The backing IMAP folder
     * @param sep The separator character
     */
    public ExtendedIMAPFolder(final IMAPFolder imapFolder, final char sep) {
        super(imapFolder.getFullName(), sep, (IMAPStore) imapFolder.getStore(), false);
        this.imapFolder = imapFolder;
    }

    /**
     * Delegates to {@link #notifyFolderListeners(int)}.
     *
     * @param type The type of FolderEvent
     */
    public void triggerNotifyFolderListeners(final int type) {
        notifyFolderListeners(type);
    }

    @Override
    public void addACL(final ACL acl) throws MessagingException {
        imapFolder.addACL(acl);
    }

    @Override
    public void addConnectionListener(final ConnectionListener l) {
        imapFolder.addConnectionListener(l);
    }

    @Override
    public void addFolderListener(final FolderListener l) {
        imapFolder.addFolderListener(l);
    }

    @Override
    public void addMessageChangedListener(final MessageChangedListener l) {
        imapFolder.addMessageChangedListener(l);
    }

    @Override
    public void addMessageCountListener(final MessageCountListener l) {
        imapFolder.addMessageCountListener(l);
    }

    @Override
    public Message[] addMessages(final Message[] msgs) throws MessagingException {
        return imapFolder.addMessages(msgs);
    }

    @Override
    public void addRights(final ACL acl) throws MessagingException {
        imapFolder.addRights(acl);
    }

    @Override
    public void appendMessages(final Message[] msgs) throws MessagingException {
        imapFolder.appendMessages(msgs);
    }

    @Override
    public AppendUID[] appendUIDMessages(final Message[] msgs) throws MessagingException {
        return imapFolder.appendUIDMessages(msgs);
    }

    @Override
    public void close(final boolean expunge) throws MessagingException {
        imapFolder.close(expunge);
    }

    @Override
    public void copyMessages(final Message[] msgs, final Folder folder) throws MessagingException {
        imapFolder.copyMessages(msgs, folder);
    }

    @Override
    public boolean create(final int type) throws MessagingException {
        return imapFolder.create(type);
    }

    @Override
    public boolean delete(final boolean recurse) throws MessagingException {
        return imapFolder.delete(recurse);
    }

    @Override
    public Object doCommand(final ProtocolCommand cmd) throws MessagingException {
        return imapFolder.doCommand(cmd);
    }

    @Override
    public Object doCommandIgnoreFailure(final ProtocolCommand cmd) throws MessagingException {
        return imapFolder.doCommandIgnoreFailure(cmd);
    }

    @Override
    public Object doOptionalCommand(final String err, final ProtocolCommand cmd) throws MessagingException {
        return imapFolder.doOptionalCommand(err, cmd);
    }

    @Override
    public boolean equals(final Object obj) {
        return imapFolder.equals(obj);
    }

    @Override
    public boolean exists() throws MessagingException {
        return imapFolder.exists();
    }

    @Override
    public Message[] expunge() throws MessagingException {
        return imapFolder.expunge();
    }

    @Override
    public Message[] expunge(final Message[] msgs) throws MessagingException {
        return imapFolder.expunge(msgs);
    }

    @Override
    public void fetch(final Message[] msgs, final FetchProfile fp) throws MessagingException {
        imapFolder.fetch(msgs, fp);
    }

    @Override
    public void forceClose() throws MessagingException {
        imapFolder.forceClose();
    }

    @Override
    public ACL[] getACL() throws MessagingException {
        return imapFolder.getACL();
    }

    @Override
    public String[] getAttributes() throws MessagingException {
        return imapFolder.getAttributes();
    }

    @Override
    public int getDeletedMessageCount() throws MessagingException {
        return imapFolder.getDeletedMessageCount();
    }

    @Override
    public Folder getFolder(final String name) throws MessagingException {
        return imapFolder.getFolder(name);
    }

    @Override
    public String getFullName() {
        return imapFolder.getFullName();
    }

    @Override
    public Message getMessage(final int msgnum) throws MessagingException {
        return imapFolder.getMessage(msgnum);
    }

    @Override
    public Message getMessageByUID(final long uid) throws MessagingException {
        return imapFolder.getMessageByUID(uid);
    }

    @Override
    public int getMessageCount() throws MessagingException {
        return imapFolder.getMessageCount();
    }

    @Override
    public Message[] getMessages() throws MessagingException {
        return imapFolder.getMessages();
    }

    @Override
    public Message[] getMessages(final int start, final int end) throws MessagingException {
        return imapFolder.getMessages(start, end);
    }

    @Override
    public Message[] getMessages(final int[] msgnums) throws MessagingException {
        return imapFolder.getMessages(msgnums);
    }

    @Override
    public Message[] getMessagesByUID(final long start, final long end) throws MessagingException {
        return imapFolder.getMessagesByUID(start, end);
    }

    @Override
    public Message[] getMessagesByUID(final long[] uids) throws MessagingException {
        return imapFolder.getMessagesByUID(uids);
    }

    @Override
    public int getMode() {
        return imapFolder.getMode();
    }

    @Override
    public String getName() {
        return imapFolder.getName();
    }

    @Override
    public int getNewMessageCount() throws MessagingException {
        return imapFolder.getNewMessageCount();
    }

    @Override
    public Folder getParent() throws MessagingException {
        return imapFolder.getParent();
    }

    @Override
    public Flags getPermanentFlags() {
        return imapFolder.getPermanentFlags();
    }

    @Override
    public Quota[] getQuota() throws MessagingException {
        return imapFolder.getQuota();
    }

    @Override
    public char getSeparator() throws MessagingException {
        return imapFolder.getSeparator();
    }

    @Override
    public Store getStore() {
        return imapFolder.getStore();
    }

    @Override
    public int getType() throws MessagingException {
        return imapFolder.getType();
    }

    @Override
    public long getUID(final Message message) throws MessagingException {
        return imapFolder.getUID(message);
    }

    @Override
    public long getUIDNext() throws MessagingException {
        return imapFolder.getUIDNext();
    }

    @Override
    public long getUIDValidity() throws MessagingException {
        return imapFolder.getUIDValidity();
    }

    @Override
    public int getUnreadMessageCount() throws MessagingException {
        return imapFolder.getUnreadMessageCount();
    }

    @Override
    public URLName getURLName() throws MessagingException {
        return imapFolder.getURLName();
    }

    @Override
    public void handleResponse(final Response r) {
        imapFolder.handleResponse(r);
    }

    @Override
    public int hashCode() {
        return imapFolder.hashCode();
    }

    @Override
    public boolean hasNewMessages() throws MessagingException {
        return imapFolder.hasNewMessages();
    }

    @Override
    public void idle() throws MessagingException {
        imapFolder.idle();
    }

    @Override
    public boolean isOpen() {
        return imapFolder.isOpen();
    }

    @Override
    public boolean isSubscribed() {
        return imapFolder.isSubscribed();
    }

    @Override
    public Folder[] list() throws MessagingException {
        return imapFolder.list();
    }

    @Override
    public Folder[] list(final String pattern) throws MessagingException {
        return imapFolder.list(pattern);
    }

    @Override
    public Rights[] listRights(final String name) throws MessagingException {
        return imapFolder.listRights(name);
    }

    @Override
    public Folder[] listSubscribed() throws MessagingException {
        return imapFolder.listSubscribed();
    }

    @Override
    public Folder[] listSubscribed(final String pattern) throws MessagingException {
        return imapFolder.listSubscribed(pattern);
    }

    @Override
    public Rights myRights() throws MessagingException {
        return imapFolder.myRights();
    }

    @Override
    public void open(final int mode) throws MessagingException {
        imapFolder.open(mode);
    }

    @Override
    public void removeACL(final String name) throws MessagingException {
        imapFolder.removeACL(name);
    }

    @Override
    public void removeConnectionListener(final ConnectionListener l) {
        imapFolder.removeConnectionListener(l);
    }

    @Override
    public void removeFolderListener(final FolderListener l) {
        imapFolder.removeFolderListener(l);
    }

    @Override
    public void removeMessageChangedListener(final MessageChangedListener l) {
        imapFolder.removeMessageChangedListener(l);
    }

    @Override
    public void removeMessageCountListener(final MessageCountListener l) {
        imapFolder.removeMessageCountListener(l);
    }

    @Override
    public void removeRights(final ACL acl) throws MessagingException {
        imapFolder.removeRights(acl);
    }

    @Override
    public boolean renameTo(final Folder f) throws MessagingException {
        return imapFolder.renameTo(f);
    }

    @Override
    public Message[] search(final SearchTerm term, final Message[] msgs) throws MessagingException {
        return imapFolder.search(term, msgs);
    }

    @Override
    public Message[] search(final SearchTerm term) throws MessagingException {
        return imapFolder.search(term);
    }

    @Override
    public void setFlags(final int start, final int end, final Flags flag, final boolean value) throws MessagingException {
        imapFolder.setFlags(start, end, flag, value);
    }

    @Override
    public void setFlags(final int[] msgnums, final Flags flag, final boolean value) throws MessagingException {
        imapFolder.setFlags(msgnums, flag, value);
    }

    @Override
    public void setFlags(final Message[] msgs, final Flags flag, final boolean value) throws MessagingException {
        imapFolder.setFlags(msgs, flag, value);
    }

    @Override
    public void setQuota(final Quota quota) throws MessagingException {
        imapFolder.setQuota(quota);
    }

    @Override
    public void setSubscribed(final boolean subscribe) throws MessagingException {
        imapFolder.setSubscribed(subscribe);
    }

    @Override
    public String toString() {
        return imapFolder.toString();
    }

}
