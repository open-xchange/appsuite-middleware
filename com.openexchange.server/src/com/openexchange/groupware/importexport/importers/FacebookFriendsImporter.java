package com.openexchange.groupware.importexport.importers;

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
import com.openexchange.groupware.contact.ContactInterfaceDiscoveryService;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.importexport.AbstractImporter;
import com.openexchange.groupware.importexport.Format;
import com.openexchange.groupware.importexport.ImportExportExceptionCodes;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.java.Strings;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;

public class FacebookFriendsImporter extends AbstractImporter {

	@Override
	protected String getNameForFieldInTruncationError(final int id,
			final OXException dataTruncation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    public boolean canImport(final ServerSession sessObj, final Format format,
			final List<String> folders, final Map<String, String[]> optionalParams)
			throws OXException {
		return Format.FacebookFriends == format;
	}

	@Override
    public List<ImportResult> importData(final ServerSession sessObj, final Format format,
			final InputStream is, final List<String> folders,
			final Map<String, String[]> optionalParams) throws OXException {

		final int fid = checkAndGetFolder(folders, sessObj);
		final Scanner scanner = new Scanner(is, "UTF-8");
		final StringBuilder html = new StringBuilder();
		while(scanner.hasNextLine()){
			html.append(scanner.nextLine());
		}
		final Pattern p= Pattern.compile("<div\\s+class\\s?=\\s?[\"']friend[\"']\\s?>\\s?<span\\s+class\\s?=\\s?[\"']profile[\"']\\s?>(.+?)</span\\s?>\\s?</div\\s?>");
		final Matcher m = p.matcher(html);

        final List<ImportResult> list = new ArrayList<ImportResult>();

		final List<Contact> contacts = new LinkedList<Contact>();

		while(m.find()){
			final String displayName = m.group(1);
			final Contact c = new Contact();
			c.setDisplayName(displayName);
			c.setParentFolderID(fid);
			contacts.add(c);
		}

        ContactInterface contactInterface;
		try {
			contactInterface = ServerServiceRegistry.getInstance().getService(
			        ContactInterfaceDiscoveryService.class).newContactInterface(fid, sessObj);
		} catch (final OXException e1) {
			throw ImportExportExceptionCodes.CONTACT_INTERFACE_MISSING.create();
		}


		for(final Contact c: contacts) {
			ImportResult res = null;
			try {
				contactInterface.insertContactObject(c);
				res = new ImportResult(
				        Integer.toString(fid),
				        Integer.toString(c.getObjectID()),
						new Date());
			} catch (final OXException e) {
				res = new ImportResult();
				res.setException(ImportExportExceptionCodes.COULD_NOT_WRITE.create());
				res.setDate(new Date());
			}
			list.add(res);
		}

		return list;
	}

	private int checkAndGetFolder(final List<String> folders, final ServerSession session) throws OXException {

        final OXFolderAccess folderAccess = new OXFolderAccess(session.getContext());

        for (final String fid : folders) {

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
