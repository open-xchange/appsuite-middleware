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

package com.openexchange.index.attachments;


/**
 * {@link AttachmentUUID}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class AttachmentUUID {

    private final String uuid;
    

    private AttachmentUUID(int module, String service, String account, String folder, String id, int attachmentNum) {
        super();
        StringBuilder tmp = new StringBuilder(64);
        tmp.append(module).append('/');
        if (service != null) {
            tmp.append(service).append('/');
        }
        if (account != null) {
            tmp.append(account).append('/');
        }
        tmp.append(folder).append('/').append(id).append('/').append(attachmentNum);
        uuid = tmp.toString();
    }

    public static AttachmentUUID newUUID(int module, String service, String account, String folder, String objectId, int attachmentId) {
        return new AttachmentUUID(module, service, account, folder, objectId, attachmentId);
    }

    public static AttachmentUUID newUUID(int module, String account, String folder, String objectId, int attachmentId) {
        return new AttachmentUUID(module, null, account, folder, objectId, attachmentId);
    }

    public static AttachmentUUID newUUID(int module, String folder, String objectId, int attachmentId) {
        return new AttachmentUUID(module, null, null, folder, objectId, attachmentId);
    }
    
    public static AttachmentUUID newUUID(Attachment attachment) {
        return new AttachmentUUID(attachment.getModule(), 
            attachment.getService(), 
            attachment.getAccount(),
            attachment.getFolder(),
            attachment.getObjectId(),
            attachment.getAttachmentId());
    }
    
    @Override
    public String toString() {        
        return uuid;
    }
}
