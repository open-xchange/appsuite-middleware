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

package com.openexchange.filestore.s3.internal.config;

import com.openexchange.java.Strings;

/**
 * {@link S3CredentialsSource} - The S3 credentials source indicating from what source to take credentials from to authenticate against S3
 * end-point.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public enum S3CredentialsSource {

    /**
     * Credentials are taken from <code>"com.openexchange.filestore.s3.[filestoreID].accessKey"</code> property and
     * <code>"com.openexchange.filestore.s3.[filestoreID].secretKey"</code> property respectively.
     */
    STATIC("static"),
    /**
     * Credentials are taken from integrated AWS Identity and Access Management (IAM). See also
     * <a href="https://docs.aws.amazon.com/IAM/latest/UserGuide/introduction.html">https://docs.aws.amazon.com/IAM/latest/UserGuide/introduction.html</a>
     */
    IAM("iam"),
    ;

    private final String identifier;

    private S3CredentialsSource(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Gets the identifier.
     *
     * @return The identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String toString() {
        return identifier;
    }

    /**
     * Gets the credentials source for given identifier.
     *
     * @param id The identifier to look-up by
     * @return The associated credentials source or <code>null</code>
     */
    public static S3CredentialsSource credentialsSourceFor(String id) {
        if (Strings.isEmpty(id)) {
            return null;
        }

        String toLookUp = Strings.asciiLowerCase(id).trim();
        for (S3CredentialsSource credentialsSource : values()) {
            if (toLookUp.equals(credentialsSource.identifier)) {
                return credentialsSource;
            }
        }
        return null;
    }

}
