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

    ACCOUNT_NAME("OXACCOUNTNAME"),
    DISPLAY_NAME("OXDISPLAYNAME"),
    EMAIL_ADDRESS("OXEMAILADDRESS"),
    IMAP_PORT("OXIMAPPORT"),
    IMAP_SERVER("OXIMAPSERVER"),
    IMAP_SSL("OXIMAPSSL"),
    IMAP_USER_NAME("OXIMAPUSERNAME"),
    SERVER_TIMEOUT("OXSERVERTIMEOUT"),
    SMTP_AUTH("OXSMTPAUTH"),
    SMTP_AUTH_METHOD("OXSMTPAUTHMETHOD"),
    SMTP_PORT("OXSMTPPORT"),
    SMTP_SERVER("OXSMTPSERVER"),
    SMTP_SSL("OXSMTPSSL"),
    URL("OXURL");

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
    
    public String getValue(String serverURL, ServerSession session, MailAccount account) {
        MailAccountSMTPProperties props = new MailAccountSMTPProperties(account);
        
        switch (this) {
        case ACCOUNT_NAME:
            return quote(account.getName());
        case DISPLAY_NAME:
            return quote(session.getUser().getDisplayName());
        case EMAIL_ADDRESS:
            return quote(account.getPrimaryAddress());
        case IMAP_PORT:
            return quote(Integer.toString(account.getMailPort()));
        case IMAP_SERVER:
            return quote(account.getTransportServer());
        case IMAP_SSL:
            return b2i(account.isMailSecure());
        case IMAP_USER_NAME:
            return quote(account.getLogin());
        case SERVER_TIMEOUT:
            return quote(Integer.toString(props.getSmtpTimeout()));
        case SMTP_AUTH:
            return b2i(props.isSmtpAuth());
        case SMTP_AUTH_METHOD:
            return quote(Integer.toString(AuthMethod.SMTP.getValue()));
        case SMTP_PORT:
            return quote(Integer.toString(account.getTransportPort()));
        case SMTP_SERVER:
            return quote(account.getTransportServer());
        case SMTP_SSL:
            return b2i(account.isTransportSecure());
        case URL:
            return quote(serverURL);

        default:
            throw new IllegalArgumentException(this.name());
        }
    }
    
    private String quote(String value) {
        return "\"" + value + "\"";
    }
    
    private String b2i(boolean value) {
        return quote(value ? "1" : "0");
    }
    
    private enum AuthMethod {
        POP3(0),
        SMTP(1),
        POP_BEFORE_SMTP(1);
        
        private int value;

        private AuthMethod(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return this.value;
        }
    }
}
