
package com.openexchange.ajax.kata.fixtures;

import com.openexchange.ajax.kata.Step;
import com.openexchange.ajax.kata.folders.FolderCreateStep;
import com.openexchange.ajax.kata.folders.FolderDeleteStep;
import com.openexchange.ajax.kata.folders.FolderUpdateStep;
import com.openexchange.ajax.kata.folders.FolderVerificationStep;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.fixtures.Fixture;

public class FolderFixtureTransformer extends AbstractFixtureTransformer<FolderObject> {

    @Override
    public boolean handles(Class class1, String fixtureName, Fixture fixture) {
        return class1 == FolderObject.class;
    }

    @Override
    public Step transform(Class class1, String fixtureName, Fixture fixture, String displayName) {
        if (isDelete(fixtureName)) {
            return assign(fixtureName, new FolderDeleteStep((FolderObject) fixture.getEntry(), displayName, (String) fixture.getAttribute("expectedError")));
        } else if (isUpdate(fixtureName)) {
            return assign(fixtureName, new FolderUpdateStep((FolderObject) fixture.getEntry(), displayName, (String) fixture.getAttribute("expectedError")));
        } else if (isVerification(fixtureName)) {
            return assign(fixtureName, new FolderVerificationStep((FolderObject) fixture.getEntry(), displayName));
        } else if (isCreate(fixtureName)) {
            FolderCreateStep step = new FolderCreateStep((FolderObject) fixture.getEntry(), displayName, (String) fixture.getAttribute("expectedError"));
            remember(fixtureName, step);
            return step;
        }
        return null;
    }
}
