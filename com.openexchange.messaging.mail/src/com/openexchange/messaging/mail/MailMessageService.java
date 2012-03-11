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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.messaging.mail;

import static com.openexchange.messaging.mail.FormStrings.FORM_LABEL_CONFIRMED_HAM;
import static com.openexchange.messaging.mail.FormStrings.FORM_LABEL_CONFIRMED_HAM_FULLNAME;
import static com.openexchange.messaging.mail.FormStrings.FORM_LABEL_CONFIRMED_SPAM;
import static com.openexchange.messaging.mail.FormStrings.FORM_LABEL_CONFIRMED_SPAM_FULLNAME;
import static com.openexchange.messaging.mail.FormStrings.FORM_LABEL_DRAFTS;
import static com.openexchange.messaging.mail.FormStrings.FORM_LABEL_DRAFTS_FULLNAME;
import static com.openexchange.messaging.mail.FormStrings.FORM_LABEL_LOGIN;
import static com.openexchange.messaging.mail.FormStrings.FORM_LABEL_PASSWORD;
import static com.openexchange.messaging.mail.FormStrings.FORM_LABEL_PERSONAL;
import static com.openexchange.messaging.mail.FormStrings.FORM_LABEL_PORT;
import static com.openexchange.messaging.mail.FormStrings.FORM_LABEL_PRIMARY_ADDRESS;
import static com.openexchange.messaging.mail.FormStrings.FORM_LABEL_PROTOCOL;
import static com.openexchange.messaging.mail.FormStrings.FORM_LABEL_SECURE;
import static com.openexchange.messaging.mail.FormStrings.FORM_LABEL_SENT;
import static com.openexchange.messaging.mail.FormStrings.FORM_LABEL_SENT_FULLNAME;
import static com.openexchange.messaging.mail.FormStrings.FORM_LABEL_SERVER;
import static com.openexchange.messaging.mail.FormStrings.FORM_LABEL_SPAM;
import static com.openexchange.messaging.mail.FormStrings.FORM_LABEL_SPAM_FULLNAME;
import static com.openexchange.messaging.mail.FormStrings.FORM_LABEL_TRANSPORT_LOGIN;
import static com.openexchange.messaging.mail.FormStrings.FORM_LABEL_TRANSPORT_PASSWORD;
import static com.openexchange.messaging.mail.FormStrings.FORM_LABEL_TRANSPORT_PORT;
import static com.openexchange.messaging.mail.FormStrings.FORM_LABEL_TRANSPORT_PROTOCOL;
import static com.openexchange.messaging.mail.FormStrings.FORM_LABEL_TRANSPORT_SECURE;
import static com.openexchange.messaging.mail.FormStrings.FORM_LABEL_TRANSPORT_SERVER;
import static com.openexchange.messaging.mail.FormStrings.FORM_LABEL_TRASH;
import static com.openexchange.messaging.mail.FormStrings.FORM_LABEL_TRASH_FULLNAME;
import static com.openexchange.messaging.mail.FormStrings.FORM_LABEL_UNIFIED_MAIL_ENABLED;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.datatypes.genericonf.ReadOnlyDynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingAccountAccess;
import com.openexchange.messaging.MessagingAccountManager;
import com.openexchange.messaging.MessagingAccountTransport;
import com.openexchange.messaging.MessagingAction;
import com.openexchange.messaging.MessagingService;
import com.openexchange.session.Session;

/**
 * {@link MailMessageService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18
 */
public final class MailMessageService implements MessagingService {

    /**
     * The identifier of {@link MailMessageService}: <code>"com.openexchange.messaging.mail"</code>.
     */
    private static final String ID = "com.openexchange.messaging.mail";

    private static final String DISPLAY_NAME = "Mail";

    /**
     * Gets a new instance.
     *
     * @return A new instance
     */
    public static MailMessageService newInstance() {
        return new MailMessageService().init();
    }

    private MessagingAccountManager accountManager;

    private final DynamicFormDescription formDescription;

    private final Set<String> secretProperties;

