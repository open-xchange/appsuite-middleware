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

package com.openexchange.mail.json.compose.share;

import java.io.InputStream;
import com.openexchange.exception.OXException;

/**
 * {@link FileItem} - Represents a file item.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class FileItem extends Item {

    /** Provides binary data for a file */
    public static interface DataProvider {

        /**
         * Gets the binary data of a file.
         *
         * @return The binary data as a stream.
         * @throws OXException If strem cannot be returned
         */
        InputStream getData() throws OXException;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final DataProvider dataProvider;
    private final long size;
    private final String mimeType;

    /**
     * Initializes a new {@link FileItem}.
     *
     * @param id The item identifier
     * @param name The name
     * @param size The size in bytes or <code>-1</code>
     * @param mimeType The MIME type
     * @param dataProvider The data provider
     */
    public FileItem(String id, String name, long size, String mimeType, DataProvider dataProvider) {
        super(id, name);
        this.dataProvider = dataProvider;
        this.size = size;
        this.mimeType = mimeType;
    }

    /**
     * Gets the MIME type.
     *
     * @return The MIME type
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Gets the size in bytes.
     *
     * @return The size in bytes or <code>-1</code>
     */
    public long getSize() {
        return size;
    }

    /**
     * Gets the binary data of the file.
     *
     * @return The binary data as a stream
     * @throws OXException If stream cannot be returned
     */
    public InputStream getData() throws OXException {
        return dataProvider.getData();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        if (getId() != null) {
            builder.append("id=").append(getId()).append(", ");
        }
        if (getName() != null) {
            builder.append("name=").append(getName()).append(", ");
        }
        if (dataProvider != null) {
            builder.append("dataProvider=").append(dataProvider).append(", ");
        }
        builder.append("size=").append(size).append(", ");
        if (mimeType != null) {
            builder.append("mimeType=").append(mimeType);
        }
        builder.append(']');
        return builder.toString();
    }

}
