/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
