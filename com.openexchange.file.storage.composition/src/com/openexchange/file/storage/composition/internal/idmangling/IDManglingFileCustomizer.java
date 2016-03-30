/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
