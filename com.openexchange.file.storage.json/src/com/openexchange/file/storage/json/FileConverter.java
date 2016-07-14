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

package com.openexchange.file.storage.json;

import java.util.Date;
import org.json.JSONArray;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.json.actions.files.AJAXInfostoreRequest;
import com.openexchange.file.storage.json.osgi.FileFieldCollector;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link FileConverter}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FileConverter implements ResultConverter {

    private final FileMetadataWriter writer;

    /**
     * Initializes a new {@link FileConverter}.
     *
     * @param fieldCollector The collector for additional file fields
     */
    public FileConverter(FileFieldCollector fieldCollector) {
        super();
        writer = new FileMetadataWriter(fieldCollector);
    }

    @Override
    public void convert(final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session, final Converter converter) throws OXException {
        AJAXInfostoreRequest infostoreRequest = new AJAXInfostoreRequest(requestData, session);
        Object resultObject = result.getResultObject();
        if (File.class.isInstance(resultObject)) {
            /*
             * write single file result
             */
            resultObject = writer.write(infostoreRequest, (File) resultObject);
        } else if (Delta.class.isInstance(resultObject)) {
            /*
             * write delta result
             */
            SearchIterator<File> newAndModifiedIterator = null;
            SearchIterator<File> deletedIterator = null;
            try {
                Delta<File> deltaResult = (Delta<File>) resultObject;
                newAndModifiedIterator = deltaResult.results();
                JSONArray jsonArray = writer.write(infostoreRequest, newAndModifiedIterator);
                deletedIterator = deltaResult.getDeleted();
                while (deletedIterator.hasNext()) {
                    jsonArray.put(deletedIterator.next().getId());
                }
                resultObject = jsonArray;
            } finally {
                SearchIterators.close(newAndModifiedIterator);
                SearchIterators.close(deletedIterator);
            }
        } else if (TimedResult.class.isInstance(resultObject)) {
            /*
             * write timed files result
             */
            SearchIterator<File> searchIterator = null;
            try {
                TimedResult<File> timedResult = (TimedResult<File>) resultObject;
                result.setTimestamp(new Date(timedResult.sequenceNumber()));
                searchIterator = timedResult.results();
                resultObject = writer.write(infostoreRequest, searchIterator);
            } finally {
                SearchIterators.close(searchIterator);
            }
        } else if (SearchIterator.class.isInstance(resultObject)) {
            /*
             * write search iterator result
             */
            SearchIterator<File> searchIterator = null;
            try {
                searchIterator = (SearchIterator<File>) resultObject;
                resultObject = writer.write(infostoreRequest, searchIterator);
            } finally {
                SearchIterators.close(searchIterator);
            }
        } else {
            throw new UnsupportedOperationException("unknown result object");
        }
        result.setResultObject(resultObject);
    }

    @Override
    public String getInputFormat() {
        return "infostore";
    }

    @Override
    public String getOutputFormat() {
        return "json";
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

}
