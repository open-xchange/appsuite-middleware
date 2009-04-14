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

package com.openexchange.mailaccount.servlet.fields;

import java.util.HashMap;
import java.util.Map;

/**
 * {@link MailAccountFields} - Provides constants for mail account JSON fields.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccountFields {

    public static final String ID = "id";

    public static final String LOGIN = "login";

    public static final String PASSWORD = "password";

    public static final String MAIL_URL = "mail_url";

    public static final String TRANSPORT_URL = "transport_url";

    public static final String NAME = "name";

    public static final String PRIMARY_ADDRESS = "primary_address";

    public static final String SPAM_HANDLER = "spam_handler";

    public static final String TRASH = "trash";

    public static final String SENT = "sent";

    public static final String DRAFTS = "drafts";

    public static final String SPAM = "spam";

    public static final String CONFIRMED_SPAM = "confirmed_spam";

    public static final String CONFIRMED_HAM = "confirmed_ham";
    
    public enum Attribute {
        ID_LITERAL(ID, 1001),
        LOGIN_LITERAL(LOGIN , 1002),
        PASSWORD_LITERAL(PASSWORD, 1003),
        MAIL_URL_LITERAL(MAIL_URL, 1004),
        TRANSPORT_URL_LITERAL(TRANSPORT_URL, 1005),
        NAME_LITERAL(NAME, 1006),
        PRIMARY_ADDRESS_LITERAL(PRIMARY_ADDRESS, 1007),
        SPAM_HANDLER_LITERAL(SPAM_HANDLER, 1008),
        TRASH_LITERAL(TRASH, 1009),
        SENT_LITERAL(SENT, 1010),
        DRAFTS_LITERAL(DRAFTS,1011),
        SPAM_LITERAL(SPAM, 1012),
        CONFIRMED_SPAM_LITERAL(CONFIRMED_SPAM, 1013),
        CONFIRMED_HAM_LITERAL(CONFIRMED_HAM, 1014);
        
        private int id;
        private String attrName;
        
        private Attribute(String name, int id) {
            this.attrName = name;
            this.id = id;
        }
        
        public Object doSwitch(AttributeSwitch switcher) {
            switch(this) {
            case ID_LITERAL : return switcher.id();
            case LOGIN_LITERAL : return switcher.login();
            case PASSWORD_LITERAL : return switcher.password();
            case MAIL_URL_LITERAL : return switcher.mailURL();
            case TRANSPORT_URL_LITERAL : return switcher.transportURL();
            case NAME_LITERAL : return switcher.name();
            case PRIMARY_ADDRESS_LITERAL : return switcher.primaryAddress();
            case SPAM_HANDLER_LITERAL : return switcher.spamHandler();
            case TRASH_LITERAL : return switcher.trash();
            case DRAFTS_LITERAL : return switcher.drafts();
            case SPAM_LITERAL : return switcher.spam();
            case SENT_LITERAL : return switcher.sent();
            case CONFIRMED_SPAM_LITERAL : return switcher.confirmedSpam();
            case CONFIRMED_HAM_LITERAL : return switcher.confirmedHam();
            default: throw new IllegalArgumentException(this.getName());
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
            for(Attribute attribute : Attribute.values()) {
                byId.put(attribute.getId(), attribute);
            }
        }
        
        public static Attribute getById(int col) {
            return byId.get(col);
        }
    }
}
