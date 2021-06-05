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

package com.openexchange.file.storage.xox.subscription;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.api.client.ApiClient;
import com.openexchange.api.client.ApiClientService;
import com.openexchange.api.client.Credentials;
import com.openexchange.api.client.common.calls.contact.picture.ContactPictureCall;
import com.openexchange.contact.picture.ContactPicture;
import com.openexchange.contact.picture.PictureSearchData;
import com.openexchange.contact.picture.finder.ContactPictureFinder;
import com.openexchange.contact.picture.finder.PictureResult;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.xox.XOXFileStorageService;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.core.tools.ShareLinks;
import com.openexchange.tools.id.IDMangler;

/**
 * {@link XOXContactPicutreFinder} - Finder for pictures on remote OX systems using an existing XOX filestorage account
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class XOXContactPicutreFinder implements ContactPictureFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(XOXContactPicutreFinder.class);

    private final ServiceLookup services;
    private final XOXFileStorageService fileStorageService;

    /**
     * Initializes a new {@link XOXContactPicutreFinder}.
     * 
     * @param services The service lookup
     * @param fileStorageService The actual file storage to lookup the account in
     */
    public XOXContactPicutreFinder(ServiceLookup services, XOXFileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
        this.services = services;
    }

    @Override
    public int getRanking() {
        return 290;
    }

    @Override
    public PictureResult getPicture(Session session, PictureSearchData data) throws OXException {
        return requestContactPicture(session, data);
    }

    @Override
    public PictureResult getETag(Session session, PictureSearchData data) throws OXException {
        return requestContactPicture(session, data);
    }

    @Override
    public PictureResult getLastModified(Session session, PictureSearchData data) {
        try {
            return requestContactPicture(session, data);
        } catch (OXException e) {
            LOGGER.debug("Unable to get contact picture", e);
        }
        return new PictureResult(PictureSearchData.EMPTY_DATA);
    }

    /**
     * Send a request to the remote OX server and retrieves the contact picture
     *
     * @param session The user session
     * @param data The data to query the remote OX with
     * @return The result of the operation
     * @throws OXException In case account or service is missing
     */
    private PictureResult requestContactPicture(Session session, PictureSearchData data) throws OXException {
        /*
         * Check if account ID is like <code>xox8://1/3</code>
         */
        if (Strings.isEmpty(data.getAccountId()) || false == data.getAccountId().startsWith(fileStorageService.getId())) {
            return new PictureResult(PictureSearchData.EMPTY_DATA);
        }
        List<String> unmangle = IDMangler.unmangle(data.getAccountId());
        if (null == unmangle || unmangle.size() < 2) {
            return new PictureResult(PictureSearchData.EMPTY_DATA);
        }

        /*
         * Try to get the account
         */
        FileStorageAccount storageAccount = fileStorageService.getAccountManager().getAccount(unmangle.get(1), session);
        if (null == storageAccount) {
            return new PictureResult(PictureSearchData.EMPTY_DATA);
        }

        /*
         * Get information for the request
         */
        String shareLink = (String) storageAccount.getConfiguration().get("url");
        String baseToken = ShareLinks.extractBaseToken(shareLink);
        if (Strings.isEmpty(shareLink) || Strings.isEmpty(baseToken)) {
            return new PictureResult(PictureSearchData.EMPTY_DATA);
        }
        String password = (String) storageAccount.getConfiguration().get("password");

        /*
         * Ask the remote OX for the picture
         */
        ApiClientService apiClientService = services.getServiceSafe(ApiClientService.class);
        ApiClient apiClient = null;
        try {
            apiClient = apiClientService.getApiClient(session, shareLink, new Credentials(null, password));
            PictureSearchData searchData = new PictureSearchData(data.getUserId(), null, data.getFolderId(), data.getContactId(), data.getEmails());
            ContactPicture contactPicture = apiClient.execute(new ContactPictureCall(searchData));
            if (false == ContactPicture.ETAG_NOT_FOUND.equals(contactPicture.getETag())) {
                return new PictureResult(contactPicture);
            }
        } catch (OXException e) {
            LOGGER.debug("Unable to get contact picture", e);
        }

        return new PictureResult(PictureSearchData.EMPTY_DATA);
    }

}
