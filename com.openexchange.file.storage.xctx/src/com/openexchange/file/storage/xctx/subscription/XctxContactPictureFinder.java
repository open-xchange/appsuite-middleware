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

package com.openexchange.file.storage.xctx.subscription;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.contact.picture.ContactPicture;
import com.openexchange.contact.picture.ContactPictureService;
import com.openexchange.contact.picture.PictureSearchData;
import com.openexchange.contact.picture.finder.ContactPictureFinder;
import com.openexchange.contact.picture.finder.PictureResult;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.xctx.XctxFileStorageService;
import com.openexchange.java.Functions.OXBiFunction;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.core.tools.ShareLinks;
import com.openexchange.share.subscription.XctxSessionManager;
import com.openexchange.tools.id.IDMangler;

/**
 * {@link XctxContactPictureFinder} - Finder for pictures on another internal context using a guest session
 * of an existing Xctx filestorage account
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class XctxContactPictureFinder implements ContactPictureFinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(XctxContactPictureFinder.class);

    private final ServiceLookup services;
    private final XctxFileStorageService fileStorageService;

    /**
     * Initializes a new {@link XctxContactPictureFinder}.
     *
     * @param services The service lookup
     * @param fileStorageService The actual file storage to lookup the account in
     */
    public XctxContactPictureFinder(ServiceLookup services, XctxFileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
        this.services = services;
    }

    @Override
    public int getRanking() {
        return 300;
    }

    @Override
    public PictureResult getPicture(Session session, PictureSearchData data) throws OXException {
        Session guestSession = searchForGuest(session, data);
        Optional<ContactPicture> result = getResult(session, data, (s, d) -> s.getPicture(guestSession, d));
        ContactPicture pic = result.orElseGet(() -> ContactPicture.NOT_FOUND);

        return ContactPicture.ETAG_NOT_FOUND.equals(pic.getETag()) ? new PictureResult(data) : new PictureResult(pic);
    }

    @Override
    public PictureResult getETag(Session session, PictureSearchData data) throws OXException {
        Session guestSession = searchForGuest(session, data);
        Optional<String> result = getResult(session, data, (s, d) -> s.getETag(guestSession, d));
        String eTag = result.orElseGet(() -> ContactPicture.ETAG_NOT_FOUND);
        if (false == ContactPicture.ETAG_NOT_FOUND.equals(eTag)) {
            return new PictureResult(new ContactPicture(eTag, null, ContactPicture.UNMODIFIED));
        }
        return new PictureResult(data);
    }

    @Override
    public PictureResult getLastModified(Session session, PictureSearchData data) {
        try {
            Session guestSession = searchForGuest(session, data);
            Optional<Date> result = getResult(session, data, (s, d) -> s.getLastModified(guestSession, d));
            Date lastModified = result.orElse(ContactPicture.UNMODIFIED);
            if (false == ContactPicture.UNMODIFIED.equals(lastModified)) {
                return new PictureResult(new ContactPicture(null, null, lastModified));
            }
        } catch (OXException e) {
            LOGGER.debug("Unable to get last modified", e);
        }
        return new PictureResult(data);
    }

    /**
     * Retrieves the guest session for the account
     *
     * @param session The session to use
     * @param data The data to search with
     * @return The response or <code>null</code>
     * @throws OXException In case response can't be get, e.g. a guest session can't be obtained
     */
    private Session searchForGuest(Session session, PictureSearchData data) throws OXException {
        /*
         * Check if account ID is like <code>xctx8://1/3</code>
         */
        if (Strings.isEmpty(data.getAccountId()) || false == data.getAccountId().startsWith(fileStorageService.getId())) {
            return null;
        }
        List<String> unmangle = IDMangler.unmangle(data.getAccountId());
        if (null == unmangle || unmangle.size() < 2) {
            return null;
        }

        /*
         * Try to get the account
         */
        FileStorageAccount storageAccount = fileStorageService.getAccountManager().getAccount(unmangle.get(1), session);
        if (null == storageAccount) {
            return null;
        }

        /*
         * Obtain guest session
         */
        String shareUrl = (String) storageAccount.getConfiguration().get("url");
        String baseToken = ShareLinks.extractBaseToken(shareUrl);
        if (Strings.isEmpty(shareUrl) || Strings.isEmpty(baseToken)) {
            return null;
        }
        String password = (String) storageAccount.getConfiguration().get("password");

        XctxSessionManager sessionManager = services.getServiceSafe(XctxSessionManager.class);
        return sessionManager.getGuestSession(session, baseToken, password);
    }

    /**
     * Executes a function on the {@link ContactPictureService} and returns the result
     *
     * @param <T> The type of return value
     * @param session The session of the <b>guest</b>
     * @param data The data to search the picture with
     * @param f The function on the service
     * @return The result if a guest session can be found, or an empty optional
     * @throws OXException In case of error
     */
    private <T> Optional<T> getResult(Session guestSession, PictureSearchData data, OXBiFunction<ContactPictureService, PictureSearchData, T, OXException> f) throws OXException {
        if (null == guestSession) {
            return Optional.empty();
        }
        ContactPictureService contactPictureService = services.getServiceSafe(ContactPictureService.class);
        PictureSearchData searchData = new PictureSearchData(data.getUserId(), null, data.getFolderId(), data.getContactId(), data.getEmails());
        return Optional.ofNullable(f.apply(contactPictureService, searchData));
    }

}
