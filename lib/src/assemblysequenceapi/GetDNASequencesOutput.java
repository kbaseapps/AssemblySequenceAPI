
package assemblysequenceapi;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


/**
 * <p>Original spec-file type: GetDNASequencesOutput</p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("com.googlecode.jsonschema2pojo")
@JsonPropertyOrder({
    "dna_sequences"
})
public class GetDNASequencesOutput {

    @JsonProperty("dna_sequences")
    private Map<String, String> dnaSequences;
    private Map<java.lang.String, Object> additionalProperties = new HashMap<java.lang.String, Object>();

    @JsonProperty("dna_sequences")
    public Map<String, String> getDnaSequences() {
        return dnaSequences;
    }

    @JsonProperty("dna_sequences")
    public void setDnaSequences(Map<String, String> dnaSequences) {
        this.dnaSequences = dnaSequences;
    }

    public GetDNASequencesOutput withDnaSequences(Map<String, String> dnaSequences) {
        this.dnaSequences = dnaSequences;
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
        return ((((("GetDNASequencesOutput"+" [dnaSequences=")+ dnaSequences)+", additionalProperties=")+ additionalProperties)+"]");
    }

}
