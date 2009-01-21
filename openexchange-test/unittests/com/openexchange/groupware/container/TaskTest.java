
package com.openexchange.groupware.container;

import java.util.Date;
import com.openexchange.groupware.tasks.Task;

public class TaskTest extends CalendarObjectTest {

    public void testFindDifferingFields() {
        Task dataObject = getTask();
        Task otherDataObject = getTask();

        otherDataObject.setActualCosts(1.2f);
        assertDifferences(dataObject, otherDataObject, Task.ACTUAL_COSTS);

        otherDataObject.setActualDuration(12l);
        assertDifferences(dataObject, otherDataObject, Task.ACTUAL_COSTS, Task.ACTUAL_DURATION);


        otherDataObject.setAlarm(new Date(23));
        assertDifferences(dataObject, otherDataObject, Task.ACTUAL_COSTS, Task.ACTUAL_DURATION, Task.ALARM);

        otherDataObject.setBillingInformation("Blupp");
        assertDifferences(
            dataObject,
            otherDataObject,
            Task.ACTUAL_COSTS,
            Task.ACTUAL_DURATION,
            Task.ALARM,
            Task.BILLING_INFORMATION);

        otherDataObject.setCompanies("Blupp");
        assertDifferences(
            dataObject,
            otherDataObject,
            Task.ACTUAL_COSTS,
            Task.ACTUAL_DURATION,
            Task.ALARM,
            Task.BILLING_INFORMATION,
            Task.COMPANIES);

        otherDataObject.setCurrency("Blupp");
        assertDifferences(
            dataObject,
            otherDataObject,
            Task.ACTUAL_COSTS,
            Task.ACTUAL_DURATION,
            Task.ALARM,
            Task.BILLING_INFORMATION,
            Task.COMPANIES,
            Task.CURRENCY);

        otherDataObject.setDateCompleted(new Date(23));
        assertDifferences(
            dataObject,
            otherDataObject,
            Task.ACTUAL_COSTS,
            Task.ACTUAL_DURATION,
            Task.ALARM,
            Task.BILLING_INFORMATION,
            Task.COMPANIES,
            Task.CURRENCY,
            Task.DATE_COMPLETED);

        otherDataObject.setPercentComplete(12);
        assertDifferences(
            dataObject,
            otherDataObject,
            Task.ACTUAL_COSTS,
            Task.ACTUAL_DURATION,
            Task.ALARM,
            Task.BILLING_INFORMATION,
            Task.COMPANIES,
            Task.CURRENCY,
            Task.DATE_COMPLETED,
            Task.PERCENT_COMPLETED);

        otherDataObject.setPriority(12);
        assertDifferences(
            dataObject,
            otherDataObject,
            Task.ACTUAL_COSTS,
            Task.ACTUAL_DURATION,
            Task.ALARM,
            Task.BILLING_INFORMATION,
            Task.COMPANIES,
            Task.CURRENCY,
            Task.DATE_COMPLETED,
            Task.PERCENT_COMPLETED,
            Task.PRIORITY);

        otherDataObject.setProjectID(12);
        assertDifferences(
            dataObject,
            otherDataObject,
            Task.ACTUAL_COSTS,
            Task.ACTUAL_DURATION,
            Task.ALARM,
            Task.BILLING_INFORMATION,
            Task.COMPANIES,
            Task.CURRENCY,
            Task.DATE_COMPLETED,
            Task.PERCENT_COMPLETED,
            Task.PRIORITY,
            Task.PROJECT_ID);

        otherDataObject.setStatus(12);
        assertDifferences(
            dataObject,
            otherDataObject,
            Task.ACTUAL_COSTS,
            Task.ACTUAL_DURATION,
            Task.ALARM,
            Task.BILLING_INFORMATION,
            Task.COMPANIES,
            Task.CURRENCY,
            Task.DATE_COMPLETED,
            Task.PERCENT_COMPLETED,
            Task.PRIORITY,
            Task.PROJECT_ID,
            Task.STATUS);

        otherDataObject.setTargetCosts(1.2f);
        assertDifferences(
            dataObject,
            otherDataObject,
            Task.ACTUAL_COSTS,
            Task.ACTUAL_DURATION,
            Task.ALARM,
            Task.BILLING_INFORMATION,
            Task.COMPANIES,
            Task.CURRENCY,
            Task.DATE_COMPLETED,
            Task.PERCENT_COMPLETED,
            Task.PRIORITY,
            Task.PROJECT_ID,
            Task.STATUS,
            Task.TARGET_COSTS);

        otherDataObject.setTargetDuration(12l);
        assertDifferences(
            dataObject,
            otherDataObject,
            Task.ACTUAL_COSTS,
            Task.ACTUAL_DURATION,
            Task.ALARM,
            Task.BILLING_INFORMATION,
            Task.COMPANIES,
            Task.CURRENCY,
            Task.DATE_COMPLETED,
            Task.PERCENT_COMPLETED,
            Task.PRIORITY,
            Task.PROJECT_ID,
            Task.STATUS,
            Task.TARGET_COSTS,
            Task.TARGET_DURATION);

        otherDataObject.setTripMeter("Blupp");
        assertDifferences(
            dataObject,
            otherDataObject,
            Task.ACTUAL_COSTS,
            Task.ACTUAL_DURATION,
            Task.ALARM,
            Task.BILLING_INFORMATION,
            Task.COMPANIES,
            Task.CURRENCY,
            Task.DATE_COMPLETED,
            Task.PERCENT_COMPLETED,
            Task.PRIORITY,
            Task.PROJECT_ID,
            Task.STATUS,
            Task.TARGET_COSTS,
            Task.TARGET_DURATION,
            Task.TRIP_METER);

    }

    public Task getTask() {
        Task task = new Task();

        fillTask(task);

        return task;
    }

    public void fillTask(Task object) {
        super.fillCalendarObject(object);

        object.setActualCosts(-1.2f);

        object.setActualDuration(-12l);

        object.setAfterComplete(new Date(42));

        object.setAlarm(new Date(42));

        object.setBillingInformation("Bla");

        object.setCompanies("Bla");

        object.setCurrency("Bla");

        object.setDateCompleted(new Date(42));

        object.setPercentComplete(-12);

        object.setPriority(-12);

        object.setProjectID(-12);

        object.setStatus(-12);

        object.setTargetCosts(-1.2f);

        object.setTargetDuration(-12l);

        object.setTripMeter("Bla");
    }
}
