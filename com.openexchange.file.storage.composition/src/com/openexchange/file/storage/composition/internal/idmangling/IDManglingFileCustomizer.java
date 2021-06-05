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

package com.openexchange.file.storage.composition.internal.idmangling;

import java.io.InputStream;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.Document;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.UserizedFile;
import com.openexchange.groupware.results.CustomizableDelta;
import com.openexchange.groupware.results.CustomizableTimedResult;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.tools.iterator.CustomizableSearchIterator;
import com.openexchange.tools.iterator.Customizer;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * {@link IDManglingFileCustomizer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class IDManglingFileCustomizer implements Customizer<File> {

    private final String service;
    private final String account;

    public IDManglingFileCustomizer(final String service, final String account) {
        super();
        this.service = service;
        this.account = account;
    }

    @Override
    public File customize(final File thing) throws OXException {
        return fixIDs(thing, service, account);
    }

    public static SearchIterator<File> fixIDs(final SearchIterator<File> iterator, final String service, final String account) {
        return new CustomizableSearchIterator<File>(iterator, new IDManglingFileCustomizer(service, account));
    }

    public static TimedResult<File> fixIDs(final TimedResult<File> result, final String service, final String account) {
        return new CustomizableTimedResult<File>(result, new IDManglingFileCustomizer(service, account));
    }

    public static Delta<File> fixIDs(final Delta<File> delta, final String service, final String account) {
        return new CustomizableDelta<File>(delta, new IDManglingFileCustomizer(service, account));
    }

    public static File fixIDs(final File file, final String service, final String account) {
        if (file instanceof UserizedFile) {
            return new IDManglingUserizedFile((UserizedFile) file, service, account);
        }

        return new IDManglingFile(file, service, account);
    }

    /**
     * Adjusts the file- and folder identifiers returned by the encapsulated metadata reference provided via {@link Document#getFile}.
     *
     * @param document The document to adjust the IDs for
     * @param service The service identifier to apply
     * @param account The account identifier to apply
     * @return The adjusted document
     */
    public static Document fixIDs(final Document document, final String service, final String account) {
        return new Document(document) {

            @Override
            public InputStream getData() throws OXException {
                return document.getData();
            }

            @Override
            public File getFile() {
                File file = super.getFile();
                return null == file ? null : fixIDs(file, service, account);
            }
        };
    }

}
