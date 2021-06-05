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

package com.openexchange.notification.mail;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import javax.mail.internet.InternetAddress;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.notification.service.CommonNotificationVariables;
import com.openexchange.serverconfig.NotificationMailConfig;
import com.openexchange.templating.TemplateService;
import com.openexchange.user.User;

/**
 * Encapsulates all data that is necessary to compose notification mails.
 * Instances must be created via a {@link Builder}, use {@link MailData#newBuilder()}
 * to create an according instance.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class MailData {

    /**
     * Instantiates a new mail data builder and returns it.
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * A builder for {@link MailData} instances.
     */
    public static final class Builder {

        private InternetAddress sender;
        private InternetAddress recipient;
        private String subject;
        private String templateName;
        private Map<String, Object> templateVars;
        private NotificationMailConfig mailConfig;
        private Context context;
        private String textContent;
        private User sendingUser;
        private String noReplyAddressPersonal;
        private final Map<String, String> mailHeaders;

        Builder() {
            super();
            mailHeaders = new LinkedHashMap<>();
        }


        /**
         * Sets the personal part for the no-reply address in case the mail is supposed to be sent via no-reply transport.
         * <p>
         * Only considered if no-reply transport with configured no-reply address is used.
         *
         * @param noReplyAddressPersonal The personal part for the no-reply address
         * @return This builder
         */
        public Builder setNoReplyAddressPersonal(String noReplyAddressPersonal) {
            this.noReplyAddressPersonal = noReplyAddressPersonal;
            return this;
        }

        /**
         * Mandatory. Sets the recipient.
         *
         * @param recipient The recipient
         * @return This builder
         */
        public Builder setRecipient(InternetAddress recipient) {
            this.recipient = recipient;
            return this;
        }

        /**
         * Mandatory. Sets the subject.
         *
         * @param subject The subject
         * @return This builder
         */
        public Builder setSubject(String subject) {
            this.subject = subject;
            return this;
        }

        /**
         * Mandatory. Sets the file name of the template to be used to compile the HTML mail part.
         * The according template will be loaded via {@link TemplateService#loadTemplate(String)}.
         *
         * @param templateName The templates file name
         * @return This builder
         */
        public Builder setHtmlTemplate(String templateName) {
            this.templateName = templateName;
            return this;
        }

        /**
         * Mandatory. Sets the root object used by the template engine to substitute all variables.
         * Some style-related variables will be set internally, you must not the following keys
         * within the map:
         * <ul>
         *  <li>{@value CommonNotificationVariables#BUTTON_BACKGROUND_COLOR}</li>
         *  <li>{@value CommonNotificationVariables#BUTTON_BORDER_COLOR}</li>
         *  <li>{@value CommonNotificationVariables#BUTTON_COLOR}</li>
         *  <li>{@value CommonNotificationVariables#FOOTER_TEXT}</li>
         *  <li>{@value CommonNotificationVariables#FOOTER_IMAGE_SRC}</li>
         *  <li>{@value CommonNotificationVariables#FOOTER_IMAGE_ALT}</li>
         * </ul>
         *
         * @param templateVars The template root object
         * @return This builder
         */
        public Builder setTemplateVars(Map<String, Object> templateVars) {
            this.templateVars = templateVars;
            return this;
        }

        /**
         * Mandatory. Sets the notification mail configuration that is used to determine the mails
         * basic layout.
         *
         * @param mailConfig The configuration
         * @return This builder
         */
        public Builder setMailConfig(NotificationMailConfig mailConfig) {
            this.mailConfig = mailConfig;
            return this;
        }

        /**
         * Mandatory. Sets the context.
         *
         * @param context The context
         * @return This builder
         */
        public Builder setContext(Context context) {
            this.context = context;
            return this;
        }

        /**
         * Optional. Sets the user in whose name the mail shall be sent. If set, the configured
         * sender address for the user will be determined and set as <code>To</code> header on
         * the composed mail object. Besides this, an arbitrary sender address can be set via
         * {@link #setSender(InternetAddress)}.
         *
         * @param sendingUser The user
         * @return This builder
         */
        public Builder setSendingUser(User sendingUser) {
            this.sendingUser = sendingUser;
            return this;
        }

        /**
         * Optional. Sets the sender address. This value always overrides {@link #setSendingUser(User)}.
         * The address will be set as <code>To</code> header on the composed mail object.
         *
         * @param sender The sender address
         * @return This builder
         */
        public Builder setSender(InternetAddress sender) {
            this.sender = sender;
            return this;
        }

        /**
         * Mandatory. Sets the plain text content. Composed mails will always be of type
         * <code>multipart/alternative</code> with two enclosed parts. The first
         * enclosed part is always a part with type <code>text/html</code>, containing
         * the html content set via {@link #setHtmlContent(String)}. The second part is
         * always of type <code>text/plain</code>. Its content is the value set via this
         * method. If no text conten is set, it will be derived from the HTML content.
         *
         * @param textContent The plain text content
         * @return This builder
         */
        public Builder setTextContent(String textContent) {
            this.textContent = textContent;
            return this;
        }

        /**
         * Adds a header that will be set on the resulting mail object.
         *
         * @param name The header value
         * @param value The header value
         * @return This builder
         */
        public Builder addMailHeader(String name, String value) {
            this.mailHeaders.put(name, value);
            return this;
        }

        /**
         * Validates the set data and returns a {@link MailData} instance if everything is valid.
         *
         * @return The appropriate {@code MailData} instance from this builder's arguments
         * @throws IllegalArgumentException If a mandatory field has not been set
         */
        public MailData build() {
            checkNotNull(recipient, "recipient");
            checkNotNull(subject, "subject");
            checkNotNull(templateName, "templateName");
            checkNotNull(templateVars, "templateVars");
            checkNotNull(context, "context");
            return new MailData(sender, recipient, subject, templateName, templateVars, mailConfig, context, textContent, sendingUser, mailHeaders, noReplyAddressPersonal);
        }

        private static final void checkNotNull(Object obj, String name) {
            if (obj == null) {
                throw new IllegalArgumentException("A value for field '" + name + "' has not been set!");
            }
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final InternetAddress sender;
    private final InternetAddress recipient;
    private final String subject;
    private final String templateName;
    private final Map<String, Object> templateVars;
    private final NotificationMailConfig mailConfig;
    private final Context context;
    private final String textContent;
    private final User sendingUser;
    private final Map<String, String> mailHeaders;
    private final Optional<String> noReplyAddressPersonal;

    /**
     * Initializes a new {@link MailData}.
     * @param noReplyAddressPersonal
     */
    MailData(InternetAddress sender, InternetAddress recipient, String subject, String templateName, Map<String, Object> templateVars, NotificationMailConfig mailConfig, Context context, String textContent, User sendingUser, Map<String, String> mailHeaders, String noReplyAddressPersonal) {
        super();
        this.sender = sender;
        this.recipient = recipient;
        this.subject = subject;
        this.templateName = templateName;
        this.templateVars = templateVars;
        this.mailConfig = mailConfig;
        this.context = context;
        this.textContent = textContent;
        this.sendingUser = sendingUser;
        this.mailHeaders = mailHeaders;
        this.noReplyAddressPersonal = Optional.ofNullable(noReplyAddressPersonal);
    }

    /**
     * Gets the optional personal part for the no-reply address in case the mail is supposed to be sent via no-reply transport.
     *
     * @return The optional personal part for the no-reply address
     */
    public Optional<String> getNoReplyAddressPersonal() {
        return noReplyAddressPersonal;
    }

    public InternetAddress getSender() {
        return sender;
    }

    public User getSendingUser() {
        return sendingUser;
    }

    public InternetAddress getRecipient() {
        return recipient;
    }

    public String getSubject() {
        return subject;
    }

    public String getTemplateName() {
        return templateName;
    }

    public Map<String, Object> getTemplateVars() {
        return templateVars;
    }

    public NotificationMailConfig getMailConfig() {
        return mailConfig;
    }

    public String getTextContent() {
        return textContent;
    }

    public Context getContext() {
        return context;
    }

    public Map<String, String> getMailHeaders() {
        return mailHeaders;
    }
}