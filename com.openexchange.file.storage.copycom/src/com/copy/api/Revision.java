
package com.copy.api;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "revision_id",
    "modified_time",
    "size",
    "latest",
    "conflict",
    "id",
    "type",
    "creator"
})
public class Revision {

    @JsonProperty("revision_id")
    private String revisionId;
    @JsonProperty("modified_time")
    private Integer modifiedTime;
    @JsonProperty("size")
    private Integer size;
    @JsonProperty("latest")
    private Boolean latest;
    @JsonProperty("conflict")
    private Boolean conflict;
    @JsonProperty("id")
    private String id;
    @JsonProperty("type")
    private String type;
    @JsonProperty("creator")
    private Creator creator;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("revision_id")
    public String getRevisionId() {
        return revisionId;
    }

    @JsonProperty("revision_id")
    public void setRevisionId(String revisionId) {
        this.revisionId = revisionId;
    }

    @JsonProperty("modified_time")
    public Integer getModifiedTime() {
        return modifiedTime;
    }

    @JsonProperty("modified_time")
    public void setModifiedTime(Integer modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    @JsonProperty("size")
    public Integer getSize() {
        return size;
    }

    @JsonProperty("size")
    public void setSize(Integer size) {
        this.size = size;
    }

    @JsonProperty("latest")
    public Boolean getLatest() {
        return latest;
    }

    @JsonProperty("latest")
    public void setLatest(Boolean latest) {
        this.latest = latest;
    }

    @JsonProperty("conflict")
    public Boolean getConflict() {
        return conflict;
    }

    @JsonProperty("conflict")
    public void setConflict(Boolean conflict) {
        this.conflict = conflict;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("creator")
    public Creator getCreator() {
        return creator;
    }

    @JsonProperty("creator")
    public void setCreator(Creator creator) {
        this.creator = creator;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
