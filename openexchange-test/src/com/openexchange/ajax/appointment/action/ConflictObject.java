
package com.openexchange.ajax.appointment.action;

import java.util.List;

public class ConflictObject {

    private String title;
    private int shownAs;
    private int id;
    private int createdBy;
    private long startDate;
    private long endDate;
    private List<Participant> participants;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getShownAs() {
        return shownAs;
    }

    public void setShownAs(int shown_as) {
        this.shownAs = shown_as;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int created_by) {
        this.createdBy = created_by;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long start_date) {
        this.startDate = start_date;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long end_date) {
        this.endDate = end_date;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }
}
