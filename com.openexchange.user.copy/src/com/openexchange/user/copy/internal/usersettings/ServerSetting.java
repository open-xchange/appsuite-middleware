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
