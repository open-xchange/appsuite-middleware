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

package com.openexchange.mail.compose;

/**
 * {@link VCardAndFileName} - A pair of vCard and name for the ".vcf" file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class VCardAndFileName {

    private final byte[] vcard;
    private final String fileName;

    /**
     * Initializes a new {@link VCardAndFileName}.
     *
     * @param vcard The vCard bytes
     * @param fileName The file name
     */
    public VCardAndFileName(byte[] vcard, String fileName) {
        super();
        this.vcard = vcard;
        this.fileName = fileName;
    }

    /**
     * Gets the vcard bytes
     *
     * @return The vcard bytes
     */
    public byte[] getVcard() {
        return vcard;
    }

    /**
     * Gets the file name
     *
     * @return The file name
     */
    public String getFileName() {
        return fileName;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        if (vcard != null) {
            builder.append("vcard=<not-null>").append(", ");
        }
        if (fileName != null) {
            builder.append("fileName=").append(fileName);
        }
        builder.append("]");
        return builder.toString();
    }

}
