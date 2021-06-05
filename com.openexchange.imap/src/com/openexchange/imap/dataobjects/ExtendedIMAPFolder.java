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
    public ExtendedIMAPFolder(IMAPFolder imapFolder, char sep) {
        super(imapFolder.getFullName(), sep, (IMAPStore) imapFolder.getStore(), Boolean.FALSE);
        this.imapFolder = imapFolder;
    }

    /**
     * Delegates to {@link #notifyFolderListeners(int)}.
     *
     * @param type The type of FolderEvent
     */
    public void triggerNotifyFolderListeners(int type) {
        notifyFolderListeners(type);
    }

    @Override
    public void addACL(ACL acl) throws MessagingException {
        imapFolder.addACL(acl);
    }

    @Override
    public void addConnectionListener(ConnectionListener l) {
        imapFolder.addConnectionListener(l);
    }

    @Override
    public void addFolderListener(FolderListener l) {
        imapFolder.addFolderListener(l);
    }

    @Override
    public void addMessageChangedListener(MessageChangedListener l) {
        imapFolder.addMessageChangedListener(l);
    }

    @Override
    public void addMessageCountListener(MessageCountListener l) {
        imapFolder.addMessageCountListener(l);
    }

    @Override
    public Message[] addMessages(Message[] msgs) throws MessagingException {
        return imapFolder.addMessages(msgs);
    }

    @Override
    public void addRights(ACL acl) throws MessagingException {
        imapFolder.addRights(acl);
    }

    @Override
    public void appendMessages(Message[] msgs) throws MessagingException {
        imapFolder.appendMessages(msgs);
    }

    @Override
    public AppendUID[] appendUIDMessages(Message[] msgs) throws MessagingException {
        return imapFolder.appendUIDMessages(msgs);
    }

    @Override
    public void close(boolean expunge) throws MessagingException {
        imapFolder.close(expunge);
    }

    @Override
    public void copyMessages(Message[] msgs, Folder folder) throws MessagingException {
        imapFolder.copyMessages(msgs, folder);
    }

    @Override
    public boolean create(int type) throws MessagingException {
        return imapFolder.create(type);
    }

    @Override
    public boolean delete(boolean recurse) throws MessagingException {
        return imapFolder.delete(recurse);
    }

    @Override
    public Object doCommand(ProtocolCommand cmd) throws MessagingException {
        return imapFolder.doCommand(cmd);
    }

    @Override
    public Object doCommandIgnoreFailure(ProtocolCommand cmd) throws MessagingException {
        return imapFolder.doCommandIgnoreFailure(cmd);
    }

    @Override
    public Object doOptionalCommand(String err, ProtocolCommand cmd) throws MessagingException {
        return imapFolder.doOptionalCommand(err, cmd);
    }

    @Override
    public boolean equals(Object obj) {
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
    public Message[] expunge(Message[] msgs) throws MessagingException {
        return imapFolder.expunge(msgs);
    }

    @Override
    public void fetch(Message[] msgs, FetchProfile fp) throws MessagingException {
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
    public Folder getFolder(String name) throws MessagingException {
        return imapFolder.getFolder(name);
    }

    @Override
    public String getFullName() {
        return imapFolder.getFullName();
    }

    @Override
    public Message getMessage(int msgnum) throws MessagingException {
        return imapFolder.getMessage(msgnum);
    }

    @Override
    public Message getMessageByUID(long uid) throws MessagingException {
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
    public Message[] getMessages(int start, int end) throws MessagingException {
        return imapFolder.getMessages(start, end);
    }

    @Override
    public Message[] getMessages(int[] msgnums) throws MessagingException {
        return imapFolder.getMessages(msgnums);
    }

    @Override
    public Message[] getMessagesByUID(long start, long end) throws MessagingException {
        return imapFolder.getMessagesByUID(start, end);
    }

    @Override
    public Message[] getMessagesByUID(long[] uids) throws MessagingException {
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
    public long getUID(Message message) throws MessagingException {
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
    public void handleResponse(Response r) {
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
    public Folder[] list(String pattern) throws MessagingException {
        return imapFolder.list(pattern);
    }

    @Override
    public Rights[] listRights(String name) throws MessagingException {
        return imapFolder.listRights(name);
    }

    @Override
    public Folder[] listSubscribed() throws MessagingException {
        return imapFolder.listSubscribed();
    }

    @Override
    public Folder[] listSubscribed(String pattern) throws MessagingException {
        return imapFolder.listSubscribed(pattern);
    }

    @Override
    public Rights myRights() throws MessagingException {
        return imapFolder.myRights();
    }

    @Override
    public void open(int mode) throws MessagingException {
        imapFolder.open(mode);
    }

    @Override
    public void removeACL(String name) throws MessagingException {
        imapFolder.removeACL(name);
    }

    @Override
    public void removeConnectionListener(ConnectionListener l) {
        imapFolder.removeConnectionListener(l);
    }

    @Override
    public void removeFolderListener(FolderListener l) {
        imapFolder.removeFolderListener(l);
    }

    @Override
    public void removeMessageChangedListener(MessageChangedListener l) {
        imapFolder.removeMessageChangedListener(l);
    }

    @Override
    public void removeMessageCountListener(MessageCountListener l) {
        imapFolder.removeMessageCountListener(l);
    }

    @Override
    public void removeRights(ACL acl) throws MessagingException {
        imapFolder.removeRights(acl);
    }

    @Override
    public boolean renameTo(Folder f) throws MessagingException {
        return imapFolder.renameTo(f);
    }

    @Override
    public Message[] search(SearchTerm term, Message[] msgs) throws MessagingException {
        return imapFolder.search(term, msgs);
    }

    @Override
    public Message[] search(SearchTerm term) throws MessagingException {
        return imapFolder.search(term);
    }

    @Override
    public void setFlags(int start, int end, Flags flag, boolean value) throws MessagingException {
        imapFolder.setFlags(start, end, flag, value);
    }

    @Override
    public void setFlags(int[] msgnums, Flags flag, boolean value) throws MessagingException {
        imapFolder.setFlags(msgnums, flag, value);
    }

    @Override
    public void setFlags(Message[] msgs, Flags flag, boolean value) throws MessagingException {
        imapFolder.setFlags(msgs, flag, value);
    }

    @Override
    public void setQuota(Quota quota) throws MessagingException {
        imapFolder.setQuota(quota);
    }

    @Override
    public void setSubscribed(boolean subscribe) throws MessagingException {
        imapFolder.setSubscribed(subscribe);
    }

    @Override
    public String toString() {
        return imapFolder.toString();
    }

}
