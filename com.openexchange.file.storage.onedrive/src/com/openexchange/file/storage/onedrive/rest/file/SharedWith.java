
package com.openexchange.file.storage.onedrive.rest.file;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import org.apache.commons.lang.builder.ToStringBuilder;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * {@link SharedWith} <td>
 * <p>
 * Info about who can access the folder. The options are:
 * </p>
 * <ul>
 * <li><strong>People I selected</strong></li>
 * <li><strong>Just me</strong></li>
 * <li><strong>Everyone (public)</strong></li>
 * <li><strong>Friends</strong></li>
 * <li><strong>My friends and their friends</strong></li>
 * <li><strong>People with a link</strong></li>
 * </ul>
 * The default is <strong>Just me</strong>.</td>
 * <p>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "access"
})
public class SharedWith {

    @JsonProperty("access")
    private String access;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * Initializes a new {@link SharedWith}.
     */
    public SharedWith() {
        super();
        access = "Just me";
    }

    @JsonProperty("access")
    public String getAccess() {
        return access;
    }

    @JsonProperty("access")
    public void setAccess(String access) {
        this.access = access;
    }

    public SharedWith withAccess(String access) {
        this.access = access;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public SharedWith withAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
        return this;
    }

}
