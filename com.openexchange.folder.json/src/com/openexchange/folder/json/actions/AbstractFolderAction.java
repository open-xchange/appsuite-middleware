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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import com.openexchange.exception.OXException;
import com.openexchange.folder.json.services.ServiceRegistry;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.java.StringAllocator;
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
     * @return The default allowed modules
     */
    protected static List<ContentType> getDefaultAllowedModules() {
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
     * Parses the optional content type array parameter. Return {@link #getDefaultAllowedModules()} if not present.
     *
     * @param parameterName The parameter name
     * @param request The request
     * @return The parsed array of {@link ContentType} as a list.
     * @throws OXException If an invalid content type is denoted
     */
    protected static List<ContentType> parseOptionalContentTypeArrayParameter(final String parameterName, final AJAXRequestData request) throws OXException {
        final String tmp = request.getParameter(parameterName);
        if (null == tmp) {
            return getDefaultAllowedModules();
        }
        final String[] sa = PAT.split(tmp, 0);
        final List<ContentType> ret = new ArrayList<ContentType>(sa.length);
        /*
         * Get available content types
         */
        final Map<Integer, ContentType> availableContentTypes =
            ServiceRegistry.getInstance().getService(FolderService.class, true).getAvailableContentTypes();
        Map<String, ContentType> tmpMap = null;
        for (final String str : sa) {
            final int module = getUnsignedInteger(str);
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
                final ContentType ct = tmpMap.get(str);
                if (null == ct) {
                    com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(AbstractFolderAction.class)).error("No content type for string: " + str);
                    throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(parameterName, tmp);
                }
                ret.add(ct);
            } else {
                final Integer key = Integer.valueOf(module);
                final ContentType ct = availableContentTypes.get(key);
                if (null == ct) {
                    com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(AbstractFolderAction.class)).error("No content type for module: " + key);
                    throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(parameterName, tmp);
                }
                ret.add(ct);
            }
        }
        return ret;
    }

    protected static ContentType parseContentTypeParameter(final String parameterName, final AJAXRequestData request) throws OXException {
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
        if (module < 0) {
            /*
             * Not a number
             */
            for (final Map.Entry<Integer, ContentType> entry : availableContentTypes.entrySet()) {
                final ContentType ct = entry.getValue();
                if (ct.toString().equals(tmp)) {
                    return ct;
                }
            }
            /*
             * Not found
             */
            com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(AbstractFolderAction.class)).error("No content type for module: " + tmp);
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(parameterName, tmp);
        }
        /*
         * A number
         */
        final Integer key = Integer.valueOf(module);
        final ContentType ct = availableContentTypes.get(key);
        if (null == ct) {
            com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(AbstractFolderAction.class)).error("No content type for module: " + key);
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(parameterName, tmp);
        }
        return ct;
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
        final StringAllocator builder = new StringAllocator(length);
        for (int i = 0; i < length; i++) {
            final char c = chars.charAt(i);
            builder.append((c >= 'A') && (c <= 'Z') ? (char) (c ^ 0x20) : c);
        }
        return builder.toString();
    }

}
