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

package com.openexchange.imageconverter.api;

import java.util.ArrayList;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.annotation.NonNull;

/**
 * {@link ImageFormat}
 *
 * @author <a href="mailto:kai.ahrens@open-xchange.com">Kai Ahrens</a>
 * @since v7.8.3
 */
public class ImageFormat implements Comparable<ImageFormat> {

    /**
     * {@link ImageType}
     *
     * @author <a href="mailto:kai.ahrens@open-xchange.com">Kai Ahrens</a>
     * @since v7.8.3
     */
    public enum ImageType {

        JPG("jpg"),

        PNG("png");

        /**
         * Initializes a new {@link ImageType}.
         * @param typeName
         */
        ImageType(@NonNull final String typeName) {
            m_typeName = typeName;
        }

        /* (non-Javadoc)
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return m_typeName;
        }

        /**
         * @param typeName
         * @return
         */
        public static ImageType createFrom(final String typeName) {
            return (StringUtils.isNotEmpty(typeName) ? valueOf(typeName.toUpperCase()) : ImageType.JPG);
        }

        // - Members -----------------------------------------------------------

        private String m_typeName = null;
    }

    /**
     * Initializes a new {@link ImageFormat}.
     * @param formatName
     */
    ImageFormat() {
        this(ImageType.JPG, 128, -1, 75);
    }

    /**
     * Initializes a new {@link ImageFormat}.
     * @param formatName
     */
    ImageFormat(@NonNull final ImageType imageType) {
        this(imageType, 128, -1, 75);
    }

    /**
     * Initializes a new {@link ImageFormat}.
     * @param formatName
     */
    ImageFormat(@NonNull final ImageType imageType, final int width, final int height) {
        this(imageType, width, height, 75);
    }


    /**
     * Initializes a new {@link ImageFormat}.
     * @param formatName
     */
    ImageFormat(@NonNull final ImageType imageType, final int width, final int height, final int quality) {
        super();

        m_type = imageType;
        m_width = width;
        m_height = height;
        m_quality = quality;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return new StringBuilder("ImageFormat").
            append(" [").
            append("imageType: ").append(m_type).
            append(", ").append("width: ").append(m_width).
            append(", ").append("height: ").append(m_height).
            append(", ").append("quality: ").append(m_quality).
            append(']').toString();
    }

    @Override
    public int compareTo(ImageFormat other) {
        int ret = m_type.compareTo(other.m_type);

        if (0 == ret) {
            final long area1 = Math.abs((long) getWidth() * getHeight());
            final long area2 = Math.abs((long) other.getWidth() * other.getHeight());

            if (area1 == area2) {
                ret = getQuality() < other.getQuality() ? -1 : (getQuality() > other.getQuality() ? 1 : 0);
            } else {
                ret = (area1 < area2) ? -1 :  1;
            }
        }

        return ret;
    }

    /**
     * @return
     */
    public String getKey() {
        return new StringBuilder(m_type.toString()).append(':').
            append(m_width).append('x').
            append(m_height).append('@').
            append(m_quality).toString();

    }

    /**
     * @return
     */
    public ImageType getImageType() {
        return m_type;
    }

    /**
     * @return
     */
    public String getFormatName() {
        return m_type.toString();
    }

    /**
     * @return
     */
    public int getWidth() {
        return m_width;
    }

    /**
     * @return
     */
    public int getHeight() {
        return m_height;
    }

    /**
     * @return
     */
    public int getQuality() {
        return m_quality;
    }

    /**
     * @param typeName
     * @param width
     * @param height
     * @param quality
     * @return
     */
    public static ImageFormat createFrom(@NonNull final String typeName, int width, int height, int quality) {
        return new ImageFormat(ImageType.createFrom(typeName), width, height, quality);
    }

    /**
     * @param imageFormatsStr
     * @return
     */
    public static ImageFormat[] parseImageFormats(final String imageFormatsStr) {
        final ArrayList<ImageFormat> imageFormatList = new ArrayList<>();

        if (StringUtils.isNotEmpty(imageFormatsStr)) {
            final String[] imageFormatStr = imageFormatsStr.split("[,;]");

            for (String curImageFormatStr : imageFormatStr) {
                final ImageFormat curImageFormat = parseImageFormat(curImageFormatStr);

                if (null != curImageFormat) {
                    imageFormatList.add(curImageFormat);
                }

            }
        }

        return imageFormatList.toArray(new ImageFormat[imageFormatList.size()]);
    }

    /**
     * @param imageFormatStr
     * @return
     */
    public static ImageFormat parseImageFormat(final String imageFormatStr) {
        ImageFormat ret = null;

        if (StringUtils.isNotEmpty(imageFormatStr)) {
            final String curFormatStr = imageFormatStr.trim();

            final int colPos = curFormatStr.indexOf(':');
            final int crossPos = curFormatStr.indexOf('x');
            int atPos = curFormatStr.lastIndexOf('@');
            final String format = (colPos > 0) ? curFormatStr.substring(0, colPos).toLowerCase() : "jpg";

            if (atPos < 0) {
                atPos = curFormatStr.length();
            }

            try {
                final int width = ((colPos > -1) && (crossPos > (colPos + 1))) ? Integer.valueOf(curFormatStr.substring(colPos + 1, crossPos)).intValue() : -1;
                final int height = ((crossPos > -1) && (atPos > (crossPos + 1))) ?  Integer.valueOf(curFormatStr.substring(crossPos + 1, atPos)).intValue() : -1;
                final int quality = (atPos < (curFormatStr.length() - 1)) ? Integer.valueOf(curFormatStr.substring(atPos + 1)).intValue() : (format.equals("jpg") ? 75 : 7);

                ret = ImageFormat.createFrom(format, width, height, quality);
            } catch (NumberFormatException e) {
                LOG.error(e.getMessage());
            }
        }

        return ret;
    }

    // - Static members --------------------------------------------------------

    final private static Logger LOG = LoggerFactory.getLogger(ImageFormat.class);

    // - Members ---------------------------------------------------------------

    private ImageType m_type = null;
    private int m_width = 128;
    private int m_height = -1;
    private int m_quality = 75;
}
