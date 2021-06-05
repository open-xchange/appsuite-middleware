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

package com.openexchange.tools.images.transformations;

import java.io.InputStream;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.imagetransformation.TransformedImage;
import com.openexchange.java.Streams;

/**
 * {@link TransformedImageImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class TransformedImageImpl implements TransformedImage {

    private final int width;
    private final int height;
    private final long size;
    private final String formatName;
    private final byte[] imageData;
    private final ThresholdFileHolder imageFile;
    private final byte[] md5;
    private final int transformationExpenses;

    public TransformedImageImpl(int width, int height, long size, String formatName, byte[] imageData, byte[] md5, final int transformationExpenses) {
        super();
        this.width = width;
        this.height = height;
        this.size = size;
        this.formatName = formatName;
        this.imageData = imageData;
        this.imageFile = null;
        this.md5 = md5;
        this.transformationExpenses = transformationExpenses;
    }

    public TransformedImageImpl(int width, int height, long size, String formatName, ThresholdFileHolder imageFile, byte[] md5, final int transformationExpenses) {
        super();
        if (null == imageFile) {
            throw new IllegalArgumentException("imageFile must not be null");
        }
        this.width = width;
        this.height = height;
        this.size = size;
        this.formatName = formatName;
        this.imageData = null;
        this.imageFile = imageFile;
        this.md5 = md5;
        this.transformationExpenses = transformationExpenses;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public String getFormatName() {
        return formatName;
    }

    @Override
    public byte[] getImageData() throws OXException {
        return null != imageData ? imageData : imageFile.toByteArray();
    }

    @Override
    public InputStream getImageStream() throws OXException {
        return null != imageData ? Streams.newByteArrayInputStream(imageData) : imageFile.getStream();
    }

    @Override
    public IFileHolder getImageFile() {
        return imageFile;
    }

    @Override
    public byte[] getMD5() {
        return md5;
    }

    @Override
    public int getTransformationExpenses() {
        return transformationExpenses;
    }

    @Override
    public String toString() {
        return formatName + " | " + width + " x " + height + " | " + size + " bytes";
    }

    @Override
    public void close() {
        ThresholdFileHolder imageFile = this.imageFile;
        if (null != imageFile) {
            imageFile.close();
        }
    }

}