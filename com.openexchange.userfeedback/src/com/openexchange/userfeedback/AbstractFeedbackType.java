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

package com.openexchange.userfeedback;

import java.sql.Connection;
import com.openexchange.exception.OXException;

/**
 * {@link AbstractFeedbackType}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.4
 */
public abstract class AbstractFeedbackType implements FeedbackType {

    @Override
    public long storeFeedback(Object feedback, Connection con) throws OXException {
        Object validatedFeedback = prepareAndValidateFeedback(feedback);
        return storeFeedbackInternal(validatedFeedback, con);
    }

    public abstract long storeFeedbackInternal(Object feedback, Connection con) throws OXException;

    /**
     * Returns the updated and ready-to-persist feedback object
     * 
     * @param feedback
     * @return ready-to-persist object
     * @throws OXException
     */
    public final Object prepareAndValidateFeedback(Object feedback) throws OXException {
        checkFeedback(feedback);

        Object normalizedFeedback = normalize(feedback);
        Object cleanUpFeedback = cleanUp(normalizedFeedback);

        validate(cleanUpFeedback);

        return cleanUpFeedback;
    }

    /**
     * Syntactic check of the feedback content. Checks if the provided feedback has the minimum requirements for further processing.
     * 
     * @param feedback The feedback object to check
     * @throws OXException
     */
    protected abstract void checkFeedback(Object feedback) throws OXException;

    /**
     * Semantic check of the feedback content. Validates the normalized ({@link #normalize(Object)}) and cleaned ({@link #cleanUp(Object)}) feedback content.
     * 
     * @param feedback The feedback object to validate
     * @throws OXException
     */
    protected abstract void validate(Object feedback) throws OXException;

    /**
     * Ensures the feedback is well prepared for persisting. No additional and the minimum required information are available.
     * 
     * @param feedback The feedback object to clean up
     * @return The cleaned and syntactic ready feedback
     * @throws OXException
     */
    protected abstract Object cleanUp(Object feedback) throws OXException;

    /**
     * Ensures a normalized representation of the feedback object for upcoming cleanups (e. g. lower case keys in JSON)
     * 
     * @param feedback The feedback object to normalize
     * @return The normalized feedback
     * @throws OXException
     */
    protected abstract Object normalize(Object feedback) throws OXException;
}
