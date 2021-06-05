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

package com.openexchange.ajax.requesthandler;

import java.lang.annotation.Annotation;
import com.openexchange.exception.OXException;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractAJAXActionAnnotationProcessor}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public abstract class AbstractAJAXActionAnnotationProcessor<T extends Annotation> implements AJAXActionAnnotationProcessor {

    @Override
    public boolean handles(AJAXActionService action) {
        return action.getClass().isAnnotationPresent(getAnnotation());
    }

    @Override
    public void process(AJAXActionService action, AJAXRequestData requestData, ServerSession session) throws OXException {
        T annotation = action.getClass().getAnnotation(getAnnotation());
        if (annotation == null) {
            throw new IllegalArgumentException("Action '" + action.getClass().getName() + "' is not annotated with '" + getAnnotation().getName()
                + "'. You must call AJAXActionAnnotationProcessor.handles() first!");
        }

        doProcess(annotation, action, requestData, session);
    }

    protected abstract Class<T> getAnnotation();

    protected abstract void doProcess(T annotation, AJAXActionService action, AJAXRequestData requestData, ServerSession session) throws OXException;

}
