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

package com.openexchange.mail.json.actions;

import static com.openexchange.java.Autoboxing.I;
import java.io.Closeable;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.mail.internet.InternetAddress;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.common.collect.ImmutableSet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.AJAXRequestResultPostProcessor;
import com.openexchange.ajax.requesthandler.AJAXState;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.ajax.requesthandler.crypto.CryptographicServiceAuthenticationFactory;
import com.openexchange.ajax.requesthandler.oauth.OAuthConstants;
import com.openexchange.annotation.NonNull;
import com.openexchange.antivirus.AntiVirusEncapsulatedContent;
import com.openexchange.antivirus.AntiVirusEncapsulationUtil;
import com.openexchange.antivirus.AntiVirusResult;
import com.openexchange.antivirus.AntiVirusResultEvaluatorService;
import com.openexchange.antivirus.AntiVirusService;
import com.openexchange.antivirus.exceptions.AntiVirusServiceExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailField;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.OAuthMailErrorCodes;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.json.MailActionConstants;
import com.openexchange.mail.json.MailActionFactory;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.json.utils.Column;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.mime.filler.MimeMessageFiller;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.utils.ContactCollectorUtility;
import com.openexchange.mail.utils.DisplayMode;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractMailAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction(module = AbstractMailAction.MODULE, type = RestrictedAction.Type.READ)
public abstract class AbstractMailAction implements AJAXActionService, MailActionConstants {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractMailAction.class);

    protected static final String MODULE = MailActionFactory.MODULE;

    private static final class MailInterfacePostProcessor implements AJAXRequestResultPostProcessor {

        private final MailServletInterface newMailInterface;

        MailInterfacePostProcessor(MailServletInterface newMailInterface) {
            this.newMailInterface = newMailInterface;
        }

        @Override
        public void doPostProcessing(AJAXRequestData requestData, AJAXRequestResult requestResult, Exception e) {
            newMailInterface.close();
        }
    }

    private static final AJAXRequestResult RESULT_JSON_NULL = new AJAXRequestResult(JSONObject.NULL, "json");

    public static final @NonNull int[] FIELDS_ALL_ALIAS = new int[] { 600, 601 };

    public static final @NonNull int[] FIELDS_LIST_ALIAS = new int[] { 600, 601, 614, 602, 611, 603, 612, 607, 652, 610, 608, 102 };

    public static final @NonNull Column[] COLUMNS_ALL_ALIAS = new Column[] { Column.field(600), Column.field(601) };

    public static final @NonNull Column[] COLUMNS_LIST_ALIAS = new Column[] { Column.field(600), Column.field(601), Column.field(614), Column.field(602), Column.field(611), Column.field(603), Column.field(612), Column.field(607), Column.field(652), Column.field(610), Column.field(608), Column.field(102) };

    // ----------------------------------------------------------------------------------------------------------------------------------

    private final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractMailAction}.
     */
    protected AbstractMailAction(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Cachable formats: <code>"apiResponse"</code>, <code>"json"</code>.
     */
    protected static final Set<String> CACHABLE_FORMATS = ImmutableSet.of("apiResponse", "json");

    /**
     * Gets the service of specified type
     *
     * @param clazz The service's class
     * @return The service or <code>null</code> if absent
     */
    protected <S> S getService(Class<? extends S> clazz) {
        return services.getService(clazz);
    }

    /**
     * Retrieves the specified service if available
     *
     * @return The specified service
     * @throws OXException if the service is absent
     */
    protected <S> S optService(Class<? extends S> clazz) throws OXException {
        return optService(clazz, true);
    }

    /**
     * Retrieves the specified service if available
     *
     * @param clazz The service's class
     * @param throwEx Whether to throw an exception on service's absence
     * @return The specified service
     * @throws OXException if the service is absent
     */
    protected <S> S optService(Class<? extends S> clazz, boolean throwEx) throws OXException {
        S service = services.getOptionalService(clazz);
        if (service == null && throwEx) {
            throw ServiceExceptionCode.serviceUnavailable(clazz);
        }
        return service;
    }

    /**
     * Gets the mail interface.
     *
     * @param mailRequest The mail request
     * @return The mail interface
     * @throws OXException If mail interface cannot be initialized
     */
    protected MailServletInterface getMailInterface(MailRequest mailRequest) throws OXException {
        // Requests can control whether or not to decrypt messages or verify signatures
        boolean decrypt = AJAXRequestDataTools.parseBoolParameter(mailRequest.getParameter("decrypt"));
        boolean verify = AJAXRequestDataTools.parseBoolParameter(mailRequest.getParameter("verify"));

        // Parsing crypto authentication from the request if decrypting
        String cryptoAuthentication = null;
        if (decrypt) {
            CryptographicServiceAuthenticationFactory encryptionAuthenticationFactory = services.getOptionalService(CryptographicServiceAuthenticationFactory.class);
            if (encryptionAuthenticationFactory != null) {
                cryptoAuthentication = encryptionAuthenticationFactory.createAuthenticationFrom(mailRequest.getRequest());
            }
        }

        // Get mail interface
        AJAXState state = mailRequest.getRequest().getState();
        if (state == null) {
            // No AJAX state
            MailServletInterface mailInterface = mailRequest.getMailServletInterface();
            if (mailInterface == null) {
                MailServletInterface newMailInterface = decrypt || verify ? // If decrypting or verifying, get Crypto Aware MailServlet
                    MailServletInterface.getInstanceWithDecryptionSupport(mailRequest.getSession(), cryptoAuthentication) : MailServletInterface.getInstance(mailRequest.getSession());
                mailRequest.setMailServletInterface(newMailInterface);
                mailInterface = newMailInterface;
            }
            return mailInterface;
        }

        MailServletInterface mailInterface = state.optProperty(PROPERTY_MAIL_IFACE);
        if (mailInterface == null) {
            MailServletInterface newMailInterface = decrypt || verify ? MailServletInterface.getInstanceWithDecryptionSupport(mailRequest.getSession(), cryptoAuthentication) : MailServletInterface.getInstance(mailRequest.getSession());
            mailInterface = state.putProperty(PROPERTY_MAIL_IFACE, newMailInterface);
            if (null == mailInterface) {
                mailInterface = newMailInterface;
            } else {
                newMailInterface.close(true);
            }
        }
        return mailInterface;
    }

    /**
     * Gets the closeables.
     *
     * @param mailRequest The mail request
     * @return The closeables or <code>null</code> if state is absent
     */
    protected Collection<Closeable> getCloseables(MailRequest mailRequest) {
        final AJAXState state = mailRequest.getRequest().getState();
        if (state == null) {
            return null;
        }
        Collection<Closeable> closeables = state.optProperty(PROPERTY_CLOSEABLES);
        if (null == closeables) {
            final Collection<Closeable> newCloseables = new LinkedList<Closeable>();
            closeables = state.putProperty(PROPERTY_CLOSEABLES, newCloseables);
            if (null == closeables) {
                closeables = newCloseables;
            }
        }
        return closeables;
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        if (!session.getUserPermissionBits().hasWebMail()) {
            throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create("mail");
        }

        MailRequest req = new MailRequest(requestData, session);
        AJAXRequestResult result = null;
        try {
            result = perform(req);
            return result;
        } catch (IllegalStateException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof OXException) {
                throw (OXException) cause;
            }
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } catch (OXException e) {
            //Bug 42565
            if (MailExceptionCode.NO_ATTACHMENT_FOUND.equals(e)) {
                throw AjaxExceptionCodes.HTTP_ERROR.create(I(404), e.getMessage());
            }
            if (OAuthMailErrorCodes.NO_ACCOUNT_ACCESS.equals(e)) {
                Optional<Integer> optStatus = OAuthMailErrorCodes.getHttpStatus(e);
                if (optStatus.isPresent()) {
                    throw AjaxExceptionCodes.HTTP_ERROR.create(optStatus.get(), e.getMessage());
                }
            }
            throw e;
        } finally {
            requestData.cleanUploads();
            if (null != result) {
                MailServletInterface mailInterface = req.getMailServletInterface();
                if (null != mailInterface) {
                    result.addPostProcessor(new MailInterfacePostProcessor(mailInterface));
                }
            }
        }
    }

    /**
     * Performs specified mail request.
     *
     * @param req The mail request
     * @return The result
     * @throws OXException If an error occurs
     * @throws JSONException If a JSON error occurs
     */
    protected abstract AJAXRequestResult perform(MailRequest req) throws OXException, JSONException;

    /**
     * Checks whether specified mail should be discarded according to filter flags
     *
     * @param m The mail to check
     * @param ignoreSeen <code>true</code> to ignore \Seen ones; otherwise <code>false</code>
     * @param ignoreDeleted <code>true</code> to ignore \Deleted ones that are \Seen; otherwise <code>false</code>
     * @return <code>true</code> if mail should be discarded according to filter flags; otherwise <code>false</code>
     */
    protected static boolean discardMail(MailMessage m, boolean ignoreSeen, boolean ignoreDeleted) {
        if (null == m) {
            return true;
        }
        if (ignoreSeen && m.isSeen()) {
            // Discard \Seen mails
            return true;
        }
        if (ignoreDeleted && m.isDeleted() && m.isSeen()) {
            // Discard \Seen mails that are \Deleted
            return true;
        }
        // Do not discard
        return false;
    }

    /**
     * Triggers the contact collector for specified mail's addresses.
     *
     * @param session The session
     * @param mail The mail
     * @param memorizeAddresses Whether contact-collector is supposed to be triggered
     * @param incrementUseCount Whether the associated contacts' use-count is supposed to be incremented
     * @throws OXException
     */
    public static void triggerContactCollector(ServerSession session, MailMessage mail, boolean memorizeAddresses, boolean incrementUseCount) throws OXException {
        if (null != mail) {
            triggerContactCollector(session, Collections.singletonList(mail), memorizeAddresses, incrementUseCount);
        }
    }

    /**
     * Triggers the contact collector for specified mail's addresses.
     *
     * @param session The session
     * @param mails The mails
     * @param memorizeAddresses Whether contact-collector is supposed to be triggered
     * @param incrementUseCount Whether the associated contacts' use-count is supposed to be incremented
     * @throws OXException
     */
    public static void triggerContactCollector(ServerSession session, Collection<? extends MailMessage> mails, boolean memorizeAddresses, boolean incrementUseCount) throws OXException {
        ContactCollectorUtility.triggerContactCollector(session, mails, memorizeAddresses, incrementUseCount);
    }

    protected static final String VIEW_RAW = "raw";

    protected static final String VIEW_TEXT = "text";

    protected static final String VIEW_HTML = "html";

    protected static final String VIEW_HTML_BLOCKED_IMAGES = "noimg";

    protected static final String VIEW_DOCUMENT = "document";

    /**
     * Detects the display mode.
     *
     * @param modifyable whether modifiable.
     * @param view the view
     * @param usm The user mail settings
     * @return The display mode
     */
    public static DisplayMode detectDisplayMode(boolean modifyable, String view, UserSettingMail usm) {
        final DisplayMode displayMode;
        if (null != view) {
            if (VIEW_RAW.equals(view)) {
                displayMode = DisplayMode.RAW;
            } else if (VIEW_TEXT.equals(view)) {
                usm.setDisplayHtmlInlineContent(false);
                displayMode = modifyable ? DisplayMode.MODIFYABLE : DisplayMode.DISPLAY;
            } else if (VIEW_HTML.equals(view)) {
                usm.setDisplayHtmlInlineContent(true);
                usm.setAllowHTMLImages(true);
                displayMode = modifyable ? DisplayMode.MODIFYABLE : DisplayMode.DISPLAY;
            } else if (VIEW_HTML_BLOCKED_IMAGES.equals(view)) {
                usm.setDisplayHtmlInlineContent(true);
                usm.setAllowHTMLImages(false);
                displayMode = modifyable ? DisplayMode.MODIFYABLE : DisplayMode.DISPLAY;
            } else if (VIEW_DOCUMENT.equals(view)) {
                displayMode = DisplayMode.DOCUMENT;
            } else {
                LOG.warn("Unknown value in parameter {}: {}. Using user's mail settings as fallback.", Mail.PARAMETER_VIEW, view);
                displayMode = modifyable ? DisplayMode.MODIFYABLE : DisplayMode.DISPLAY;
            }
        } else {
            displayMode = modifyable ? DisplayMode.MODIFYABLE : DisplayMode.DISPLAY;
        }
        return displayMode;
    }

    /**
     * Gets the result filled with JSON <code>NULL</code>.
     *
     * @return The result with JSON <code>NULL</code>.
     */
    protected static AJAXRequestResult getJSONNullResult() {
        return RESULT_JSON_NULL;
    }

    /**
     * Check whether the given request is made via OAuth.
     *
     * @param request The request
     * @return <code>true</code> if so
     */
    protected boolean isOAuthRequest(AJAXRequestData request) {
        return request.containsProperty(OAuthConstants.PARAM_OAUTH_ACCESS);
    }

    /**
     * Resolves specified "from" address to associated account identifier
     *
     * @param session The session
     * @param from The from address
     * @param checkTransportSupport <code>true</code> to check transport support
     * @param checkFrom <code>true</code> to check for validity
     * @return The account identifier
     * @throws OXException If address cannot be resolved
     */
    protected static int resolveFrom2Account(ServerSession session, InternetAddress from, boolean checkTransportSupport, boolean checkFrom) throws OXException {
        return MimeMessageFiller.resolveFrom2Account(session, from, checkTransportSupport, checkFrom);
    }

    protected static String getDefaultSendAddress(ServerSession session) {
        return session.getUserSettingMail().getSendAddr();
    }

    protected static boolean isEmpty(String string) {
        return com.openexchange.java.Strings.isEmpty(string);
    }

    /**
     * Checks given columns.
     * <ul>
     * <li>Add MailField.ID if MailField.ORIGINAL_ID is contained
     * <li>Add MailField.FOLDER_ID if MailField.ORIGINAL_FOLDER_ID is contained
     * </ul>
     *
     * @param columns The columns to check
     * @return The checked columns in its mail field representation
     */
    protected static int[] prepareColumns(int[] columns) {
        return prepareColumns(columns, new int[0]);
    }

    /**
     * Checks given columns.
     * <ul>
     * <li>Add MailField.ID if MailField.ORIGINAL_ID is contained
     * <li>Add MailField.FOLDER_ID if MailField.ORIGINAL_FOLDER_ID is contained
     * </ul>
     *
     * @param columns The columns to check
     * @param forcedColumns Optional enforced columns to contain
     * @return The checked columns in its mail field representation
     */
    protected static int[] prepareColumns(int[] columns, int... forcedColumns) {
        int[] fields = columns;

        EnumSet<MailField> set = EnumSet.copyOf(Arrays.asList(MailField.getMatchingFields(fields)));
        if (set.contains(MailField.ORIGINAL_FOLDER_ID) && !set.contains(MailField.FOLDER_ID)) {
            int[] tmp = fields;
            fields = new int[tmp.length + 1];
            fields[0] = MailListField.FOLDER_ID.getField();
            System.arraycopy(tmp, 0, fields, 1, tmp.length);
        }
        if (set.contains(MailField.ORIGINAL_ID) && !set.contains(MailField.ID)) {
            int[] tmp = fields;
            fields = new int[tmp.length + 1];
            fields[0] = MailListField.ID.getField();
            System.arraycopy(tmp, 0, fields, 1, tmp.length);
        }
        if (null != forcedColumns && forcedColumns.length > 0) {
            for (int forcedColumn : forcedColumns) {
                MailField extraField = MailField.getField(forcedColumn);
                if (null != extraField && !set.contains(extraField)) {
                    int[] tmp = fields;
                    fields = new int[tmp.length + 1];
                    System.arraycopy(tmp, 0, fields, 0, tmp.length);
                    fields[tmp.length] = forcedColumn;
                }
            }
        }

        return fields;
    }

    /**
     * Extracts the mail uid (if available) from the specified {@link OXException}
     *
     * @param e The {@link OXException}
     * @return The extracted mail uid, if available, or <code>null</code>
     */
    String getUidFromException(OXException e) {
        final Object[] args = e.getDisplayArgs();
        final String uid;
        if (null == args || 0 == args.length) {
            uid = null;
        } else {
            uid = args[0] == null ? null : args[0].toString();
        }
        return uid;
    }

    protected boolean getIgnoreDeleted(MailRequest mailRequest, boolean defaultValue) {
        String parameter = mailRequest.getParameter("deleted");
        return parameter == null ? defaultValue : !AJAXRequestDataTools.parseBoolParameter(parameter);
    }

    /**
     * Scans the attachments (optionally specified in the sequenceIds) of the mail with the specified unique identifier
     * in the specified folder. If no sequence identifiers are specified, then all attachments of the mail will be scanned.
     *
     * @param request The {@link MailRequest}
     * @param mailInterface the {@link MailServletInterface}
     * @param folderPath the folder path of the mail
     * @param uid The unique identifier of the mail
     * @param sequenceIds The optional sequence identifiers of the attachment
     * @throws OXException if one of the attachments is too large, or if the {@link AntiVirusService} is absent,
     *             or if any of the attachments is infected, or if a timeout or any other error is occurred
     */
    protected void scanAttachments(MailRequest request, MailServletInterface mailInterface, String folderPath, String uid, String[] sequenceIds) throws OXException {
        String scan = request.getParameter("scan");
        Boolean s = Strings.isEmpty(scan) ? Boolean.FALSE : Boolean.valueOf(scan);
        if (false == s.booleanValue()) {
            LOG.debug("No anti-virus scanning was performed.");
            return;
        }
        AntiVirusService antiVirusService = services.getOptionalService(AntiVirusService.class);
        if (antiVirusService == null) {
            throw AntiVirusServiceExceptionCodes.ANTI_VIRUS_SERVICE_ABSENT.create();
        }
        if (false == antiVirusService.isEnabled(request.getSession())) {
            return;
        }
        AntiVirusEncapsulatedContent content = encapsulateContent(request);
        if (sequenceIds == null) {
            for (MailPart mailPart : mailInterface.getAllMessageAttachments(folderPath, uid)) {
                scan(mailPart, getUniqueId(folderPath, uid, mailPart), mailPart.getSize(), antiVirusService, content);
            }
            return;
        }
        for (String sequenceId : sequenceIds) {
            MailPart mailPart = mailInterface.getMessageAttachment(folderPath, uid, sequenceId, false);
            scan(mailPart, getUniqueId(folderPath, uid, mailPart), mailPart.getSize(), antiVirusService, content);
        }
    }

    /**
     * Performs a scan (if a scan is requested by the specified {@link MailRequest}, i.e. via the <code>scan</code>
     * URL parameter) of the {@link InputStream} of the specified {@link IFileHolder}.
     *
     * @param request The {@link MailRequest}
     * @param fileHolder The {@link IFileHolder}
     * @param mailId The mail identifier
     * @throws OXException if the file is too large, or if the {@link AntiVirusService} is absent,
     *             or if the file is infected, or if a timeout or any other error is occurred
     */
    protected void scan(MailRequest request, IFileHolder fileHolder, String mailId) throws OXException {
        String scan = request.getParameter("scan");
        Boolean s = Strings.isEmpty(scan) ? Boolean.FALSE : Boolean.valueOf(scan);
        if (false == s.booleanValue()) {
            LOG.debug("No anti-virus scanning was performed.");
            return;
        }
        AntiVirusService antiVirusService = services.getOptionalService(AntiVirusService.class);
        if (antiVirusService == null) {
            throw AntiVirusServiceExceptionCodes.ANTI_VIRUS_SERVICE_ABSENT.create();
        }
        if (false == antiVirusService.isEnabled(request.getSession())) {
            return;
        }
        AntiVirusResult result = antiVirusService.scan(fileHolder, mailId, encapsulateContent(request));
        services.getServiceSafe(AntiVirusResultEvaluatorService.class).evaluate(result, fileHolder.getName());
    }
    
    /**
     * Encapsulates the HTTP content from the specified request
     *
     * @param request The request
     * @return The content
     */
    private AntiVirusEncapsulatedContent encapsulateContent(MailRequest request) {
        return AntiVirusEncapsulationUtil.encapsulate(request.getRequest().optHttpServletRequest(), request.getRequest().optHttpServletResponse());
    }

    /**
     * Performs the actual scan
     *
     * @param mailPart The mail part to scan
     * @param mailId The unique mail identifier
     * @param contentSize The content size of the mail part
     * @param antiVirusService The anti virus service
     * @param content The encapsulated content
     * @throws OXException if the mail part is too large, or if the {@link AntiVirusService} is absent,
     *             or if the mail part is infected, or if a timeout or any other error is occurred
     */
    private void scan(MailPart mailPart, String mailId, long contentSize, AntiVirusService antiVirusService, AntiVirusEncapsulatedContent content) throws OXException {
        AntiVirusResult result = antiVirusService.scan(() -> mailPart.getInputStream(), mailId, contentSize, content);
        String filename = mailPart.getFileName();
        if (Strings.isEmpty(filename)) {
            List<String> extensions = MimeType2ExtMap.getFileExtensions(mailPart.getContentType().getBaseType());
            filename = extensions == null || extensions.isEmpty() ? "part.dat" : "part." + extensions.get(0);
        }
        services.getServiceSafe(AntiVirusResultEvaluatorService.class).evaluate(result, filename);
    }

    /**
     * Generates an identifier that uniquely identifies the specified {@link MailPart}.
     * It checks for the sequence id and if found is returned prepended with the folder path
     * and the mail identifier, otherwise only the folderPath and the mail identifier are
     * returned.
     *
     * @param folderPath The folder path
     * @param mailId the mail identifier
     * @param mailPart The {@link MailPart}
     * @return The id
     */
    protected String getUniqueId(String folderPath, String mailId, MailPart mailPart) {
        String id = mailPart.getSequenceId();
        // Dunno about this... maybe there is a better way to get a unique id for a mail attachment
        if (Strings.isNotEmpty(id)) {
            return folderPath + "/" + mailId + "/" + id;
        }
        return folderPath + "/" + mailId;
    }
}
