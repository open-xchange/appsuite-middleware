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

package com.openexchange.html.internal.image;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;

/**
 * {@link ImageProcessor}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class ImageProcessor {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ImageProcessor.class);

    private static final ImageProcessor INSTANCE = new ImageProcessor();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static ImageProcessor getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    private final Pattern imgPattern;
    private final Pattern srcPattern;

    /**
     * Initializes a new {@link ImageProcessor}.
     */
    private ImageProcessor() {
        super();
        imgPattern = Pattern.compile("<img[^>]*>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        srcPattern = Pattern.compile("(?:src=\"([^\"]*)\")|(?:src='([^']*)')|(?:src=[^\"']([^\\s>]*))", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    }

    /**
     * Gets the imgPattern
     *
     * @return The imgPattern
     */
    public Pattern getImgPattern() {
        return imgPattern;
    }

    /**
     * Replaces the &lt;img&gt; tags in given content using specified handler.
     *
     * @param content The content
     * @param handler The handler to use
     * @return The handled content
     */
    public String replaceImages(String content, ImageHandler handler) {
        if (null == content) {
            return null;
        }
        try {
            Matcher imgMatcher = imgPattern.matcher(content);
            if (imgMatcher.find()) {
                /*
                 * Start replacing
                 */
                StringBuilder sb = new StringBuilder(content.length());
                int lastMatch = 0;
                do {
                    sb.append(content.substring(lastMatch, imgMatcher.start()));

                    String imgTag = imgMatcher.group();
                    replaceSrcAttribute(imgTag, sb, handler);
                    lastMatch = imgMatcher.end();
                } while (imgMatcher.find());
                sb.append(content.substring(lastMatch));
                return sb.toString();
            }

        } catch (Exception e) {
            LOG.error("", e);
        }
        return content;
    }

    private String replaceSrcAttribute(String imgTag, StringBuilder sb, ImageHandler handler) {
        Matcher srcMatcher = srcPattern.matcher(imgTag);
        int lastMatch = 0;
        if (srcMatcher.find()) {
            /*
             * 'src' attribute found
             */
            sb.append(imgTag.substring(lastMatch, srcMatcher.start()));
            try {
                /*
                 * Extract URL
                 */
                int group = 1;
                String urlStr = srcMatcher.group(group);
                if (urlStr == null) {
                    urlStr = srcMatcher.group(++group);
                    if (urlStr == null) {
                        urlStr = srcMatcher.group(++group);
                    }
                }
                /*
                 * Handle...
                 */
                if (urlStr.startsWith("cid", 0)) {
                    sb.append(srcMatcher.group(0));
                } else {
                    handler.handleImage(urlStr, srcMatcher.group(0), sb);
                }

            } catch (OXException e) {
                LOG.warn("Image processing failed for \"img\" tag: {}", imgTag, e);
                sb.append(srcMatcher.group());
            } catch (RuntimeException e) {
                LOG.warn("URL replacement failed for \"img\" tag: {}", imgTag, e);
                sb.append(srcMatcher.group());
            }
            lastMatch = srcMatcher.end();
        }
        sb.append(imgTag.substring(lastMatch));
        return sb.toString();
    }

}
