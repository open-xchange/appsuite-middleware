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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.notification;

import static com.openexchange.notification.CommonNotificationVariables.BUTTON_BACKGROUND_COLOR;
import static com.openexchange.notification.CommonNotificationVariables.BUTTON_BORDER_COLOR;
import static com.openexchange.notification.CommonNotificationVariables.BUTTON_COLOR;
import static com.openexchange.notification.CommonNotificationVariables.FOOTER_IMAGE_SRC;
import static com.openexchange.notification.CommonNotificationVariables.FOOTER_TEXT;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;
import com.openexchange.config.ConfigurationService;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.osgi.util.ServiceCallWrapper;
import com.openexchange.osgi.util.ServiceCallWrapper.ServiceException;
import com.openexchange.osgi.util.ServiceCallWrapper.ServiceUser;
import com.openexchange.serverconfig.NotificationMailConfig;


/**
 * {@link BasicNotificationTemplate}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class BasicNotificationTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(BasicNotificationTemplate.class);

    private static final AtomicReference<String> TEMPLATES_PATH = new AtomicReference<>();

    private final NotificationMailConfig mailConfig;

    private BasicNotificationTemplate(NotificationMailConfig mailConfig) {
        super();
        this.mailConfig = mailConfig;
    }

    public static BasicNotificationTemplate newInstance(NotificationMailConfig mailConfig) {
        return new BasicNotificationTemplate(mailConfig);
    }

    /**
     * Injects the variable substitutions for button styles and the footer into the passed
     * map.
     *
     * @param vars The map containing the variables for template processing
     */
    public void applyStyle(Map<String, Object> vars) {
        vars.put(BUTTON_COLOR, mailConfig.getButtonTextColor());
        vars.put(BUTTON_BACKGROUND_COLOR, mailConfig.getButtonBackgroundColor());
        vars.put(BUTTON_BORDER_COLOR, mailConfig.getButtonBorderColor());
    }

    /**
     * Applies the footer text and image src substitutions to the passed
     * map.
     *
     * @param vars The map containing the variables for template processing
     * @param imageType The footer image if one was applied, otherwise <code>null</code>
     */
    public FooterImage applyFooter(Map<String, Object> vars) {
        String footerText = mailConfig.getFooterText();
        FooterImage footerImage = loadFooterImage();
        if (Strings.isNotEmpty(footerText)) {
            vars.put(FOOTER_TEXT, footerText);
        }

        if (footerImage != null) {
            String cid = UUID.randomUUID().toString();
            vars.put(FOOTER_IMAGE_SRC, "cid:" + cid);
            footerImage.setContentId(cid);
            return footerImage;
        }

        return null;
    }

    public static final class FooterImage {

        private final String contentType;

        private final String fileName;

        private final byte[] data;

        private String cid;


        private FooterImage(String contentType, String fileName, byte[] data) {
            super();
            this.contentType = contentType;
            this.fileName = fileName;
            this.data = data;
        }

        /**
         * Gets the content type, e.g. <code>image/png</code>
         *
         * @return The content type
         */
        public String getContentType() {
            return contentType;
        }

        /**
         * Gets the content id that has been set as the <code>src</code> attributes
         * value.
         *
         * @return The plain content id without any cid-prefix, brackets or applied encoding.
         */
        public String getContentId() {
            return cid;
        }

        /**
         * Gets the image data as byte array
         *
         * @return The data
         */
        public byte[] getData() {
            return data;
        }

        /**
         * Gets the image data as base64 encoded string
         *
         * @return The data
         */
        public String getB64Data() {
            return BaseEncoding.base64().encode(data);
        }

        /**
         * Gets a sane file name including the correct file extension for the image.
         *
         * @return The file name, e.g. <code>footer_logo.png</code>
         */
        public String getFileName() {
            return fileName;
        }

        void setContentId(String cid) {
            this.cid = cid;
        }

    }

    /**
     * Loads the footer image specified in the given mail config and returns it.
     *
     * @param mailConfig The mail notification config
     * @return The footer image or <code>null</code> if none is specified or loading it failed.
     */
    private FooterImage loadFooterImage() {
        String imageName = mailConfig.getFooterImage();
        if (Strings.isEmpty(imageName)) {
            return null;
        }

        String mimeType = MimeType2ExtMap.getContentType(imageName);
        if (mimeType == null || !mimeType.startsWith("image/")) {
            LOG.warn("Footer image {} seems not to be a valid image file. Ensure its file ending matches a common 'image/*' mime type.", imageName);
            return null;
        }

        String templatesPath = getTemplatesPath();
        if (Strings.isEmpty(templatesPath)) {
            return null;
        }

        File image = new File(new File(templatesPath), imageName);
        try {
            byte[] imageBytes = ByteStreams.toByteArray(new FileInputStream(image));
            return new FooterImage(mimeType, "footer_image." + MimeType2ExtMap.getFileExtension(mimeType), imageBytes);
        } catch (IOException e) {
            LOG.warn("Could not load and convert footer image {} from path {}.", imageName, templatesPath, e);
            return null;
        }
    }

    private String getTemplatesPath() {
        String templatesPath = TEMPLATES_PATH.get();
        if (templatesPath == null) {
            try {
                templatesPath = ServiceCallWrapper.doServiceCall(getClass(), ConfigurationService.class, new ServiceUser<ConfigurationService, String>() {
                    @Override
                    public String call(ConfigurationService service) throws Exception {
                        return service.getProperty("com.openexchange.templating.path");
                    }
                });

                TEMPLATES_PATH.set(templatesPath);
            } catch (ServiceException e) {
                LOG.warn("Could not determine templates path.", e);
            }
        }

        return templatesPath;
    }

}
