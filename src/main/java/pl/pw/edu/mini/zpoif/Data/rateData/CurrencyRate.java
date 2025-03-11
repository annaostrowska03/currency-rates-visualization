package pl.pw.edu.mini.zpoif.Data.rateData;
import com.fasterxml.jackson.annotation.*;

import javax.annotation.processing.Generated;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "table",
        "no",
        "effectiveDate",
        "rates"
})

@Generated("jsonschema2pojo")
public class CurrencyRate {
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();
    @JsonProperty("table")
    private String table;
    @JsonProperty("no")
    private String no;
    @JsonProperty("effectiveDate")
    private Date effectiveDate;
    @JsonProperty("rates")
    private List<Rate> rates = null;

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public List<Rate> getRates() {
        return rates;
    }

    public void setRates(List<Rate> rates) {
        this.rates = rates;
    }




}
