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

package com.openexchange.folder.json.actions;

import static com.openexchange.folder.json.Tools.getUnsignedInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.oauth.OAuthConstants;
import com.openexchange.calendar.json.AppointmentActionFactory;
import com.openexchange.contacts.json.ContactActionFactory;
import com.openexchange.exception.OXException;
import com.openexchange.folder.json.services.ServiceRegistry;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.SystemContentType;
import com.openexchange.folderstorage.database.contentType.CalendarContentType;
import com.openexchange.folderstorage.database.contentType.ContactContentType;
import com.openexchange.folderstorage.database.contentType.TaskContentType;
import com.openexchange.java.Strings;
import com.openexchange.oauth.provider.exceptions.OAuthInsufficientScopeException;
import com.openexchange.oauth.provider.grant.OAuthGrant;
import com.openexchange.tasks.json.TaskActionFactory;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractFolderAction} - An abstract folder action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractFolderAction implements AJAXActionService {

    /**
     * <code>"ignoreTranslation"</code>.
     */
    public static final String PARAM_IGNORE_TRANSLATION = "ignoreTranslation".intern();

    /**
     * Initializes a new {@link AbstractFolderAction}.
     */
    protected AbstractFolderAction() {
        super();
    }

    @Override
    public final AJAXRequestResult perform(final AJAXRequestData request, final ServerSession session) throws OXException {
        try {
            return doPerform(request, session);
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Performs the action.
     *
     * @param request The AJAX request data
     * @param session The associated session
     * @throws OXException If an OX error occurs
     * @throws JSONException If a JSON error occurs
     */
    protected abstract AJAXRequestResult doPerform(final AJAXRequestData request, final ServerSession session) throws OXException, JSONException;

    /**
     * Checks if <i>Unified Mail</i> shall be suppressed for specified request.
     *
     * @param request The request data
     * @param session The associated session
     * @return <code>true</code> to suppress <i>Unified Mail</i>; otherwise <code>false</code>
     */
    protected static Boolean isSuppressUnifiedMail(final AJAXRequestData request, final ServerSession session) {
        return Boolean.valueOf(isUsmEas(session.getClient()));
    }

    private static boolean isUsmEas(final String clientId) {
        if (Strings.isEmpty(clientId)) {
            return false;
        }
        final String uc = Strings.toUpperCase(clientId);
        return uc.startsWith("USM-EAS") || uc.startsWith("USM-JSON");
    }

    /**
     * Gets the default tree identifier to use if request does not provide any.
     *
     * @return The default tree identifier
     */
    protected static String getDefaultTreeIdentifier() {
        return FolderStorage.REAL_TREE_ID;
    }

    /**
     * Gets the default allowed modules.
     *
     * @param request The request
     * @return The default allowed modules
     */
    protected static List<ContentType> getDefaultAllowedModules(final AJAXRequestData request) {
        if (isOAuthRequest(request)) {
            return new ArrayList<>(getReadableContentTypesForOAuthRequest(getOAuthGrant(request)));
        }

        return Collections.emptyList();
    }

    private static final Pattern PAT = Pattern.compile(" *, *");

    /**
     * Parses specified parameter into an array of <code>int</code>.
     *
     * @param parameterName The parameter name
     * @param request The request
     * @return The parsed array of <code>int</code>
     * @throws OXException If parameter is not present in given request
     */
    protected static int[] parseIntArrayParameter(final String parameterName, final AJAXRequestData request) throws OXException {
        final String tmp = request.getParameter(parameterName);
        if (null == tmp) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(parameterName);
        }
        final String[] sa = PAT.split(tmp, 0);
        final int[] columns = new int[sa.length];
        for (int i = 0; i < sa.length; i++) {
            columns[i] = getUnsignedInteger(sa[i]);
        }
        return columns;
    }

    /**
     * Parses specified optional parameter into an array of <code>int</code>.
     *
     * @param parameterName The parameter name
     * @param request The request
     * @return The parsed array of <code>int</code>; a zero length array is returned if parameter is missing
     */
    protected static int[] parseOptionalIntArrayParameter(final String parameterName, final AJAXRequestData request) {
        final String tmp = request.getParameter(parameterName);
        if (null == tmp) {
            return new int[0];
        }
        final String[] sa = PAT.split(tmp, 0);
        final int[] columns = new int[sa.length];
        for (int i = 0; i < sa.length; i++) {
            columns[i] = getUnsignedInteger(sa[i]);
        }
        return columns;
    }

    /**
     * Parses the optional content type array parameter. Return {@link #getDefaultAllowedModules(AJAXRequestData)} if not present.
     *
     * @param request The request
     * @return The parsed array of {@link ContentType} as a list.
     * @throws OXException If an invalid content type is denoted
     */
    protected static List<ContentType> collectAllowedContentTypes(final AJAXRequestData request) throws OXException {
        final String tmp = request.getParameter("allowed_modules");
        if (null == tmp) {
            return getDefaultAllowedModules(request);
        }
        final String[] sa = PAT.split(tmp, 0);
        final List<ContentType> ret = new ArrayList<ContentType>(sa.length);
        /*
         * Get available content types
         */
        final Map<Integer, ContentType> availableContentTypes = ServiceRegistry.getInstance().getService(FolderService.class, true).getAvailableContentTypes();

        Map<String, ContentType> tmpMap = null;
        for (final String str : sa) {
            final int module = getUnsignedInteger(str);
            final ContentType contentType;
            if (module < 0) {
                /*
                 * Not a number
                 */
                if (null == tmpMap) {
                    tmpMap = new HashMap<String, ContentType>(availableContentTypes.size());
                    for (final ContentType ct : availableContentTypes.values()) {
                        tmpMap.put(ct.toString(), ct);
                    }
                }
                contentType = tmpMap.get(str);
            } else {
                final Integer key = Integer.valueOf(module);
                contentType = availableContentTypes.get(key);
            }

            if (null == contentType) {
                org.slf4j.LoggerFactory.getLogger(AbstractFolderAction.class).error("No content type for module: {}", str);
                throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("allowed_modules", tmp);
            }

            Set<ContentType> oAuthWhitelist = null;
            if (isOAuthRequest(request)) {
                oAuthWhitelist = getReadableContentTypesForOAuthRequest(getOAuthGrant(request));
            }
            if (oAuthWhitelist != null && !oAuthWhitelist.contains(contentType)) {
                throw new OAuthInsufficientScopeException(OAuthContentTypes.readScopeForContentType(contentType));
            }

            ret.add(contentType);
        }
        return ret;
    }

    protected static ContentType parseAndCheckContentTypeParameter(final String parameterName, final AJAXRequestData request) throws OXException {
        final String tmp = request.getParameter(parameterName);
        if (null == tmp) {
            return null;
        }
        /*
         * Get available content types
         */
        final Map<Integer, ContentType> availableContentTypes =
            ServiceRegistry.getInstance().getService(FolderService.class, true).getAvailableContentTypes();
        final int module = getUnsignedInteger(tmp);
        ContentType contentType = null;
        if (module < 0) {
            /*
             * Not a number
             */
            for (final Map.Entry<Integer, ContentType> entry : availableContentTypes.entrySet()) {
                final ContentType ct = entry.getValue();
                if (ct.toString().equals(tmp)) {
                    contentType = ct;
                    break;
                }
            }
        } else {
            /*
             * A number
             */
            final Integer key = Integer.valueOf(module);
            contentType = availableContentTypes.get(key);
        }

        if (null == contentType) {
            org.slf4j.LoggerFactory.getLogger(AbstractFolderAction.class).error("No content type for module: {}", tmp);
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(parameterName, tmp);
        }

        Set<ContentType> oAuthWhitelist = null;
        if (isOAuthRequest(request)) {
            oAuthWhitelist = getReadableContentTypesForOAuthRequest(getOAuthGrant(request));
        }
        if (oAuthWhitelist != null && !oAuthWhitelist.contains(contentType)) {
            throw new OAuthInsufficientScopeException(OAuthContentTypes.readScopeForContentType(contentType));
        }

        return contentType;
    }

    static final class OAuthContentTypes {

        static ContentType contentTypeForReadScope(String scope) {
            switch (scope) {
                case ContactActionFactory.OAUTH_READ_SCOPE:
                    return ContactContentType.getInstance();

                case AppointmentActionFactory.OAUTH_READ_SCOPE:
                    return CalendarContentType.getInstance();

                case TaskActionFactory.OAUTH_READ_SCOPE:
                    return TaskContentType.getInstance();

                default:
                    return null;
            }
        }

        static ContentType contentTypeForWriteScope(String scope) {
            switch (scope) {
                case ContactActionFactory.OAUTH_WRITE_SCOPE:
                    return ContactContentType.getInstance();

                case AppointmentActionFactory.OAUTH_WRITE_SCOPE:
                    return CalendarContentType.getInstance();

                case TaskActionFactory.OAUTH_WRITE_SCOPE:
                    return TaskContentType.getInstance();

                default:
                    return null;
            }
        }

        static String readScopeForContentType(ContentType contentType) {
            if (contentType == ContactContentType.getInstance()) {
                return ContactActionFactory.OAUTH_READ_SCOPE;
            } else if (contentType == CalendarContentType.getInstance()) {
                return AppointmentActionFactory.OAUTH_READ_SCOPE;
            } else if (contentType == TaskContentType.getInstance()) {
                return TaskActionFactory.OAUTH_READ_SCOPE;
            }

            return null;
        }

        static String writeScopeForContentType(ContentType contentType) {
            if (contentType == ContactContentType.getInstance()) {
                return ContactActionFactory.OAUTH_WRITE_SCOPE;
            } else if (contentType == CalendarContentType.getInstance()) {
                return AppointmentActionFactory.OAUTH_WRITE_SCOPE;
            } else if (contentType == TaskContentType.getInstance()) {
                return TaskActionFactory.OAUTH_WRITE_SCOPE;
            }

            return null;
        }

    }

    /**
     * Check whether the given request is made via OAuth.
     *
     * @param request The request
     * @return <code>true</code> if so
     */
    protected static boolean isOAuthRequest(AJAXRequestData request) {
        return request.containsProperty(OAuthConstants.PARAM_OAUTH_GRANT);
    }

    /**
     * Gets the OAuth grant if the given request is made via OAuth.
     *
     * @param request The request
     * @return The grant
     */
    protected static OAuthGrant getOAuthGrant(AJAXRequestData request) {
        return request.getProperty(OAuthConstants.PARAM_OAUTH_GRANT);
    }

    /**
     * Checks whether write operations are permitted for the given folder content type and OAuth grant.
     *
     * @param contentType The content type
     * @param grant The grant
     * @return <code>true</code> if write operations are permitted
     */
    protected static boolean mayWriteViaOAuthRequest(ContentType contentType, OAuthGrant grant) {
        String scope = OAuthContentTypes.writeScopeForContentType(contentType);
        if (scope != null && grant.getScopes().has(scope)) {
            return true;
        }

        return false;
    }

    /**
     * Checks whether read operations are permitted for the given folder content type and OAuth grant.
     *
     * @param contentType The content type
     * @param grant The grant
     * @return <code>true</code> if read operations are permitted
     */
    protected static boolean mayReadViaOAuthRequest(ContentType contentType, OAuthGrant grant) {
        if (contentType == SystemContentType.getInstance()) {
            return true;
        }

        String scope = OAuthContentTypes.readScopeForContentType(contentType);
        if (scope != null && grant.getScopes().has(scope)) {
            return true;
        }

        return false;
    }

    /**
     * Gets all content types whose folders are readable for an OAuth request.
     *
     * @param grant The grant
     * @return A set of content types
     */
    protected static Set<ContentType> getReadableContentTypesForOAuthRequest(OAuthGrant grant) {
        Set<ContentType> contentTypes = new HashSet<>();
        contentTypes.add(SystemContentType.getInstance());
        for (String scope : grant.getScopes().get()) {
            ContentType contentType = OAuthContentTypes.contentTypeForReadScope(scope);
            if (contentType != null) {
                contentTypes.add(contentType);
            }
        }

        return contentTypes;
    }

    private static Set<String> TRUES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("true", "yes", "on", "1", "y")));

    /**
     * Parses string to boolean.
     *
     * @param string The string to parse
     * @param defaultValue The default value to return if passed string is <code>null</code>
     * @return The parsed boolean value or the default value if passed string is <code>null</code>
     */
    public static boolean parseBoolean(final String string, final boolean defaultValue) {
        if (null == string) {
            return defaultValue;
        }
        return TRUES.contains(toLowerCase(string).trim());
    }

    protected Map<String, Object> parametersFor(final Object... objects) {
        if (null == objects) {
            return null;
        }
        final int length = objects.length;
        if (length == 0) {
            return null;
        }
        if (length % 2 != 0) {
            throw new IllegalArgumentException("Eden number of objects required");
        }
        final Map<String, Object> ret = new HashMap<String, Object>(length >> 1);
        for (int i = 0; i < length; i += 2) {
            ret.put(objects[i].toString(), objects[i+1]);
        }
        return ret;
    }

    /** ASCII-wise to lower-case */
    private static String toLowerCase(final CharSequence chars) {
        if (null == chars) {
            return null;
        }
        final int length = chars.length();
        final StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            final char c = chars.charAt(i);
            builder.append((c >= 'A') && (c <= 'Z') ? (char) (c ^ 0x20) : c);
        }
        return builder.toString();
    }

}
