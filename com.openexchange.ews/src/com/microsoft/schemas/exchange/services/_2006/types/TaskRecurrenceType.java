
package com.microsoft.schemas.exchange.services._2006.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for TaskRecurrenceType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TaskRecurrenceType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://schemas.microsoft.com/exchange/services/2006/types}TaskRecurrencePatternTypes"/>
 *         &lt;group ref="{http://schemas.microsoft.com/exchange/services/2006/types}RecurrenceRangeTypes"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaskRecurrenceType", propOrder = {
    "relativeYearlyRecurrence",
    "absoluteYearlyRecurrence",
    "relativeMonthlyRecurrence",
    "absoluteMonthlyRecurrence",
    "weeklyRecurrence",
    "dailyRecurrence",
    "dailyRegeneration",
    "weeklyRegeneration",
    "monthlyRegeneration",
    "yearlyRegeneration",
    "noEndRecurrence",
    "endDateRecurrence",
    "numberedRecurrence"
})
public class TaskRecurrenceType {

    @XmlElement(name = "RelativeYearlyRecurrence")
    protected RelativeYearlyRecurrencePatternType relativeYearlyRecurrence;
    @XmlElement(name = "AbsoluteYearlyRecurrence")
    protected AbsoluteYearlyRecurrencePatternType absoluteYearlyRecurrence;
    @XmlElement(name = "RelativeMonthlyRecurrence")
    protected RelativeMonthlyRecurrencePatternType relativeMonthlyRecurrence;
    @XmlElement(name = "AbsoluteMonthlyRecurrence")
    protected AbsoluteMonthlyRecurrencePatternType absoluteMonthlyRecurrence;
    @XmlElement(name = "WeeklyRecurrence")
    protected WeeklyRecurrencePatternType weeklyRecurrence;
    @XmlElement(name = "DailyRecurrence")
    protected DailyRecurrencePatternType dailyRecurrence;
    @XmlElement(name = "DailyRegeneration")
    protected DailyRegeneratingPatternType dailyRegeneration;
    @XmlElement(name = "WeeklyRegeneration")
    protected WeeklyRegeneratingPatternType weeklyRegeneration;
    @XmlElement(name = "MonthlyRegeneration")
    protected MonthlyRegeneratingPatternType monthlyRegeneration;
    @XmlElement(name = "YearlyRegeneration")
    protected YearlyRegeneratingPatternType yearlyRegeneration;
    @XmlElement(name = "NoEndRecurrence")
    protected NoEndRecurrenceRangeType noEndRecurrence;
    @XmlElement(name = "EndDateRecurrence")
    protected EndDateRecurrenceRangeType endDateRecurrence;
    @XmlElement(name = "NumberedRecurrence")
    protected NumberedRecurrenceRangeType numberedRecurrence;

    /**
     * Gets the value of the relativeYearlyRecurrence property.
     * 
     * @return
     *     possible object is
     *     {@link RelativeYearlyRecurrencePatternType }
     *     
     */
    public RelativeYearlyRecurrencePatternType getRelativeYearlyRecurrence() {
        return relativeYearlyRecurrence;
    }

    /**
     * Sets the value of the relativeYearlyRecurrence property.
     * 
     * @param value
     *     allowed object is
     *     {@link RelativeYearlyRecurrencePatternType }
     *     
     */
    public void setRelativeYearlyRecurrence(RelativeYearlyRecurrencePatternType value) {
        this.relativeYearlyRecurrence = value;
    }

    /**
     * Gets the value of the absoluteYearlyRecurrence property.
     * 
     * @return
     *     possible object is
     *     {@link AbsoluteYearlyRecurrencePatternType }
     *     
     */
    public AbsoluteYearlyRecurrencePatternType getAbsoluteYearlyRecurrence() {
        return absoluteYearlyRecurrence;
    }

    /**
     * Sets the value of the absoluteYearlyRecurrence property.
     * 
     * @param value
     *     allowed object is
     *     {@link AbsoluteYearlyRecurrencePatternType }
     *     
     */
    public void setAbsoluteYearlyRecurrence(AbsoluteYearlyRecurrencePatternType value) {
        this.absoluteYearlyRecurrence = value;
    }

    /**
     * Gets the value of the relativeMonthlyRecurrence property.
     * 
     * @return
     *     possible object is
     *     {@link RelativeMonthlyRecurrencePatternType }
     *     
     */
    public RelativeMonthlyRecurrencePatternType getRelativeMonthlyRecurrence() {
        return relativeMonthlyRecurrence;
    }

    /**
     * Sets the value of the relativeMonthlyRecurrence property.
     * 
     * @param value
     *     allowed object is
     *     {@link RelativeMonthlyRecurrencePatternType }
     *     
     */
    public void setRelativeMonthlyRecurrence(RelativeMonthlyRecurrencePatternType value) {
        this.relativeMonthlyRecurrence = value;
    }

    /**
     * Gets the value of the absoluteMonthlyRecurrence property.
     * 
     * @return
     *     possible object is
     *     {@link AbsoluteMonthlyRecurrencePatternType }
     *     
     */
    public AbsoluteMonthlyRecurrencePatternType getAbsoluteMonthlyRecurrence() {
        return absoluteMonthlyRecurrence;
    }

    /**
     * Sets the value of the absoluteMonthlyRecurrence property.
     * 
     * @param value
     *     allowed object is
     *     {@link AbsoluteMonthlyRecurrencePatternType }
     *     
     */
    public void setAbsoluteMonthlyRecurrence(AbsoluteMonthlyRecurrencePatternType value) {
        this.absoluteMonthlyRecurrence = value;
    }

