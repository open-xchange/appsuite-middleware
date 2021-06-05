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
