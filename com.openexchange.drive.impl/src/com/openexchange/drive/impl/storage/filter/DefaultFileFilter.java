/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.drive.impl.storage.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;

/**
 * {@link DefaultFileFilter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class DefaultFileFilter implements FileFilter {

    @Override
    public List<File> findAll(SearchIterator<File> searchIterator) throws OXException {
        List<File> files = new ArrayList<File>();
        if (null != searchIterator) {
            try {
                while (searchIterator.hasNext()) {
                    File file = searchIterator.next();
                    if (accept(file)) {
                        files.add(file);
                    }
                }
            } finally {
                SearchIterators.close(searchIterator);
            }
        }
        return files;
    }

    @Override
    public List<File> findAll(Collection<? extends File> collection) throws OXException {
        List<File> files = new ArrayList<File>();
        if (null != collection) {
            for (File file : collection) {
                if (accept(file)) {
                    files.add(file);
                }
            }
        }
        return files;
    }

    @Override
    public File find(SearchIterator<File> searchIterator) throws OXException {
        if (null != searchIterator) {
            try {
                while (searchIterator.hasNext()) {
                    File file = searchIterator.next();
                    if (accept(file)) {
                        return file;
                    }
                }
            } finally {
                SearchIterators.close(searchIterator);
            }
        }
        return null;
    }

    @Override
    public File find(Collection<? extends File> collection) throws OXException {
        if (null != collection) {
            for (File file : collection) {
                if (accept(file)) {
                    return file;
                }
            }
        }
        return null;
    }

}
