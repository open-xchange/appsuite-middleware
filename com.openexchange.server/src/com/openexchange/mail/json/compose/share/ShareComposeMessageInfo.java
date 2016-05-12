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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail.json.compose.share;

import java.util.Date;
import java.util.List;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.json.compose.ComposeRequest;

/**
 * {@link ShareComposeMessageInfo}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class ShareComposeMessageInfo {

    private final ShareComposeLink shareLink;
    private final List<Recipient> recipients;
    private final ShareTransportComposeContext composeContext;
    private final String password;
    private final Date expirationDate;
    private final ComposedMailMessage source;
    private final ComposeRequest composeRequest;

    /**
     * Initializes a new {@link ShareComposeMessageInfo}.
     */
    public ShareComposeMessageInfo(ShareComposeLink shareLink, List<Recipient> recipients, String password, Date expirationDate, ComposedMailMessage source, ShareTransportComposeContext context, ComposeRequest composeRequest) {
        super();
        this.shareLink = shareLink;
        this.recipients = recipients;
        this.password = password;
        this.expirationDate = expirationDate;
        this.source = source;
        this.composeContext = context;
        this.composeRequest = composeRequest;
    }

    /**
     * Gets the compose request
     *
     * @return The compose request
     */
    public ComposeRequest getComposeRequest() {
        return composeRequest;
    }

    /**
     * Gets the source message
     *
     * @return The source message
     */
    public ComposedMailMessage getSource() {
        return source;
    }

    /**
     * Gets the password
     *
     * @return The password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Gets the expiration date
     *
     * @return The expiration date
     */
    public Date getExpirationDate() {
        return expirationDate;
    }

    /**
     * Gets the compose context
     *
     * @return The compose context
     */
    public ShareTransportComposeContext getComposeContext() {
        return composeContext;
    }

    /**
     * Gets the share link
     *
     * @return The share link
     */
    public ShareComposeLink getShareLink() {
        return shareLink;
    }

    /**
     * Gets the recipients that are supposed to receive the share link.
     *
     * @return The recipients
     */
    public List<Recipient> getRecipients() {
        return recipients;
    }

}
