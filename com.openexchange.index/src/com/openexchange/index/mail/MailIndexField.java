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

package com.openexchange.index.mail;

import com.openexchange.index.IndexField;
import com.openexchange.mail.MailField;


/**
 * {@link MailIndexField}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public enum MailIndexField implements IndexField {
    
    UUID(null),
    TIMESTAMP(null),
    CONTEXT(null),
    USER(null),
    ACCOUNT(MailField.ACCOUNT_NAME),
    FULL_NAME(MailField.FOLDER_ID),
    ID(MailField.ID),
    COLOR_LABEL(MailField.COLOR_LABEL),
    ATTACHMENT(MailField.CONTENT_TYPE),
    RECEIVED_DATE(MailField.RECEIVED_DATE),
    SENT_DATE(MailField.SENT_DATE),
    SIZE(MailField.SIZE),
    FLAG_ANSWERED(null),
    FLAG_DELETED(null),
    FLAG_DRAFT(null),
    FLAG_FLAGGED(null),
    FLAG_RECENT(null),
    FLAG_SEEN(null),
    FLAG_USER(null),
    FLAG_SPAM(null),
    FLAG_FORWARDED(null),
    FLAG_READ_ACK(null),
    USER_FLAGS(null),
    FROM(MailField.FROM),
    SENDER(null),
    TO(MailField.TO),
    CC(MailField.CC),
    BCC(MailField.BCC),
    SUBJECT(MailField.SUBJECT),
    CONTENT_FLAG(null),
    CONTENT(MailField.BODY);
    
    
    private final MailField mailField;
    
    
    private MailIndexField(final MailField mailField) {
        this.mailField = mailField;
    }
    
    public boolean hasMailField() {
        return mailField != null;
    }
    
    public MailField getMailField() {
        return mailField;
    }

}
