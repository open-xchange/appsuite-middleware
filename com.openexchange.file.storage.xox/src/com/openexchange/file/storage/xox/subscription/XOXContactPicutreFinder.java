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
 * {@link XOXContactPicutreFinder}
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
         * Get information for the reuqest
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
            PictureSearchData searchData = new PictureSearchData(data.getUserId(), null, null, null, data.getEmails());
            ContactPicture contactPicture = apiClient.execute(new ContactPictureCall(searchData));
            if (false == ContactPicture.NOT_FOUND.getETag().equals(contactPicture.getETag())) {
                return new PictureResult(contactPicture);
            }
        } catch (OXException e) {
            LOGGER.debug("Unable to get contact picture", e);
        }

        return new PictureResult(PictureSearchData.EMPTY_DATA);
    }

}
