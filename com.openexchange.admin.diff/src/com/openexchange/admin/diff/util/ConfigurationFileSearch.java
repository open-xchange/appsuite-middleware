package com.openexchange.admin.diff.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.openexchange.admin.diff.file.domain.ConfigurationFile;

/**
 * Class that searches for a file within a list based on a given name.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public class ConfigurationFileSearch {

    /**
     * Search within a list of ConfigurationFiles to find the file with the given file name
     *
     * @param lInstalledFiles - List to search within
     * @param fileName - file name to search for
     * @return List<ConfigurationFile> with the search results
     */
    public List<ConfigurationFile> search(List<ConfigurationFile> lInstalledFiles, final String fileName) {
        return searchIn(lInstalledFiles, new Matcher<ConfigurationFile>() {

            @Override
            public boolean matches(ConfigurationFile p) {
                return p.getName().equals(fileName);
            }
        });
    }

    private static <T> List<T> searchIn(List<T> list, Matcher<T> m) {
        List<T> r = Collections.synchronizedList(new ArrayList<T>());
        for (T t : list) {
            if (m.matches(t)) {
                r.add(t);
            }
        }
        return r;
    }

    private interface Matcher<T> {

        public boolean matches(T t);
    }
}
