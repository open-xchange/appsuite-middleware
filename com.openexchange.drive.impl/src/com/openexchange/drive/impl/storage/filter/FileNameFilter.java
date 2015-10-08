package com.openexchange.drive.impl.storage.filter;

import com.openexchange.drive.impl.internal.PathNormalizer;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.java.Strings;

/**
 * {@link FileNameFilter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class FileNameFilter extends DefaultFileFilter {

    /**
     * Filter that accepts all files having a non-empty filename.
     */
    public static final FileNameFilter ACCEPT_ALL = new FileNameFilter() {

        @Override
        protected boolean accept(String fileName) throws OXException {
            return false == Strings.isEmpty(fileName);
        }
    };

    /**
     * Creates a new filter to accept files matching the supplied name.
     *
     * @param name The filename to accept
     * @param normalizeFileNames <code>true</code> to consider normalized matching, <code>false</code>, otherwise
     * @return
     */
    public static FileNameFilter byName(final String name, final boolean normalizeFileNames) {
        return new FileNameFilter() {

            @Override
            protected boolean accept(String fileName) throws OXException {
                return name.equals(fileName) || normalizeFileNames && PathNormalizer.equals(name, fileName);
            }
        };
    }

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
        return null != file && null != file.getFileName() && accept(file.getFileName());
    }

}