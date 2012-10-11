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
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.importexport.formats.Format;
import com.openexchange.tools.session.ServerSession;

public class FacebookArchiveImporter extends AbstractImporter {

	protected FacebookFriendsImporter delegate = new FacebookFriendsImporter();

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
		return format == Format.FacebookArchive;
	}

	@Override
    public List<ImportResult> importData(final ServerSession sessObj, final Format format,
			final InputStream is, final List<String> folders,
			final Map<String, String[]> optionalParams) throws OXException {


		List<ImportResult> results = new LinkedList<ImportResult>();
        try {
            final ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry entry;


            while ((entry = zis.getNextEntry()) != null) {
            	if(! entry.getName().endsWith("/friends.html")){
            		continue;
            	}

                results = delegate.importData(sessObj, format, zis, folders, optionalParams);
            }

            zis.close();
            is.close();
        } catch (final IOException e) {
            final org.apache.commons.logging.Log log = com.openexchange.log.LogFactory.getLog(FacebookArchiveImporter.class);
            log.error("Unexpected exception.", e);
        }

		return results;
	}

}
