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
        } catch (final OXException e) {
            if (!MailExceptionCode.NO_CONTENT.equals(e)) {
                throw e;
            }
            LOG.debug("", e);
            fileHolder.close();
            fileHolder = new ThresholdFileHolder();
            fileHolder.write(new byte[0]);
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
