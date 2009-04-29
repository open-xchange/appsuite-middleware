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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.mailaccount;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.openexchange.mailaccount.servlet.fields.MailAccountFields;

/**
 * {@link Attribute}.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public enum Attribute {

    ID_LITERAL(MailAccountFields.ID, 1001),
    LOGIN_LITERAL(MailAccountFields.LOGIN, 1002),
    PASSWORD_LITERAL(MailAccountFields.PASSWORD, 1003),
    MAIL_URL_LITERAL(MailAccountFields.MAIL_URL, 1004),
    TRANSPORT_URL_LITERAL(MailAccountFields.TRANSPORT_URL, 1005),
    NAME_LITERAL(MailAccountFields.NAME, 1006),
    PRIMARY_ADDRESS_LITERAL(MailAccountFields.PRIMARY_ADDRESS, 1007),
    SPAM_HANDLER_LITERAL(MailAccountFields.SPAM_HANDLER, 1008),
    TRASH_LITERAL(MailAccountFields.TRASH, 1009),
    SENT_LITERAL(MailAccountFields.SENT, 1010),
    DRAFTS_LITERAL(MailAccountFields.DRAFTS, 1011),
    SPAM_LITERAL(MailAccountFields.SPAM, 1012),
    CONFIRMED_SPAM_LITERAL(MailAccountFields.CONFIRMED_SPAM, 1013),
    CONFIRMED_HAM_LITERAL(MailAccountFields.CONFIRMED_HAM, 1014),
    MAIL_SERVER_LITERAL(MailAccountFields.MAIL_SERVER, 1015),
    MAIL_PORT_LITERAL(MailAccountFields.MAIL_PORT, 1016),
    MAIL_PROTOCOL_LITERAL(MailAccountFields.MAIL_PROTOCOL, 1017),
    MAIL_SECURE_LITERAL(MailAccountFields.MAIL_SECURE, 1018),
    TRANSPORT_SERVER_LITERAL(MailAccountFields.TRANSPORT_SERVER, 1019),
    TRANSPORT_PORT_LITERAL(MailAccountFields.TRANSPORT_PORT, 1020),
    TRANSPORT_PROTOCOL_LITERAL(MailAccountFields.TRANSPORT_PROTOCOL, 1021),
    TRANSPORT_SECURE_LITERAL(MailAccountFields.TRANSPORT_SECURE, 1022),
    TRANSPORT_LOGIN_LITERAL(MailAccountFields.TRANSPORT_LOGIN, 1023),
    TRANSPORT_PASSWORD_LITERAL(MailAccountFields.TRANSPORT_PASSWORD, 1024),
    UNIFIED_INBOX_ENABLED_LITERAL(MailAccountFields.UNIFIED_INBOX_ENABLED, 1025);

    public static final Set<Attribute> MAIL_URL_ATTRIBUTES = Collections.unmodifiableSet(EnumSet.of(Attribute.MAIL_PORT_LITERAL, Attribute.MAIL_PROTOCOL_LITERAL, Attribute.MAIL_SECURE_LITERAL, Attribute.MAIL_SERVER_LITERAL));
    public static final Set<Attribute> TRANSPORT_URL_ATTRIBUTES = Collections.unmodifiableSet(EnumSet.of(Attribute.TRANSPORT_PORT_LITERAL, Attribute.TRANSPORT_PROTOCOL_LITERAL, Attribute.TRANSPORT_SECURE_LITERAL, Attribute.TRANSPORT_SERVER_LITERAL));
    
    private int id;

    private String attrName;

    private Attribute(final String name, final int id) {
        this.attrName = name;
        this.id = id;
    }

    public Object doSwitch(final AttributeSwitch switcher) {
        switch (this) {
        case ID_LITERAL:
            return switcher.id();
        case LOGIN_LITERAL:
            return switcher.login();
        case PASSWORD_LITERAL:
            return switcher.password();
        case MAIL_URL_LITERAL:
            return switcher.mailURL();
        case TRANSPORT_URL_LITERAL:
            return switcher.transportURL();
        case NAME_LITERAL:
            return switcher.name();
        case PRIMARY_ADDRESS_LITERAL:
            return switcher.primaryAddress();
        case SPAM_HANDLER_LITERAL:
            return switcher.spamHandler();
        case TRASH_LITERAL:
            return switcher.trash();
        case DRAFTS_LITERAL:
            return switcher.drafts();
        case SPAM_LITERAL:
            return switcher.spam();
        case SENT_LITERAL:
            return switcher.sent();
        case CONFIRMED_SPAM_LITERAL:
            return switcher.confirmedSpam();
        case CONFIRMED_HAM_LITERAL:
            return switcher.confirmedHam();
        case MAIL_SERVER_LITERAL:
            return switcher.mailServer();
        case MAIL_PORT_LITERAL:
            return switcher.mailPort();
        case MAIL_PROTOCOL_LITERAL:
            return switcher.mailProtocol();
        case MAIL_SECURE_LITERAL:
            return switcher.mailSecure();
        case TRANSPORT_SERVER_LITERAL:
            return switcher.transportServer();
        case TRANSPORT_PORT_LITERAL:
            return switcher.transportPort();
        case TRANSPORT_PROTOCOL_LITERAL:
            return switcher.transportProtocol();
        case TRANSPORT_SECURE_LITERAL:
            return switcher.transportSecure();
        case TRANSPORT_LOGIN_LITERAL:
            return switcher.transportLogin();
        case TRANSPORT_PASSWORD_LITERAL:
            return switcher.transportPassword();
        case UNIFIED_INBOX_ENABLED_LITERAL:
            return switcher.unifiedINBOXEnabled();
        default:
            throw new IllegalArgumentException(this.getName());
        }
    }

    public String getName() {
        return attrName;
    }

    public int getId() {
        return id;
    }

    private static Map<Integer, Attribute> byId = new HashMap<Integer, Attribute>();

    static {
        for (final Attribute attribute : Attribute.values()) {
            byId.put(Integer.valueOf(attribute.getId()), attribute);
        }
    }

    public static Attribute getById(final int col) {
        return byId.get(Integer.valueOf(col));
    }
}
