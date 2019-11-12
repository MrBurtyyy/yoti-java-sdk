package com.yoti.api.examples.springboot;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yoti.api.client.spi.remote.IssuingAttribute;

import java.util.List;

public class ThirdPartyAttributeIssuingPayload {

    @JsonProperty("issuance_token")
    private String issuanceToken;

    @JsonProperty("attributes")
    private List<IssuingAttribute> issuingAttributes;

    public ThirdPartyAttributeIssuingPayload(String issuanceToken, List<IssuingAttribute> issuingAttributes) {
        this.issuanceToken = issuanceToken;
        this.issuingAttributes = issuingAttributes;
    }

    public String getIssuanceToken() {
        return issuanceToken;
    }

    public void setIssuanceToken(String issuanceToken) {
        this.issuanceToken = issuanceToken;
    }

    public List<IssuingAttribute> getIssuingAttributes() {
        return issuingAttributes;
    }

    public void setIssuingAttributes(List<IssuingAttribute> issuingAttributes) {
        this.issuingAttributes = issuingAttributes;
    }
}
