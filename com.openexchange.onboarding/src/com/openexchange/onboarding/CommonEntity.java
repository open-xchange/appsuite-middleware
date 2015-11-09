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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.onboarding;

import com.openexchange.exception.OXException;
import com.openexchange.session.Session;

/**
 * {@link CommonEntity} - An enumeration for common entities without a description.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public enum CommonEntity implements Entity {

    /**
     * The entity for Apple iOS
     */
    APPLE_IOS(DefaultEntity.newInstance("apple.ios", "com.openexchange.onboarding.ios.", false), Platform.APPLE),
    /**
     * The entity for Apple OSX
     */
    APPLE_OSX(DefaultEntity.newInstance("apple.osx", "com.openexchange.onboarding.osx.", false), Platform.APPLE),
    /**
     * The entity for Apple iPad (via iOS)
     */
    APPLE_IOS_IPAD(DefaultEntity.newInstance("apple.ios.ipad", "com.openexchange.onboarding.ipad.", false), Platform.APPLE),
    /**
     * The entity for Apple iPhone (via iOS)
     */
    APPLE_IOS_IPHONE(DefaultEntity.newInstance("apple.ios.iphone", "com.openexchange.onboarding.iphone.", false), Platform.APPLE),

    /**
     * The entity for Android/Google tablet
     */
    ANDROID_TABLET(DefaultEntity.newInstance("android.tablet", "com.openexchange.onboarding.android.tablet.", false), Platform.ANDROID_GOOGLE),
    /**
     * The entity for Android/Google phone
     */
    ANDROID_PHONE(DefaultEntity.newInstance("android.phone", "com.openexchange.onboarding.android.phone.", false), Platform.ANDROID_GOOGLE),

    /**
     * The entity for Windows Desktop 8 + 10
     */
    WINDOWS_DESKTOP_8_10(DefaultEntity.newInstance("windows.desktop", "com.openexchange.onboarding.windows.desktop.", false), Platform.WINDOWS),

    ;

    private final Entity delegate;
    private final Platform platform;

    private CommonEntity(Entity delegate, Platform platform) {
        this.delegate = delegate;
        this.platform = platform;
    }

    /**
     * Gets the platform associated with this entity
     *
     * @return The platform
     */
    public Platform getPlatform() {
        return platform;
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public String getDisplayName(Session session) throws OXException {
        return delegate.getDisplayName(session);
    }

    @Override
    public Icon getIcon(Session session) throws OXException {
        return delegate.getIcon(session);
    }

    @Override
    public String getDescription(Session session) throws OXException {
        return delegate.getDescription(session);
    }

}
