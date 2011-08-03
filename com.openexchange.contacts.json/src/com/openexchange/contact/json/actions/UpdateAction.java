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

package com.openexchange.contact.json.actions;

import java.util.Date;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.contact.json.ContactRequest;
import com.openexchange.contact.json.RequestTools;
import com.openexchange.contact.json.converters.ContactParser;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link UpdateAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class UpdateAction extends ContactAction {

    /**
     * Initializes a new {@link UpdateAction}.
     * @param serviceLookup
     */
    public UpdateAction(ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    protected AJAXRequestResult perform(ContactRequest req) throws OXException {
        ServerSession session = req.getSession();
        int folder = req.getFolder();
        int id = req.getId();
        long timestamp = req.getTimestamp();
        Date date = new Date(timestamp);
        boolean containsImage = req.containsImage();
        JSONObject json = req.getContactJSON(containsImage);
        
        ContactParser parser = new ContactParser();
        Contact contact = parser.parse(json);
        contact.setObjectID(id);
        
        if (containsImage) {
            UploadEvent uploadEvent = null;               
            try {
                uploadEvent = req.getUploadEvent();
                UploadFile file = uploadEvent.getUploadFileByFieldName("file");
                if (file == null) {
                    throw AjaxExceptionCodes.NoUploadImage.create();
                }
                
                RequestTools.setImageData(contact, file);
            } finally {
                if (uploadEvent != null) {
                    uploadEvent.cleanUp();
                }
            }
        }
        
        ContactInterface contactInterface = getContactInterfaceDiscoveryService().newContactInterface(folder, session);
        contactInterface.updateContactObject(contact, folder, date);
        
        JSONObject response = new JSONObject();
        return new AJAXRequestResult(response, contact.getLastModified(), "json");
    }

}
