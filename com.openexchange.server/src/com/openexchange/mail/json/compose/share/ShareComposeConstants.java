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

package com.openexchange.mail.json.compose.share;

import com.openexchange.mail.mime.MessageHeaders;

/**
 * {@link ShareComposeConstants}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class ShareComposeConstants {

    /**
     * Initializes a new {@link ShareComposeConstants}.
     */
    private ShareComposeConstants() {
        super();
    }

    /** <code>"X-Open-Xchange-Share-Reference"</code>: The special header containing the share reference */
    public static final String HEADER_SHARE_REFERENCE = MessageHeaders.HDR_X_OPEN_XCHANGE_SHARE_REFERENCE;

    /** <code>"X-Open-Xchange-Share-Type"</code>: The special header containing the share type */
    public static final String HEADER_SHARE_TYPE = MessageHeaders.HDR_X_OPEN_XCHANGE_SHARE_TYPE;

    /** <code>"X-Open-Xchange-Share-URL"</code>: The special header containing the share URL */
    public static final String HEADER_SHARE_URL = MessageHeaders.HDR_X_OPEN_XCHANGE_SHARE_URL;

    /** The special user flag to mark a message having a share reference */
    public static final String USER_SHARE_REFERENCE = "$ShareReference";

}
