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

import static com.openexchange.notification.CommonNotificationVariables.*;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.java.util.Pair;
import com.openexchange.serverconfig.NotificationMailConfig;
import com.openexchange.templating.TemplateService;


/**
 * {@link TemplateHelper}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class TemplateHelper {

    private static final Logger LOG = LoggerFactory.getLogger(TemplateHelper.class);

    /**
     * Injects the variable substitutions for button styles and the footer into the passed
     * map.
     *
     * @param vars The map containing the variables for template processing
     * @param mailConfig The mail configuration that defines the substitutions to be applied
     * @param templateService The template service to load and encode a potential footer image.
     */
    public static void injectNotificationMailConfig(Map<String, Object> vars, NotificationMailConfig mailConfig, TemplateService templateService) {
        vars.put(BUTTON_COLOR, mailConfig.getButtonTextColor());
        vars.put(BUTTON_BACKGROUND_COLOR, mailConfig.getButtonBackgroundColor());
        vars.put(BUTTON_BORDER_COLOR, mailConfig.getButtonBorderColor());
        String footerImageName = mailConfig.getFooterImage();
        if (Strings.isNotEmpty(footerImageName)) {
            try {
                Pair<String, String> footerImagePair = templateService.encodeTemplateImage(footerImageName);
                vars.put(FOOTER_IMAGE_CONTENT_TYPE, footerImagePair.getFirst());
                vars.put(FOOTER_IMAGE, footerImagePair.getSecond());
            } catch (OXException e) {
                LOG.error("Configured notification mail file '{}' could not be loaded. Please check your 'as-config.yml', the footer image will be ignored for now.", footerImageName, e);
            }
        }
        String footerText = mailConfig.getFooterText();
        if (Strings.isNotEmpty(footerText)) {
            vars.put(FOOTER_TEXT, footerText);
        }
    }

}
