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

    private int m_cropX = 0;

    private int m_cropY = 0;

    private int m_cropWidth = -1;

    private int m_cropHeight = -1;

    private boolean m_compress = false;
}
