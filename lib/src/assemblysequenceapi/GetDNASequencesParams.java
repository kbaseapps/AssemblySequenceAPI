
package assemblysequenceapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import us.kbase.common.service.Tuple4;


/**
 * <p>Original spec-file type: GetDNASequencesParams</p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "assembly_ref",
    "contigset_ref",
    "requested_features"
})
public class GetDNASequencesParams {

    @JsonProperty("assembly_ref")
    private java.lang.String assemblyRef;
    @JsonProperty("contigset_ref")
    private java.lang.String contigsetRef;
    @JsonProperty("requested_features")
    private Map<String, List<Tuple4 <String, Long, String, Long>>> requestedFeatures;
    private Map<java.lang.String, Object> additionalProperties = new HashMap<java.lang.String, Object>();

    @JsonProperty("assembly_ref")
    public java.lang.String getAssemblyRef() {
        return assemblyRef;
    }

    @JsonProperty("assembly_ref")
    public void setAssemblyRef(java.lang.String assemblyRef) {
        this.assemblyRef = assemblyRef;
    }

    public GetDNASequencesParams withAssemblyRef(java.lang.String assemblyRef) {
        this.assemblyRef = assemblyRef;
        return this;
    }

    @JsonProperty("contigset_ref")
    public java.lang.String getContigsetRef() {
        return contigsetRef;
    }

    @JsonProperty("contigset_ref")
    public void setContigsetRef(java.lang.String contigsetRef) {
        this.contigsetRef = contigsetRef;
    }

    public GetDNASequencesParams withContigsetRef(java.lang.String contigsetRef) {
        this.contigsetRef = contigsetRef;
        return this;
    }

    @JsonProperty("requested_features")
    public Map<String, List<Tuple4 <String, Long, String, Long>>> getRequestedFeatures() {
        return requestedFeatures;
    }

    @JsonProperty("requested_features")
    public void setRequestedFeatures(Map<String, List<Tuple4 <String, Long, String, Long>>> requestedFeatures) {
        this.requestedFeatures = requestedFeatures;
    }

    public GetDNASequencesParams withRequestedFeatures(Map<String, List<Tuple4 <String, Long, String, Long>>> requestedFeatures) {
        this.requestedFeatures = requestedFeatures;
        return this;
    }

    @JsonAnyGetter
    public Map<java.lang.String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperties(java.lang.String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public java.lang.String toString() {
        return ((((((((("GetDNASequencesParams"+" [assemblyRef=")+ assemblyRef)+", contigsetRef=")+ contigsetRef)+", requestedFeatures=")+ requestedFeatures)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
