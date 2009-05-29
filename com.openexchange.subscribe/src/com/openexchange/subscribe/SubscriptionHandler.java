
package com.openexchange.subscribe;

import com.openexchange.groupware.AbstractOXException;


public interface SubscriptionHandler {

    public abstract void handleSubscription(Subscription subscription) throws AbstractOXException;

}
