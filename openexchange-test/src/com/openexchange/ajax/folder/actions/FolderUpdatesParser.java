
package com.openexchange.ajax.folder.actions;

import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.CommonUpdatesParser;
import com.openexchange.ajax.parser.FolderParser;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.fixtures.transformators.FolderModuleTransformator;

/**
 * {@link FolderUpdatesParser} - parses an updates-response, which does use a different format than other responses, so FolderParser cannot
 * be used directly.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class FolderUpdatesParser extends CommonUpdatesParser<FolderUpdatesResponse> {

    protected FolderUpdatesParser(final boolean failOnError, final int[] columns) {
        super(failOnError, columns);
    }

    @Override
    protected FolderUpdatesResponse createResponse(final Response response) throws JSONException {
        /*
         * Calling super.createResponse initiates the modified and deleted ids for the update response
         */
        final FolderUpdatesResponse folderUpdateResponse = super.createResponse(response);
        final JSONArray rows = (JSONArray) response.getData();
        if (rows == null) {
            return folderUpdateResponse;
        }
        for (int i = 0, size = rows.length(); i < size; i++) {
            final Object arrayOrId = rows.get(i);
            final FolderObject folder = new FolderObject();

            if (arrayOrId instanceof String) {
                //Deleted folders are already parsed by the CommonUpdatesParser
                continue;
            }
            if (!(arrayOrId instanceof JSONArray)) {
                continue;
            }
            final JSONArray row = (JSONArray) arrayOrId;

            for (int colIndex = 0; colIndex < getColumns().length; colIndex++) {
                for (int columnPos = 0; columnPos < getColumns().length; columnPos++) {
                    try {
                        Parser.parse(row.get(columnPos), getColumns()[columnPos], folder);
                    } catch (OXException e) {
                        throw new JSONException(e);
                    }
                }
            }
            folderUpdateResponse.addFolder(folder);
        }
        return folderUpdateResponse;
    }

    public boolean getsIgnored(final int column) {
        return column == DataObject.LAST_MODIFIED_UTC || column == FolderObject.OWN_RIGHTS // generated based on user
            || column == FolderObject.SUMMARY // Thorben said that one's mail-specific
            || column == FolderObject.STANDARD_FOLDER || column == FolderObject.TOTAL || column == FolderObject.NEW || column == FolderObject.UNREAD || column == FolderObject.DELETED || column == FolderObject.CAPABILITIES || column == FolderObject.SUBSCRIBED || column == FolderObject.SUBSCR_SUBFLDS || column > 1000;// big numbers are used by addons that might not be installed for core tests.
    }

    @Override
    protected FolderUpdatesResponse instantiateResponse(final Response response) {
        return new FolderUpdatesResponse(response);
    }

    public Object transform(final Object actual, final int column) throws JSONException {
        switch (column) {
            case DataObject.CREATION_DATE:
            case DataObject.LAST_MODIFIED:
                return new Date(((Long) actual).longValue());
            case FolderObject.MODULE:
                final FolderModuleTransformator trafo = new FolderModuleTransformator();
                try {
                    return trafo.transform((String) actual);
                } catch (final OXException e) {
                    e.printStackTrace();
                    return null; // TODO: Tierlieb: Change?
                }
            case FolderObject.PERMISSIONS_BITS:
                // like [{"group":true,"bits":4,"entity":0}]
                final FolderParser parser = new FolderParser();
                try {
                    return parser.parseOCLPermission((JSONArray) actual, null);
                } catch (final OXException e) {
                    e.printStackTrace();
                    return null; // TODO: Tierlieb: Change?
                }
            default:
                return actual;
        }

    }

}
