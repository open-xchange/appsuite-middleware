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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.zmal.dataobjects;

import java.io.InputStream;
import java.util.List;
import javax.activation.DataHandler;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;


/**
 * {@link ZmalMailMessage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ZmalMailMessage extends MailMessage {

    private String mailId;
    private int unreadCount;
    private Object content;
    private DataHandler dataHandler;
    private InputStream inputStream;
    private final int count;
    private List<MailPart> parts;

    /**
     * Initializes a new {@link ZmalMailMessage}.
     */
    public ZmalMailMessage() {
        super();
        count = MailMessage.NO_ENCLOSED_PARTS;
    }

    @Override
    public String getMailId() {
        return mailId;
    }

    @Override
    public void setMailId(String id) {
        this.mailId = id;
    }

    @Override
    public int getUnreadMessages() {
        return unreadCount;
    }

    @Override
    public void setUnreadMessages(int unreadMessages) {
        this.unreadCount = unreadMessages;
    }

    @Override
    public Object getContent() throws OXException {
        return content;
    }

    @Override
    public DataHandler getDataHandler() throws OXException {
        return dataHandler;
    }

    @Override
    public InputStream getInputStream() throws OXException {
        return inputStream;
    }

    @Override
    public int getEnclosedCount() throws OXException {
        return count;
    }

    @Override
    public MailPart getEnclosedMailPart(int index) throws OXException {
        return parts.get(index);
    }

    @Override
    public void loadContent() throws OXException {
        // Not applicable
    }

    @Override
    public void prepareForCaching() {
        // Not applicable
    }

}
