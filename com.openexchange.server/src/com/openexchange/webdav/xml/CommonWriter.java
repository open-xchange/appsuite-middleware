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

package com.openexchange.webdav.xml;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.jdom2.Element;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;

/**
 * CommonWriter
 *
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 */
public abstract class CommonWriter extends FolderChildWriter {

    protected void writeCommonElements(final CommonObject commonobject, final Element e_prop) throws OXException, SearchIteratorException, UnsupportedEncodingException {

        if (commonobject.containsParentFolderID() && commonobject.getParentFolderID() == 0) {
            addElement("personal_folder_id", commonobject.getParentFolderID(), e_prop);
            commonobject.setParentFolderID(-1);
        }

        if (commonobject.getNumberOfAttachments() > 0) {
            writeElementAttachments(commonobject, e_prop);
        }

        writeFolderChildElements(commonobject, e_prop);

        addElement("categories", commonobject.getCategories(), e_prop);
        addElement("private_flag", commonobject.getPrivateFlag(), e_prop);
    }

    protected void writeElementAttachments(final CommonObject commonobject, final Element e_prop) throws OXException, SearchIteratorException, UnsupportedEncodingException {
        final Element e_attachments = new Element("attachments", XmlServlet.NS);
        SearchIterator<AttachmentMetadata> it = null;
        try {
            XmlServlet.attachmentBase.startTransaction();
            final TimedResult<AttachmentMetadata> tResult = XmlServlet.attachmentBase.getAttachments(sessionObj, commonobject.getParentFolderID(),
                    commonobject.getObjectID(), getModule(), ctx, userObj,
                    UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessionObj.getUserId(),
                            ctx));

            it = tResult.results();

            while (it.hasNext()) {
                final AttachmentMetadata attachmentMeta = it.next();

                final Element e = new Element("attachment", XmlServlet.NS);

                String filename = attachmentMeta.getFilename();

                if (filename != null) {
                    filename = URLEncoder.encode(filename, "UTF-8");
                }

                e.addContent(correctCharacterData(filename));
                e.setAttribute("id", Integer.toString(attachmentMeta.getId()), XmlServlet.NS);
                e.setAttribute("last_modified", Long.toString(attachmentMeta.getCreationDate().getTime()), XmlServlet.NS);
                e.setAttribute("mimetype", attachmentMeta.getFileMIMEType(), XmlServlet.NS);
                e.setAttribute("rtf_flag", String.valueOf(attachmentMeta.getRtfFlag()), XmlServlet.NS);

                e_attachments.addContent(e);
            }
        } finally {
            if(it != null) {
                it.close();
            }

            XmlServlet.attachmentBase.commit();
            XmlServlet.attachmentBase.finish();
        }

        e_prop.addContent(e_attachments);
    }

    protected abstract int getModule();
}
