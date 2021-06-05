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

package com.openexchange.client.onboarding;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.slf4j.Logger;
import com.openexchange.java.Streams;

/**
 * Template-based implementation of an {@link Icon}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class TemplateIcon implements Icon {

    private static final long serialVersionUID = 7821572419974173720L;

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(TemplateIcon.class);

    private final String mimeType;
    private final File file;

    /**
     * Initializes a new {@link TemplateIcon}.
     */
    public TemplateIcon(String name) {
        this(name, null);
    }

    /**
     * Initializes a new {@link TemplateIcon}.
     */
    public TemplateIcon(String name, String mimeType) {
        super();
        FileInfo fileInfo = OnboardingUtility.getTemplateFileInfo(name);
        this.file = fileInfo.getFile();
        this.mimeType = null == mimeType ? fileInfo.getMimeType() : mimeType;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public long getSize() {
        return file.length();
    }

    @Override
    public byte[] getData() {
        if (false == file.exists()) {
            LOG.debug("Icon image {} does not exist.", file.getPath());
            return new byte[0];
        }

        try {
            return Streams.stream2bytes(new FileInputStream(file));
        } catch (java.io.FileNotFoundException e) {
            LOG.debug("Icon image {} does not exist.", file.getPath(), e);
            return new byte[0];
        } catch (IOException e) {
            LOG.debug("Could not load icon image {}.", file.getPath(), e);
            return new byte[0];
        }
    }

    @Override
    public IconType getType() {
        return IconType.RAW;
    }

}
