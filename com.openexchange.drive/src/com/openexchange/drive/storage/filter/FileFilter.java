package com.openexchange.drive.storage.filter;

import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;

/**
 * {@link StorageFileFilter}
 *
 * Filter for file storage files.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface FileFilter {

    /**
     * Tests whether or not the specified file should be accepted as result or not.
     *
     * @param file The file to check
     * @return <code>true</code> if the file is accepted, <code>false</code>, otherwise
     * @throws OXException
     */
    boolean accept(File file) throws OXException;

}