
package com.openexchange.file.storage.parse;

import java.util.List;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;

/**
 * {@link FileMetadataParserService} - The JSON parsing service for file storage.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface FileMetadataParserService {

    /**
     * Parses given JSON object to a file.
     *
     * @param object The JSON object
     * @return The parsed file
     * @throws OXException If parsing fails
     */
    public File parse(JSONObject object) throws OXException;

    /**
     * Gets all present fields from given JSON object.
     *
     * @param object The JSON object
     * @return The present fields
     */
    public List<Field> getFields(final JSONObject object);

}
