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

package com.openexchange.chronos.itip.sender;

import java.util.Map;
import com.openexchange.groupware.container.mail.MailObject;
import com.openexchange.session.Session;

/**
 * {@link ITipMailObject} - Workaround to satisfy logic in {@link DefaultMailSenderService}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
class ITipMailObject extends MailObject {

    /**
     * See {@link MailObject#MailObject(Session, String, String, int, String)}
     */
    public ITipMailObject(Session session, String objectId, String folderId, int module, String type) {
        super(session, objectId, folderId, module, type);
    }

    /**
     * See {@link MailObject#MailObject(Session, int, int, int, String)}
     */
    public ITipMailObject(Session session, int objectId, int folderId, int module, String type) {
        super(session, objectId, folderId, module, type);
    }

    /**
     * Sets given additional header.
     * <p>
     * Note: Header name is <b>not</b> required to start with <code>"X-"</code> prefix. Use with caution!
     *
     * @param name The header name
     * @param value The header value
     * @throws IllegalArgumentException If name/value is <code>null</code>
     * @see MailObject#setAdditionalHeader(String, String)
     */
    @Override
    public void setAdditionalHeader(String name, String value) {
        if (null == name) {
            throw new IllegalArgumentException("name is null");
        }
        if (null == value) {
            throw new IllegalArgumentException("value is null");
        }
        additionalHeaders.put(name, value);
    }

    /**
     * Sets given additional header.
     * <p>
     * Note: Header name is <b>not</b> required to start with <code>"X-"</code> prefix. Use with caution!
     * 
     * @param headers The headers
     * @see MailObject#setAdditionalHeaders(Map)
     */
    @Override
    public void setAdditionalHeaders(Map<? extends String, ? extends String> headers) {
        additionalHeaders.putAll(headers);
    }

}
