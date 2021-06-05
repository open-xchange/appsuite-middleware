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

package com.openexchange.userfeedback;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.userfeedback.export.ExportResultConverter;

/**
 * {@link FeedbackType}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public interface FeedbackType {

    /**
     * Stores Feedback data
     *
     * @param feedback The data
     * @param con The write connection to the global db
     * @return The id of the newly created entry or -1
     * @throws OXException
     */
    public long storeFeedback(Object feedback, Connection con) throws OXException;

    /**
     * Retrieves the requested feedbacks wrapped in an {@link ExportResultConverter}.
     *
     * @param metaDataList The feedback metadata to retrieve
     * @param con A read connection to the global db
     * @return A list of feedback objects
     * @throws OXException
     */
    public ExportResultConverter getFeedbacks(List<FeedbackMetaData> metaDataList, Connection con) throws OXException;

    /**
     * Retrieves the requested feedbacks wrapped in an {@link ExportResultConverter}.
     *
     * @param metaDataList The feedback metadata to retrieve
     * @param con A read connection to the global db
     * @param configuration A read connection to the global db
     * @return A list of feedback objects
     * @throws OXException
     */
    public ExportResultConverter getFeedbacks(List<FeedbackMetaData> metaDataList, Connection con, Map<String, String> configuration) throws OXException;

    /**
     * Deletes multiple feedback entries
     * 
     * @param ids A list of feedback entries
     * @param con A write connection to the global db
     * @throws OXException
     */
    public void deleteFeedbacks(List<Long> ids, Connection con) throws OXException;

    /**
     * Retrieves the feedback type
     *
     * @return The feedback type
     */
    public String getType();
}
