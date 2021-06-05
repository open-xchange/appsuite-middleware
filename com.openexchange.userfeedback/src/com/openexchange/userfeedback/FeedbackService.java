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

import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.session.Session;
import com.openexchange.userfeedback.export.ExportResultConverter;
import com.openexchange.userfeedback.export.ExportType;
import com.openexchange.userfeedback.filter.FeedbackFilter;

/**
 * {@link FeedbackService}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public interface FeedbackService {

    /**
     * Stores feedback data and metadata for a given user
     *
     * @param session The session of the user
     * @param feedback The feedback data
     * @param params Additional parameters that have to be considered while persisting. Has to include parameter 'type'.
     * @throws OXException if feedback couldn't be stored
     */
    void store(Session session, Object feedback, Map<String, String> params) throws OXException;

    /**
     * Exports feedback data within an {@link ExportResultConverter}. The export data becomes generated/formatted based on the {@link ExportType} defined for {@link ExportResultConverter#get(ExportType)}
     * 
     * @param ctxGroup The context group to export for
     * @param filter The filter (containing the type of report) to limit the results
     * @return {@link ExportResultConverter} wrapping the data
     * @throws OXException if an export isn't possible due to errors
     */
    ExportResultConverter export(String ctxGroup, FeedbackFilter filter) throws OXException;

    /**
     * Exports feedback data within an {@link ExportResultConverter}. The export data becomes generated/formatted based on the {@link ExportType} defined for {@link ExportResultConverter#get(ExportType)}
     * 
     * @param ctxGroup The context group to export for
     * @param filter The filter (containing the type of report) to limit the results
     * @param configuration The configuration that provides information about the export preparation
     * @return {@link ExportResultConverter} wrapping the data
     * @throws OXException if an export isn't possible due to errors
     */
    ExportResultConverter export(String ctxGroup, FeedbackFilter filter, Map<String, String> configuration) throws OXException;

    /**
     * Delete feedback data
     * 
     * @param ctxGroup The context group
     * @param filter Feedback filter to determine which feedback should be deleted
     * @throws OXException If feedback could not be deleted
     */
    void delete(String ctxGroup, FeedbackFilter filter) throws OXException;
}
