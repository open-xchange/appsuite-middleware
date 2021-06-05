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

package com.openexchange.userfeedback.nps.v1;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.openexchange.userfeedback.fields.GenericUserFeedbackExportFields;
import com.openexchange.userfeedback.fields.UserFeedbackField;

/**
 * 
 * {@link NPSv1ExportFields}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.4
 */
public class NPSv1ExportFields extends GenericUserFeedbackExportFields {

    public static final UserFeedbackField SCORE = new UserFeedbackField("Score", "score", true, 5);
    public static final UserFeedbackField COMMENT = new UserFeedbackField("Comment", "comment", true, 20000);
    public static final UserFeedbackField QUESTION_ID = new UserFeedbackField("Question", "questionid", true, 5);

    public static final List<UserFeedbackField> ALL = Arrays.asList(DATE, SCORE, QUESTION_ID, COMMENT, APP, ENTRY_POINT, OPERATING_SYSTEM, BROWSER, BROWSER_VERSION, USER_AGENT, SCREEN_RESOLUTION, LANGUAGE, USER, SERVER_VERSION, CLIENT_VERSION);

    public static List<UserFeedbackField> getFieldsRequiredByClient() {
        return ALL.stream().filter(x -> x.isProvidedByClient()).collect(Collectors.toList());
    }
}
