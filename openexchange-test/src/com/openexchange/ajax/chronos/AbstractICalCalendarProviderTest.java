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

package com.openexchange.ajax.chronos;

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import com.openexchange.ajax.chronos.manager.CalendarFolderManager;
import com.openexchange.testing.httpclient.invoker.ApiException;
import com.openexchange.testing.httpclient.models.FolderCalendarConfig;
import com.openexchange.testing.httpclient.models.FolderCalendarExtendedProperties;
import com.openexchange.testing.httpclient.models.FolderPermission;
import com.openexchange.testing.httpclient.models.NewFolderBody;
import com.openexchange.testing.httpclient.models.NewFolderBodyFolder;

/**
 * {@link AbstractICalCalendarProviderTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 8.0.0
 */
public class AbstractICalCalendarProviderTest extends AbstractExternalProviderChronosTest {

    protected AbstractICalCalendarProviderTest() {
        super(CalendarFolderManager.ICAL_ACCOUNT_PROVIDER_ID);
    }

    protected String createAccount(NewFolderBody body) throws ApiException {
        return this.folderManager.createFolder(body);
    }

    protected long dateToMillis(String date) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyyMMdd'T'HHmmssZ");
        return formatter.parseDateTime(date).getMillis();
    }

    protected String createDefaultAccount(String externalUri) throws ApiException {
        FolderCalendarConfig config = new FolderCalendarConfig();
        NewFolderBodyFolder folder = createFolder(externalUri, config);
        addPermissions(folder);

        NewFolderBody body = new NewFolderBody();
        body.setFolder(folder);

        return createAccount(body);
    }

    protected NewFolderBodyFolder createFolder(String externalUri, FolderCalendarConfig config) {
        config.setEnabled(Boolean.TRUE);
        config.setUri(externalUri);
        NewFolderBodyFolder folder = new NewFolderBodyFolder();
        folder.setModule("event");
        folder.setComOpenexchangeCalendarConfig(config);
        folder.setSubscribed(Boolean.TRUE);
        folder.setTitle("testFolder_" + System.nanoTime());
        folder.setComOpenexchangeCalendarProvider(CalendarFolderManager.ICAL_ACCOUNT_PROVIDER_ID);
        folder.setComOpenexchangeCalendarExtendedProperties(new FolderCalendarExtendedProperties());
        return folder;
    }

    protected void addPermissions(NewFolderBodyFolder folder) {
        FolderPermission perm = new FolderPermission();
        perm.setEntity(defaultUserApi.getCalUser());
        perm.setGroup(Boolean.FALSE);
        perm.setBits(I(403710016));

        List<FolderPermission> permissions = new ArrayList<>();
        permissions.add(perm);

        folder.setPermissions(permissions);
    }
}
