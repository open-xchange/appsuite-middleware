package com.openexchange.importexport.importers;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import com.openexchange.exception.OXException;
import com.openexchange.importexport.ImportResult;
import com.openexchange.importexport.formats.Format;
import com.openexchange.tools.session.ServerSession;

public class FacebookArchiveImporter extends AbstractImporter {

	protected FacebookFriendsImporter delegate = new FacebookFriendsImporter();

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
		return format == Format.FacebookArchive;
	}

	@Override
    public List<ImportResult> importData(ServerSession sessObj, Format format,
			InputStream is, List<String> folders,
			Map<String, String[]> optionalParams) throws OXException {


		List<ImportResult> results = new LinkedList<ImportResult>();
        try {
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry entry;


            while ((entry = zis.getNextEntry()) != null) {
            	if(! entry.getName().endsWith("/friends.html")){
            		continue;
            	}

                results = delegate.importData(sessObj, format, zis, folders, optionalParams);
            }

            zis.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

		return results;
	}

}