    /**
     * Gets the value of the weeklyRecurrence property.
     * 
     * @return
     *     possible object is
     *     {@link WeeklyRecurrencePatternType }
     *     
     */
    public WeeklyRecurrencePatternType getWeeklyRecurrence() {
        return weeklyRecurrence;
    }

    /**
     * Sets the value of the weeklyRecurrence property.
     * 
     * @param value
     *     allowed object is
     *     {@link WeeklyRecurrencePatternType }
     *     
     */
    public void setWeeklyRecurrence(WeeklyRecurrencePatternType value) {
        this.weeklyRecurrence = value;
    }

    /**
     * Gets the value of the dailyRecurrence property.
     * 
     * @return
     *     possible object is
     *     {@link DailyRecurrencePatternType }
     *     
     */
    public DailyRecurrencePatternType getDailyRecurrence() {
        return dailyRecurrence;
    }

    /**
     * Sets the value of the dailyRecurrence property.
     * 
     * @param value
     *     allowed object is
     *     {@link DailyRecurrencePatternType }
     *     
     */
    public void setDailyRecurrence(DailyRecurrencePatternType value) {
        this.dailyRecurrence = value;
    }

    /**
     * Gets the value of the dailyRegeneration property.
     * 
     * @return
     *     possible object is
     *     {@link DailyRegeneratingPatternType }
     *     
     */
    public DailyRegeneratingPatternType getDailyRegeneration() {
        return dailyRegeneration;
    }

    /**
     * Sets the value of the dailyRegeneration property.
     * 
     * @param value
     *     allowed object is
     *     {@link DailyRegeneratingPatternType }
     *     
     */
    public void setDailyRegeneration(DailyRegeneratingPatternType value) {
        this.dailyRegeneration = value;
    }

    /**
     * Gets the value of the weeklyRegeneration property.
     * 
     * @return
     *     possible object is
     *     {@link WeeklyRegeneratingPatternType }
     *     
     */
    public WeeklyRegeneratingPatternType getWeeklyRegeneration() {
        return weeklyRegeneration;
    }

    /**
     * Sets the value of the weeklyRegeneration property.
     * 
     * @param value
     *     allowed object is
     *     {@link WeeklyRegeneratingPatternType }
     *     
     */
    public void setWeeklyRegeneration(WeeklyRegeneratingPatternType value) {
        this.weeklyRegeneration = value;
    }

    /**
     * Gets the value of the monthlyRegeneration property.
     * 
     * @return
     *     possible object is
     *     {@link MonthlyRegeneratingPatternType }
     *     
     */
    public MonthlyRegeneratingPatternType getMonthlyRegeneration() {
        return monthlyRegeneration;
    }

    /**
     * Sets the value of the monthlyRegeneration property.
     * 
     * @param value
     *     allowed object is
     *     {@link MonthlyRegeneratingPatternType }
     *     
     */
    public void setMonthlyRegeneration(MonthlyRegeneratingPatternType value) {
        this.monthlyRegeneration = value;
    }

    /**
     * Gets the value of the yearlyRegeneration property.
     * 
     * @return
     *     possible object is
     *     {@link YearlyRegeneratingPatternType }
     *     
     */
    public YearlyRegeneratingPatternType getYearlyRegeneration() {
        return yearlyRegeneration;
    }

    /**
     * Sets the value of the yearlyRegeneration property.
     * 
     * @param value
     *     allowed object is
     *     {@link YearlyRegeneratingPatternType }
     *     
     */
    public void setYearlyRegeneration(YearlyRegeneratingPatternType value) {
        this.yearlyRegeneration = value;
    }

    /**
     * Gets the value of the noEndRecurrence property.
     * 
     * @return
     *     possible object is
     *     {@link NoEndRecurrenceRangeType }
     *     
     */
    public NoEndRecurrenceRangeType getNoEndRecurrence() {
        return noEndRecurrence;
    }

    /**
     * Sets the value of the noEndRecurrence property.
     * 
     * @param value
     *     allowed object is
     *     {@link NoEndRecurrenceRangeType }
     *     
     */
    public void setNoEndRecurrence(NoEndRecurrenceRangeType value) {
        this.noEndRecurrence = value;
    }

    /**
     * Gets the value of the endDateRecurrence property.
     * 
     * @return
     *     possible object is
     *     {@link EndDateRecurrenceRangeType }
     *     
     */
    public EndDateRecurrenceRangeType getEndDateRecurrence() {
        return endDateRecurrence;
    }

    /**
     * Sets the value of the endDateRecurrence property.
     * 
     * @param value
     *     allowed object is
     *     {@link EndDateRecurrenceRangeType }
     *     
     */
    public void setEndDateRecurrence(EndDateRecurrenceRangeType value) {
        this.endDateRecurrence = value;
    }

    /**
     * Gets the value of the numberedRecurrence property.
     * 
     * @return
     *     possible object is
     *     {@link NumberedRecurrenceRangeType }
     *     
     */
    public NumberedRecurrenceRangeType getNumberedRecurrence() {
        return numberedRecurrence;
    }

    /**
     * Sets the value of the numberedRecurrence property.
     * 
     * @param value
     *     allowed object is
     *     {@link NumberedRecurrenceRangeType }
     *     
     */
    public void setNumberedRecurrence(NumberedRecurrenceRangeType value) {
        this.numberedRecurrence = value;
    }

}
