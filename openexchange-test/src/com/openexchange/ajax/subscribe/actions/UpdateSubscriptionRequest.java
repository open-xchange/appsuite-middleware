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

package com.openexchange.ajax.subscribe.actions;

import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.ajax.framework.Params;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.json.SubscriptionJSONWriter;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class UpdateSubscriptionRequest extends AbstractSubscriptionRequest<UpdateSubscriptionResponse> {

    private DynamicFormDescription formDescription;

    private Subscription subscription;

    public void setFormDescription(DynamicFormDescription formDescription) {
        this.formDescription = formDescription;
    }

    public DynamicFormDescription getFormDescription() {
        return formDescription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public UpdateSubscriptionRequest() {
        super();
    }

    public UpdateSubscriptionRequest(Subscription subscription, DynamicFormDescription formDescription) {
        this();
        setFormDescription(formDescription);
        setSubscription(subscription);
    }

    @Override
    public Object getBody() throws JSONException {
        SubscriptionJSONWriter writer = new SubscriptionJSONWriter();
        return writer.write(subscription, getFormDescription(), null, null);
    }

    @Override
    public Method getMethod() {
        return Method.PUT;
    }

    @Override
    public Parameter[] getParameters() {
        return new Params(AJAXServlet.PARAMETER_ACTION, AJAXServlet.ACTION_UPDATE).toArray();
    }

    @Override
    public AbstractAJAXParser<UpdateSubscriptionResponse> getParser() {
        return new AbstractAJAXParser<UpdateSubscriptionResponse>(getFailOnError()) {

            @Override
            protected UpdateSubscriptionResponse createResponse(final Response response) throws JSONException {
                return new UpdateSubscriptionResponse(response);
            }
        };
    }

}
