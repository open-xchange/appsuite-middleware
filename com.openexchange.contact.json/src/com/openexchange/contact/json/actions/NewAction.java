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

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;


/**
 * {@link NewAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class NewAction extends ContactAction {

    /**
     * Initializes a new {@link NewAction}.
     * @param serviceLookup
     */
    public NewAction(ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    protected AJAXRequestResult perform(ContactRequest req) throws OXException {
        ServerSession session = req.getSession();
        boolean containsImage = req.containsImage();
        JSONObject json = req.getContactJSON(containsImage);
        if (!json.has("folder_id")) {
            throw OXException.mandatoryField("missing folder");
        }
                
        try {
            int folder = json.getInt("folder_id");
            ContactInterface contactInterface = getContactInterfaceDiscoveryService().newContactInterface(folder, session);
            ContactParser parser = new ContactParser();
            Contact contact = parser.parse(json);
            if (containsImage) {              
                UploadEvent uploadEvent = null;               
                try {
                    uploadEvent = req.getUploadEvent();
                    UploadFile file = uploadEvent.getUploadFileByFieldName("file");
                    if (file == null) {
                        throw AjaxExceptionCodes.NoUploadImage.create();
                    }
                    
                    setImageData(contact, file);
                } finally {
                    if (uploadEvent != null) {
                        uploadEvent.cleanUp();
                    }
                }
                
            }
            
            contactInterface.insertContactObject(contact);
            JSONObject object = new JSONObject("{\"id\":" + contact.getObjectID() + "}");
            return new AJAXRequestResult(object, contact.getLastModified(), "json");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }        
    }
    
    private void setImageData(Contact contact, UploadFile file) throws OXException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file.getTmpFile());
            ByteArrayOutputStream tmp = new UnsynchronizedByteArrayOutputStream((int) file.getSize());
            final byte[] buf = new byte[2048];
            int len = -1;
            while ((len = fis.read(buf)) != -1) {
                tmp.write(buf, 0, len);
            }
            contact.setImage1(tmp.toByteArray());
            contact.setImageContentType(file.getContentType());
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }        
    }
}
