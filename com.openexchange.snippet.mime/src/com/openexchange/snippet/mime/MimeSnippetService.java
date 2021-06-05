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

package com.openexchange.snippet.mime;

import java.util.Collections;
import java.util.List;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.quota.QuotaProvider;
import com.openexchange.session.Session;
import com.openexchange.snippet.QuotaAwareSnippetService;
import com.openexchange.snippet.mime.groupware.QuotaMode;

/**
 * {@link MimeSnippetService} - The "filestore" using snippet service.
 * <p>
 * <b>&nbsp;&nbsp;How SnippetService selection works</b>
 * <hr>
 * <p>
 * The check if "filestore" capability is available/permitted as per CapabilityService is performed through examining
 * "MimeSnippetService.neededCapabilities()" method in "SnippetAction.getSnippetService()".
 * <p>
 * Available SnippetServices are sorted rank-wise, with RdbSnippetService having default (0) ranking and MimeSnippetService with a rank of
 * 10. Thus MimeSnippetService is preferred provided that "filestore" capability is indicated by CapabilityService.
 * <p>
 * If missing, RdbSnippetService is selected.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MimeSnippetService implements QuotaAwareSnippetService {

    private final QuotaProvider quotaProvider;
    private final LeanConfigurationService leanConfigurationService;

    /**
     * Initializes a new {@link MimeSnippetService}.
     */
    public MimeSnippetService(QuotaProvider quotaProvider, LeanConfigurationService leanConfigurationService) {
        super();
        this.quotaProvider = quotaProvider;
        this.leanConfigurationService = leanConfigurationService;
    }

    @Override
    public MimeSnippetManagement getManagement(final Session session) throws OXException {
        return new MimeSnippetManagement(session, quotaProvider, leanConfigurationService);
    }

    @Override
    public List<String> neededCapabilities() {
        return Collections.singletonList("filestore");
    }

    @Override
    public List<String> getFilesToIgnore(int contextId) throws OXException {
        if (!ignoreQuota()) {
            return Collections.emptyList();
        }
        return MimeSnippetFileAccess.getFiles(contextId);
    }

    @Override
    public boolean ignoreQuota() {
       return QuotaMode.DEDICATED.equals(MimeSnippetManagement.getMode());
    }

}
