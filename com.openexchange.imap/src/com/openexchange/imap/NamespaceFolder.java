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

package com.openexchange.imap;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.MethodNotSupportedException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.imap.protocol.ListInfo;

/**
 * {@link NamespaceFolder} - Represents a namespace folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class NamespaceFolder extends IMAPFolder {

    /**
     * Creates a new namespace folder.
     *
     * @param store The IMAP store
     * @param name The folder's name
     * @param separator The folder's separator
     */
    public NamespaceFolder(IMAPStore store, String name, char separator) {
        super(name, separator, store, Boolean.TRUE);
        exists = true; // of course
        type = HOLDS_FOLDERS; // obviously
        this.separator = separator;
        fullName = name;

    }

    @Override
    public String getName() {
        return fullName;
    }

    @Override
    public Folder getParent() throws MessagingException {
        return store.getDefaultFolder();
    }

    @Override
    public Folder[] list(String pattern) throws MessagingException {
        String fullName = this.fullName;
        char separator = getSeparator();
        final ListInfo[] li = (ListInfo[]) doCommand(new ProtocolCommand() {

            @Override
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                return p.list("", new StringBuilder().append(fullName).append(separator).append(pattern).toString());
            }
        });
        if (li == null) {
            return new Folder[0];
        }
        final IMAPFolder[] folders = new IMAPFolder[li.length];
        for (int i = 0; i < folders.length; i++) {
            folders[i] = (IMAPFolder) store.getFolder(getFullname(li[i]));
        }
        return folders;
    }

    @Override
    public Folder[] listSubscribed(String pattern) throws MessagingException {
        String fullName = this.fullName;
        char separator = getSeparator();
        final ListInfo[] li = (ListInfo[]) doCommand(new ProtocolCommand() {

            @Override
            public Object doCommand(IMAPProtocol p) throws ProtocolException {
                return p.lsub("", new StringBuilder().append(fullName).append(separator).append(pattern).toString());
            }
        });
        if (li == null) {
            return new Folder[0];
        }
        final IMAPFolder[] folders = new IMAPFolder[li.length];
        for (int i = 0; i < folders.length; i++) {
            folders[i] = (IMAPFolder) store.getFolder(getFullname(li[i]));
        }
        return folders;
    }

    private static final String getFullname(ListInfo listInfo) {
        String fullName = listInfo.name;
        final char separator = listInfo.separator;
        final int len = fullName.length();
        if ((separator != '\0') && (len > 0) && (fullName.charAt(len - 1) == separator)) {
            fullName = fullName.substring(0, len - 1);
        }
        return fullName;
    }

    @Override
    public boolean hasNewMessages() throws MessagingException {
        // Not applicable on NamespaceFolder
        return false;
    }

    @Override
    public Folder getFolder(String name) throws MessagingException {
        final Folder[] folders = list(new StringBuilder(name).append('%').toString());
        for (int i = 0; i < folders.length; i++) {
            if (folders[i].getName().equals(name)) {
                return folders[i];
            }
        }
        return null;
    }

    @Override
    public boolean delete(boolean recurse) throws MessagingException {
        // Not applicable on NamespaceFolder
        throw new MethodNotSupportedException("Cannot delete Namespace Folder");
    }

    @Override
    public boolean renameTo(Folder f) throws MessagingException {
        // Not applicable on NamespaceFolder
        throw new MethodNotSupportedException("Cannot rename Namespace Folder");
    }

    @Override
    public void appendMessages(Message[] msgs) throws MessagingException {
        // Not applicable on NamespaceFolder
        throw new MethodNotSupportedException("Cannot append to Namespace Folder");
    }

    @Override
    public Message[] expunge() throws MessagingException {
        // Not applicable on NamespaceFolder
        throw new MethodNotSupportedException("Cannot expunge Namespace Folder");
    }

    @Override
    public boolean exists() throws MessagingException {
        return exists;
    }
}
