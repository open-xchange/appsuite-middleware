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

package com.openexchange.filemanagement.internal;

import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.DistributedFileUtils;
import com.openexchange.mail.utils.MailPasswordUtil;


/**
 * {@link DistributedFileUtilsImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class DistributedFileUtilsImpl implements DistributedFileUtils {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(DistributedFileUtilsImpl.class);
    }

    /**
     * Initializes a new {@link DistributedFileUtilsImpl}.
     */
    public DistributedFileUtilsImpl() {
        super();
    }

    private static final java.security.Key DFM_KEY = MailPasswordUtil.generateSecretKey("open-xchange");

    @Override
    public String encodeId(String rawId) throws OXException {
        if (rawId == null) {
            return null;
        }

        try {
            String encrypted = MailPasswordUtil.encrypt(rawId, DFM_KEY);
            return com.openexchange.ajax.AJAXUtility.encodeUrl(encrypted);
        } catch (java.security.GeneralSecurityException x) {
            LoggerHolder.LOG.debug("Failed to encode identifier", x);
            return rawId;
        }
    }

    @Override
    public String decodeId(String encodedId) throws OXException {
        if (encodedId == null) {
            return null;
        }

        try {
            String urlDecoded = com.openexchange.ajax.AJAXUtility.decodeUrl(encodedId, "UTF-8");
            return MailPasswordUtil.decrypt(urlDecoded, DFM_KEY);
        } catch (java.security.GeneralSecurityException x) {
            LoggerHolder.LOG.debug("Failed to decode identifier", x);
            return encodedId;
        }
    }

}
