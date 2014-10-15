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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.share.servlet.internal;

import javax.mail.internet.InternetAddress;
import com.openexchange.share.Share;
import com.openexchange.share.notification.ShareNotification;


/**
 * {@link ResetPasswordShareNotification}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class ResetPasswordShareNotification implements ShareNotification<InternetAddress> {

    private final Share share;
    private final String url;
    private final String title;
    private final String message;
    private final InternetAddress recipient;
    private final InternetAddress sender;

    /**
     * Initializes a new {@link ResetPasswordShareNotification}.
     *
     * @param share The share
     * @param url The share URL
     * @param title The message title
     * @param message The message text
     * @param recipient The address to send to
     */
    public ResetPasswordShareNotification(Share share, String url, String title, String message, InternetAddress recipient, InternetAddress sender) {
        super();
        this.share = share;
        this.url = url;
        this.title = title;
        this.message = message;
        this.recipient = recipient;
        this.sender = sender;
    }

    @Override
    public Share getShare() {
        return share;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public InternetAddress getTransportInfo() {
        return recipient;
    }

    /**
     * Gets the sender
     *
     * @return The sender
     */
    public InternetAddress getSender() {
        return sender;
    }

}
