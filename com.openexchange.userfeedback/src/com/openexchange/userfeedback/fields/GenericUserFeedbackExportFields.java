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

package com.openexchange.userfeedback.fields;

import java.util.Arrays;
import java.util.List;

/**
 * {@link GenericUserFeedbackExportFields}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.4
 */
public class GenericUserFeedbackExportFields {

    public static final UserFeedbackField DATE = new UserFeedbackField("Date", "date", false);
    public static final UserFeedbackField APP = new UserFeedbackField("App", "app", true, 50);
    public static final UserFeedbackField ENTRY_POINT = new UserFeedbackField("Entry Point", "entry_point", true, 50);
    public static final UserFeedbackField OPERATING_SYSTEM = new UserFeedbackField("Operating System", "operating_system", true, 50);
    public static final UserFeedbackField BROWSER = new UserFeedbackField("Browser", "browser", true, 50);
    public static final UserFeedbackField BROWSER_VERSION = new UserFeedbackField("Browser Version", "browser_version", true, 10);
    public static final UserFeedbackField USER_AGENT = new UserFeedbackField("User Agent", "user_agent", true, 200);
    public static final UserFeedbackField SCREEN_RESOLUTION = new UserFeedbackField("Screen Resolution", "screen_resolution", true, 20);
    public static final UserFeedbackField LANGUAGE = new UserFeedbackField("Language", "language", true, 20);
    public static final UserFeedbackField USER = new UserFeedbackField("User", "user", false);
    public static final UserFeedbackField SERVER_VERSION = new UserFeedbackField("Server Version", "server_version", false);
    public static final UserFeedbackField CLIENT_VERSION = new UserFeedbackField("Client Version", "client_version", true, 20);

    public static final List<UserFeedbackField> ALL_TYPE_UNSPECIFIC_FIELDS = Arrays.asList(DATE, APP, ENTRY_POINT, OPERATING_SYSTEM, BROWSER, BROWSER_VERSION, USER_AGENT, SCREEN_RESOLUTION, LANGUAGE, USER, SERVER_VERSION, CLIENT_VERSION);

}
