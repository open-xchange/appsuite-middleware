/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellctual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks.
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
 *     Copyright (C) 2016 OX Software GmbH
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

import java.io.File;
import org.apache.commons.lang.ArrayUtils;
import com.openexchange.config.ConfigurationService;

/**
 * {@link ImageConverterConfig}
 *
 * @author <a href="mailto:kai.ahrens@open-xchange.com">Kai Ahrens</a>
 * @since v7.8.3
 */
/**
 * {@link ImageConverterConfig}
 *
 * @author <a href="mailto:kai.ahrens@open-xchange.com">Kai Ahrens</a>
 * @since v7.8.3
 */
public class ImageConverterConfig {

    /**
     * Initializes a new {@link ImageConverterConfig}.
     */
    @SuppressWarnings("unused")
    private ImageConverterConfig() {
        this(null);
    }

    /**
     * Initializes a new {@link ImageConverterConfig}.
     * @param configurationService
     */
    public ImageConverterConfig(final ConfigurationService configurationService) {
        implInit(configurationService);
    }

    /**
     * @return
     */
    public ImageFormat[] getImageFormats() {
        return m_imageFormats;
    }

    // - Implementation --------------------------------------------------------

    void implInit(final ConfigurationService configService) {
        if (null != configService) {
            m_imageFormats = ImageFormat.parseImageFormats(configService.getProperty("com.openexchange.imageconverter.targetFormats", "jpg:512x-1@75,jpg:256x-1@75,jpg:128x-1@75"));

            IMAGE_SERVER_SEARCHPATH = configService.getProperty("com.openexchange.imageconverter.imagemagick.searchPath", "/usr/bin");

            final File spoolDir = new File(configService.getProperty("com.openexchange.imageserver.imagemagick.spoolPath", "/tmp"));

            if ((spoolDir.exists() || spoolDir.mkdirs()) && spoolDir.isDirectory() && spoolDir.canWrite()) {
                IMAGE_SERVER_SPOOLDIR = spoolDir;
            }

            IMAGE_SERVER_USE_GRAPHICSMAGICK = configService.getBoolProperty("com.openexchange.imageconverter.imagemagick.useGraphicsMagick", false);

            if (ArrayUtils.isEmpty(m_imageFormats)) {
                m_imageFormats = DEAFULT_IMAGEFORMATS;
            }
        }
    }

    // - Internally used properties --------------------------------------------

    /**
     * DEAFULT_IMAGEFORMATS
     */
    private final static ImageFormat[] DEAFULT_IMAGEFORMATS = {
        ImageFormat.createFrom("jpg", 512, -1, 75),
        ImageFormat.createFrom("jpg", 256, -1, 75),
        ImageFormat.createFrom("jpg", 128, -1, 75),
    };

    // - Members ---------------------------------------------------------------

    private ImageFormat[] m_imageFormats = DEAFULT_IMAGEFORMATS;

    // - Public members --------------------------------------------------------

    /**
     * The search ch path for ImageMagick
     */
    public static String IMAGE_SERVER_SEARCHPATH = "/usr/bin";

    /**
     * The spool path (working directory) for ImageMagick
     */
    public static File IMAGE_SERVER_SPOOLDIR = new File("/tmp");

    /**
     * Flag to indicate the usage of GraphicsMagick
     */
    public static boolean IMAGE_SERVER_USE_GRAPHICSMAGICK = false;

    /**
     * the location of the log
     */
    public static String IMAGECONVERTER_LOGFILE = null;

    /**
     * determines the amount and detail of logging data;
     * possible values are ERROR, WARN, INFO, DEBUG, TRACE
     * disabled
     */
    public static String IMAGECONVERTER_LOGLEVEL = null;

    /**
     * Internal flag to enable the debug mode
     */
    public static boolean IMAGECONVERTER_DEBUG = false;
}
