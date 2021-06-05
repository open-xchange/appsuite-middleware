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

package com.openexchange.ajax.requesthandler.converters.preview;

import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractMailPreviewResultConverter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
abstract class AbstractMailPreviewResultConverter implements ResultConverter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractMailPreviewResultConverter.class);

    protected final AbstractPreviewResultConverter resultConverter;

    /**
     * Initializes a new {@link AbstractMailPreviewResultConverter}.
     */
    protected AbstractMailPreviewResultConverter(final AbstractPreviewResultConverter resultConverter) {
        super();
        this.resultConverter = resultConverter;
    }

    @Override
    public String getInputFormat() {
        return "mail";
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @Override
    public String getOutputFormat() {
        return resultConverter.getOutputFormat();
    }

    @Override
    public void convert(final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session, final Converter converter) throws OXException {
        /*
         * Create file holder from mail
         */
        final MailMessage mail = (MailMessage) result.getResultObject();
        ThresholdFileHolder fileHolder = new ThresholdFileHolder();
        try {
            mail.writeTo(fileHolder.asOutputStream());
        } catch (OXException e) {
            if (!MailExceptionCode.NO_CONTENT.equals(e)) {
                fileHolder.close();
                throw e;
            }
            LOG.debug("", e);
            fileHolder.close();
            fileHolder = new ThresholdFileHolder();
            fileHolder.writeZeroBytes();
        }
        mail.prepareForCaching();
        /*
         * Create appropriate file holder
         */
        fileHolder.setContentType("application/octet-stream");

        String subject = mail.getSubject();
        if (subject == null) { // in case no subject was set
            subject = StringHelper.valueOf(session.getUser().getLocale()).getString(MailStrings.DEFAULT_SUBJECT);
        }

        fileHolder.setName(new StringBuilder(subject).append(".eml").toString());
        result.setResultObject(fileHolder, "file");
        result.setParameter("__mail", mail);
        /*
         * Convert
         */
        resultConverter.convert(requestData, result, session, converter);
    }

}
