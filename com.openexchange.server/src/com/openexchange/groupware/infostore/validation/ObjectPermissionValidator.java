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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.groupware.infostore.validation;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserConfigurationService;


/**
 * {@link ObjectPermissionValidator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ObjectPermissionValidator implements InfostoreValidator {

    @Override
    public String getName() {
        return ObjectPermissionValidator.class.getSimpleName();
    }

    @Override
    public DocumentMetadataValidation validate(ServerSession session, DocumentMetadata metadata) {
        DocumentMetadataValidation validation = new DocumentMetadataValidation();
        List<ObjectPermission> objectPermissions = metadata.getObjectPermissions();
        if (null != objectPermissions) {
            /*
             * check session user's capabilities
             */
            if (false == session.getUserConfiguration().hasFullSharedFolderAccess()) {
                String name = session.getUser().getDisplayName();
                validation.setError(Metadata.OBJECT_PERMISSIONS_LITERAL, "User " + session.getUser().getDisplayName() + " has no permission to share items.");
                validation.setException(InfostoreExceptionCodes.VALIDATION_FAILED_INAPPLICABLE_PERMISSIONS.create(name));
                return validation;
            }
            /*
             * check capabilities of each permission entity, too, as well as there existence
             */
            UserConfigurationService userConfigurationService = ServerServiceRegistry.getServize(UserConfigurationService.class);
            for (ObjectPermission permission : metadata.getObjectPermissions()) {
                if (false == permission.isGroup()) {
                    try {
                        UserConfiguration userConfig = userConfigurationService.getUserConfiguration(permission.getEntity(), session.getContext(), false);
                        if (false == userConfig.hasFullSharedFolderAccess() || false == userConfig.hasInfostore()) {
                            User user = ServerServiceRegistry.getServize(UserService.class).getUser(permission.getEntity(), session.getContext());
                            String name = user.getDisplayName();
                            validation.setError(Metadata.OBJECT_PERMISSIONS_LITERAL, "User " + name + " has no permission to see share items.");
                            validation.setException(InfostoreExceptionCodes.VALIDATION_FAILED_INAPPLICABLE_PERMISSIONS.create(name));
                            return validation;
                        }
                    } catch (OXException e) {
                        if ("USR-0010".equals(e.getErrorCode())) {
                            // user not found
                            validation.setError(Metadata.OBJECT_PERMISSIONS_LITERAL, e.getDisplayMessage(session.getUser().getLocale()));
                            validation.setException(InfostoreExceptionCodes.VALIDATION_FAILED_INAPPLICABLE_PERMISSIONS.create(permission.getEntity()));
                            return validation;
                        }
                        org.slf4j.LoggerFactory.getLogger(ObjectPermissionValidator.class).warn(
                            "Error getting user configuration for permission entity {}", permission.getEntity(), e);
                    }
                } else {
                    try {
                        ServerServiceRegistry.getServize(GroupService.class).getGroup(session.getContext(), permission.getEntity());
                    } catch (OXException e) {
                        if ("GRP-0017".equals(e.getErrorCode())) {
                            // group not found
                            validation.setError(Metadata.OBJECT_PERMISSIONS_LITERAL, e.getDisplayMessage(session.getUser().getLocale()));
                            validation.setException(InfostoreExceptionCodes.VALIDATION_FAILED_INAPPLICABLE_PERMISSIONS.create(permission.getEntity()));
                            return validation;
                        }
                        org.slf4j.LoggerFactory.getLogger(ObjectPermissionValidator.class).warn(
                            "Error getting group for permission entity {}", permission.getEntity(), e);
                    }
                }
            }
        }
        return validation;
    }

}
