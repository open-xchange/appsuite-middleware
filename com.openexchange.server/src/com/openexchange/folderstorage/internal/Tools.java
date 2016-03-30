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

package com.openexchange.folderstorage.internal;

import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.database.contentType.InfostoreContentType;
import com.openexchange.folderstorage.type.DocumentsType;
import com.openexchange.folderstorage.type.MusicType;
import com.openexchange.folderstorage.type.PicturesType;
import com.openexchange.folderstorage.type.TemplatesType;
import com.openexchange.folderstorage.type.TrashType;
import com.openexchange.folderstorage.type.VideosType;
import com.openexchange.framework.request.RequestContextHolder;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.settings.impl.ConfigTree;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link Tools} - A utility class for folder storage processing.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Tools {

    /**
     * Initializes a new {@link Tools}.
     */
    private Tools() {
        super();
    }

    private static final ConcurrentMap<String, Future<TimeZone>> TZ_MAP = new ConcurrentHashMap<String, Future<TimeZone>>();

    /**
     * Gets the <code>TimeZone</code> for the given ID.
     *
     * @param timeZoneID The ID for a <code>TimeZone</code>, either an abbreviation such as "PST", a full name such as
     *            "America/Los_Angeles", or a custom ID such as "GMT-8:00".
     * @return The specified <code>TimeZone</code>, or the GMT zone if the given ID cannot be understood.
     */
    public static TimeZone getTimeZone(final String timeZoneID) {
        Future<TimeZone> future = TZ_MAP.get(timeZoneID);
        if (null == future) {
            final FutureTask<TimeZone> ft = new FutureTask<TimeZone>(new Callable<TimeZone>() {

                @Override
                public TimeZone call() throws Exception {
                    return TimeZone.getTimeZone(timeZoneID);
                }
            });
            future = TZ_MAP.putIfAbsent(timeZoneID, ft);
            if (null == future) {
                future = ft;
                ft.run();
            }
        }
        try {
            return future.get();
        } catch (final InterruptedException e) {
            // Keep interrupted status
            Thread.currentThread().interrupt();
            final IllegalStateException ise = new IllegalStateException(e.getMessage());
            ise.initCause(e);
            throw ise;
        } catch (final CancellationException e) {
            final IllegalStateException ise = new IllegalStateException(e.getMessage());
            ise.initCause(e);
            throw ise;
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                final IllegalStateException ise = new IllegalStateException(e.getMessage());
                ise.initCause(e);
                throw ise;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new IllegalStateException("Not unchecked", cause);
        }
    }

    /**
     * Checks if given folder identifier appears to be a global identifier.
     *
     * @param id The folder identifier to check
     * @return <code>true</code> if global; otherwise <code>false</code>
     */
    public static boolean isGlobalId(String id) {
        return getUnsignedInteger(id) > 0;
    }

    /**
     * The radix for base <code>10</code>.
     */
    private static final int RADIX = 10;

    /**
     * Parses a positive <code>int</code> value from passed {@link String} instance.
     *
     * @param s The string to parse
     * @return The parsed positive <code>int</code> value or <code>-1</code> if parsing failed
     */
    public static int getUnsignedInteger(final String s) {
        if (s == null) {
            return -1;
        }

        final int max = s.length();

        if (max <= 0) {
            return -1;
        }
        if (s.charAt(0) == '-') {
            return -1;
        }

        int result = 0;
        int i = 0;

        final int limit = -Integer.MAX_VALUE;
        final int multmin = limit / RADIX;
        int digit;

        if (i < max) {
            digit = digit(s.charAt(i++));
            if (digit < 0) {
                return -1;
            }
            result = -digit;
        }
        while (i < max) {
            /*
             * Accumulating negatively avoids surprises near MAX_VALUE
             */
            digit = digit(s.charAt(i++));
            if (digit < 0) {
                return -1;
            }
            if (result < multmin) {
                return -1;
            }
            result *= RADIX;
            if (result < limit + digit) {
                return -1;
            }
            result -= digit;
        }
        return -result;
    }

    private static int digit(final char c) {
        switch (c) {
        case '0':
            return 0;
        case '1':
            return 1;
        case '2':
            return 2;
        case '3':
            return 3;
        case '4':
            return 4;
        case '5':
            return 5;
        case '6':
            return 6;
        case '7':
            return 7;
        case '8':
            return 8;
        case '9':
            return 9;
        default:
            return -1;
        }
    }

    /**
     * Calculates the bits from given permission.
     *
     * @param perm The permission
     * @return The bits calculated from given permission
     * @deprecated Deprecated as of 7.8.0. Use {@link Permissions#createPermissionBits(Permission)}
     */
    @Deprecated
    public static int createPermissionBits(final Permission perm) {
        return createPermissionBits(
            perm.getFolderPermission(),
            perm.getReadPermission(),
            perm.getWritePermission(),
            perm.getDeletePermission(),
            perm.isAdmin());
    }

    /**
     * Calculates the bits from given permissions.
     *
     * @param fp The folder permission
     * @param rp The read permission
     * @param wp The write permission
     * @param dp The delete permission
     * @param adminFlag <code>true</code> if admin access; otherwise <code>false</code>
     * @return The bits calculated from given permissions
     *
     * @deprecated Deprecated as of 7.8.0. Use {@link Permissions#createPermissionBits(int, int, int, int, boolean)}
     */
    @Deprecated
    public static int createPermissionBits(final int fp, final int rp, final int wp, final int dp, final boolean adminFlag) {
        return Permissions.createPermissionBits(fp, rp, wp, dp, adminFlag);
    }

    /**
     * Gets the identifier of a specific default folder as defined by the config tree setting.
     * <p/>
     * Currently, only config tree paths of infostore default folders are mapped.
     *
     * @param session The session
     * @param contentType The content type to get the default folder for
     * @param type The folder type
     * @return The default folder identifier, or <code>null</code> if not set
     * @throws OXException
     */
    public static String getConfiguredDefaultFolder(ServerSession session, ContentType contentType, Type type) throws OXException {
        String settingsPath = getDefaultFolderSettingsPath(contentType, type);
        if (null != settingsPath) {
            Setting setting = ConfigTree.getInstance().getSettingByPath(settingsPath);
            if (null != setting) {
                setting.getShared().getValue(session, session.getContext(), session.getUser(), session.getUserConfiguration(), setting);
                Object value = setting.getSingleValue();
                if (null != value && String.class.isInstance(value)) {
                    return (String) value;
                }
            }
        }
        return null;
    }

    /**
     * Gets the settings path in the config tree for a given content- and folder-type.
     * <p/>
     * Currently, only config tree paths of infostore default folders are mapped.
     *
     * @param contentType The content type
     * @param type The folder type
     * @return The settings path, or <code>null</code> if not known
     */
    private static String getDefaultFolderSettingsPath(ContentType contentType, Type type) {
        if (InfostoreContentType.class.isInstance(contentType)) {
            if (TrashType.getInstance().equals(type)) {
                return "modules/infostore/folder/trash";
            } else if (DocumentsType.getInstance().equals(type)) {
                return "modules/infostore/folder/documents";
            } else if (TemplatesType.getInstance().equals(type)) {
                return "modules/infostore/folder/templates";
            } else if (VideosType.getInstance().equals(type)) {
                return "modules/infostore/folder/videos";
            } else if (MusicType.getInstance().equals(type)) {
                return "modules/infostore/folder/music";
            } else if (PicturesType.getInstance().equals(type)) {
                return "modules/infostore/folder/pictures";
            } else  {
                return "folder/infostore";
            }
        }
        return null;
    }

    /**
     * Tries to get the host data of the according HTTP request.
     *
     * @param session The session or <code>null</code>
     * @return The host data or <code>null</code> if none could be determined
     */
    public static HostData getHostData(Session session) {
        com.openexchange.framework.request.RequestContext requestContext = RequestContextHolder.get();
        if (requestContext == null) {
            if (session != null) {
                return (HostData) session.getParameter(HostnameService.PARAM_HOST_DATA);
            }
        }

        return requestContext.getHostData();
    }

}
