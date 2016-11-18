
package com.openexchange.ajax.kata.fixtures;

import com.openexchange.ajax.kata.Step;
import com.openexchange.ajax.kata.contacts.ContactCreateStep;
import com.openexchange.ajax.kata.contacts.ContactDeleteStep;
import com.openexchange.ajax.kata.contacts.ContactUpdateStep;
import com.openexchange.ajax.kata.contacts.ContactVerificationStep;
import com.openexchange.groupware.container.Contact;
import com.openexchange.test.fixtures.Fixture;

/**
 * {@link ContactFixtureTransformer}
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class ContactFixtureTransformer extends AbstractFixtureTransformer<Contact> {

    @Override
    public boolean handles(Class class1, String fixtureName, Fixture fixture) {
        return class1 == Contact.class;
    }

    @Override
    public Step transform(Class class1, String fixtureName, Fixture fixture, String displayName) {
        if (isDelete(fixtureName)) {
            return assign(fixtureName, new ContactDeleteStep((Contact) fixture.getEntry(), displayName, (String) fixture.getAttribute("expectedError")));
        } else if (isUpdate(fixtureName)) {
            return assign(fixtureName, new ContactUpdateStep((Contact) fixture.getEntry(), displayName, (String) fixture.getAttribute("expectedError")));
        } else if (isVerification(fixtureName)) {
            return assign(fixtureName, new ContactVerificationStep((Contact) fixture.getEntry(), displayName));
        } else if (isCreate(fixtureName)) {
            ContactCreateStep step = new ContactCreateStep((Contact) fixture.getEntry(), displayName, (String) fixture.getAttribute("expectedError"));
            remember(fixtureName, step);
            return step;
        }
        return null;
    }

}
