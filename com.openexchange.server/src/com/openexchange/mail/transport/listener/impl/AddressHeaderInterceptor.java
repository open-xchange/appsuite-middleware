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

package com.openexchange.mail.transport.listener.impl;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.mail.dataobjects.SecuritySettings;
import com.openexchange.mail.transport.listener.MailTransportListener;
import com.openexchange.mail.transport.listener.Reply;
import com.openexchange.mail.transport.listener.Result;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.user.UserService;

/**
 * {@link AddressHeaderInterceptor}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class AddressHeaderInterceptor implements MailTransportListener, Reloadable {

    /** The {@link Logger} of this class */
    private final static Logger LOGGER = LoggerFactory.getLogger(AddressHeaderInterceptor.class);

    /** The property of interest */
    private final static String PRIMARY_ADDRESS_HEADER = "com.openexchange.smtp.setPrimaryAddressHeader";

    /** User service to determinate the users primary mail address */
    private final UserService userService;

    /** The value of {@value #PRIMARY_ADDRESS_HEADER} */
    private String primaryAddressHeader;

    /** An value indicating if the configured value of {@value #PRIMARY_ADDRESS_HEADER} is valid and can be used */
    private boolean valid;

    /**
     * Initializes a new {@link AddressHeaderInterceptor}.
     * 
     * @param services The {@link ServiceLookup}
     * 
     */
    public AddressHeaderInterceptor(ServiceLookup services) {
        super();
        this.userService = services.getService(UserService.class);
        if (null == userService) {
            ServiceExceptionCode.absentService(UserService.class);
        }
        reloadConfiguration(services.getService(ConfigurationService.class));
    }

    // ----- MailTransportListener ----- 
    @Override
    public boolean checkSettings(SecuritySettings securitySettings, Session session) throws OXException {
        return false;
    }

    @Override
    public Result onBeforeMessageTransport(MimeMessage message, Address[] recipients, SecuritySettings securitySettings, Session session) throws OXException {
        if (valid) {
            try {
                User user = userService.getUser(session.getUserId(), session.getContextId());
                if (null != user) {
                    // Check if primary account of user is used
                    if (addHeader(message, user, new Address[] { message.getSender() }) ||  // Sender set and equal?
                        addHeader(message, user, message.getFrom())) {                      // From set and any equal? 
                        return new InsertResult(message, recipients);
                    }
                }
            } catch (MessagingException e) {
                LOGGER.error("Couldn't add the primary address header.", e);
            } catch (OXException e) {
                LOGGER.error("The user {} in context {} couldn't be found. Therefore the primary mail address couldn't be added the the mail.");
            }
        } else {
            // Only log if property is set
            if (Strings.isNotEmpty(primaryAddressHeader)) {
                LOGGER.error("The value \"" + primaryAddressHeader + "\" of property" + PRIMARY_ADDRESS_HEADER + " isn't valid. Therefore the header can't be appended to the mail.");
            }
        }
        LOGGER.debug("Didn't add primary address header to the mail. Either property isn't set or a non internal mail account was used to send the mail.");
        return new InsertResult(message, recipients);
    }

    @Override
    public void onAfterMessageTransport(MimeMessage message, Exception exception, Session session) throws OXException {
        // Ignore
    }

    // ----- Reloadable -----
    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        this.primaryAddressHeader = null;
        this.valid = false;

        if (null != configService) {
            String property = configService.getProperty(PRIMARY_ADDRESS_HEADER);
            if (Strings.isNotEmpty(property)) {
                this.primaryAddressHeader = property;
                this.valid = validate(property);
            }
        } else {
            LOGGER.debug("The ConfigurationService is absent. Can't load {}", PRIMARY_ADDRESS_HEADER);
        }
    }

    @Override
    public Interests getInterests() {
        return DefaultInterests.builder().propertiesOfInterest(PRIMARY_ADDRESS_HEADER).build();
    }

    /**
     * Checks if the property value is an 'all ASCII' string only containing '-' as delimiter.
     * 
     * @param string The sting to check
     * @return <code>true</code> if the string consists out of ASCII letters.
     */
    private boolean validate(String string) {
        String property = string.trim();
        property = property.replaceAll("-", "");
        for (int i = 0; i < property.length(); i++) {
            char c = property.charAt(i);
            if (false == Strings.isAsciiLetter(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Add the configured header to the mail if it matches any of the given recipients
     * 
     * @param message The message to send
     * @param user The user to get the primary mail and the aliases from
     * @param recipients The recipients to check
     * @return <code>true</code> if the header was added, <code>false</code> otherwise
     * @throws MessagingException In case mail can't be added
     */
    private boolean addHeader(MimeMessage message, User user, Address[] recipients) throws MessagingException {
        if (null == recipients || recipients.length < 0) {
            return false;
        }
        for (String alias : user.getAliases()) {
            Address address = new InternetAddress(alias);
            for (Address recipient : recipients) {
                if (address.equals(recipient)) {
                    message.addHeader(primaryAddressHeader, user.getMail());
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 
     * {@link InsertResult}
     *
     * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
     * @since v7.10.0
     */
    private final class InsertResult implements Result {

        private final MimeMessage mimeMessage;
        private final Address[]   recipients;

        InsertResult(MimeMessage mimeMessage, Address[] recipients) {
            super();
            this.mimeMessage = mimeMessage;
            this.recipients = recipients;
        }

        @Override
        public Reply getReply() {
            return Reply.NEUTRAL;
        }

        @Override
        public MimeMessage getMimeMessage() {
            return mimeMessage;
        }

        @Override
        public Address[] getRecipients() {
            return recipients;
        }
    }
}
