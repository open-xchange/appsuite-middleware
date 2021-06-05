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
        } catch (NumberFormatException x) {
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
