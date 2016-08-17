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

package com.openexchange.client.onboarding;

import java.util.Collections;
import java.util.Map;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.client.onboarding.notification.mail.OnboardingProfileCreatedNotificationMail;
import com.openexchange.client.onboarding.osgi.Services;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.dataobjects.compose.ComposeType;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.transport.MailTransport;
import com.openexchange.mail.transport.TransportProvider;
import com.openexchange.mail.transport.TransportProviderRegistry;
import com.openexchange.notification.mail.MailAttachment;
import com.openexchange.notification.mail.MailAttachments;
import com.openexchange.notification.mail.MailData;
import com.openexchange.notification.mail.NotificationMailFactory;
import com.openexchange.session.Session;

/**
 * {@link ObjectResult} - A result for a file.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class FileResult implements Result {

    private final IFileHolder file;

    /**
     * Initializes a new {@link ObjectResult}.
     *
     * @param file The file
     */
    public FileResult(IFileHolder file) {
        super();
        this.file = file;
    }

    /**
     * Gets the file
     *
     * @return The file
     */
    public IFileHolder getFile() {
        return file;
    }

    @Override
    public ResultObject getResultObject(OnboardingRequest request, Session session) throws OXException {
        OnboardingAction action = request.getAction();
        switch (action) {
            case DOWNLOAD:
                return new SimpleResultObject(file, "file");
            case EMAIL:
                return sendEmailResult(request, session);
            default:
                throw OnboardingExceptionCodes.UNSUPPORTED_ACTION.create(action.getId());
        }
    }

    @Override
    public ResultReply getReply() {
        return ResultReply.ACCEPT;
    }

    // --------------------------------------------- E-Mail utils --------------------------------------------------------------

    private TransportProvider getTransportProvider() {
        return TransportProviderRegistry.getTransportProvider("smtp");
    }

    private ResultObject sendEmailResult(OnboardingRequest request, Session session) throws OXException {
        Map<String, Object> input = request.getInput();
        if (null == input) {
            throw OnboardingExceptionCodes.MISSING_INPUT_FIELD.create(CommonInput.EMAIL_ADDRESS.getFirstElementName());
        }

        String emailAddress = (String) input.get(CommonInput.EMAIL_ADDRESS.getFirstElementName());
        if (Strings.isEmpty(emailAddress)) {
            throw OnboardingExceptionCodes.MISSING_INPUT_FIELD.create(CommonInput.EMAIL_ADDRESS.getFirstElementName());
        }

        boolean error = true;
        MailTransport transport = getTransportProvider().createNewNoReplyTransport(session.getContextId());
        try {
            NotificationMailFactory notify = Services.getService(NotificationMailFactory.class);

            MailData data = OnboardingProfileCreatedNotificationMail.createProfileNotificationMail(emailAddress, request.getHostData().getHost(), file.getName(), session);
            MailAttachment mailAttachment = MailAttachments.newMailAttachment(file);
            // TODO: Add Content-Id?

            ComposedMailMessage message = notify.createMail(data, Collections.singleton(mailAttachment));
            transport.sendMailMessage(message, ComposeType.NEW);

            ResultObject resultObject = new SimpleResultObject(OnboardingUtility.getTranslationFor(OnboardingStrings.RESULT_MAIL_SENT, session), "string");
            error = false;
            return resultObject;
        } catch (RuntimeException e) {
            throw OnboardingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            transport.close();
            if (error) {
                Streams.close(file);
            }
        }
    }

}
