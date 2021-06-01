///*
// * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
// * @license AGPL-3.0
// *
// * This code is free software: you can redistribute it and/or modify
// * it under the terms of the GNU Affero General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU Affero General Public License for more details.
// *
// * You should have received a copy of the GNU Affero General Public License
// * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
// *
// * Any use of the work other than as authorized under this license or copyright law is prohibited.
// *
// */
//
//package com.openexchange.caldav.mixins;
//
//import com.openexchange.dav.acl.mixins.PrincipalURL;
//import com.openexchange.dav.resources.CommonFolderCollection;
//import com.openexchange.exception.OXException;
//import com.openexchange.groupware.ldap.User;
//import com.openexchange.webdav.protocol.Protocol;
//import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;
//
///**
// * The {@link Owner}
// *
// * This property identifies a particular principal as being the "owner" of the
// * resource. Since the owner of a resource often has special access control
// * capabilities (e.g., the owner frequently has permanent DAV:write-acl
// * privilege), clients might display the resource owner in their user
// * interface.
// *
// * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
// */
//public class Owner extends SingleXMLPropertyMixin {
//
//    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Owner.class);
//    private static final String PROPERTY_NAME = "owner";
//    private final CommonFolderCollection<?> collection;
//
//    public Owner(CommonFolderCollection<?> collection) {
//        super(Protocol.DAV_NS.getURI(), PROPERTY_NAME);
//        this.collection = collection;
//    }
//
//    @Override
//    protected String getValue() {
//        User owner = null;
//        try {
//            owner = collection.getOwner();
//        } catch (OXException e) {
//            LOG.warn("error determining owner from folder collection '{}'", collection.getFolder(), e);
//        }
//        return null != owner ? PrincipalURL.getValue(owner.getId()) : null;
//    }
//
//}
