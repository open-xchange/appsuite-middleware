/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are public by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
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
