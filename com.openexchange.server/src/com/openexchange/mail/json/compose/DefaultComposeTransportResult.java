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

package com.openexchange.mail.json.compose;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.Validate;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.DelegatingComposedMailMessage;
import com.openexchange.mail.json.compose.share.StoredAttachmentsControl;

/**
 * {@link DefaultComposeTransportResult}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class DefaultComposeTransportResult implements ComposeTransportResult {

    /**
     * Creates a new builder instance.
     *
     * @return The new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** A builder for an instance of <code>DefaultComposeTransportResult</code> */
    public static class Builder {

        private List<? extends ComposedMailMessage> transportMessages;
        private ComposedMailMessage sentMessage;
        private boolean transportEqualToSent;
        private StoredAttachmentsControl attachmentsControl;
        private boolean sanitize;

        Builder() {
            super();
            transportEqualToSent = false;
            sanitize = true;
        }

        /**
         * Sets the transport messages
         *
         * @param transportMessages The transport messages to set
         * @return This builder
         */
        public Builder withTransportMessages(List<? extends ComposedMailMessage> transportMessages) {
            return withTransportMessages(transportMessages, true);
        }

        /**
         * Sets the transport messages
         *
         * @param transportMessages The transport messages to set
         * @param sanitize <code>true</code> to sanitize given messages; otherwise <code>false</code>
         * @return This builder
         */
        public Builder withTransportMessages(List<? extends ComposedMailMessage> transportMessages, boolean sanitize) {
            this.transportMessages = transportMessages;
            this.sanitize = sanitize;
            return this;
        }

        /**
         * Sets the sent message
         *
         * @param sentMessage The sent message to set
         * @return This builder
         */
        public Builder withSentMessage(ComposedMailMessage sentMessage) {
            this.sentMessage = sentMessage;
            return this;
        }

        /**
         * Sets that the transport message is equal to the one that is supposed to be stored to standard Sent folder.
         *
         * @return This builder
         */
        public Builder withTransportEqualToSent() {
            this.transportEqualToSent = true;
            return this;
        }

        /**
         * Sets the attachments' control
         *
         * @param attachmentsControl The attachments' control to set
         * @return This builder
         */
        public Builder withAttachmentsControl(StoredAttachmentsControl attachmentsControl) {
            this.attachmentsControl = attachmentsControl;
            return this;
        }

        /**
         * Builds the <code>DefaultComposeTransportResult</code> instance from this builder's arguments.
         *
         * @return The <code>DefaultComposeTransportResult</code> instance
         */
        public DefaultComposeTransportResult build() {
            return new DefaultComposeTransportResult(transportMessages, sentMessage, transportEqualToSent, attachmentsControl, sanitize);
        }

    }

    // -------------------------------------------------------------------------------------------------------------

    private final List<? extends ComposedMailMessage> transportMessages;
    private final ComposedMailMessage sentMessage;
    private final boolean transportEqualToSent;
    private final StoredAttachmentsControl attachmentsControl;

    /**
     * Initializes a new {@link DefaultComposeTransportResult}.
     */
    DefaultComposeTransportResult(List<? extends ComposedMailMessage> transportMessages, ComposedMailMessage sentMessage, boolean transportEqualToSent, StoredAttachmentsControl attachmentsControl, boolean sanitize) {
        super();
        this.transportEqualToSent = transportEqualToSent;
        Validate.notNull(transportMessages, "Transport messages must not be null");
        Validate.notNull(sentMessage, "Sent message must not be null");
        this.attachmentsControl = attachmentsControl;

        if (sanitize) {
            List<ComposedMailMessage> tmp = new ArrayList<>(transportMessages.size());
            for (ComposedMailMessage transportMessage : transportMessages) {
                tmp.add(sanitize(transportMessage, false));
            }
            this.transportMessages = tmp;
        } else {
            this.transportMessages = transportMessages;
        }

        if (sanitize) {
            this.sentMessage = sanitize(sentMessage, true);
        } else {
            this.sentMessage = sentMessage;
        }
    }

    @Override
    public boolean isTransportEqualToSent() {
        return transportEqualToSent;
    }

    @Override
    public List<? extends ComposedMailMessage> getTransportMessages() {
        return transportMessages;
    }

    @Override
    public ComposedMailMessage getSentMessage() {
        return sentMessage;
    }

    @Override
    public void commit() throws OXException {
        if (attachmentsControl != null) {
            attachmentsControl.commit();
        }
    }

    @Override
    public void rollback() throws OXException {
        if (attachmentsControl != null) {
            attachmentsControl.rollback();
        }
    }

    @Override
    public void finish() throws OXException {
        if (attachmentsControl != null) {
            attachmentsControl.finish();
        }
    }

    private static ComposedMailMessage sanitize(ComposedMailMessage toSanitize, boolean expect) {
        if (expect) {
            if (false == toSanitize.isAppendToSentFolder()) {
                // Adjust append-to-sent flag
                DelegatingComposedMailMessage wrappingMessage = new DelegatingComposedMailMessage(toSanitize);
                wrappingMessage.setAppendToSentFolder(true);
                return wrappingMessage;
            }
        } else {
            if (toSanitize.isAppendToSentFolder()) {
                // Adjust append-to-sent flag
                DelegatingComposedMailMessage wrappingMessage = new DelegatingComposedMailMessage(toSanitize);
                wrappingMessage.setAppendToSentFolder(false);
                return wrappingMessage;
            }
        }

        // Return as-is
        return toSanitize;
    }

}
