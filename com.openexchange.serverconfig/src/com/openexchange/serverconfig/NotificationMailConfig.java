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
