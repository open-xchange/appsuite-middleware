
package com.openexchange.ajax.kata.fixtures;

import com.openexchange.ajax.kata.Step;
import com.openexchange.ajax.kata.contacts.ContactCreateStep;
import com.openexchange.ajax.kata.contacts.ContactUpdateStep;
import com.openexchange.ajax.kata.contacts.ContactVerificationStep;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.test.fixtures.Fixture;

/**
 * {@link ContactFixtureTransformer}
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class ContactFixtureTransformer extends AbstractFixtureTransformer<ContactObject> {

    public boolean handles(Class class1, String fixtureName, Fixture fixture) {
        return class1 == ContactObject.class;
    }

    public Step transform(Class class1, String fixtureName, Fixture fixture, String displayName) {
        if (isCreate(fixtureName)) {
            ContactCreateStep step = new ContactCreateStep(
                (ContactObject) fixture.getEntry(),
                displayName,
                (String) fixture.getAttribute("expectedError"));
            remember(fixtureName, step);
            return step;
        } else if (isUpdate(fixtureName)) {
            return assign(fixtureName, new ContactUpdateStep(
                (ContactObject) fixture.getEntry(),
                displayName,
                (String) fixture.getAttribute("expectedError")));
        } else if (isVerfication(fixtureName)) {
            return assign(fixtureName, new ContactVerificationStep((ContactObject) fixture.getEntry(), displayName));
        }
        return null;
    }

}
