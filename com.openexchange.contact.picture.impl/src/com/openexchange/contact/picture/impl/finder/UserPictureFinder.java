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

package com.openexchange.contact.picture.impl.finder;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.util.LinkedHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.contact.picture.PictureSearchData;
import com.openexchange.contact.picture.finder.ContactPictureFinder;
import com.openexchange.contact.picture.finder.PictureResult;
import com.openexchange.contact.picture.impl.ContactPictureUtil;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link UserPictureFinder} - Checks if user exists and set the user information like contact id etc.
 * <pre>Note: This finder is unable to find pictures itself it only provides additional data. </pre>
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public class UserPictureFinder implements ContactPictureFinder {

    private final static Logger LOGGER = LoggerFactory.getLogger(UserPictureFinder.class);

    private final UserService userService;

    /**
     * Initializes a new {@link UserPictureFinder}.
     *
     * @param userService The {@link UserService}
     */
    public UserPictureFinder(UserService userService) {
        super();
        this.userService = userService;
    }

    @Override
    public PictureResult getPicture(Session session, PictureSearchData data) {
        return provideUserData(session, data);
    }

    @Override
    public PictureResult getETag(Session session, PictureSearchData data) {
        return provideUserData(session, data);
    }

    @Override
    public PictureResult getLastModified(Session session, PictureSearchData data) {
        return provideUserData(session, data);
    }

    private PictureResult provideUserData(Session session, PictureSearchData data) {
        if (data.hasUser() && Strings.isEmpty(data.getAccountId())) {
            try {
                User user = userService.getUser(i(data.getUserId()), session.getContextId());
                if (null != user) {
                    LinkedHashSet<String> set = null;
                    if (i(data.getUserId()) == session.getUserId() || ContactPictureUtil.hasGAB(session)) {
                        set = new LinkedHashSet<>();
                        set.add(user.getMail());
                        for (String string : user.getAliases()) {
                            set.add(string);
                        }
                    }
                    return new PictureResult(new PictureSearchData(null, null, user.isGuest() ? Integer.toString(FolderObject.VIRTUAL_GUEST_CONTACT_FOLDER_ID) : Integer.toString(FolderObject.SYSTEM_LDAP_FOLDER_ID), Integer.toString(user.getContactId()), set));
                }
            } catch (OXException e) {
                LOGGER.debug("Unable to find user with identifier {} in context {}", data.getUserId(), I(session.getContextId()), e);
            }
        }

        return new PictureResult(data);
    }

    @Override
    public int getRanking() {
        return 1000;
    }

}
