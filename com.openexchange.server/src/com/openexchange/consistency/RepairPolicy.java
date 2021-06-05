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

package com.openexchange.consistency;

/**
 * {@link RepairPolicy}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public enum RepairPolicy {

    /**
     * It will handle any missing Drive entries for a file
     */
    MISSING_ENTRY_FOR_FILE,
    /**
     * It will handle any missing attachments
     */
    MISSING_FILE_FOR_ATTACHMENT,
    /**
     * It will handle any missing infoitems
     */
    MISSING_FILE_FOR_INFOITEM,
    /**
     * It will handle any missing snippets
     */
    MISSING_FILE_FOR_SNIPPET,
    /**
     * It will handle any missing vcards
     */
    MISSING_FILE_FOR_VCARD,
    /**
     * It will handle any missing composition space attachments
     */
    MISSING_ATTACHMENT_FILE_FOR_MAIL_COMPOSE,
    
    /**
     * It will handle any missing previews
     */
    MISSING_FILE_FOR_PREVIEW
    ;
}
