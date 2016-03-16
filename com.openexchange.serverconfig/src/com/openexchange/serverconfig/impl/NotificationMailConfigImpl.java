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

package com.openexchange.serverconfig.impl;

import com.openexchange.serverconfig.NotificationMailConfig;

/**
 * {@link NotificationMailConfigImpl}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since v7.8.0
 */
public class NotificationMailConfigImpl implements NotificationMailConfig {

    private String buttonTextColor;
    private String buttonBackgroundColor;
    private String buttonBorderColor;
    private String footerText;
    private String footerImage;
    private String footerImageAltText;
    private boolean embedFooterImage;

    @Override
    public String getButtonTextColor() {
        return buttonTextColor;
    }

    /**
     * Sets the text color for button labels
     *
     * @param color The color to set
     */
    public void setButtonTextColor(String color) {
        this.buttonTextColor = color;
    }

    @Override
    public String getButtonBackgroundColor() {
        return buttonBackgroundColor;
    }

    /**
     * Sets the background color for buttons
     *
     * @param color The color to set
     */
    public void setButtonBackgroundColor(String color) {
        this.buttonBackgroundColor = color;
    }

    @Override
    public String getButtonBorderColor() {
        return buttonBorderColor;
    }

    /**
     * Sets the border color for buttons
     *
     * @param color The color to set
     */
    public void setButtonBorderColor(String color) {
        this.buttonBorderColor = color;
    }

    @Override
    public String getFooterText() {
        return footerText;
    }

    /**
     * Sets the text for mail footers
     *
     * @param footerText The text to set
     */
    public void setFooterText(String footerText) {
        this.footerText = footerText;
    }

    @Override
    public String getFooterImage() {
        return footerImage;
    }

    /**
     * Sets the footer image as file name below <code>/opt/open-xchange/templates</code>.
     *
     * @param footerImage The images file name
     */
    public void setFooterImage(String fileName) {
        this.footerImage = fileName;
    }

    @Override
    public String getFooterImageAltText() {
        return footerImageAltText;
    }

    /**
     * Sets the alternative text of the footer image. This text must be set, if a footer
     * image is set.
     *
     * @param footerImageAltText The text
     */
    public void setFooterImageAltText(String footerImageAltText) {
        this.footerImageAltText = footerImageAltText;
    }

    @Override
    public boolean embedFooterImage() {
        return embedFooterImage;
    }

    /**
     * Sets whether the footer image shall be embedded via data URL or contained in a separate
     * MIME part and referenced via content ID.
     *
     * @param embedFooterImage <code>true</code> to use data URLs
     */
    public void setEmbedFooterImage(boolean embedFooterImage) {
        this.embedFooterImage = embedFooterImage;
    }

}
