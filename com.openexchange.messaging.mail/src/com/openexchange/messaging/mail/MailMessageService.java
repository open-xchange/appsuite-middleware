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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.messaging.MessagingAccountAccess;
import com.openexchange.messaging.MessagingAccountManager;
import com.openexchange.messaging.MessagingAccountTransport;
import com.openexchange.messaging.MessagingAction;
import com.openexchange.messaging.MessagingException;
import com.openexchange.messaging.MessagingService;
import com.openexchange.messaging.generic.ReadOnlyDynamicFormDescription;
import com.openexchange.session.Session;

/**
 * {@link MailMessageService}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
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

    /**
     * Initializes a new {@link MailMessageService}.
     */
    private MailMessageService() {
        super();
        final DynamicFormDescription formDescription = new DynamicFormDescription();
        /*
         * Configuration
         */
        formDescription.add(FormElement.input(MailConstants.MAIL_LOGIN, "Login", true, ""));
        formDescription.add(FormElement.password(MailConstants.MAIL_PASSWORD, "Password", true, ""));
        /*-
         * Confirmed ham
         * Confirmed ham fullname
         */
        formDescription.add(FormElement.input(MailConstants.MAIL_CONFIRMED_HAM, "Confirmed Ham", false, "confirmed-ham"));
        formDescription.add(FormElement.input(MailConstants.MAIL_CONFIRMED_HAM_FULLNAME, "Confirmed Ham Fullname", false, ""));
        /*-
         * Confirmed spam
         * Confirmed spam fullname
         */
        formDescription.add(FormElement.input(MailConstants.MAIL_CONFIRMED_SPAM, "Confirmed Spam", false, "confirmed-spam"));
        formDescription.add(FormElement.input(MailConstants.MAIL_CONFIRMED_SPAM_FULLNAME, "Confirmed Spam Fullname", false, ""));
        /*-
         * Drafts
         * Drafts fullname
         */
        formDescription.add(FormElement.input(MailConstants.MAIL_DRAFTS, "Drafts", false, "Drafts"));
        formDescription.add(FormElement.input(MailConstants.MAIL_DRAFTS_FULLNAME, "Drafts Fullname", false, ""));
        /*-
         * Sent
         * Sent fullname
         */
        formDescription.add(FormElement.input(MailConstants.MAIL_SENT, "Sent", false, "Sent"));
        formDescription.add(FormElement.input(MailConstants.MAIL_SENT_FULLNAME, "Sent Fullname", false, ""));
        /*-
         * Spam
         * Spam fullname
         */
        formDescription.add(FormElement.input(MailConstants.MAIL_SPAM, "Spam", false, "Spam"));
        formDescription.add(FormElement.input(MailConstants.MAIL_SPAM_FULLNAME, "Spam Fullname", false, ""));
        /*-
         * Trash
         * Trash fullname
         */
        formDescription.add(FormElement.input(MailConstants.MAIL_TRASH, "Trash", false, "Trash"));
        formDescription.add(FormElement.input(MailConstants.MAIL_TRASH_FULLNAME, "Trash Fullname", false, ""));
        /*-
         * Mail port
         * Mail protocol
         * Mail secure
         * Mail server
         */
        formDescription.add(FormElement.input(MailConstants.MAIL_PORT, "Mail Port", true, ""));
        formDescription.add(FormElement.input(MailConstants.MAIL_PROTOCOL, "Mail Protocol", true, ""));
        formDescription.add(FormElement.checkbox(MailConstants.MAIL_SECURE, "Mail Secure", false, Boolean.FALSE));
        formDescription.add(FormElement.input(MailConstants.MAIL_SERVER, "Mail Server", true, ""));
        /*-
         * Primary address
         * Personal
         */
        formDescription.add(FormElement.input(MailConstants.MAIL_PRIMARY_ADDRESS, "Primary Address", true, ""));
        formDescription.add(FormElement.input(MailConstants.MAIL_PERSONAL, "Personal", false, ""));
        /*-
         * Transport login
         * Transport password
         */
        formDescription.add(FormElement.input(MailConstants.TRANSPORT_LOGIN, "Transport Login", false, ""));
        formDescription.add(FormElement.password(MailConstants.TRANSPORT_PASSWORD, "Transport Password", false, ""));
        /*-
         * Transport port
         * Transport protocol
         * Transport secure
         * Transport server
         */
        formDescription.add(FormElement.input(MailConstants.TRANSPORT_PORT, "Mail Port", false, ""));
        formDescription.add(FormElement.input(MailConstants.TRANSPORT_PROTOCOL, "Mail Protocol", false, ""));
        formDescription.add(FormElement.checkbox(MailConstants.TRANSPORT_SECURE, "Mail Secure", false, Boolean.FALSE));
        formDescription.add(FormElement.input(MailConstants.TRANSPORT_SERVER, "Mail Server", false, ""));
        /*
         * Unified mail enabled
         */
        formDescription.add(FormElement.checkbox(MailConstants.UNIFIED_MAIL_ENABLED, "Unified Mail Enabled", false, Boolean.FALSE));
        /*
         * Create read-only view on generated form description
         */
        this.formDescription = new ReadOnlyDynamicFormDescription(formDescription);
    }

    private MailMessageService init() {
        accountManager = new MailMessagingAccountManager(this);
        return this;
    }

    public MessagingAccountAccess getAccountAccess(final int accountId, final Session session) throws MessagingException {
        return new MailMessagingAccountAccess(accountId, session);
    }

    public MessagingAccountManager getAccountManager() {
        return accountManager;
    }

    public MessagingAccountTransport getAccountTransport(final int accountId, final Session session) throws MessagingException {
        return new MailMessagingAccountTransport(accountId, session);
    }

    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    public DynamicFormDescription getFormDescription() {
        return formDescription;
    }

    public String getId() {
        return ID;
    }

    private static final List<MessagingAction> ACTIONS =
        Collections.unmodifiableList(Arrays.asList(
            new MessagingAction(MailConstants.TYPE_FORWARD, MessagingAction.Type.STORAGE),
            new MessagingAction(MailConstants.TYPE_REPLY, MessagingAction.Type.STORAGE),
            new MessagingAction(MailConstants.TYPE_REPLY_ALL, MessagingAction.Type.STORAGE)));

    public List<MessagingAction> getMessageActions() {
        return ACTIONS;
    }

}
