package com.openexchange.ajax.folder.actions;

import java.util.Date;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.CommonUpdatesParser;
import com.openexchange.ajax.parser.FolderParser;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.fixtures.FixtureException;
import com.openexchange.test.fixtures.transformators.FolderModuleTransformator;


/**
 * 
 * {@link FolderUpdatesParser} - parses an updates-response, which does use 
 * a different format than  other responses, so FolderParser cannot be used
 * directly.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 *
 */
public class FolderUpdatesParser extends CommonUpdatesParser<FolderUpdatesResponse> {

    protected FolderUpdatesParser(boolean failOnError, int[] columns) {
        super(failOnError, columns);

    }

    
    @Override
    protected FolderUpdatesResponse createResponse(Response response) throws JSONException {
        FolderUpdatesResponse folderUpdateResponse = super.createResponse(response);
        JSONArray rows = (JSONArray) response.getData();
        if (rows == null) {
            return folderUpdateResponse;
        }
        for (int i = 0, size = rows.length(); i < size; i++) {
            Object arrayOrId = rows.get(i);
            FolderObject folder = new FolderObject();
            
            if(!JSONArray.class.isInstance(arrayOrId)) {
                continue;
            }
            JSONArray row = (JSONArray) arrayOrId;

            for (int colIndex = 0; colIndex < getColumns().length; colIndex++) {
                Object value = row.get(colIndex);
                if (value == JSONObject.NULL) {
                    continue;
                }
                int column = getColumns()[colIndex];
                if (getsIgnored(column)) {
                    continue;
                }
                value = transform(value, column);
                folder.set(column, value);
            }
            folderUpdateResponse.addFolder(folder);
        }
        return folderUpdateResponse;
    }

    public boolean getsIgnored(int column){
        return 
           column == DataObject.LAST_MODIFIED_UTC
        || column == FolderObject.OWN_RIGHTS //generated based on user
        || column == FolderObject.SUMMARY //Thorben said that one's mail-specific
        || column  == FolderObject.STANDARD_FOLDER
        || column  == FolderObject.TOTAL
        || column  == FolderObject.NEW
        || column  == FolderObject.UNREAD
        || column  == FolderObject.DELETED
        || column  == FolderObject.CAPABILITIES
        || column  == FolderObject.SUBSCRIBED
        || column  == FolderObject.SUBSCR_SUBFLDS
        ;
    }
    
    @Override
    protected FolderUpdatesResponse instanciateResponse(Response response) {
        return new FolderUpdatesResponse(response);
    }


    public Object transform(Object actual, int column) throws JSONException {
        switch (column) {
        case DataObject.CREATION_DATE:
        case DataObject.LAST_MODIFIED:
            return new Date( ( (Long) actual).longValue() );
        case FolderObject.MODULE:
            FolderModuleTransformator trafo = new FolderModuleTransformator();
            try {
                    return trafo.transform((String) actual);
                } catch (FixtureException e) {
                    e.printStackTrace();
                    return null; //TODO: Tierlieb: Change?
                }
        case FolderObject.PERMISSIONS_BITS:
            // like [{"group":true,"bits":4,"entity":0}]
            FolderParser parser = new FolderParser();
            try {
                    return parser.parseOCLPermission((JSONArray) actual, null);
                } catch (OXException e) {
                    e.printStackTrace();
                    return null; //TODO: Tierlieb: Change?
                }
        default:
            return actual;
        }

    }
    
}
