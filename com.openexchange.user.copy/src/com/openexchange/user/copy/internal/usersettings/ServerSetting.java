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

package com.openexchange.user.copy.internal.usersettings;

/**
 * {@link ServerSetting}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ServerSetting {

    private int folder;

    private boolean enabled;

    private int defaultStatusPrivate;

    private int defaultStatusPublic;

    private boolean contactCollectOnMailTransport;

    private boolean contactCollectOnMailAccess;

    private int folderTree;
    
    private byte[] uuidBinary;


    public ServerSetting() {
        super();
    }

    public int getFolder() {
        return folder;
    }

    public void setFolder(final int folder) {
        this.folder = folder;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public int getDefaultStatusPrivate() {
        return defaultStatusPrivate;
    }

    public void setDefaultStatusPrivate(final int defaultStatusPrivate) {
        this.defaultStatusPrivate = defaultStatusPrivate;
    }

    public int getDefaultStatusPublic() {
        return defaultStatusPublic;
    }

    public void setDefaultStatusPublic(final int defaultStatusPublic) {
        this.defaultStatusPublic = defaultStatusPublic;
    }

    public boolean isContactCollectOnMailTransport() {
        return contactCollectOnMailTransport;
    }

    public void setContactCollectOnMailTransport(final boolean contactCollectOnMailTransport) {
        this.contactCollectOnMailTransport = contactCollectOnMailTransport;
    }

    public boolean isContactCollectOnMailAccess() {
        return contactCollectOnMailAccess;
    }

    public void setContactCollectOnMailAccess(final boolean contactCollectOnMailAccess) {
        this.contactCollectOnMailAccess = contactCollectOnMailAccess;
    }

    public int getFolderTree() {
        return folderTree;
    }

    public void setFolderTree(final int folderTree) {
        this.folderTree = folderTree;
    }
    
    public byte[] getUuidBinary() {
        return uuidBinary;
    }
    
    public void setUuidBinary(final byte[] uuidBinary) {
        this.uuidBinary = uuidBinary;
    }

}
