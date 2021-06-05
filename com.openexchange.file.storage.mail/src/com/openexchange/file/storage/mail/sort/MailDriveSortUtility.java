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

package com.openexchange.file.storage.mail.sort;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.search.SearchTerm;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.imap.sort.IMAPSort;
import com.openexchange.imap.sort.IMAPSort.SortPartialResult;
import com.openexchange.mail.IndexRange;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.SortTerm;

/**
 * {@link MailDriveSortUtility}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public enum MailDriveSortUtility {

    ;

    /**
     * Initializes a new {@link MailDriveSortUtility}.
     */
    private MailDriveSortUtility() {
    }

    /**
     * Compiles the sort term expressions for specified sort field and sort order.
     *
     * @param sort The sort field
     * @param order The sort order
     * @return The sort term expressions or <code>null</code> if specified sort field is not supported
     */
    public static SortTerm[] getSortTerms(Field sort, SortDirection order) {
        SortTerm sortTerm;
        switch (sort) {
            case CREATED:
                sortTerm = SortTerm.ARRIVAL;
                break;
            case LAST_MODIFIED:
                sortTerm = SortTerm.ARRIVAL;
                break;
            case LAST_MODIFIED_UTC:
                sortTerm = SortTerm.ARRIVAL;
                break;
            case FILENAME:
                sortTerm = SortTerm.SUBJECT;
                break;
            case FILE_SIZE:
                sortTerm = SortTerm.SIZE;
                break;
            case TITLE:
                sortTerm = SortTerm.SUBJECT;
                break;
            case CATEGORIES:
                // fall-through
            case COLOR_LABEL:
                // fall-through
            case CONTENT:
                // fall-through
            case CREATED_BY:
                // fall-through
            case CURRENT_VERSION:
                // fall-through
            case DESCRIPTION:
                // fall-through
            case FILE_MD5SUM:
                // fall-through
            case FILE_MIMETYPE:
                // fall-through
            case FOLDER_ID:
                // fall-through
            case ID:
                // fall-through
            case LOCKED_UNTIL:
                // fall-through
            case META:
                // fall-through
            case MODIFIED_BY:
                // fall-through
            case NUMBER_OF_VERSIONS:
                // fall-through
            case OBJECT_PERMISSIONS:
                // fall-through
            case SEQUENCE_NUMBER:
                // fall-through
            case SHAREABLE:
                // fall-through
            case URL:
                // fall-through
            case VERSION:
                // fall-through
            case VERSION_COMMENT:
                // fall-through
            default:
                sortTerm = null;
                break;

        }
        if (sortTerm == null) {
            return null;
        }

        return SortDirection.DESC == order ? new SortTerm[] { SortTerm.REVERSE, sortTerm } : new SortTerm[] { sortTerm };
    }

    /**
     * Performs an ESORT command with sorting by given sort term expressions, optionally filtering by specified search term and providing
     * the defined range from given IMAP folder.
     *
     * @param sortTerms The sort term expressions
     * @param searchTerm The optional search term to filter by
     * @param startIndex The range start index
     * @param endIndex The range end index (exclusive)
     * @param imapFolder The IMAP folder to sort/search in
     * @return The message range or <code>null</code> if ESORT command does not succeed
     * @throws MessagingException If ESORT command failed fatally
     */
    public static int[] performEsortAndGetSeqNums(SortTerm[] sortTerms, SearchTerm searchTerm, int startIndex, int endIndex, IMAPFolder imapFolder) throws MessagingException {
        SortPartialResult result = IMAPSort.sortReturnPartial(sortTerms, searchTerm, new IndexRange(startIndex, endIndex), imapFolder);
        switch (result.reason) {
            case SUCCESS:
                {
                    int[] seqNums = result.seqnums;
                    if (null != seqNums) {
                        // SORT RETURN PARTIAL command succeeded
                        return seqNums;
                    }
                }
                break;
            case COMMAND_FAILED:
                break;
            case FOLDER_CLOSED:
                {
                    // Apparently, SORT RETURN PARTIAL command failed
                    try {    imapFolder.close(false);    } catch (Exception x) { /*Ignore*/ }
                    try {    imapFolder.open(IMAPFolder.READ_ONLY);    } catch (Exception x) { /*Ignore*/ }
                }
                break;
            default:
                break;
        }
        return null;
    }

    /**
     * Performs an ESORT command with sorting by given sort term expressions, optionally filtering by specified search term and providing
     * the defined range from given IMAP folder.
     *
     * @param sortTerms The sort term expressions
     * @param searchTerm The optional search term to filter by
     * @param startIndex The range start index
     * @param endIndex The range end index (exclusive)
     * @param imapFolder The IMAP folder to sort/search in
     * @return The message range or <code>null</code> if ESORT command does not succeed
     * @throws MessagingException If ESORT command failed fatally
     */
    public static Message[] performEsort(SortTerm[] sortTerms, SearchTerm searchTerm, int startIndex, int endIndex, IMAPFolder imapFolder) throws MessagingException {
        int[] seqNums = performEsortAndGetSeqNums(sortTerms, searchTerm, startIndex, endIndex, imapFolder);
        if (null != seqNums) {
            // SORT RETURN PARTIAL command succeeded
            return imapFolder.getMessages(seqNums);
        }
        return null;
    }

}