    /**
     * Initializes a new {@link MailMessageService}.
     */
    private MailMessageService() {
        super();
        final DynamicFormDescription tmpDescription = new DynamicFormDescription();
        /*
         * Configuration
         */
        tmpDescription.add(FormElement.input(MailConstants.MAIL_LOGIN, FORM_LABEL_LOGIN, true, ""));
        tmpDescription.add(FormElement.password(MailConstants.MAIL_PASSWORD, FORM_LABEL_PASSWORD, true, ""));
        /*-
         * Confirmed ham
         * Confirmed ham fullname
         */
        tmpDescription.add(FormElement.input(MailConstants.MAIL_CONFIRMED_HAM, FORM_LABEL_CONFIRMED_HAM, false, "confirmed-ham"));
        tmpDescription.add(FormElement.input(MailConstants.MAIL_CONFIRMED_HAM_FULLNAME, FORM_LABEL_CONFIRMED_HAM_FULLNAME, false, ""));
        /*-
         * Confirmed spam
         * Confirmed spam fullname
         */
        tmpDescription.add(FormElement.input(MailConstants.MAIL_CONFIRMED_SPAM, FORM_LABEL_CONFIRMED_SPAM, false, "confirmed-spam"));
        tmpDescription.add(FormElement.input(MailConstants.MAIL_CONFIRMED_SPAM_FULLNAME, FORM_LABEL_CONFIRMED_SPAM_FULLNAME, false, ""));
        /*-
         * Drafts
         * Drafts fullname
         */
        tmpDescription.add(FormElement.input(MailConstants.MAIL_DRAFTS, FORM_LABEL_DRAFTS, false, "Drafts"));
        tmpDescription.add(FormElement.input(MailConstants.MAIL_DRAFTS_FULLNAME, FORM_LABEL_DRAFTS_FULLNAME, false, ""));
        /*-
         * Sent
         * Sent fullname
         */
        tmpDescription.add(FormElement.input(MailConstants.MAIL_SENT, FORM_LABEL_SENT, false, "Sent"));
        tmpDescription.add(FormElement.input(MailConstants.MAIL_SENT_FULLNAME, FORM_LABEL_SENT_FULLNAME, false, ""));
        /*-
         * Spam
         * Spam fullname
         */
        tmpDescription.add(FormElement.input(MailConstants.MAIL_SPAM, FORM_LABEL_SPAM, false, "Spam"));
        tmpDescription.add(FormElement.input(MailConstants.MAIL_SPAM_FULLNAME, FORM_LABEL_SPAM_FULLNAME, false, ""));
        /*-
         * Trash
         * Trash fullname
         */
        tmpDescription.add(FormElement.input(MailConstants.MAIL_TRASH, FORM_LABEL_TRASH, false, "Trash"));
        tmpDescription.add(FormElement.input(MailConstants.MAIL_TRASH_FULLNAME, FORM_LABEL_TRASH_FULLNAME, false, ""));
        /*-
         * Mail port
         * Mail protocol
         * Mail secure
         * Mail server
         */
        tmpDescription.add(FormElement.input(MailConstants.MAIL_PORT, FORM_LABEL_PORT, true, ""));
        tmpDescription.add(FormElement.input(MailConstants.MAIL_PROTOCOL, FORM_LABEL_PROTOCOL, true, ""));
        tmpDescription.add(FormElement.checkbox(MailConstants.MAIL_SECURE, FORM_LABEL_SECURE, false, Boolean.FALSE));
        tmpDescription.add(FormElement.input(MailConstants.MAIL_SERVER, FORM_LABEL_SERVER, true, ""));
        /*-
         * Primary address
         * Personal
         */
        tmpDescription.add(FormElement.input(MailConstants.MAIL_PRIMARY_ADDRESS, FORM_LABEL_PRIMARY_ADDRESS, true, ""));
        tmpDescription.add(FormElement.input(MailConstants.MAIL_PERSONAL, FORM_LABEL_PERSONAL, false, ""));
        /*-
         * Transport login
         * Transport password
         */
        tmpDescription.add(FormElement.input(MailConstants.TRANSPORT_LOGIN, FORM_LABEL_TRANSPORT_LOGIN, false, ""));
        tmpDescription.add(FormElement.password(MailConstants.TRANSPORT_PASSWORD, FORM_LABEL_TRANSPORT_PASSWORD, false, ""));
        /*-
         * Transport port
         * Transport protocol
         * Transport secure
         * Transport server
         */
        tmpDescription.add(FormElement.input(MailConstants.TRANSPORT_PORT, FORM_LABEL_TRANSPORT_PORT, false, ""));
        tmpDescription.add(FormElement.input(MailConstants.TRANSPORT_PROTOCOL, FORM_LABEL_TRANSPORT_PROTOCOL, false, ""));
        tmpDescription.add(FormElement.checkbox(MailConstants.TRANSPORT_SECURE, FORM_LABEL_TRANSPORT_SECURE, false, Boolean.FALSE));
        tmpDescription.add(FormElement.input(MailConstants.TRANSPORT_SERVER, FORM_LABEL_TRANSPORT_SERVER, false, ""));
        /*
         * Unified mail enabled
         */
        tmpDescription.add(FormElement.checkbox(MailConstants.UNIFIED_MAIL_ENABLED, FORM_LABEL_UNIFIED_MAIL_ENABLED, false, Boolean.FALSE));
        /*
         * Create read-only view on generated form description
         */
        formDescription = new ReadOnlyDynamicFormDescription(tmpDescription);
        secretProperties = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(MailConstants.MAIL_PASSWORD, MailConstants.TRANSPORT_PASSWORD)));
    }

    private MailMessageService init() {
        accountManager = new MailMessagingAccountManager(this);
        return this;
    }

    @Override
    public Set<String> getSecretProperties() {
        return secretProperties;
    }

    @Override
    public MessagingAccountAccess getAccountAccess(final int accountId, final Session session) throws OXException {
        return new MailMessagingAccountAccess(accountId, session);
    }

    @Override
    public MessagingAccountManager getAccountManager() {
        return accountManager;
    }

    @Override
    public MessagingAccountTransport getAccountTransport(final int accountId, final Session session) throws OXException {
        return new MailMessagingAccountTransport(accountId, session);
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public DynamicFormDescription getFormDescription() {
        return formDescription;
    }

    @Override
    public String getId() {
        return ID;
    }

    private static final List<MessagingAction> ACTIONS =
        Collections.unmodifiableList(Arrays.asList(
            new MessagingAction(MailConstants.TYPE_FORWARD, MessagingAction.Type.STORAGE),
            new MessagingAction(MailConstants.TYPE_REPLY, MessagingAction.Type.STORAGE),
            new MessagingAction(MailConstants.TYPE_REPLY_ALL, MessagingAction.Type.STORAGE)));

    @Override
    public List<MessagingAction> getMessageActions() {
        return ACTIONS;
    }

    @Override
    public int[] getStaticRootPermissions() {
        return null;
    }

}
