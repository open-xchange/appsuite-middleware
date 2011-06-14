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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.messaging;

import com.openexchange.messaging.MessagingHeader.KnownHeader;


/**
 * {@link MessagingMessageGetSwitch}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MessagingMessageGetSwitch implements MessagingMessageSwitcher {

    public Object accountName(final Object... args) {
        throw new UnsupportedOperationException(); // TODO
    }

    public Object bcc(final Object... args) throws MessagingException {
        return header(args[0], KnownHeader.BCC.toString());
    }

    public Object body(final Object... args) throws MessagingException {
        final MessagingMessage message = (MessagingMessage)args[0];
        return message.getContent();
    }

    public Object cc(final Object... args) throws MessagingException {
        return header(args[0], KnownHeader.CC.toString());
    }

    public Object colorLabel(final Object... args) throws MessagingException {
        return ((MessagingMessage)args[0]).getColorLabel();
    }

    public Object contentType(final Object... args) throws MessagingException {
        return header(args[0], KnownHeader.CONTENT_TYPE.toString()); 
    }

    public Object dispositionNotificationTo(final Object... args) throws MessagingException {
        return header(args[0], KnownHeader.DISPOSITION_NOTIFICATION_TO.toString());
    }

    public Object flags(final Object... args) throws MessagingException {
        return ((MessagingMessage)args[0]).getFlags();
    }

    public Object folderId(final Object... args) {
        return ((MessagingMessage)args[0]).getFolder();
    }

    public Object from(final Object... args) throws MessagingException {
        return header(args[0], KnownHeader.FROM.toString());
    }

    public Object full(final Object... args) {
        return args[0];
    }

    public Object headers(final Object... args) throws MessagingException {
        return ((MessagingMessage)args[0]).getHeaders();
    }

    public Object id(final Object... args) {
        return ((MessagingMessage)args[0]).getId();
    }

    public Object priority(final Object... args) throws MessagingException {
        return header(args[0], KnownHeader.PRIORITY.toString());
    }

    public Object receivedDate(final Object... args) {
        return Long.valueOf(((MessagingMessage)args[0]).getReceivedDate());
    }

    public Object sentDate(final Object... args) throws MessagingException {
        return header(args[0], KnownHeader.SENT_DATE.toString());
    }

    public Object size(final Object... args) throws MessagingException {
        return Long.valueOf(((MessagingMessage)args[0]).getSize());
    }

    public Object subject(final Object... args) throws MessagingException {
        return header(args[0], KnownHeader.SUBJECT.toString());
    }

    public Object threadLevel(final Object... args) {
        return ((MessagingMessage)args[0]).getThreadLevel();
    }

    public Object to(final Object... args) throws MessagingException {
        return header(args[0], KnownHeader.TO.toString());
    }

    private Object header(final Object object, final String header) throws MessagingException {
        return ((MessagingMessage)object).getHeader(header);
    }

    public Object picture(final Object... args) {
        return ((MessagingMessage)args[0]).getPicture();
    }
    
    public Object url(final Object... args) throws MessagingException {
        return ((MessagingMessage)args[0]).getUrl();
    }

}
