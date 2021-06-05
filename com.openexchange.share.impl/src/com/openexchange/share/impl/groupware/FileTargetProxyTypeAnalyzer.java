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

package com.openexchange.share.impl.groupware;

import com.openexchange.file.storage.File;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.share.groupware.KnownTargetProxyType;
import com.openexchange.share.groupware.TargetProxyType;


/**
 * {@link FileTargetProxyTypeAnalyzer}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since v7.8.0
 */
public class FileTargetProxyTypeAnalyzer {

    public static TargetProxyType analyzeType(File file) {
        String mimeType = null;
        if (Strings.isNotEmpty(file.getFileName())) {
            mimeType = MimeType2ExtMap.getContentType(file.getFileName(), null);
        }
        if (null == mimeType) {
            mimeType = file.getFileMIMEType();
        }

        //minimalistic distinction between images and other types
        if (null != mimeType && mimeType.startsWith("image")) {
            return KnownTargetProxyType.IMAGE;
        }

        return KnownTargetProxyType.FILE;
    }
}
