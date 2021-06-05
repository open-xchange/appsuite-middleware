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

package com.openexchange.mail.authenticity.impl.trusted.internal;

import java.rmi.server.UID;
import com.openexchange.mail.authenticity.impl.trusted.Icon;

/**
 * {@link ImageIcon}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class ImageIcon implements Icon {

    private final byte[] byteArray;
    private static final String MIME_TYPE = "image/png";
    private final String UID;
    private static final String PREFIX = "trustedMail_";

    /**
     * Initializes a new {@link ImageIcon}.
     *
     * @param byteArray the content of the image icon
     */
    public ImageIcon(byte[] byteArray) {
        super();
        this.byteArray = byteArray;
        UID = PREFIX + new UID().toString();

    }

    @Override
    public String getMimeType() {
        return MIME_TYPE;
    }

    @Override
    public byte[] getData() {
        return byteArray;
    }

    @Override
    public String getUID() {
        return UID;
    }
}
