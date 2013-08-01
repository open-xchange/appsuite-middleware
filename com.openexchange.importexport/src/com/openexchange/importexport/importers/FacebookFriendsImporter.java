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

package com.openexchange.importexport.importers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.contact.ContactService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.importexport.formats.Format;
import com.openexchange.importexport.osgi.ImportExportServices;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;

public class FacebookFriendsImporter extends ContactImporter {


    public FacebookFriendsImporter(ServiceLookup services) {
        super(services);
    }

    @Override
    public boolean canImport(ServerSession sessObj, Format format,
			List<String> folders, Map<String, String[]> optionalParams)
			throws OXException {
		return Format.FacebookFriends == format;
	}

	@Override
    public List<ImportResult> importData(ServerSession sessObj, Format format,
			InputStream is, List<String> folders,
			Map<String, String[]> optionalParams) throws OXException {

		int fid = checkAndGetFolder(folders, sessObj);
		Scanner scanner = new Scanner(is, "UTF-8");
		StringBuilder html = new StringBuilder();
		while(scanner.hasNextLine()){
			html.append(scanner.nextLine());
		}
		Pattern p= Pattern.compile("<div\\s+class\\s?=\\s?[\"']friend[\"']\\s?>\\s?<span\\s+class\\s?=\\s?[\"']profile[\"']\\s?>(.+?)</span\\s?>\\s?</div\\s?>");
		Matcher m = p.matcher(html);

        final List<ImportResult> list = new ArrayList<ImportResult>();

		List<Contact> contacts = new LinkedList<Contact>();

		while(m.find()){
			String displayName = m.group(1);
			Contact c = new Contact();
			c.setDisplayName(displayName);
			c.setParentFolderID(fid);
			contacts.add(c);
		}

		ContactService contactService = ImportExportServices.getContactService();
		if (null == contactService) {
			throw ImportExportExceptionCodes.CONTACT_INTERFACE_MISSING.create();
		}

		for(Contact c: contacts) {
			ImportResult res = null;
			try {
			    super.createContact(sessObj, c, Integer.toString(fid));
				res = new ImportResult(
						String.valueOf(fid),
						String.valueOf(c.getObjectID()),
						new Date());
			} catch (OXException e) {
				res = new ImportResult();
				res.setException(ImportExportExceptionCodes.COULD_NOT_WRITE.create());
				res.setDate(new Date());
			}
			list.add(res);
		}

		return list;
	}

	private int checkAndGetFolder(List<String> folders, ServerSession session) throws OXException {

        final OXFolderAccess folderAccess = new OXFolderAccess(session.getContext());

        for (String fid : folders) {

            final int folderId = Integer.parseInt(fid);
            FolderObject fo;
            try {
                fo = folderAccess.getFolderObject(folderId);
            } catch (final OXException e) {
                throw ImportExportExceptionCodes.LOADING_FOLDER_FAILED.create(fid);
            }

            if (fo.getModule() == FolderObject.CONTACT) {
                return folderId;
            }
        }
        throw ImportExportExceptionCodes.LOADING_FOLDER_FAILED.create(Strings.join(folders, ","));
	}

}
