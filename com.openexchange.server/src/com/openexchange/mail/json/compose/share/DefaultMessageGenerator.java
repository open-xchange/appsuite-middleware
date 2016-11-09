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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail.json.compose.share;

import static com.openexchange.mail.json.compose.share.ShareComposeConstants.HEADER_SHARE_REFERENCE;
import static com.openexchange.mail.json.compose.share.ShareComposeConstants.HEADER_SHARE_TYPE;
import static com.openexchange.mail.json.compose.share.ShareComposeConstants.HEADER_SHARE_URL;
import static com.openexchange.mail.json.compose.share.ShareComposeConstants.USER_SHARE_REFERENCE;
import static com.openexchange.mail.text.HtmlProcessing.htmlFormat;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.html.HtmlService;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.i18n.Translator;
import com.openexchange.i18n.TranslatorFactory;
import com.openexchange.java.Strings;
import com.openexchange.java.UnsynchronizedStringWriter;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.TextBodyMailPart;
import com.openexchange.mail.json.compose.ComposeRequest;
import com.openexchange.mail.json.compose.Utilities;
import com.openexchange.mail.json.compose.share.spi.MessageGenerator;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.TransportAccount;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.notification.service.CommonNotificationVariables;
import com.openexchange.notification.service.FullNameBuilderService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.serverconfig.NotificationMailConfig;
import com.openexchange.serverconfig.ServerConfig;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.session.Session;
import com.openexchange.share.notification.NotificationStrings;
import com.openexchange.templating.OXTemplate;
import com.openexchange.templating.TemplateService;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link DefaultMessageGenerator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class DefaultMessageGenerator implements MessageGenerator {

    private static final DefaultMessageGenerator INSTANCE = new DefaultMessageGenerator();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static DefaultMessageGenerator getInstance() {
        return INSTANCE;
    }


    // ------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link DefaultMessageGenerator}.
     */
    protected DefaultMessageGenerator() {
        super();
    }

    /**
     * Gets the context associated with specified session
     *
     * @param session The session
     * @return The context
     * @throws OXException If context cannot be returned
     */
    protected Context getContext(Session session) throws OXException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getContext();
        }
        return ContextStorage.getStorageContext(session.getContextId());
    }

    /**
     * Gets the user associated with specified session
     *
     * @param session The session
     * @return The user
     * @throws OXException If user cannot be returned
     */
    protected User getSessionUser(Session session) throws OXException {
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getUser();
        }
        return UserStorage.getInstance().getUser(session.getUserId(), getContext(session));
    }

    /**
     * Gets the locale associated with specified session's user.
     *
     * @param session The session
     * @return The locale of session-associated user
     * @throws OXException If locale cannot be returned
     */
    protected Locale getSessionUserLocale(Session session) throws OXException {
        return getSessionUser(session).getLocale();
    }

    /**
     * Determines the locale to use when composing a message for external recipients.
     *
     * @param composeContext The compose context
     * @return The locale for external recipients
     * @throws OXException If locale for external recipients cannot be returned
     */
    protected Locale determineLocaleForExternalRecipients(ShareTransportComposeContext composeContext) throws OXException {
        Locale locale;
        {
            ServerSession session = composeContext.getSession();
            String sLocale = Utilities.getValueFromProperty("com.openexchange.mail.compose.share.externalRecipientsLocale", "user-defined", session);
            if (Strings.isEmpty(sLocale) || "user-defined".equalsIgnoreCase(sLocale)) {
                locale = session.getUser().getLocale();
            } else {
                locale = LocaleTools.getLocale(sLocale);
                if (null == locale) {
                    locale = session.getUser().getLocale();
                }
            }
        }
        return locale;
    }

    /**
     * Checks whether this message generator is supposed to create equal messages by locale; otherwise it generates an individual message
     * for each recipient.
     *
     * @return <code>true</code> for equal messages by locale; otherwise <code>false</code> to generate an individual ones
     */
    protected boolean equalMessagesByLocale() {
        return true;
    }

    /**
     * Checks whether the prefix is supposed to be loaded from template.
     *
     * @return <code>true</code> to load from template; otherwise <code>false</code> to generate the prefix manually
     */
    protected boolean loadPrefixFromTemplate() {
        return true;
    }

    /**
     * Gets the name of the template to use (provided that {@link #loadPrefixFromTemplate()} returned <code>true</code>).
     *
     * @return The template name
     */
    protected String getTemplateName() {
        return "notify.share.compose.prefix.html.tmpl";
    }

    @Override
    public boolean applicableFor(ComposeRequest composeRequest) throws OXException {
        return true;
    }

    @Override
    public List<ComposedMailMessage> generateTransportMessagesFor(ShareComposeMessageInfo info, ShareReference shareReference, Map<String, String> cidMapping, int moreFiles) throws OXException {
        ShareComposeLink shareLink = info.getShareLink();
        Map<String, String> headers = mapFor(HEADER_SHARE_TYPE, shareLink.getType(), HEADER_SHARE_URL, shareLink.getLink());
        return equalMessagesByLocale() ? generateEqualMessagesByLocale(info, shareReference, headers, cidMapping, moreFiles) : generateIndividualMessages(info, shareReference, headers, cidMapping, moreFiles);
    }

    /**
     * Generates equal messages by locale.
     *
     * @param info The message info providing the link and target recipients
     * @param shareReference The share reference
     * @param headers The headers to apply
     * @return The generated messages
     * @throws OXException If messages cannot be generated
     */
    protected List<ComposedMailMessage> generateEqualMessagesByLocale(ShareComposeMessageInfo info, ShareReference shareReference, Map<String, String> headers, Map<String, String> cidMapping, int moreFiles) throws OXException {
        Collection<Recipient> recipients = info.getRecipients();

        Map<Locale, ComposedMailMessage> internalMessages = new LinkedHashMap<>(recipients.size());
        Map<Locale, ComposedMailMessage> externalMessages = new LinkedHashMap<>(recipients.size());

        Locale localeForExternalRecipients = null;
        for (Recipient recipient : recipients) {
            if (recipient.isUser()) {
                Locale locale = recipient.getUser().getLocale();
                ComposedMailMessage composedMessage = internalMessages.get(locale);
                if (null == composedMessage) {
                    composedMessage = generateInternalVersion(recipient, info, shareReference, headers, cidMapping, moreFiles);
                    internalMessages.put(locale, composedMessage);
                } else {
                    // Adds specified recipient
                    composedMessage.addRecipient(addressFor(recipient));
                }
            } else {
                if (null == localeForExternalRecipients) {
                    localeForExternalRecipients = determineLocaleForExternalRecipients(info.getComposeContext());
                }
                ComposedMailMessage composedMessage = externalMessages.get(localeForExternalRecipients);
                if (null == composedMessage) {
                    composedMessage = generateExternalVersion(localeForExternalRecipients, recipient, info, shareReference, headers, cidMapping, moreFiles);
                    externalMessages.put(localeForExternalRecipients, composedMessage);
                } else {
                    // Adds specified recipient
                    composedMessage.addRecipient(addressFor(recipient));
                }
            }
        }
        List<ComposedMailMessage> messages = new ArrayList<>(internalMessages.size() + externalMessages.size());
        messages.addAll(internalMessages.values());
        messages.addAll(externalMessages.values());
        return messages;
    }

    /**
     * Generates individual messages for each recipient.
     *
     * @param info The message info providing the link and target recipients
     * @param shareReference The share reference
     * @param headers The headers to apply
     * @return The individually generated messages
     * @throws OXException If messages cannot be generated
     */
    protected List<ComposedMailMessage> generateIndividualMessages(ShareComposeMessageInfo info, ShareReference shareReference, Map<String, String> headers, Map<String, String> cidMapping, int moreFiles) throws OXException {
        Collection<Recipient> recipients = info.getRecipients();

        List<ComposedMailMessage> messages = new ArrayList<>(recipients.size());

        Locale localeForExternalRecipients = null;
        for (Recipient recipient : recipients) {
            if (recipient.isUser()) {
                messages.add(generateInternalVersion(recipient, info, shareReference, headers, cidMapping, moreFiles));
            } else {
                if (null == localeForExternalRecipients) {
                    localeForExternalRecipients = determineLocaleForExternalRecipients(info.getComposeContext());
                }
                messages.add(generateExternalVersion(localeForExternalRecipients, recipient, info, shareReference, headers, cidMapping, moreFiles));
            }
        }
        return messages;
    }

    @Override
    public ComposedMailMessage generateSentMessageFor(ShareComposeMessageInfo info, ShareReference shareReference, Map<String, String> cidMapping, int moreFiles) throws OXException {
        ShareComposeLink shareLink = info.getShareLink();
        Map<String, String> headers = mapFor(HEADER_SHARE_TYPE, shareLink.getType(), HEADER_SHARE_URL, shareLink.getLink(), HEADER_SHARE_REFERENCE, ShareReference.generateStringForMime(shareReference));
        ComposedMailMessage sentMessage = generateInternalVersion(info.getRecipients().get(0), info, shareReference, headers, cidMapping, moreFiles);
        sentMessage.addUserFlag(USER_SHARE_REFERENCE);
        return sentMessage;
    }

    /**
     * Generates the compose message for an internal recipient.
     *
     * @param recipient The internal recipient
     * @param info The share info object
     * @param shareReference The share reference
     * @param shareHeaders The optional share headers to set
     * @return The compose message
     * @throws OXException If compose message cannot be returned
     */
    protected ComposedMailMessage generateInternalVersion(Recipient recipient, ShareComposeMessageInfo info, ShareReference shareReference, Map<String, String> shareHeaders, Map<String, String> cidMapping, int moreFiles) throws OXException {
        // Generate locale-specific version using user's locale
        Locale locale = recipient.getUser().getLocale();
        return generateLocaleSpecificVersion(locale, recipient, info, shareReference, shareHeaders, cidMapping, moreFiles);
    }

    /**
     * Generates the compose message for an external recipient.
     *
     * @param localeForExternalRecipients The configured locale for external recipients
     * @param recipient The external recipient
     * @param info The share info object
     * @param shareReference The share reference
     * @param shareHeaders The optional share headers to set
     * @return The compose message
     * @throws OXException If compose message cannot be returned
     */
    protected ComposedMailMessage generateExternalVersion(Locale localeForExternalRecipients, Recipient recipient, ShareComposeMessageInfo info, ShareReference shareReference, Map<String, String> shareHeaders, Map<String, String> cidMapping, int moreFiles) throws OXException {
        // Generate locale-specific version using the configured locale for external recipients
        return generateLocaleSpecificVersion(localeForExternalRecipients, recipient, info, shareReference, shareHeaders, cidMapping, moreFiles);
    }

    /**
     * Generates the locale-specific compose message.
     *
     * @param locale The locale
     * @param recipient The recipient
     * @param info The share info object
     * @param shareReference The share reference
     * @param shareHeaders The share headers to add
     * @return The generated message for specified locale
     * @throws OXException If message cannot be generated
     */
    protected ComposedMailMessage generateLocaleSpecificVersion(Locale locale, Recipient recipient, ShareComposeMessageInfo info, ShareReference shareReference, Map<String, String> shareHeaders, Map<String, String> cidMapping, int moreFiles) throws OXException {
        ShareTransportComposeContext composeContext = info.getComposeContext();
        ComposedMailMessage composedMessage = Utilities.copyOfSourceMessage(composeContext);

        // Alter text content to include share reference
        TextBodyMailPart textPart = composeContext.getTextPart().copy();

        if (composeContext.isPlainText()) {
            String plainText = textPart.getPlainText();
            if (null == plainText) {
                String text = (String) textPart.getContent();
                StringBuilder textBuilder = new StringBuilder(text.length() + 512);

                // Append the prefix that notifies about to access the message's attachment via provided share link
                textBuilder.append(generatePrefix(locale, info, shareReference, false, cidMapping, moreFiles, false));

                // Append actual text
                textBuilder.append(text);

                // Replace text with composed one
                textPart.setText(textBuilder.toString());
                composedMessage.setBodyPart(textPart);
            } else {
                StringBuilder plainTextBuilder = new StringBuilder(plainText.length() + 512);

                // Append the prefix that notifies about to access the message's attachment via provided share link
                plainTextBuilder.append(generatePrefix(locale, info, shareReference, loadPrefixFromTemplate(), cidMapping, moreFiles, true));

                plainTextBuilder.append(plainText);

                textPart.setPlainText(plainTextBuilder.toString());
                composedMessage.setBodyPart(textPart);
            }
        } else {
            String text = (String) textPart.getContent();
            StringBuilder textBuilder = new StringBuilder(text.length() + 512);

            // Append the prefix that notifies about to access the message's attachment via provided share link
            textBuilder.append(generatePrefix(locale, info, shareReference, loadPrefixFromTemplate(), cidMapping, moreFiles, false));

            // Append actual text
            textBuilder.append(text);

            // Replace text with composed one
            textPart.setText(textBuilder.toString());
            composedMessage.setBodyPart(textPart);
        }

        // Set specified recipient
        composedMessage.addRecipient(addressFor(recipient));

        // Set share headers
        if (null != shareHeaders) {
            for (Map.Entry<String, String> header : shareHeaders.entrySet()) {
                composedMessage.setHeader(header.getKey(), header.getValue());
            }
        }

        // Return composed message
        return composedMessage;
    }

    /**
     * Gets the {@link InternetAddress} instance for specified recipient.
     *
     * @param recipient The recipient
     * @return The recipient's address
     * @throws OXException If address cannot be generated
     */
    private InternetAddress addressFor(Recipient recipient) throws OXException {
        try {
            String personal = recipient.getPersonal();
            return new QuotedInternetAddress(recipient.getAddress(), personal, "UTF-8");
        } catch (AddressException e) {
            throw MimeMailException.handleMessagingException(e);
        } catch (UnsupportedEncodingException e) {
            throw MailExceptionCode.ENCODING_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Generates the prefix to insert.
     *
     * @param locale The locale to use for translation
     * @param link The link
     * @param info The share info object
     * @param shareReference The share reference
     * @param fromTemplate <code>true</code> to generate the prefix by loading from template; otherwise <code>false</code>
     * @return The generated prefix
     * @throws OXException If generating prefix from template fails
     */
    protected String generatePrefix(Locale locale, ShareComposeMessageInfo info, ShareReference shareReference, boolean fromTemplate, Map<String, String> previews, int moreFiles, boolean isPlainText) throws OXException {
        if (isPlainText) {
            HtmlService htmlService = ServerServiceRegistry.getInstance().getService(HtmlService.class);
            return htmlService.html2text(generatePrefixPlain(locale, info, shareReference), true);
        }
        return fromTemplate ? loadPrefixFromTemplate(locale, info, shareReference, previews, moreFiles) : generatePrefixPlain(locale, info, shareReference);
    }

    /**
     * Generates the prefix to insert.
     *
     * @param locale The locale to use for translation
     * @param info The share info object
     * @param shareReference The share reference
     * @return The generated prefix
     * @throws OXException If generating prefix from template fails
     */
    protected String generatePrefixPlain(Locale locale, ShareComposeMessageInfo info, ShareReference shareReference) throws OXException {
        TranslatorFactory translatorFactory = MessageGenerators.getTranslatorFactory();
        if (null == translatorFactory) {
            throw ServiceExceptionCode.absentService(TranslatorFactory.class);
        }

        Translator translator = translatorFactory.translatorFor(locale);
        StringBuilder textBuilder = new StringBuilder(512);

        List<Item> items = shareReference.getItems();

        // Intro
        {
            String translated = translator.translate(items.size() > 1 ? ShareComposeStrings.SHARED_ATTACHMENTS_INTRO_MULTI : ShareComposeStrings.SHARED_ATTACHMENTS_INTRO_SINGLE);
            String fullName = getFullName(info, shareReference, translator);
            translated = String.format(translated, fullName);
            textBuilder.append(htmlFormat(translated)).append("<br>");
        }

        // Files
        {
            textBuilder.append("<ul>");
            for (Item item : items) {
                textBuilder.append("<li>");
                String fileName = item.getName();
                textBuilder.append(Strings.isEmpty(fileName) ? translator.translate(ShareComposeStrings.DEFAULT_FILE_NAME) : htmlFormat(fileName));
                textBuilder.append("</li>");
            }
            textBuilder.append("</ul>").append("<br>");
        }

        // Link
        {
            String translated = translator.translate(items.size() > 1 ? NotificationStrings.VIEW_FILES : NotificationStrings.VIEW_FILE);
            textBuilder.append(htmlFormat(translated)).append(": ").append(buildLink(info.getShareLink(), true)).append("<br>");
        }

        // password
        if (null != info.getPassword()) {
            String translated = translator.translate(ShareComposeStrings.SHARED_ATTACHMENTS_PASSWORD);
            translated = String.format(translated, "<b>" + StringEscapeUtils.escapeHtml(info.getPassword()) + "</b>");
            textBuilder.append(translated).append("<br>");
        }

        // expiration
        if (null != info.getExpirationDate()) {
            String translated = translator.translate(ShareComposeStrings.SHARED_ATTACHMENTS_EXPIRATION);
            translated = String.format(translated, DateFormat.getDateInstance(DateFormat.LONG, locale).format(info.getExpirationDate()));
            textBuilder.append(translated).append("<br>");
        }

        // Extra line-break
        textBuilder.append("<br>");

        return textBuilder.toString();
    }

    /** The template place-holder for the introduction text */
    protected static final String VARIABLE_INTRO = "intro";

    /** The template place-holder for the share link */
    protected static final String VARIABLE_LINK = "link";

    /** The template place-holder for the share link */
    protected static final String VARIABLE_LINK_TEXT = "link_text";

    /** The template place-holder for the password */
    protected static final String VARIABLE_PASSWORD = "password";

    /** The template place-holder for the expiration date */
    protected static final String VARIABLE_EXPIRATION = "expiration";

    /** The template place-holder for the listing of file names */
    protected static final String VARIABLE_FILE_NAMES = "filenames";

    protected static final String VARIABLE_PREVIEW = "previews";

    protected static final String VARIABLE_MORE_FILES = "more";

    /**
     * Loads the prefix to insert from a template.
     *
     * @param locale The locale to use for translation
     * @param info The share info object
     * @param shareReference The share reference
     * @return The loaded prefix
     * @throws OXException If loading prefix from template fails
     */
    protected String loadPrefixFromTemplate(Locale locale, ShareComposeMessageInfo info, ShareReference shareReference, Map<String, String> cidMapping, int moreFiles) throws OXException {
        TemplateService templateService = MessageGenerators.getTemplateService();
        if (null == templateService) {
            throw ServiceExceptionCode.absentService(TemplateService.class);
        }

        TranslatorFactory translatorFactory = MessageGenerators.getTranslatorFactory();
        if (null == translatorFactory) {
            throw ServiceExceptionCode.absentService(TranslatorFactory.class);
        }

        Translator translator = translatorFactory.translatorFor(locale);
        Map<String, Object> vars = new HashMap<>(4);

        List<Item> items = shareReference.getItems();

        // Intro
        {
            String translated = translator.translate(items.size() > 1 ? ShareComposeStrings.SHARED_ATTACHMENTS_INTRO_MULTI : ShareComposeStrings.SHARED_ATTACHMENTS_INTRO_SINGLE);
            String fullName = getFullName(info, shareReference, translator);
            translated = String.format(translated, fullName);
            vars.put(VARIABLE_INTRO, translated);
        }

        // Filenames and previews
        {
            Map<String, String> filenameMapping = generateFilenameMapping(items, translator);
            Map<String, String> previewImages = new HashMap<>(cidMapping.size());
            for (String preview : cidMapping.keySet()) {
                previewImages.put(preview, "cid:" + cidMapping.get(preview));
            }
            vars.put(VARIABLE_PREVIEW, previewImages);
            vars.put(VARIABLE_FILE_NAMES, filenameMapping);
            if (moreFiles > 0) {
                vars.put(VARIABLE_MORE_FILES, moreFiles == 1 ? translator.translate(NotificationStrings.DRIVE_MAIL_ONE_MORE_FILE) : String.format(translator.translate(NotificationStrings.DRIVE_MAIL_MORE_FILES), moreFiles));
            }
        }

        // Link
        {
            String translated = translator.translate(items.size() > 1 ? NotificationStrings.VIEW_FILES : NotificationStrings.VIEW_FILE);
            vars.put(VARIABLE_LINK_TEXT, translated);
            vars.put(VARIABLE_LINK, buildLink(info.getShareLink(), false));
        }

        // password
        if (null != info.getPassword()) {
            String translated = translator.translate(ShareComposeStrings.SHARED_ATTACHMENTS_PASSWORD);
            translated = String.format(translated, "<b>" + StringEscapeUtils.escapeHtml(info.getPassword()) + "</b>");
            vars.put(VARIABLE_PASSWORD, translated);
        }

        // expiration
        if (null != info.getExpirationDate()) {
            String translated = translator.translate(ShareComposeStrings.SHARED_ATTACHMENTS_EXPIRATION);
            translated = String.format(translated, DateFormat.getDateInstance(DateFormat.LONG, locale).format(info.getExpirationDate()));
            vars.put(VARIABLE_EXPIRATION, translated);
        }

        // Apply style
        {
            ComposeRequest composeRequest = info.getComposeRequest();
            ServerConfigService serverConfigService = ServerServiceRegistry.getInstance().getService(ServerConfigService.class);
            ServerConfig serverConfig = serverConfigService.getServerConfig(composeRequest.getRequest().getHostname(), composeRequest.getSession());
            NotificationMailConfig mailConfig = serverConfig.getNotificationMailConfig();
            vars.put(CommonNotificationVariables.BUTTON_COLOR, mailConfig.getButtonTextColor());
            vars.put(CommonNotificationVariables.BUTTON_BACKGROUND_COLOR, mailConfig.getButtonBackgroundColor());
            vars.put(CommonNotificationVariables.BUTTON_BORDER_COLOR, mailConfig.getButtonBorderColor());
        }

        return compileTemplate(getTemplateName(), vars, templateService);
    }

    /**
     * Gets the full name to use
     *
     * @param info The message info
     * @param shareReference The share reference
     * @param translator The translator
     * @return The full name to use
     * @throws OXException If full name cannot be determined
     */
    protected String getFullName(ShareComposeMessageInfo info, ShareReference shareReference, Translator translator) throws OXException {
        // Check by "From" address
        InternetAddress[] from = info.getSource().getFrom();
        if (null != from) {
            for (InternetAddress fromAddr : from) {
                String personal = fromAddr.getPersonal();
                if (false == Strings.isEmpty(personal)) {
                    return StringEscapeUtils.escapeHtml(personal);
                }
            }
        }

        // Try to determine by account
        int accountId = info.getSource().getAccountId();
        if (accountId > 0) {
            // Ensure account identifier does not point to Unified Mail account
            ServerServiceRegistry registry = ServerServiceRegistry.getInstance();
            UnifiedInboxManagement management = registry.getService(UnifiedInboxManagement.class);
            if ((null != management) && (accountId != management.getUnifiedINBOXAccountID(info.getComposeContext().getSession()))) {
                MailAccountStorageService storageService = registry.getService(MailAccountStorageService.class);

                String personal;
                String address;
                try {
                    MailAccount mailAccount = storageService.getMailAccount(accountId, shareReference.getUserId(), shareReference.getContextId());
                    personal = mailAccount.getPersonal();
                    address = mailAccount.getPrimaryAddress();
                } catch (OXException e) {
                    if (false == MailAccountExceptionCodes.NOT_FOUND.equals(e)) {
                        throw e;

                    }

                    // Retry with transport account
                    TransportAccount transportAccount = storageService.getTransportAccount(accountId, shareReference.getUserId(), shareReference.getContextId());
                    personal = transportAccount.getPersonal();
                    address = transportAccount.getPrimaryAddress();
                }

                return StringEscapeUtils.escapeHtml(Strings.isEmpty(personal) ? address : personal);
            }
        }

        // Last but not least, build the full name from associated user
        FullNameBuilderService fullNameBuilderService = ServerServiceRegistry.getInstance().getService(FullNameBuilderService.class);
        return StringEscapeUtils.escapeHtml(fullNameBuilderService.buildFullName(shareReference.getUserId(), shareReference.getContextId(), translator));
    }

    /**
     * Helper method to compile a given template file into conform HTML.
     *
     * @param templateFile Name of the template file
     * @param vars The template root object as map, containing all necessary variables
     * @param templateService The template service
     */
    protected String compileTemplate(String templateFile, Map<String, Object> vars, TemplateService templateService) throws OXException {
        OXTemplate template = templateService.loadTemplate(templateFile);
        UnsynchronizedStringWriter writer = new UnsynchronizedStringWriter(2048);
        template.process(vars, writer);
        return writer.toString();
    }

    /**
     * Builds specified link.
     *
     * @param composeLink The link to build from
     */
    protected String buildLink(ShareComposeLink composeLink, boolean withAnchor) {
        String link = composeLink.getLink();
        if (false == withAnchor) {
            return link;
        }

        char quot = link.indexOf('"') < 0 ? '"' : '\'';
        StringBuilder linkBuilder = new StringBuilder(128);
        linkBuilder.append("<a href=").append(quot).append(link).append(quot).append('>');
        String name = composeLink.getName();
        linkBuilder.append((null != name && name.length() > 0) ? name : link);
        linkBuilder.append("</a>");
        return linkBuilder.toString();
    }

    /**
     * Gets a map for specified arguments.
     *
     * @param args The arguments
     * @return The resulting map
     */
    protected static Map<String, String> mapFor(String... args) {
        if (null == args) {
            return null;
        }

        int length = args.length;
        if (0 == length || (length % 2) != 0) {
            return null;
        }

        Map<String, String> map = new LinkedHashMap<>(length >> 1);
        for (int i = 0; i < length; i+=2) {
            map.put(args[i], args[i+1]);
        }
        return map;
    }

    private Map<String, String> generateFilenameMapping(List<Item> items, Translator translator) {
        if (null == items || items.size() == 0) {
            return Collections.emptyMap();
        }
        Map<String, String> mapping = new HashMap<>();
        for (Item i : items) {
            String name = i.getName();
            if (null == name || Strings.isEmpty(name)) {
                name = translator.translate(ShareComposeStrings.DEFAULT_FILE_NAME);
            } else {
                name = StringEscapeUtils.escapeHtml(name);
                if (name.length() > 30) {
                    String base = FilenameUtils.getBaseName(name);
                    String ext = FilenameUtils.getExtension(name);
                    name = Strings.abbreviate(base, 25);
                    if (Strings.isNotEmpty(ext)) {
                        name = name + ext;
                    }
                }
            }
            mapping.put(i.getId(), name);
        }
        return mapping;
    }

}
