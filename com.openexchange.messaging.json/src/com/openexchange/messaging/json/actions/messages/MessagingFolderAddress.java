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

package com.openexchange.messaging.json.actions.messages;

import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingExceptionCodes;

/**
 * {@link MessagingFolderAddress}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MessagingFolderAddress {

    public static boolean matches(final String folderId) {
        return folderId.contains("://");
    }

    public static MessagingFolderAddress parse(final String folderId) throws OXException {
        final MessagingFolderAddress address = new MessagingFolderAddress();
        int state = 0;
        final StringBuilder builder = new StringBuilder();
        final int length = folderId.length();
        for (int i = 0; i < length; i++) {
            final char c = folderId.charAt(i);
            switch (c) {
            case ':':
                switch (state) {
                case 0:
                    state = 1;
                    break;
                default:
                    builder.append(c);
                    break;
                }
                break;
            case '/':
                switch (state) {
                case 1:
                    state = 2;
                    break;
                case 2:
                    address.setMessagingService(builder.toString());
                    builder.setLength(0);
                    state = 3;
                    break;
                case 3:
                    state = 4;
                    address.setAccount(builder.toString());
                    builder.setLength(0);
                    break;
                default:
                    builder.append(c);
                }
                break;
            default:
                builder.append(c);
                break;
            }
        }

        switch (state) {
        case 2:
            address.setMessagingService(builder.toString());
            builder.setLength(0);
            state = 3;
            break;
        case 3:
            state = 4;
            address.setAccount(builder.toString());
            builder.setLength(0);
            break;
        case 4:
            address.setFolder(builder.toString());
            break;
        }
        return address;
    }

    private String messagingService = "";

    private int account = -1;

    private String folder = "";

    public String getMessagingService() {
        return messagingService;
    }

    public void setMessagingService(final String messagingService) {
        this.messagingService = messagingService;
    }

    public int getAccount() {
        return account;
    }

    public void setAccount(final String account) throws OXException {
        try {
            this.account = Integer.parseInt(account);
        } catch (final NumberFormatException x) {
            throw MessagingExceptionCodes.INVALID_PARAMETER.create("account", account);
        }
    }

    public void setAccount(final int accountID) {
        account = accountID;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(final String folder) {
        this.folder = folder;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + account;
        result = prime * result + ((folder == null) ? 0 : folder.hashCode());
        result = prime * result + ((messagingService == null) ? 0 : messagingService.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MessagingFolderAddress other = (MessagingFolderAddress) obj;
        if (account != other.account) {
            return false;
        }
        if (folder == null) {
            if (other.folder != null) {
                return false;
            }
        } else if (!folder.equals(other.folder)) {
            return false;
        }
        if (messagingService == null) {
            if (other.messagingService != null) {
                return false;
            }
        } else if (!messagingService.equals(other.messagingService)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return messagingService + "://" + account + "/" + folder;
    }

    public String getAccountAddress() {
        return messagingService + "://" + account;
    }

}
