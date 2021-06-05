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
