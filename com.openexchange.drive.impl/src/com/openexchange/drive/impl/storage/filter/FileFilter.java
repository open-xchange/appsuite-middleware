package com.openexchange.drive.impl.storage.filter;

import java.util.Collection;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.tools.iterator.SearchIterator;

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

    /**
     * Finds all files from the supplied search iterator that are accepted by this filter.
     *
     * @param searchIterator The search iterator to browse
     * @return The accepted files, or an empty list of there are none
     * @throws OXException
     */
    List<File> findAll(SearchIterator<File> searchIterator) throws OXException;

    /**
     * Finds all files from the supplied collection that are accepted by this filter.
     *
     * @param collection The collection to browse
     * @return The accepted files, or an empty list of there are none
     * @throws OXException
     */
    List<File> findAll(Collection<? extends File> collection) throws OXException;

    /**
     * Finds the first file from the supplied search iterator that is accepted by this filter.
     *
     * @param searchIterator The search iterator to browse
     * @return The first accepted file, or <code>null</code> if none was found
     * @throws OXException
     */
    File find(SearchIterator<File> searchIterator) throws OXException;

    /**
     * Finds the first file from the supplied collection that is accepted by this filter.
     *
     * @param collection The collection to browse
     * @return The first accepted file, or <code>null</code> if none was found
     * @throws OXException
     */
    File find(Collection<? extends File> collection) throws OXException;

}