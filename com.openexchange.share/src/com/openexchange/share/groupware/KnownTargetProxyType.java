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

package com.openexchange.share.groupware;

/**
 * {@link KnownTargetProxyType} - Enumeration of the different types that are available in the drive module and might become available as
 * {@link TargetProxy}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since v7.8.0
 */
public enum KnownTargetProxyType implements TargetProxyType {
    IMAGE("picture", TargetProxyTypeStrings.IMAGE_FILE_TYPE),
    FILE("file", TargetProxyTypeStrings.GENERAL_FILE_TYPE),
    FOLDER("folder", TargetProxyTypeStrings.FOLDER_TYPE),
    CALENDAR("calendar", TargetProxyTypeStrings.CALENDAR_TYPE),
    ;

    private String id;
    private String displayName;

    KnownTargetProxyType(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

}
