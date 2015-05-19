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

package com.openexchange.share.notification.mail.impl;

import javax.mail.internet.InternetAddress;
import com.openexchange.share.notification.DefaultShareCreatedNotification;
import com.openexchange.share.notification.ShareNotificationService.Transport;
import com.openexchange.share.notification.mail.ShareMailAware;

/**
 * {@link ShareCreatedMailNotification}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since v7.8.0
 */
public class ShareCreatedMailNotification extends DefaultShareCreatedNotification<InternetAddress> implements ShareMailAware {

    ShareMailDelegate shareMailDelegate;
    private boolean causedGuestCreation;

    /**
     * Initializes a new {@link ShareCreatedMailNotification}.
     * @param transport
     */
    public ShareCreatedMailNotification() {
        super(Transport.MAIL);
        shareMailDelegate = new ShareMailDelegate();
    }

    public ShareCreatedMailNotification(NotificationType type) {
        super(Transport.MAIL, type);
        shareMailDelegate = new ShareMailDelegate();
    }

    @Override
    public String getProductName() {
        return shareMailDelegate.getProductName();
    }

    /**
     * @see com.openexchange.share.notification.mail.impl.ShareMailDelegate#setProductName(java.lang.String)
     */
    @Override
    public void setProductName(String productName) {
        shareMailDelegate.setProductName(productName);
    }

    @Override
    public String getButtonColor() {
        return shareMailDelegate.getButtonColor();
    }

    /**
     * @see com.openexchange.share.notification.mail.impl.ShareMailDelegate#setButtonColor(java.lang.String)
     */
    @Override
    public void setButtonColor(String buttonColor) {
        shareMailDelegate.setButtonColor(buttonColor);
    }

    @Override
    public String getButtonBackgroundColor() {
        return shareMailDelegate.getButtonBackgroundColor();
    }

    /**
     * @see com.openexchange.share.notification.mail.impl.ShareMailDelegate#setButtonBackgroundColor(java.lang.String)
     */
    @Override
    public void setButtonBackgroundColor(String buttonBackgroundColor) {
        shareMailDelegate.setButtonBackgroundColor(buttonBackgroundColor);
    }

    @Override
    public String getButtonBorderColor() {
        return shareMailDelegate.getButtonBorderColor();
    }

    /**
     * @see com.openexchange.share.notification.mail.impl.ShareMailDelegate#setButtonBorderColor(java.lang.String)
     */
    @Override
    public void setButtonBorderColor(String buttonBorderColor) {
        shareMailDelegate.setButtonBorderColor(buttonBorderColor);
    }

    @Override
    public String getFooterText() {
        return shareMailDelegate.getFooterText();
    }

    /**
     * @see com.openexchange.share.notification.mail.impl.ShareMailDelegate#setFooterText(java.lang.String)
     */
    @Override
    public void setFooterText(String footerText) {
        shareMailDelegate.setFooterText(footerText);
    }

    @Override
    public String getFooterImage() {
        return shareMailDelegate.getFooterImage();
    }

    /**
     * @see com.openexchange.share.notification.mail.impl.ShareMailDelegate#setFooterImage(java.lang.String)
     */
    @Override
    public void setFooterImage(String footerImage) {
        shareMailDelegate.setFooterImage(footerImage);
    }

    /**
     * Set the causedGuestCreation flag to indicate if a new guest was created for this share and the mail to be phrased differently.
     *
     * @param true if a new guest was created, else false
     */
    public boolean getCausedGuestCreation() {
        return causedGuestCreation;
    }
    /**
     * Set the causedGuestCreation flag to indicate if a new guest was created for this share and the mail to be phrased differently.
     *
     * @param causedGuestCreation the flag
     */
    public void setCausedGuestCreation(boolean causedGuestCreation) {
        this.causedGuestCreation = causedGuestCreation;
    }




}
