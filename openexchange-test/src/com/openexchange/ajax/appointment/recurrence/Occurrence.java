
package com.openexchange.ajax.appointment.recurrence;

import java.util.Date;

public class Occurrence {

    private Date startDate = null;

    private Date endDate = null;

    private int position = -1;

    public Occurrence(final Date startDate, final Date endDate, final int position) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.position = position;
    }

    public Date getEndDate() {
        return endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public int getPosition() {
        return position;
    }
}
