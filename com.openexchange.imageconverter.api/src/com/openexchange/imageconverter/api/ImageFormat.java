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

import static org.apache.commons.lang.StringUtils.isNotEmpty;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.annotation.NonNull;
import com.openexchange.imagetransformation.ScaleType;

/**
 * {@link ImageFormat}
 *
 * @author <a href="mailto:kai.ahrens@open-xchange.com">Kai Ahrens</a>
 * @since v7.10
 */
/**
 * {@link ImageFormat}
 *
 * @author <a href="mailto:kai.ahrens@open-xchange.com">Kai Ahrens</a>
 * @since v7.10
 */
public class ImageFormat implements Comparable<ImageFormat> {

    public final static int DEFAULT_IMAGE_QUALITY = 75;

    /**
     * {@link ImageType}
     *
     * @author <a href="mailto:kai.ahrens@open-xchange.com">Kai Ahrens</a>
     * @since v7.10
     */
    public enum ImageType {

        AUTO("auto"),

        JPG("jpg"),

        PNG("png");

        /**
         * Initializes a new {@link ImageType}.
         * @param shortName
         */
        ImageType(@NonNull final String shortName) {
            m_shortName = shortName;
        }

        /* (non-Javadoc)
         * @see java.lang.Enum#toString()
         */
        public String getShortName() {
            return m_shortName;
        }

        /**
         * @param other
         * @return
         */
        public long getAbsDistance(final ImageType other) {
            return ((null != other) ? ((other == this) ? 0 : 1) : Long.MAX_VALUE);
        }

        /**
         * @param shortName
         * @return
         */
        public static ImageType createFrom(final String shortName) {
            ImageType ret = null;

            if (isNotEmpty(shortName)) {
                String lookUp = shortName.trim().toLowerCase();

                for (final ImageType imageType : ImageType.values()) {
                    if (lookUp.equals(imageType.getShortName())) {
                        ret = imageType;
                        break;
                    }
                }
            }

            return (null != ret) ? ret : JPG;
        }

        // - Members -----------------------------------------------------------

        private final String m_shortName;
    }

    /**
     * Initializes a new {@link ImageFormat}.
     * @param formatName
     */
    public ImageFormat() {
        super();
    }

