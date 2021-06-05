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

package com.openexchange.ajax.requesthandler.responseRenderers.actions;

import com.openexchange.imageconverter.api.ImageFormat;
import com.openexchange.imagetransformation.Utility;

/**
 * {@link TransformImageParameters}
 *
 * @author <a href="mailto:kai.ahrens@open-xchange.com">Kai Ahrens</a>
 * @since v7.8.4
 */
public class TransformImageParameters extends ImageFormat {

    /**
     * Initializes a new {@link TransformImageParameters}.
     */
    public TransformImageParameters() {
        super();
    }

    public TransformImageParameters(final String imageMimeType) {
        super();

        setImageMimeType(imageMimeType);
    }

    @Override
    public void setImageType(final ImageType imageType) {
        super.setImageType(imageType);
        implUpdateMimeType();
    }

    /**
     * @return
     */
    public String getImageMimeType() {
        return m_imageMimeType;
    }

    /**
     * @return
     */
    public void setImageMimeType(String imageMimeType) {
        this.setImageType(ImageType.createFrom(Utility.getImageFormat(imageMimeType)));
    }

    /**
     * @return
     */
    public String getICCacheKey() {
        return m_icCacheKey;
    }

    /**
     * @param icCacheKey
     */
    public void setICCacheKey(String icCacheKey) {
        m_icCacheKey = icCacheKey;
    }

    /**
     * Gets the m_cropX
     *
     * @return The m_cropX
     */
    public int getCropX() {
        return m_cropX;
    }

    /**
     * Sets the m_cropX
     *
     * @param m_cropX The m_cropX to set
     */
    public void setCropX(int cropX) {
        m_cropX = cropX;
    }

    /**
     * Gets the m_cropY
     *
     * @return The m_cropY
     */
    public int getCropY() {
        return m_cropY;
    }

    /**
     * Sets the m_cropY
     *
     * @param m_cropY The m_cropY to set
     */
    public void setCropY(int cropY) {
        m_cropY = cropY;
    }

    /**
     * Gets the m_cropWidth
     *
     * @return The m_cropWidth
     */
    public int getCropWidth() {
        return m_cropWidth;
    }

    /**
     * Sets the m_cropWidth
     *
     * @param m_cropWidth The m_cropWidth to set
     */
    public void setCropWidth(int cropWidth) {
        m_cropWidth = (cropWidth > 0) ? cropWidth : -1;
    }

    /**
     * Gets the m_cropHeight
     *
     * @return The m_cropHeight
     */
    public int getCropHeight() {
        return m_cropHeight;
    }

    /**
     * Sets the m_cropHeight
     *
     * @param m_cropHeight The m_cropHeight to set
     */
    public void setCropHeight(int cropHeight) {
        m_cropHeight = (cropHeight > 0) ? cropHeight : -1;
    }

    /**
     * @return
     */
    public boolean isCropping() {
        return ((m_cropWidth > 0) || (m_cropHeight > 0));
    }

    /**
     * @return
     */
    public boolean isCompress() {
        return m_compress;
    }

    /**
     * @param compress
     */
    public void setCompress(boolean compress) {
        m_compress = compress;
    }

    // - Implementation --------------------------------------------------------

    protected void implUpdateMimeType() {
        final ImageType imageType = getImageType();

        if (null != imageType) {
            m_imageMimeType = "image/" + (imageType.equals(ImageType.JPG) ? "jpeg" : imageType.getShortName());
        }
    }

    // - Members ---------------------------------------------------------------

    private String m_imageMimeType = "image/jpeg";

    private String m_icCacheKey = null;

    private int m_cropX = 0;

    private int m_cropY = 0;

    private int m_cropWidth = -1;

    private int m_cropHeight = -1;

    private boolean m_compress = false;
}
