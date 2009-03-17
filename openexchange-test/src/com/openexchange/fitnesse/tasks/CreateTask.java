
package com.openexchange.fitnesse.tasks;

import com.openexchange.ajax.kata.Step;
import com.openexchange.ajax.kata.tasks.TaskCreateStep;
import com.openexchange.groupware.tasks.Task;

/**
 * {@link CreateTask} - a wrapper to use TaskCreateStep via FitNesse
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class CreateTask extends AbstractTaskFixture {

      
    @Override
    protected Step createStep(Task task, String fixtureName, String expectedError) {
        return new TaskCreateStep(task, "create task step", data.getExpectedError());
    }
}
