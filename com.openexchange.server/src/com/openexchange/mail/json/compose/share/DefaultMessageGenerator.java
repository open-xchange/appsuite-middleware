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
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Strings;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.dataobjects.compose.TextBodyMailPart;
import com.openexchange.mail.json.compose.ComposeRequest;
import com.openexchange.mail.json.compose.Utilities;
import com.openexchange.mail.json.compose.share.spi.MessageGenerator;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link DefaultMessageGenerator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class DefaultMessageGenerator implements MessageGenerator {

    private static final Pattern PATTERN_DATE = Pattern.compile(Pattern.quote("#DATE#"));

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

    @Override
    public List<ComposedMailMessage> generateTransportMessagesFor(ShareComposeMessageInfo info) throws OXException {
        ShareComposeLink shareLink = info.getShareLink();
        String password = info.getPassword();
        Date expirationDate = info.getExpirationDate();
        Map<String, String> headers = mapFor(HEADER_SHARE_TYPE, shareLink.getType(), HEADER_SHARE_URL, shareLink.getLink());

        Collection<Recipient> recipients = info.getRecipients();
        List<ComposedMailMessage> messages = new ArrayList<>(recipients.size());
        for (Recipient recipient : recipients) {
            if (recipient.isUser()) {
                messages.add(generateInternalVersion(recipient, info.getComposeContext(), shareLink, password, expirationDate, headers));
            } else {
                messages.add(generateExternalVersion(recipient, info.getComposeContext(), shareLink, password, expirationDate, headers));
            }
        }

        return messages;
    }

    @Override
    public ComposedMailMessage generateSentMessageFor(ShareComposeMessageInfo info, ShareReference shareReference) throws OXException {
        Map<String, String> headers = mapFor(HEADER_SHARE_REFERENCE, shareReference.generateReferenceString());
        ComposedMailMessage sentMessage = generateInternalVersion(info.getRecipients().get(0), info.getComposeContext(), info.getShareLink(), info.getPassword(), info.getExpirationDate(), headers);
        sentMessage.addUserFlag(USER_SHARE_REFERENCE);
        return sentMessage;
    }

    @Override
    public boolean applicableFor(ComposeRequest composeRequest) throws OXException {
        return true;
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
     * Generates the compose message for an internal recipient.
     *
     * @param recipient The internal recipient
     * @param composeContext The associated compose context
     * @param link The link to insert
     * @param password The optional password
     * @param elapsedDate The optional expiration date
     * @param shareHeaders The optional share headers to set
     * @return The compose message
     * @throws OXException If compose message cannot be returned
     */
    protected ComposedMailMessage generateInternalVersion(Recipient recipient, ShareTransportComposeContext composeContext, ShareComposeLink link, String password, Date elapsedDate, Map<String, String> shareHeaders) throws OXException {
        ComposedMailMessage composedMessage = Utilities.copyOfSourceMessage(composeContext);
        Locale locale = recipient.getUser().getLocale();

        // Alter text content to include share reference
        TextBodyMailPart textPart = composeContext.getTextPart().copy();
        textPart.setPlainText(null);
        StringHelper stringHelper = StringHelper.valueOf(locale);

        {
            String text = (String) textPart.getContent();
            StringBuilder textBuilder = new StringBuilder(text.length() + 512);
            textBuilder.append(htmlFormat(stringHelper.getString(ShareComposeStrings.SHARED_ATTACHMENTS_PREFIX))).append("<br>");
            appendLink(link, textBuilder);
            if (password != null) {
                textBuilder.append(htmlFormat(stringHelper.getString(ShareComposeStrings.SHARED_ATTACHMENTS_PASSWORD))).append(": ").append(htmlFormat(password)).append("<br>");
            }
            if (elapsedDate != null) {
                textBuilder.append(htmlFormat(PATTERN_DATE.matcher(stringHelper.getString(ShareComposeStrings.SHARED_ATTACHMENTS_EXPIRATION)).replaceFirst(DateFormat.getDateInstance(DateFormat.LONG, locale).format(elapsedDate)))).append("<br>");
            }
            textBuilder.append("<br>");
            textBuilder.append(text);
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
     * Generates the compose message for an external recipient.
     *
     * @param recipient The external recipient
     * @param composeContext The associated compose context
     * @param link The link to insert
     * @param password The optional password
     * @param elapsedDate The optional expiration date
     * @param shareHeaders The optional share headers to set
     * @return The compose message
     * @throws OXException If compose message cannot be returned
     */
    protected ComposedMailMessage generateExternalVersion(Recipient recipient, ShareTransportComposeContext composeContext, ShareComposeLink link, String password, Date elapsedDate, Map<String, String> shareHeaders) throws OXException {
        ComposedMailMessage composedMessage = Utilities.copyOfSourceMessage(composeContext);
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

        // Alter text content to include share reference
        TextBodyMailPart textPart = composeContext.getTextPart().copy();
        textPart.setPlainText(null);
        StringHelper stringHelper = StringHelper.valueOf(locale);

        {
            String text = (String) textPart.getContent();
            StringBuilder textBuilder = new StringBuilder(text.length() + 512);
            textBuilder.append(htmlFormat(stringHelper.getString(ShareComposeStrings.SHARED_ATTACHMENTS_PREFIX))).append("<br>");
            appendLink(link, textBuilder);
            if (password != null) {
                textBuilder.append(htmlFormat(stringHelper.getString(ShareComposeStrings.SHARED_ATTACHMENTS_PASSWORD))).append(": ").append(htmlFormat(password)).append("<br>");
            }
            if (elapsedDate != null) {
                textBuilder.append(htmlFormat(PATTERN_DATE.matcher(stringHelper.getString(ShareComposeStrings.SHARED_ATTACHMENTS_EXPIRATION)).replaceFirst(DateFormat.getDateInstance(DateFormat.LONG, locale).format(elapsedDate)))).append("<br>");
            }
            textBuilder.append("<br>");
            textBuilder.append(text);
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
     * Appends specified link to given <code>StringBuilder</code> instance.
     *
     * @param composeLink The link to append
     * @param textBuilder The <code>StringBuilder</code> to append to
     */
    protected void appendLink(ShareComposeLink composeLink, StringBuilder textBuilder) {
        String link = composeLink.getLink();
        final char quot;
        if (link.indexOf('"') < 0) {
            quot = '"';
        } else {
            quot = '\'';
        }
        textBuilder.append("<a href=").append(quot).append(link).append(quot).append('>');
        final String name = composeLink.getName();
        if (null != name && name.length() > 0) {
            textBuilder.append(name).append("</a><br>");
        } else {
            textBuilder.append(link).append("</a><br>");
        }
    } // End of appendLinks()

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

        Map<String, String> map = new LinkedHashMap<String, String>(length >> 1);
        for (int i = 0; i < length; i+=2) {
            map.put(args[i], args[i+1]);
        }
        return map;
    }

}
