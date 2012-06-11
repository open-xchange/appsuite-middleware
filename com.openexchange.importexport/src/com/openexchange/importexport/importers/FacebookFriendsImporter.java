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
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.importexport.formats.Format;
import com.openexchange.importexport.osgi.ImportExportServices;
import com.openexchange.java.Strings;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;

public class FacebookFriendsImporter extends AbstractImporter {

	@Override
	protected String getNameForFieldInTruncationError(int id,
			OXException dataTruncation) {
		// TODO Auto-generated method stub
		return null;
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

        ContactInterface contactInterface;
		try {
			contactInterface = ImportExportServices.getContactInterfaceDiscoveryService().newContactInterface(fid, sessObj);
		} catch (OXException e1) {
			throw ImportExportExceptionCodes.CONTACT_INTERFACE_MISSING.create();
		}


		for(Contact c: contacts) {
			ImportResult res = null;
			try {
				contactInterface.insertContactObject(c);
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
