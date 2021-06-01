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

package com.openexchange.folder.json.actions;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.util.Tools.getUnsignedInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction.Type;
import com.openexchange.ajax.requesthandler.oauth.OAuthConstants;
import com.openexchange.calendar.json.AppointmentActionFactory;
import com.openexchange.chronos.json.oauth.ChronosOAuthScope;
import com.openexchange.contacts.json.ContactActionFactory;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.json.actions.files.AbstractFileAction;
import com.openexchange.folder.json.FolderField;
import com.openexchange.folder.json.Tools;
import com.openexchange.folder.json.parser.FolderParser;
import com.openexchange.folder.json.parser.NotificationData;
import com.openexchange.folder.json.parser.ParsedFolder;
import com.openexchange.folder.json.services.ServiceRegistry;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.ContentTypeDiscoveryService;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.folderstorage.SystemContentType;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.contact.ContactContentType;
import com.openexchange.folderstorage.database.contentType.CalendarContentType;
import com.openexchange.folderstorage.database.contentType.ContactsContentType;
import com.openexchange.folderstorage.database.contentType.InfostoreContentType;
import com.openexchange.folderstorage.database.contentType.TaskContentType;
import com.openexchange.folderstorage.mail.contentType.DraftsContentType;
import com.openexchange.folderstorage.mail.contentType.MailContentType;
import com.openexchange.folderstorage.mail.contentType.SentContentType;
import com.openexchange.folderstorage.mail.contentType.SpamContentType;
import com.openexchange.folderstorage.mail.contentType.TrashContentType;
import com.openexchange.folderstorage.oauth.OAuthFolderErrorCodes;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.i18n.I18nService;
import com.openexchange.i18n.I18nServiceRegistry;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.java.Strings;
import com.openexchange.mail.json.MailActionFactory;
import com.openexchange.oauth.provider.exceptions.OAuthInsufficientScopeException;
import com.openexchange.oauth.provider.resourceserver.OAuthAccess;
import com.openexchange.session.Session;
import com.openexchange.share.ShareTargetPath;
import com.openexchange.share.notification.Entities;
import com.openexchange.share.notification.Entities.PermissionType;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.share.notification.ShareNotificationService.Transport;
import com.openexchange.share.notification.ShareNotifyExceptionCodes;
import com.openexchange.tasks.json.TaskActionFactory;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractFolderAction} - An abstract folder action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction(module = AbstractFolderAction.MODULE, type = RestrictedAction.Type.READ)
public abstract class AbstractFolderAction implements AJAXActionService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractFolderAction.class);

    protected static final String PARAM_AUTORENAME = "autorename";

    protected static final String MODULE = "folders";

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
            parsePushTokenParameter(request);
            return doPerform(request, session);
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (OXException e) {
            if (OAuthFolderErrorCodes.NO_ACCOUNT_ACCESS.equals(e)) {
                throw AjaxExceptionCodes.HTTP_ERROR.create(OAuthFolderErrorCodes.getHttpStatus(e), e.getMessage());
            }
            throw e;
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
     * @return <code>true</code> to suppress <i>Unified Mail</i>; otherwise <code>false</code>
     */
    protected static Boolean isSuppressUnifiedMail(final AJAXRequestData request) {
        Session ses = request.getSession();
        return Boolean.valueOf((ses != null && isUsmEas(ses.getClient())) || isOAuthRequest(request));
    }

    /**
     * Creates a new {@link FolderServiceDecorator} with some basic configuration
     *
     * @param request The {@link AJAXRequestData}
     * @return The {@link FolderServiceDecorator}
     * @throws OXException in case of errors
     */
    protected static FolderServiceDecorator getDecorator(final AJAXRequestData request) throws OXException {
        final String timeZoneId = request.getParameter(AJAXServlet.PARAMETER_TIMEZONE);
        //        final String mailRootFolders = request.getParameter("mailRootFolders");
        final java.util.List<ContentType> allowedContentTypes = collectAllowedContentTypes(request);
        final TimeZone timeZone = Tools.getTimeZone(timeZoneId);
        final String sAltNames = "altNames";
        final String altNames = request.getParameter(sAltNames);
        final String sSuppressUnifiedMail = "suppressUnifiedMail";
        final Boolean suppressUnifiedMail = isSuppressUnifiedMail(request);
        Locale optLocale = optLocale(request);

        // @formatter:off
        return new FolderServiceDecorator().setLocale(optLocale)
                                           .setTimeZone(timeZone)
                                           .setAllowedContentTypes(allowedContentTypes)
                                           .put(sAltNames, altNames)
                                           .put(sSuppressUnifiedMail, suppressUnifiedMail);
        // @formatter:on
    }

    private static boolean isUsmEas(final String clientId) {
        if (Strings.isEmpty(clientId)) {
            return false;
        }
        final String uc = Strings.toUpperCase(clientId);
        return uc.startsWith("USM-EAS") || uc.startsWith("USM-JSON");
    }

    /**
     * Gets the possibly specified <code>"language"</code> parameter from given request and parses its value to an appropriate instance of {@link Locale}.
     *
     * @param request The request to get the parameter from
     * @return The locale or <code>null</code>
     */
    protected static Locale optLocale(AJAXRequestData request) {
        String sLocale = request.getParameter("language");
        if (Strings.isEmpty(sLocale)) {
            return null;
        }

        Locale locale = LocaleTools.getLocale(sLocale);
        if (null == locale) {
            LOG.warn("Specified \"language\" parameter (\"{}\") cannot be parsed to a locale. Using user's locale instead.", sLocale);
            return null;
        }

        return tryBestFitFor(locale);
    }

    private static Locale tryBestFitFor(Locale locale) {
        I18nServiceRegistry i18nServiceRegistry = ServiceRegistry.getInstance().getService(I18nServiceRegistry.class);
        if (null == i18nServiceRegistry) {
            // Don't know better
            return null;
        }

        I18nService i18nService = i18nServiceRegistry.getI18nService(locale);
        if (null == i18nService) {
            LOG.warn("No suitable support for locale \"{}\". Using user's locale instead.", locale.toString());
            return null;
        }

        return i18nService.getLocale();
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
            return new ArrayList<>(getReadableContentTypesForOAuthRequest(getOAuthAccess(request)));
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
        String allowedModules = request.getParameter("allowed_modules");
        if (null == allowedModules) {
            return getDefaultAllowedModules(request);
        }
        /*
         * Get available content types & prepeare oauth whitelist if applicable
         */
        Map<Integer, List<ContentType>> availableContentTypes = ServiceRegistry.getInstance().getService(FolderService.class, true).getAvailableContentTypes();
        Set<ContentType> oAuthWhitelist = isOAuthRequest(request) ? getReadableContentTypesForOAuthRequest(getOAuthAccess(request)) : null;
        /*
         * collect allowed content types matching supplied module strings
         */
        List<ContentType> allowedContentTypes = new ArrayList<>();
        for (String module : PAT.split(allowedModules, 0)) {
            List<ContentType> matchingContentTypes = getMatchingContentTypes(availableContentTypes, module);
            if (null == matchingContentTypes || matchingContentTypes.isEmpty()) {
                org.slf4j.LoggerFactory.getLogger(AbstractFolderAction.class).error("No content type for module: {}", module);
                throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("allowed_modules", allowedModules);
            }
            if (null != oAuthWhitelist) {
                for (ContentType matchingContentType : matchingContentTypes) {
                    if (false == oAuthWhitelist.contains(matchingContentType)) {
                        throw new OAuthInsufficientScopeException(OAuthContentTypes.readScopeForContentType(matchingContentType));
                    }
                }
            }
            allowedContentTypes.addAll(matchingContentTypes);
        }
        return allowedContentTypes;
    }

    /**
     * Gets all content types matching a specific module.
     *
     * @param availableContentTypes The availabale content types as fetched from folder service
     * @param moduleString The module, either as numerical module identifier or module string
     * @return The matching content types, or an empty list if there are none
     */
    private static List<ContentType> getMatchingContentTypes(Map<Integer, List<ContentType>> availableContentTypes, String moduleString) {
        if (null == availableContentTypes || 0 == availableContentTypes.size() || Strings.isEmpty(moduleString)) {
            return Collections.emptyList();
        }
        int module = getUnsignedInteger(moduleString);
        if (-1 != module) {
            /*
             * get content types by module identifier
             */
            return availableContentTypes.get(I(module));
        }
        /*
         * get content types by module name
         */
        List<ContentType> matchingContentTypes = new ArrayList<>();
        for (List<ContentType> contentTypes : availableContentTypes.values()) {
            for (ContentType contentType : contentTypes) {
                if (moduleString.equals(contentType.toString())) {
                    matchingContentTypes.add(contentType);
                }
            }
        }
        return matchingContentTypes;
    }

    protected static ContentType parseAndCheckContentTypeParameter(final String parameterName, final AJAXRequestData request) throws OXException {
        final String tmp = request.getParameter(parameterName);
        if (null == tmp) {
            return null;
        }
        ContentType contentType = ServiceRegistry.getInstance().getService(FolderService.class, true).parseContentType(tmp);
        if (null == contentType) {
            org.slf4j.LoggerFactory.getLogger(AbstractFolderAction.class).error("No content type for module: {}", tmp);
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(parameterName, tmp);
        }

        Set<ContentType> oAuthWhitelist = null;
        if (isOAuthRequest(request)) {
            oAuthWhitelist = getReadableContentTypesForOAuthRequest(getOAuthAccess(request));
        }
        if (oAuthWhitelist != null && !containsContentTypeName(oAuthWhitelist, contentType)) {
            throw new OAuthInsufficientScopeException(OAuthContentTypes.readScopeForContentType(contentType));
        }

        return contentType;
    }

    /**
     * Checks whether operations are permitted for the given folder content type.
     * Therefore it is checked if the content type is contained in the list of allowed content types.
     *
     * @param oAuthWhitelist
     * @param contentType
     * @return True if the content type is present. Otherwise false.
     */
    private static boolean containsContentTypeName(Set<ContentType> oAuthWhitelist, ContentType contentType){
        return oAuthWhitelist.stream().filter(o -> o.toString().equals(contentType.toString())).findFirst().isPresent();
    }

    static final class OAuthContentTypes {

        private OAuthContentTypes() {
            throw new IllegalStateException("Utility class");
        }

        /**
         * Gets the {@link ContentType}s valid for the given read oauth scope
         *
         * @param scope The read oauth scope
         * @return A set of valid {@link ContentType}s
         */
        static Set<ContentType> contentTypesForReadScope(String scope) {
            switch (Type.READ.getModule(scope)) {
                // Contacts
                case ContactActionFactory.MODULE:
                    Set<ContentType> contactContentTypes = new HashSet<>(2);
                    contactContentTypes.add(ContactContentType.getInstance());
                    contactContentTypes.add(ContactsContentType.getInstance());
                    return contactContentTypes;
                // Calendar
                case ChronosOAuthScope.MODULE:
                    Set<ContentType> result = new HashSet<>(2);
                    result.add(CalendarContentType.getInstance());
                    result.add(com.openexchange.folderstorage.calendar.contentType.CalendarContentType.getInstance());
                    return result;

                // Tasks
                case TaskActionFactory.MODULE:
                    return Collections.singleton((ContentType) TaskContentType.getInstance());

                // Mails
                case MailActionFactory.MODULE:
                    Set<ContentType> mailContentTypes = new HashSet<>(4);
                    mailContentTypes.add(MailContentType.getInstance());
                    mailContentTypes.add(DraftsContentType.getInstance());
                    mailContentTypes.add(SentContentType.getInstance());
                    mailContentTypes.add(TrashContentType.getInstance());
                    mailContentTypes.add(SpamContentType.getInstance());
                    return mailContentTypes;

                //Infostore
                case AbstractFileAction.MODULE:
                    return Collections.singleton((ContentType) InfostoreContentType.getInstance());

                default:
                    return Collections.emptySet();
            }
        }

        /**
         * Gets the {@link ContentType}s valid for the given write oauth scope
         *
         * @param scope The write oauth scope
         * @return A set of valid {@link ContentType}s
         */
        static Set<ContentType> contentTypesForWriteScope(String scope) {

            switch (Type.WRITE.getModule(scope)) {
                // Contacts
                case ContactActionFactory.MODULE:
                    Set<ContentType> contactContentTypes = new HashSet<>(2);
                    contactContentTypes.add(ContactContentType.getInstance());
                    contactContentTypes.add(ContactsContentType.getInstance());
                    return contactContentTypes;
                // Calendar
                case ChronosOAuthScope.MODULE:
                    Set<ContentType> result = new HashSet<>(2);
                    result.add(CalendarContentType.getInstance());
                    result.add(com.openexchange.folderstorage.calendar.contentType.CalendarContentType.getInstance());
                    return result;

                // Tasks
                case TaskActionFactory.MODULE:
                    return Collections.singleton((ContentType) TaskContentType.getInstance());

                // Mails
                case MailActionFactory.MODULE:
                    Set<ContentType> mailContentTypes = new HashSet<>(4);
                    mailContentTypes.add(MailContentType.getInstance());
                    mailContentTypes.add(DraftsContentType.getInstance());
                    mailContentTypes.add(SentContentType.getInstance());
                    mailContentTypes.add(TrashContentType.getInstance());
                    mailContentTypes.add(SpamContentType.getInstance());
                    return mailContentTypes;

                //Infostore
                case AbstractFileAction.MODULE:
                    return Collections.singleton((ContentType) InfostoreContentType.getInstance());

                default:
                    return Collections.emptySet();
            }
        }

        static String readScopeForContentType(ContentType contentType) {
            if (contentType == ContactContentType.getInstance()||
                contentType == ContactsContentType.getInstance()) {
                return Type.READ.getScope(ContactActionFactory.MODULE);
            } else if (contentType == CalendarContentType.getInstance()) {
                return Type.READ.getScope(AppointmentActionFactory.MODULE);
            } else if (contentType == com.openexchange.folderstorage.calendar.contentType.CalendarContentType.getInstance()) {
                return Type.READ.getScope(ChronosOAuthScope.MODULE);
            } else if (contentType == TaskContentType.getInstance()) {
                return Type.READ.getScope(TaskActionFactory.MODULE);
            } else if (contentType == MailContentType.getInstance() ||
                       contentType == DraftsContentType.getInstance() ||
                       contentType == SentContentType.getInstance() ||
                       contentType == SpamContentType.getInstance() ||
                       contentType == TrashContentType.getInstance()) {
                return Type.READ.getScope(MailActionFactory.MODULE);
            } else if (contentType == InfostoreContentType.getInstance()) {
                return Type.READ.getScope(AbstractFileAction.MODULE);
            }

            return null;
        }

        static String writeScopeForContentType(ContentType contentType) {
            if (contentType == ContactContentType.getInstance()||
                contentType == ContactsContentType.getInstance()) {
                return Type.WRITE.getScope(ContactActionFactory.MODULE);
            } else if (contentType == CalendarContentType.getInstance()) {
                return Type.WRITE.getScope(AppointmentActionFactory.MODULE);
            } else if (contentType == com.openexchange.folderstorage.calendar.contentType.CalendarContentType.getInstance()) {
                return Type.WRITE.getScope(ChronosOAuthScope.MODULE);
            } else if (contentType == TaskContentType.getInstance()) {
                return Type.WRITE.getScope(TaskActionFactory.MODULE);
            } else if ( contentType == MailContentType.getInstance() ||
                        contentType == DraftsContentType.getInstance() ||
                        contentType == SentContentType.getInstance() ||
                        contentType == SpamContentType.getInstance() ||
                        contentType == TrashContentType.getInstance()) {
                return Type.WRITE.getScope(MailActionFactory.MODULE);
            } else if (contentType == InfostoreContentType.getInstance()) {
                return Type.WRITE.getScope(AbstractFileAction.MODULE);
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
        return request.containsProperty(OAuthConstants.PARAM_OAUTH_ACCESS);
    }

    /**
     * Gets the OAuth oauthAccess if the given request is made via OAuth.
     *
     * @param request The request
     * @return The oauthAccess
     */
    protected static OAuthAccess getOAuthAccess(AJAXRequestData request) {
        return request.getProperty(OAuthConstants.PARAM_OAUTH_ACCESS);
    }

    /**
     * Checks whether write operations are permitted for the given folder content type and OAuth oauthAccess.
     *
     * @param contentType The content type
     * @param oauthAccess The oauthAccess
     * @return <code>true</code> if write operations are permitted
     */
    protected static boolean mayWriteViaOAuthRequest(ContentType contentType, OAuthAccess oauthAccess) {
        String scope = OAuthContentTypes.writeScopeForContentType(contentType);
        if (scope != null && oauthAccess.getScope().has(scope)) {
            return true;
        }

        return false;
    }

    /**
     * Checks whether read operations are permitted for the given folder content type and OAuth oauthAccess.
     *
     * @param contentType The content type
     * @param oauthAccess The oauthAccess
     * @return <code>true</code> if read operations are permitted
     */
    protected static boolean mayReadViaOAuthRequest(ContentType contentType, OAuthAccess oauthAccess) {
        if (contentType == SystemContentType.getInstance()) {
            return true;
        }

        String scope = OAuthContentTypes.readScopeForContentType(contentType);
        if (scope != null && oauthAccess.getScope().has(scope)) {
            return true;
        }

        return false;
    }

    /**
     * Gets all content types whose folders are readable for an OAuth request.
     *
     * @param oauthAccess The oauthAccess
     * @return A set of content types
     */
    protected static Set<ContentType> getReadableContentTypesForOAuthRequest(OAuthAccess oauthAccess) {
        Set<ContentType> contentTypes = new HashSet<>();
        contentTypes.add(SystemContentType.getInstance());
        for (String scope : oauthAccess.getScope().get()) {
            Set<ContentType> contentType = OAuthContentTypes.contentTypesForReadScope(scope);
            if (!contentType.isEmpty()) {
                contentTypes.addAll(contentType);
            }
        }

        return contentTypes;
    }

    private static Set<String> TRUES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("true", "yes", "on", "1", "y")));

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
        return TRUES.contains(com.openexchange.java.Strings.toLowerCase(string).trim());
    }

    /**
     * Gets the timezone applicable for a request.
     *
     * @param requestData The underlying request data
     * @param session The associated session
     * @return The timezone
     */
    protected static TimeZone getTimeZone(AJAXRequestData requestData, ServerSession session) {
        String timeZoneID = requestData.getParameter("timezone");
        if (null == timeZoneID) {
            timeZoneID = session.getUser().getTimeZone();
        }
        return TimeZone.getTimeZone(timeZoneID);
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
        final Map<String, Object> ret = new HashMap<>(length >> 1);
        for (int i = 0; i < length; i += 2) {
            ret.put(objects[i].toString(), objects[i + 1]);
        }
        return ret;
    }

    /**
     * Send out share notifications for added permission entities. Those entities are calculated based on
     * the passed folder objects.
     *
     * @param notificationData The notification data
     * @param original The folder before any permission changes took place; may be <code>null</code> in case of a newly created folder
     * @param modified The folder after any permission changes took place
     * @param session The session
     * @param hostData The host data
     * @return A list of warnings to be included in the API response
     */
    protected List<OXException> sendNotifications(NotificationData notificationData, UserizedFolder original, UserizedFolder modified, ServerSession session, HostData hostData) {
        if (hostData == null) {
            return Collections.singletonList(ShareNotifyExceptionCodes.UNEXPECTED_ERROR.create("HostData was not available"));
        }

        List<Permission> addedPermissions = determineAddedPermissions(original, modified);
        if (addedPermissions.isEmpty()) {
            return Collections.emptyList();
        }

        ShareNotificationService notificationService = ServiceRegistry.getInstance().getService(ShareNotificationService.class);
        if (notificationService == null) {
            return Collections.singletonList(ShareNotifyExceptionCodes.UNEXPECTED_ERROR.create("ShareNotificationService was absent"));
        }

        Entities entities = new Entities();
        for (Permission permission : addedPermissions) {
            if (permission.isGroup()) {
                entities.addGroup(permission.getEntity(), PermissionType.FOLDER, Permissions.createPermissionBits(permission));
            } else {
                entities.addUser(permission.getEntity(), PermissionType.FOLDER, Permissions.createPermissionBits(permission));
            }
        }

        return notificationService.sendShareCreatedNotifications(notificationData.getTransport(), entities, notificationData.getMessage(), new ShareTargetPath(modified.getContentType().getModule(), modified.getID(), null), session, hostData);
    }

    private List<Permission> determineAddedPermissions(UserizedFolder original, UserizedFolder modified) {
        Permission[] oldPermissions = original == null ? new Permission[0] : original.getPermissions();
        Permission[] newPermissions = modified.getPermissions();
        List<Permission> addedPermissions = new ArrayList<>(newPermissions.length);
        for (Permission p : newPermissions) {
            boolean isNew = true;
            for (int i = oldPermissions.length; isNew && i-- > 0;) {
                Permission o = oldPermissions[i];
                if (o.getEntity() == p.getEntity() && o.isGroup() == p.isGroup() && o.getSystem() == p.getSystem() && p.getSystem() == 0) {
                    isNew = false;
                }
            }

            if (isNew) {
                addedPermissions.add(p);
            }
        }
        return addedPermissions;
    }

    /**
     * Parses notification data from the supplied JSON object.
     *
     * @param jsonObject The JSON object to parse the notification data from
     * @return The parsed notification data, or <code>null</code> if passed JSON object was <code>null</code>
     */
    protected NotificationData parseNotificationData(JSONObject jsonObject) throws JSONException, OXException {
        if (null == jsonObject) {
            return null;
        }
        NotificationData notificationData = new NotificationData();
        Transport transport = Transport.MAIL;
        if (jsonObject.hasAndNotNull("transport")) {
            transport = Transport.forID(jsonObject.getString("transport"));
            if (transport == null) {
                throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create();
            }
        }
        notificationData.setTransport(transport);
        String message = jsonObject.optString("message", null);
        if (Strings.isNotEmpty(message)) {
            notificationData.setMessage(message);
        }
        return notificationData;
    }

    /**
     * Parses the request body of create and update requests which encapsulates the affected folder and possible other
     * data.
     *
     * @param treeId The folder tree ID
     * @param folderId The requested folder ID
     * @param request The AJAX request data
     * @param session The session
     * @return The data
     * @throws OXException If the request body is invalid
     */
    protected UpdateData parseRequestBody(String treeId, String folderId, AJAXRequestData request, ServerSession session) throws OXException {
        UpdateData updateData = new UpdateData();
        JSONObject folderObject;
        {
            JSONObject data = (JSONObject) request.requireData();
            if (data.hasAndNotNull("folder")) {
                try {
                    folderObject = data.getJSONObject("folder");
                    updateData.setNotificationData(parseNotificationData(data.optJSONObject("notification")));
                } catch (JSONException e) {
                    throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create(e);
                }
            } else {
                folderObject = data;
            }
        }

        final ParsedFolder folder = new FolderParser(ServiceRegistry.getInstance().getService(ContentTypeDiscoveryService.class)).parseFolder(folderObject, getTimeZone(request, session));
        if (folderId != null) {
            folder.setID(folderId);
            try {
                final String fieldName = FolderField.SUBSCRIBED.getName();
                if (folderObject.hasAndNotNull(fieldName) && 0 == folderObject.getInt(fieldName)) {
                    /*
                     * TODO: Remove this ugly hack to fix broken UI behavior which send "subscribed":0 for db folders
                     */
                    try {
                        Integer.parseInt(folderId);
                        folder.setSubscribed(true);
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                }
            } catch (JSONException e) {
                // Ignore
            }
        }
        folder.setTreeID(treeId);

        updateData.setFolder(folder);
        return updateData;
    }

    /**
     * Retrieves the optional pushToken parameter from the request and adds it to the session
     *
     * @param request The request of the action
     */
    private void parsePushTokenParameter(AJAXRequestData request) {
        String pushToken = request.getParameter("pushToken");
        Session session = request.getSession();
        if (session != null && Strings.isNotEmpty(pushToken)) {
            session.setParameter(Session.PARAM_PUSH_TOKEN, pushToken);
        }
    }

    /**
     * Encapsulates the parsed request body data of create and update requests
     */
    protected static final class UpdateData {

        private ParsedFolder folder;
        private NotificationData notificationData;

        UpdateData() {
            super();
        }

        /**
         * Sets the folder
         *
         * @param folder The folder to set
         */
        public void setFolder(ParsedFolder folder) {
            this.folder = folder;
        }

        /**
         * Gets the folder
         *
         * @return The folder
         */
        public ParsedFolder getFolder() {
            return folder;
        }

        /**
         * Gets whether permission entities shall be notified about
         * changes (i.e. if they have been added to a folder)
         */
        public boolean notifyPermissionEntities() {
            return notificationData != null;
        }

        /**
         * Gets the notification data
         *
         * @return The notification data or <code>null</code>
         */
        public NotificationData getNotificationData() {
            return notificationData;
        }

        /**
         * Sets the notification data
         *
         * @param notificationData The notification data to set
         */
        public void setNotificationData(NotificationData notificationData) {
            this.notificationData = notificationData;
        }

    }
}
