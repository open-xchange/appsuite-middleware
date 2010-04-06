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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.openexchange.mail.MailException;
import com.openexchange.messaging.ContentType;
import com.openexchange.messaging.MessagingException;


/**
 * {@link MailContentType}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailContentType implements ContentType {

    private final com.openexchange.mail.mime.ContentType contentType;

    /**
     * Initializes a new {@link MailContentType}.
     * 
     * @param contentType The mail content type
     */
    public MailContentType(com.openexchange.mail.mime.ContentType contentType) {
        super();
        if (null == contentType) {
            throw new IllegalArgumentException("Content type is null.");
        }
        this.contentType = contentType;
    }

    public boolean containsCharsetParameter() {
        return contentType.containsCharsetParameter();
    }

    public boolean containsNameParameter() {
        return contentType.containsNameParameter();
    }

    public String getBaseType() {
        return contentType.getBaseType();
    }

    public String getCharsetParameter() {
        return contentType.getCharsetParameter();
    }

    public String getNameParameter() {
        return contentType.getNameParameter();
    }

    public String getPrimaryType() {
        return contentType.getPrimaryType();
    }

    public String getSubType() {
        return contentType.getSubType();
    }

    public boolean isMimeType(String pattern) {
        return contentType.isMimeType(pattern);
    }

    public void setBaseType(String baseType) throws MessagingException {
        try {
            contentType.setBaseType(baseType);
        } catch (MailException e) {
            throw new MessagingException(e);
        }
    }

    public void setCharsetParameter(String charset) {
        contentType.setCharsetParameter(charset);
    }

    public void setContentType(String contentType) throws MessagingException {
        try {
            this.contentType.setContentType(contentType);
        } catch (MailException e) {
            throw new MessagingException(e);
        }
    }

    public void setContentType(ContentType contentType) {
        com.openexchange.mail.mime.ContentType thisObj = this.contentType;
        thisObj.setPrimaryType(contentType.getPrimaryType());
        thisObj.setSubType(contentType.getSubType());
        /*
         * Drop parameters. Store names in separate list to avoid ConcurrentModificationException
         */
        final List<String> names = new ArrayList<String>();
        for (Iterator<String> iter = thisObj.getParameterNames(); iter.hasNext();) {
            names.add(iter.next());
        }
        for (String name : names) {
            thisObj.removeParameter(name);
        }
        /*
         * Replace with other ones
         */
        for (Iterator<String> iter = contentType.getParameterNames(); iter.hasNext();) {
            final String name = iter.next();
            thisObj.setParameter(name, contentType.getParameter(name));
        }
    }

    public void setNameParameter(String filename) {
        contentType.setNameParameter(filename);
    }

    public void setPrimaryType(String primaryType) {
        contentType.setPrimaryType(primaryType);
    }

    public void setSubType(String subType) {
        contentType.setSubType(subType);
    }

    public boolean startsWith(String prefix) {
        return contentType.startsWith(prefix);
    }

    public void addParameter(String key, String value) {
        contentType.addParameter(key, value);
    }

    public boolean containsParameter(String key) {
        return contentType.containsParameter(key);
    }

    public String getParameter(String key) {
        return contentType.getParameter(key);
    }

    public Iterator<String> getParameterNames() {
        return contentType.getParameterNames();
    }

    public String removeParameter(String key) {
        return contentType.removeParameter(key);
    }

    public void setParameter(String key, String value) {
        contentType.setParameter(key, value);
    }

    public String getName() {
        return "Content-Type";
    }

    public String getValue() {
        return contentType.toString();
    }

}
