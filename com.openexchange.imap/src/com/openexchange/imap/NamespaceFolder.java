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
    public NamespaceFolder(final IMAPStore store, final String name, final char separator) {
        super(name, separator, store, true);
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
    public Folder[] list(final String pattern) throws MessagingException {
        final ListInfo[] li = (ListInfo[]) doCommand(new ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
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
    public Folder[] listSubscribed(final String pattern) throws MessagingException {
        final ListInfo[] li = (ListInfo[]) doCommand(new ProtocolCommand() {

            @Override
            public Object doCommand(final IMAPProtocol p) throws ProtocolException {
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

    private static final String getFullname(final ListInfo listInfo) {
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
    public Folder getFolder(final String name) throws MessagingException {
        final Folder[] folders = list(new StringBuilder(name).append('%').toString());
        for (int i = 0; i < folders.length; i++) {
            if (folders[i].getName().equals(name)) {
                return folders[i];
            }
        }
        return null;
    }

    @Override
    public boolean delete(final boolean recurse) throws MessagingException {
        // Not applicable on NamespaceFolder
        throw new MethodNotSupportedException("Cannot delete Namespace Folder");
    }

    @Override
    public boolean renameTo(final Folder f) throws MessagingException {
        // Not applicable on NamespaceFolder
        throw new MethodNotSupportedException("Cannot rename Namespace Folder");
    }

    @Override
    public void appendMessages(final Message[] msgs) throws MessagingException {
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
