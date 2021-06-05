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