    /**
     * Initializes a new {@link ImageFormat}.
     * @param formatName
     */
    public ImageFormat(@NonNull final ImageType imageType) {
        super();
        m_imageType = imageType;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return new StringBuilder("ImageFormat").
            append(" [").
            append("imageType: ").append(m_imageType).
            append(", ").append("autorotate: ").append(m_autoRotate).
            append(", ").append("width: ").append(m_width).
            append(", ").append("height: ").append(m_height).
            append(", ").append("scaleType: ").append(m_scaleType).
            append(", ").append("shrinkOnly: ").append(m_shrinkOnly).
            append(", ").append("quality: ").append(m_quality).
            append(']').toString();
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(ImageFormat other) {
        int ret = 1;

        // imageType, scaleType, autoRotate and shrinkOnly must match to  check;
        // sort order based on: used area => quality
        if ((null != other) &&
            (0 == (ret = Long.compare(getUsedArea(), other.getUsedArea()))) &&
            (0 == (ret = m_imageType.compareTo(other.m_imageType))) &&
            (0 == (ret = m_scaleType.compareTo(other.m_scaleType))) &&
            (0 == (ret = Boolean.compare(m_shrinkOnly, other.m_shrinkOnly))) &&
            (0 == (ret = Boolean.compare(m_autoRotate, other.m_autoRotate)))) {

            ret = Integer.compare(getQuality(), other.getQuality());
        }

        return ret;
    }

    /**
     * @return
     */
    public String getFormatString() {
        final StringBuilder ret = new StringBuilder(m_imageType.getShortName()).append(':').
            append(m_width).append('x').
            append(m_height).append('~').
            append(m_scaleType.getKeyword());

        if (m_autoRotate) {
            ret.append('~').append("autorotate");
        }

        if (m_shrinkOnly) {
            ret.append('~').append("shrinkonly");
        }

        return ret.append('@').append(m_quality).toString();
    }

    /**
     * @return
     */
    public ImageType getImageType() {
        return m_imageType;
    }

    /**
     * @param imageType
     */
    public void setImageType(final ImageType imageType) {
        if (null != imageType) {
            m_imageType = imageType;
        }
    }

    /**
     * @return
     */
    public String getFormatShortName() {
        return m_imageType.toString();
    }

    /**
     * @return
     */
    public void setFormatShortName(final String formatShortName) {
        m_imageType = ImageType.createFrom(formatShortName);
    }

    /**
     * Gets the m_autoRotate
     *
     * @return The m_autoRotate
     */
    public boolean isAutoRotate() {
        return m_autoRotate;
    }

    /**
     * Sets the m_autoRotate
     *
     * @param m_autoRotate The m_autoRotate to set
     */
    public void setAutoRotate(boolean autoRotate) {
        m_autoRotate = autoRotate;
    }

    /**
     * @return
     */
    public int getWidth() {
        return m_width;
    }

    /**
     * Sets the m_width
     *
     * @param width The m_width to set
     */
    public void setWidth(int width) {
        m_width = (width > 0) ? width : -1;
    }

    /**
     * Gets the m_height
     *
     * @return The m_height
     */
    public int getHeight() {
        return m_height;
    }

    /**
     * Sets the m_height
     *
     * @param height The m_height to set
     */
    public void setHeight(int height) {
        m_height = (height > 0) ? height : -1;
    }

    /**
     * @return
     */
    public boolean isScaling() {
        return ((m_width > 0) || (m_height > 0));
    }

    /**
     * @return
     */
    public ScaleType getScaleType() {
        return m_scaleType;
    }

    /**
     * Sets the m_scaleType
     *
     * @param m_scaleType The m_scaleType to set
     */
    public void setScaleType(final ScaleType scaleType) {
        if (null != scaleType) {
            m_scaleType = scaleType;
        }
    }

    /**
     * Gets the m_shrinkOnly
     *
     * @return The m_shrinkOnly
     */
    public boolean isShrinkOnly() {
        return m_shrinkOnly;
    }

    /**
     * Sets the m_shrinkOnly
     *
     * @param m_shrinkOnly The m_shrinkOnly to set
     */
    public void setShrinkOnly(boolean shrinkOnly) {
        m_shrinkOnly = shrinkOnly;
    }

    /**
     * @return
     */
    public int getQuality() {
        return m_quality;
    }

    /**
     * @param quality
     */
    public void setQuality(int quality) {
        m_quality = Math.min(Math.max(1, quality), 100);
    }

    /**
     * @return
     */
    public long getUsedArea() {
        return Math.abs((long) getWidth() * getHeight());
    }

    /**
     * @param other
     * @return
     */
    public long getAbsDistance(final ImageFormat other) {
        long ret = Long.MAX_VALUE;

        if (null != other) {
            final long areaDistance = 32 * Math.abs(getUsedArea() - other.getUsedArea());
            final long imageTypeDistance =  16 * getImageType().getAbsDistance(other.getImageType());
            final long scaleTypeDistance =  4 * implGetAbsDistance(getScaleType(), other.getScaleType());
            final long shrinkOnlyDistance = 2 * implGetAbsDistance(isShrinkOnly(), other.isShrinkOnly());
            final long autoRotateDistance = implGetAbsDistance(isAutoRotate(), other.isAutoRotate());

            ret = areaDistance | imageTypeDistance | scaleTypeDistance | shrinkOnlyDistance | autoRotateDistance;
        }

        return ret;
    }

    /**
     * @param formatShortName
     * @param autoRotate
     * @param width
     * @param height
     * @param scaleType
     * @param shrinkOnly
     * @param quality
     * @return
     */
    public static ImageFormat createFrom(@NonNull final String formatShortName,
        boolean autoRotate,
        int width,
        int height,
        final ScaleType scaleType,
        boolean shrinkOnly,
        int quality) {

        final ImageFormat ret = new ImageFormat();

        ret.setFormatShortName(formatShortName);
        ret.setAutoRotate(autoRotate);
        ret.setWidth(width);
        ret.setHeight(height);
        ret.setScaleType(scaleType);
        ret.setShrinkOnly(shrinkOnly);
        ret.setQuality(quality);

        return ret;
    }

    /**
     * @param imageFormatsStr
     * @return
     */
    public static ImageFormat[] parseImageFormats(final String imageFormatsStr) {
        final ArrayList<ImageFormat> imageFormatList = new ArrayList<>();

        final String[] imageFormatStr = imageFormatsStr.split("[,;]");

        for (String curImageFormatStr : imageFormatStr) {
            final ImageFormat curImageFormat = parseImageFormat(curImageFormatStr);

            if (null != curImageFormat) {
                imageFormatList.add(curImageFormat);
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

        if (isNotEmpty(imageFormatStr)) {
            final String curFormatStr = imageFormatStr.trim().toLowerCase();

            int extentsPos = curFormatStr.indexOf(':');
            int crossPos = curFormatStr.indexOf('x', extentsPos);
            int scalePos = curFormatStr.indexOf('~', crossPos);
            int qualityPos = curFormatStr.indexOf('@', scalePos);
            final boolean hasFormat = (extentsPos > -1);
            final String imageFormatShortName = hasFormat ?
                curFormatStr.substring(0, extentsPos) :
                    ((curFormatStr.length() > 0) ? curFormatStr : ImageFormat.ImageType.AUTO.getShortName());

            if (scalePos < 0) {
                scalePos = curFormatStr.length();
            }

            if (qualityPos < 0) {
                qualityPos = curFormatStr.length();
            }

            // read extents after colon or from 0 and to minusPos or atPos (default: -1x-1)
            String extentStr = curFormatStr.substring(hasFormat ? (extentsPos + 1) : 0, Math.min(scalePos, qualityPos));

            crossPos = extentStr.indexOf('x');

            int width = -1;
            int height = -1;

            try {
                if (crossPos < 0) {
                    width = Integer.valueOf(extentStr).intValue();
                } else {
                    if (crossPos > 0) {
                        width = Integer.valueOf(extentStr.substring(0, crossPos)).intValue();
                    }

                    if (crossPos < (extentStr.length() - 1)) {
                        height = Integer.valueOf(extentStr.substring(crossPos + 1)).intValue();
                    }
                }
            } catch (@SuppressWarnings("unused") NumberFormatException e) {
                // ok, default is taken
            }

            // read scale type, norotate and expand flags:
            // scaleType: ["~contain", "~containforcedimension", "~cover", "~coverandcrop"]
            // norotate: "~norotate"
            // expand: "~expand"
            // default: "~contain"
            ScaleType scaleType = ScaleType.CONTAIN;
            boolean autoRotate = true;
            boolean shrinkOnly = true;

            if ((scalePos > -1) && (scalePos < (qualityPos -1))) {
                final String[] properties = curFormatStr.substring(scalePos + 1, qualityPos).split("~");

                for (int i = 0, length= properties.length; i < length; ++i) {
                    final String curProp = properties[i].trim().toLowerCase();

                    if (curProp.startsWith("cov")) {
                        scaleType = curProp.contains("crop") ? ScaleType.COVER_AND_CROP : ScaleType.COVER;
                    } else if (curProp.contains("force") || curProp.contains("dimension")) {
                        scaleType = ScaleType.CONTAIN_FORCE_DIMENSION;
                    } else if (curProp.contains("norotate") ) {
                        autoRotate = false;
                    } else if (curProp.contains("expand")) {
                        shrinkOnly = false;
                    }

                }
            }

            // read quality
            int quality = DEFAULT_IMAGE_QUALITY;

            if (qualityPos < (curFormatStr.length() - 1)) {
                try {
                    // quality is a positive percentage value between 1 and 100 (%)
                    quality = Math.min(100, Math.max(1, Math.abs(Integer.valueOf(curFormatStr.substring(qualityPos)).intValue())));
                } catch (@SuppressWarnings("unused") NumberFormatException e) {
                    // ok, default is taken
                }
            }

            ret = ImageFormat.createFrom(imageFormatShortName, autoRotate, width, height, scaleType, shrinkOnly, quality);
        } else {
            ret = new ImageFormat();
        }

        return ret;
    }

    // - Implementation --------------------------------------------------------

    /**
     * @param first
     * @param second
     * @return
     */
    private static long implGetAbsDistance(final boolean first, final boolean second) {
        return ((first == second) ? 0 : 1);
    }

    /**
     * @param first
     * @param second
     * @return
     */
    private static long implGetAbsDistance(final ScaleType first, final ScaleType second) {
        long ret = Long.MAX_VALUE;

        if ((null != first) && (null != second)) {
            ret = Math.abs(SCALETYPE_DISTANCE_VALUE.get(first).longValue() - SCALETYPE_DISTANCE_VALUE.get(second).longValue());
        }

        return ret;
    }

    // - Static members --------------------------------------------------------

    /**
     * SCALETYPE_DISTANCE_VALUE
     */
    final private static Map<ScaleType, Long> SCALETYPE_DISTANCE_VALUE = new HashMap<>(ScaleType.values().length);

    static {
        SCALETYPE_DISTANCE_VALUE.put(ScaleType.CONTAIN, Long.valueOf(1));
        SCALETYPE_DISTANCE_VALUE.put(ScaleType.CONTAIN_FORCE_DIMENSION, Long.valueOf(2));
        SCALETYPE_DISTANCE_VALUE.put(ScaleType.COVER, Long.valueOf(4));
        SCALETYPE_DISTANCE_VALUE.put(ScaleType.COVER_AND_CROP, Long.valueOf(5));
    }

    // - Members ---------------------------------------------------------------

    private ImageType m_imageType = ImageType.JPG;
    private boolean m_autoRotate = false;
    private int m_width = -1;
    private int m_height = -1;
    private ScaleType m_scaleType = ScaleType.CONTAIN;
    private boolean m_shrinkOnly = false;
    private int m_quality = DEFAULT_IMAGE_QUALITY;
}
