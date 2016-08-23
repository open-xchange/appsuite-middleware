
package com.openexchange.admin.soap.util.dataobjects;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CheckedDatabases", namespace = "http://dataobjects.rmi.admin.openexchange.com/xsd", propOrder = {
    "needsupdate",
    "blocked",
    "outdated"
})
public class CheckedDatabases {

    @XmlElement(nillable = true)
    protected List<Database> needsupdate;
    @XmlElement(nillable = true)
    protected List<Database> blocked;
    @XmlElement(nillable = true)
    protected List<Database> outdated;

    public List<Database> getNeedsupdate() {
        return needsupdate;
    }

    public void setNeedsupdate(List<Database> needsupdate) {
        this.needsupdate = needsupdate;
    }

    public List<Database> getBlocked() {
        return blocked;
    }

    public void setBlocked(List<Database> blocked) {
        this.blocked = blocked;
    }

    public List<Database> getOutdated() {
        return outdated;
    }

    public void setOutdated(List<Database> outdated) {
        this.outdated = outdated;
    }

}
