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

package com.openexchange.mail.messaging;

import java.util.List;
import java.util.Set;
import com.openexchange.messaging.MessagingFolder;
import com.openexchange.messaging.MessagingPermission;


/**
 * {@link MailMessagingFolder}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailMessagingFolder implements MessagingFolder {

    private static final long serialVersionUID = 7625901999652670705L;

    /**
     * Initializes a new {@link MailMessagingFolder}.
     */
    public MailMessagingFolder() {
        super();
    }

    @Override
    public boolean containsDefaultFolderType() {
        return false;
    }

    @Override
    public Set<String> getCapabilities() {
        return null;
    }

    @Override
    public DefaultFolderType getDefaultFolderType() {
        return null;
    }

    @Override
    public int getDeletedMessageCount() {
        return 0;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public int getMessageCount() {
        return 0;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public int getNewMessageCount() {
        return 0;
    }

    @Override
    public MessagingPermission getOwnPermission() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getParentId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MessagingPermission> getPermissions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public char getSeparator() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getUnreadMessageCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean hasSubfolders() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean hasSubscribedSubfolders() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isDefaultFolder() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isHoldsFolders() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isHoldsMessages() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isRootFolder() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSubscribed() {
        // TODO Auto-generated method stub
        return false;
    }

}
