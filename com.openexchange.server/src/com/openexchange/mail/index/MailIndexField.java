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

package com.openexchange.mail.index;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import com.openexchange.index.IndexField;
import com.openexchange.mail.*;


/**
 * {@link MailIndexField}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public enum MailIndexField implements IndexField {
    
    UUID(null),
    TIMESTAMP(null),
    ACCOUNT(MailField.ACCOUNT_NAME),
    FULL_NAME(MailField.FOLDER_ID),
    ID(MailField.ID),
    COLOR_LABEL(MailField.COLOR_LABEL),
    ATTACHMENT(MailField.CONTENT_TYPE),
    RECEIVED_DATE(MailField.RECEIVED_DATE),
    SENT_DATE(MailField.SENT_DATE),
    SIZE(MailField.SIZE),
    FLAG_ANSWERED(MailField.FLAGS),
    FLAG_DELETED(MailField.FLAGS),
    FLAG_DRAFT(MailField.FLAGS),
    FLAG_FLAGGED(MailField.FLAGS),
    FLAG_RECENT(MailField.FLAGS),
    FLAG_SEEN(MailField.FLAGS),
    FLAG_USER(MailField.FLAGS),
    FLAG_SPAM(MailField.FLAGS),
    FLAG_FORWARDED(MailField.FLAGS),
    FLAG_READ_ACK(MailField.FLAGS),
    USER_FLAGS(MailField.FLAGS),
    FROM(MailField.FROM),
    TO(MailField.TO),
    CC(MailField.CC),
    BCC(MailField.BCC),
    SUBJECT(MailField.SUBJECT),
    CONTENT_FLAG(null),
    CONTENT(MailField.BODY);
    
    
    private static final Map<MailField, EnumSet<MailIndexField>> reverseMap = new EnumMap<MailField, EnumSet<MailIndexField>>(MailField.class);
        
    private final MailField mailField;
    
    static {
        for (final MailIndexField field : values()) {
            final MailField tmpMailField = field.getMailField();
            if (tmpMailField != null) {                
                EnumSet<MailIndexField> enumSet = reverseMap.get(tmpMailField);
                if (enumSet == null) {
                    enumSet = EnumSet.noneOf(MailIndexField.class);
                    reverseMap.put(tmpMailField, enumSet);
                }
                enumSet.add(field);                
            }
        }
    }
    
    public static Set<MailIndexField> getFor(final MailField[] mailFields) {
        final EnumSet<MailIndexField> indexFields = EnumSet.noneOf(MailIndexField.class);
        for (final MailField mailField : mailFields) {
            final EnumSet<MailIndexField> enumSet = reverseMap.get(mailField);
            if (enumSet != null) {
                indexFields.addAll(enumSet);
            }
        }
        
        return indexFields;
    }
    
    public static MailIndexField getFor(final MailField mailField) {
        final EnumSet<MailIndexField> enumSet = reverseMap.get(mailField);
        if (enumSet.size() > 0) {
            return enumSet.iterator().next();
        }
        
        return null;
    }
    
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
