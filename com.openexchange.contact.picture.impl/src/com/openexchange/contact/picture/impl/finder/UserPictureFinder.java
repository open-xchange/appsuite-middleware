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
import com.openexchange.groupware.ldap.User;
import com.openexchange.session.Session;
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
        if (data.hasUser()) {
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
                    return new PictureResult(new PictureSearchData(null, I(FolderObject.SYSTEM_LDAP_FOLDER_ID), I(user.getContactId()), set));
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
