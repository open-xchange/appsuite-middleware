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

package com.openexchange.serverconfig;

/**
 * {@link NotificationMailConfig} - Represents the notification mail config params that are set via our as-config approach and available as part of the
 * {@link ServerConfig}.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public interface NotificationMailConfig {

    /**
     * Gets the text color for button labels
     *
     * @return The color as hexadecimal RGB code, e.g. <code>#ffffff</code>
     */
    String getButtonTextColor();

    /**
     * Gets the background color for buttons
     *
     * @return The color as hexadecimal RGB code, e.g. <code>#ffffff</code>
     */
    String getButtonBackgroundColor();

    /**
     * Gets the border color for buttons
     *
     * @return The color as hexadecimal RGB code, e.g. <code>#ffffff</code>
     */
    String getButtonBorderColor();

    /**
     * Gets the text for mail footers
     *
     * @return The footer text or <code>null</code> if none shall be displayed
     */
    String getFooterText();

    /**
     * Gets the footer image as file name below <code>/opt/open-xchange/templates</code>.
     *
     * @return The images file name or <code>null</code> if none shall be displayed
     */
    String getFooterImage();

    /**
     * Gets the alternative text of the footer image. This text is shown by email clients
     * that don't show images at all.
     *
     * @return The alternative text; not <code>null</code> if {@link #getFooterImage()} is
     * also not <code>null</code>.
     */
    String getFooterImageAltText();

    /**
     * Gets whether a potential footer image shall be embedded as data URL (i.e. in the form
     * of <code>&lt;img src="data:image/png;base64,iVBO...." /&gt;</code> or if it shall be
     * contained as a separate MIME part and be referenced via its content ID (i.e. <code>
     * &lt;img src="cid:ce29ee25-eb59-4147-a4ab-aed71224773b" /&gt;</code>.
     *
     * @return <code>true</code> if a data URL shall be used, <code>false</code> otherwise.
     */
    boolean embedFooterImage();

}
