
package com.openexchange.fitnesse.tasks;

import com.openexchange.ajax.kata.Step;
import com.openexchange.ajax.kata.tasks.TaskDeleteStep;
import com.openexchange.groupware.tasks.Task;

public class DeleteTask extends AbstractTaskFixture {

    @Override
    protected Step createStep(Task task, String fixtureName, String expectedError) {
        return new TaskDeleteStep(task, data.getFixtureName(), data.getExpectedError());
    }

    

}
