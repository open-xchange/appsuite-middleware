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

package com.openexchange.mail.structure;

import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.uuencode.UUEncodedPart;

/**
 * {@link StructureHandler} - Structure handler.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface StructureHandler {

    boolean handleAttachment(MailPart part, String id) throws OXException;

    boolean handleHeaders(Iterator<Entry<String, String>> iter) throws OXException;

    boolean handleInlineUUEncodedAttachment(UUEncodedPart part, String id) throws OXException;

    boolean handleInlineUUEncodedPlainText(String decodedTextContent, ContentType contentType, int size, String fileName, String id) throws OXException;

    boolean handleSMIMEBodyText(MailPart part) throws OXException;

    boolean handleSMIMEBodyData(byte[] data) throws OXException;

    boolean handleMultipartStart(ContentType contentType, int bodyPartCount, String id) throws OXException;

    boolean handleMultipartEnd() throws OXException;

    boolean handleNestedMessage(MailPart mailPart, String id) throws OXException;

    boolean handleReceivedDate(Date receivedDate) throws OXException;

    boolean handleSystemFlags(int flags) throws OXException;

    boolean handleUserFlags(String[] userFlags) throws OXException;

    boolean handleColorLabel(int colorLabel) throws OXException;

    boolean handleEnd(MailMessage mail) throws OXException;
}
