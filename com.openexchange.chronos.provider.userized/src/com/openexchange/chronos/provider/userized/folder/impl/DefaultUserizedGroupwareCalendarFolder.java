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

package com.openexchange.chronos.provider.userized.folder.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.chronos.Transp;
import com.openexchange.chronos.provider.CalendarPermission;
import com.openexchange.chronos.provider.groupware.GroupwareCalendarFolder;
import com.openexchange.chronos.provider.groupware.GroupwareFolderType;
import com.openexchange.chronos.provider.userized.folder.UserizedGroupwareCalendarFolder;
import com.openexchange.java.Strings;

/**
 * {@link DefaultUserizedGroupwareCalendarFolder} - Default / Fall-back for a {@link UserizedGroupwareCalendarFolder}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class DefaultUserizedGroupwareCalendarFolder implements UserizedGroupwareCalendarFolder {

    private boolean subscribed = false;
    private boolean sync       = false;

    /* User specified name, description and color for this folder */
    private String userName;
    private String userDescription;
    private String userColor;

    private Map<String, String> properties;

    private final int userId;
    private final int contextId;

    private final GroupwareCalendarFolder folder;

    /**
     * 
     * Initializes a new {@link DefaultUserizedGroupwareCalendarFolder}.
     * 
     * @param folder The {@link CalendarFolder}
     * @param userId The user the folder belongs to
     */
    public DefaultUserizedGroupwareCalendarFolder(GroupwareCalendarFolder folder, int contextId, int userId) {
        properties = new ConcurrentHashMap<>();
        this.folder = folder;
        this.contextId = contextId;
        this.userId = userId;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public int getContextId() {
        return contextId;
    }

    @Override
    public boolean isSubscribed() {
        return subscribed;
    }

    public void setSubscribed(boolean subscribed) {
        this.subscribed = subscribed;
    }

    @Override
    public boolean shouldSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

    @Override
    public boolean hasUserName() {
        return false == Strings.isEmpty(userName);
    }

    @Override
    public boolean hasUserDescription() {
        return false == Strings.isEmpty(userDescription);
    }

    @Override
    public String getUserName() {
        return userName;
    }

    public void setUserName(String alternativeName) {
        this.userName = alternativeName;
    }

    @Override
    public String getUserDescription() {
        return userDescription;
    }

    public void setUserDescription(String alternaticeDescription) {
        this.userDescription = alternaticeDescription;
    }

    @Override
    public boolean hasUserColor() {
        return null != userColor;
    }

    @Override
    public String getUserColor() {
        return userColor;
    }

    public void setUserColor(String color) {
        this.userColor = color;
    }

    @Override
    public Map<String, String> additionalProperties() {
        return properties;
    }

    /**
     * Add a property to the folder
     * 
     * @param key The name of the property
     * @param value The value of the property
     * @throws Exception See {@link ConcurrentHashMap#put(Object, Object)} for further information
     */
    public void setAdditionalProperty(String key, String value) {
        properties.put(key, value);
    }

    /* Delegate all GroupwareCalendarFolder abilities */

    @Override
    public String getId() {
        return folder.getId();
    }

    @Override
    public String getName() {
        return folder.getName();
    }

    @Override
    public List<CalendarPermission> getPermissions() {
        return folder.getPermissions();
    }

    @Override
    public String getDescription() {
        return folder.getDescription();
    }

    @Override
    public String getColor() {
        return folder.getColor();
    }

    @Override
    public Date getLastModified() {
        return folder.getLastModified();
    }

    @Override
    public Transp getTransparency() {
        return folder.getTransparency();
    }

    @Override
    public boolean isDefaultFolder() {
        return folder.isDefaultFolder();
    }

    @Override
    public String getParentId() {
        return folder.getParentId();
    }

    @Override
    public int getModifiedBy() {
        return folder.getModifiedBy();
    }

    @Override
    public int getCreatedBy() {
        return folder.getCreatedBy();
    }

    @Override
    public Date getCreationDate() {
        return folder.getCreationDate();
    }

    @Override
    public GroupwareFolderType getType() {
        return folder.getType();
    }
}
