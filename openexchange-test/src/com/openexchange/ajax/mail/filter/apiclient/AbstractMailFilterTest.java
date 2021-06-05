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

package com.openexchange.ajax.mail.filter.apiclient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.ajax.framework.AbstractConfigAwareAPIClientSession;
import com.openexchange.testing.httpclient.modules.FoldersApi;
import com.openexchange.testing.httpclient.modules.MailApi;
import com.openexchange.testing.httpclient.modules.MailfilterApi;

/**
 * {@link AbstractMailFilterTest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public abstract class AbstractMailFilterTest extends AbstractConfigAwareAPIClientSession {

    protected MailfilterApi mailfilterapi;
    protected FoldersApi folderApi;
    protected MailApi mailApi;

    Set<Integer> sieveRuleToDelete = new HashSet<>();
    List<String> foldersToDelete = new ArrayList<>();

    /**
     * Initializes a new {@link AbstractMailFilterTest}.
     */
    public AbstractMailFilterTest() {
        super();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        this.mailfilterapi = new MailfilterApi(getApiClient());
        this.folderApi = new FoldersApi(getApiClient());
        this.mailApi = new MailApi(getApiClient());
    }

    public String rememberFolder(String folderId) {
        foldersToDelete.add(folderId);
        return folderId;
    }

    public Integer rememberSieveRule(Integer ruleId) {
        sieveRuleToDelete.add(ruleId);
        return ruleId;
    }

}
