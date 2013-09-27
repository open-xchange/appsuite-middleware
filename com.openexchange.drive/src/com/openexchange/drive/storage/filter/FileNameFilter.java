package com.openexchange.drive.storage.filter;

import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;

/**
 * {@link FileNameFilter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class FileNameFilter implements FileFilter {

    /**
     * Tests whether or not the specified file should be accepted as result or not.
     *
     * @param file The file to check
     * @return <code>true</code> if the file is accepted, <code>false</code>, otherwise
     * @throws OXException
     */
    protected abstract boolean accept(String fileName) throws OXException;

    @Override
    public boolean accept(File file) throws OXException {
        return null != file && accept(file.getFileName());
    }

}