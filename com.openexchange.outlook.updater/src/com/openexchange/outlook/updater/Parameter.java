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

package com.openexchange.outlook.updater;

import java.util.HashMap;
import java.util.Map;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.smtp.config.MailAccountSMTPProperties;
import com.openexchange.tools.session.ServerSession;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public enum Parameter {

    CREATE_IMAP_ACCOUNT("OXCreateIMAPAccount"),
    ACCOUNT_NAME("OXAccountname"),
    DISPLAY_NAME("OXDisplayname"),
    EMAIL_ADDRESS("OXEmailAddress"),
    IMAP_PORT("OXIMAPPort"),
    IMAP_SERVER("OXIMAPServer"),
    IMAP_SSL("OXIMAPSSL"),
    IMAP_USER_NAME("OXIMAPUsername"),
    PROFILE_NAME("OXProfilename"),
    SERVER_TIMEOUT("OXServertimeout"),
    SMTP_AUTH("OXSMTPAuth"),
    SMTP_AUTH_METHOD("OXSMTPAuthMethod"),
    SMTP_PORT("OXSMTPPort"),
    SMTP_SERVER("OXSMTPServer"),
    SMTP_SSL("OXSMTPSSL");

    private String keyword;
    
    private static Map<String, Parameter> keyword2Param = new HashMap<String, Parameter>();

    static {
        for (Parameter param : Parameter.values()) {
            keyword2Param.put(param.getKeyword(), param);
        }
    }
    
    private Parameter(String keyword) {
        this.keyword = keyword;
    }
    
    public String getKeyword() {
        return keyword;
    }
    
    public String getValue(ServerSession session, MailAccount account) {
        MailAccountSMTPProperties props = new MailAccountSMTPProperties(account);
        
        switch (this) {
        case CREATE_IMAP_ACCOUNT:
            return Boolean.toString(true); //TODO:
        case ACCOUNT_NAME:
            return account.getName();
        case DISPLAY_NAME:
            return session.getUser().getDisplayName();
        case EMAIL_ADDRESS:
            return account.getPrimaryAddress();
        case IMAP_PORT:
            return Integer.toString(account.getMailPort());
        case IMAP_SERVER:
            return account.getTransportServer();
        case IMAP_SSL:
            return Boolean.toString(account.isMailSecure());
        case IMAP_USER_NAME:
            return account.getLogin();
        case PROFILE_NAME:
            account.getName();
        case SERVER_TIMEOUT:
            return Integer.toString(props.getSmtpTimeout());
        case SMTP_AUTH:
            return Boolean.toString(props.isSmtpAuth());
        case SMTP_AUTH_METHOD:
            return Integer.toString(AuthMethod.SMTP.getValue()); //TODO:
        case SMTP_PORT:
            return Integer.toString(account.getTransportPort());
        case SMTP_SERVER:
            return account.getTransportServer();
        case SMTP_SSL:
            return Boolean.toString(account.isTransportSecure());

        default:
            throw new IllegalArgumentException(this.name());
        }
    }
    
    private enum AuthMethod {
        POP3(1),
        SMTP(2),
        POP_BEFORE_SMTP(3);
        
        private int value;

        private AuthMethod(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return this.value;
        }
    }
}
