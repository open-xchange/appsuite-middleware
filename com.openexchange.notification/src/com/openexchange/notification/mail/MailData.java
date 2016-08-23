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

package com.openexchange.notification.mail;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.mail.internet.InternetAddress;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.notification.service.CommonNotificationVariables;
import com.openexchange.serverconfig.NotificationMailConfig;
import com.openexchange.templating.TemplateService;

/**
 * Encapsulates all data that is necessary to compose notification mails.
 * Instances must be created via a {@link Builder}, use {@link MailData#newBuilder()}
 * to create an according instance.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class MailData {

    private InternetAddress sender;
    private InternetAddress recipient;
    private String subject;
    private String templateName;
    private Map<String, Object> templateVars;
    private NotificationMailConfig mailConfig;
    private Context context;
    private String textContent;
    private User sendingUser;
    private final Map<String, String> mailHeaders = new LinkedHashMap<>();

    private MailData() {
        super();
    }

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

        private final MailData data;

        private Builder() {
            super();
            this.data = new MailData();
        }

        /**
         * Mandatory. Sets the recipient.
         *
         * @param recipient The recipient
         */
        public Builder setRecipient(InternetAddress recipient) {
            data.recipient = recipient;
            return this;
        }

        /**
         * Mandatory. Sets the subject.
         *
         * @param subject The subject
         */
        public Builder setSubject(String subject) {
            data.subject = subject;
            return this;
        }

        /**
         * Mandatory. Sets the file name of the template to be used to compile the HTML mail part.
         * The according template will be loaded via {@link TemplateService#loadTemplate(String)}.
         *
         * @param templateName The templates file name
         */
        public Builder setHtmlTemplate(String templateName) {
            data.templateName = templateName;
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
         */
        public Builder setTemplateVars(Map<String, Object> templateVars) {
            data.templateVars = templateVars;
            return this;
        }

        /**
         * Mandatory. Sets the notification mail configuration that is used to determine the mails
         * basic layout.
         *
         * @param mailConfig The configuration
         */
        public Builder setMailConfig(NotificationMailConfig mailConfig) {
            data.mailConfig = mailConfig;
            return this;
        }

        /**
         * Mandatory. Sets the context.
         *
         * @param context The context;
         */
        public Builder setContext(Context context) {
            data.context = context;
            return this;
        }

        /**
         * Optional. Sets the user in whose name the mail shall be sent. If set, the configured
         * sender address for the user will be determined and set as <code>To</code> header on
         * the composed mail object. Besides this, an arbitrary sender address can be set via
         * {@link #setSender(InternetAddress)}.
         *
         * @param sendingUser The user
         */
        public Builder setSendingUser(User sendingUser) {
            data.sendingUser = sendingUser;
            return this;
        }

        /**
         * Optional. Sets the sender address. This value always overrides {@link #setSendingUser(User)}.
         * The address will be set as <code>To</code> header on the composed mail object.
         *
         * @param sender The sender address
         */
        public Builder setSender(InternetAddress sender) {
            data.sender = sender;
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
         */
        public Builder setTextContent(String textContent) {
            data.textContent = textContent;
            return this;
        }

        /**
         * Adds a header that will be set on the resulting mail object.
         *
         * @param name The header value
         * @param value The header value
         */
        public Builder addMailHeader(String name, String value) {
            data.mailHeaders.put(name, value);
            return this;
        }

        /**
         * Validates the set data and returns a {@link MailData} instance if everything is valid.
         *
         * @throws IllegalArgumentException If a mandatory field has not been set
         */
        public MailData build() {
            checkNotNull(data.recipient, "recipient");
            checkNotNull(data.subject, "subject");
            checkNotNull(data.templateName, "templateName");
            checkNotNull(data.templateVars, "templateVars");
            checkNotNull(data.context, "context");
            return data;
        }

        private static final void checkNotNull(Object obj, String name) {
            if (obj == null) {
                throw new IllegalArgumentException("A value for field '" + name + "' has not been set!");
            }
        }

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