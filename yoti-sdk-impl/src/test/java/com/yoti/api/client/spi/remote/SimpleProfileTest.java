package com.yoti.api.client.spi.remote;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.yoti.api.client.Attribute;
import com.yoti.api.client.Profile;
import com.yoti.api.client.spi.remote.proto.AttrProto.Anchor;

public class SimpleProfileTest {

    private static final String DRIVING_LICENCE_SOURCE_TYPE = "DRIVING_LICENCE";

    private static final String GIVEN_NAMES_ATTRIBUTE = "given_names";
    private static final String SOME_KEY = "someKey";
    private static final String STARTS_WITH = "startsWith";
    private static final String STRING_VALUE = "test value";
    private static final Integer INTEGER_VALUE = 1;
    
    
    private static String DL_ANCHOR = "CjdBTkMtRE9Dz8qdV2DSwFJicqASUbdSRfmYOsJzswHQ4hDnfOUXtYeRlVOeQnVr3an"
                                    + "ESmMH7e2HEqAIMIIEHDCCAoSgAwIBAgIQIrSqBBTTXWxgGf6OvVm5XDANBgkqhkiG9w0"
                                    + "BAQsFADAuMSwwKgYDVQQDEyNkcml2aW5nLWxpY2VuY2UtcmVnaXN0cmF0aW9uLXNlcnZ"
                                    + "lcjAeFw0xODA0MDUxNDI3MzZaFw0xODA0MTIxNDI3MzZaMC4xLDAqBgNVBAMTI2RyaXZ"
                                    + "pbmctbGljZW5jZS1yZWdpc3RyYXRpb24tc2VydmVyMIIBojANBgkqhkiG9w0BAQEFAAO"
                                    + "CAY8AMIIBigKCAYEA3u2JsiXZftQXRG255RiFHuknxzgGdQ1Qys6O+/Dn/nwEOPbzGBn"
                                    + "4VTMfT1tCl7lD96Eq/qf0v3M6jLWQNJYqt7FbqlH0qtfQLT8fHX04vKwWkJdAvcpOSVd"
                                    + "1i2iyO5wVsvoXCt2ODyMGhd7/6qHeNZei50ARV8zF8diqneNq87Fgg1seuF+YEVAj14y"
                                    + "bjNmTk+MQvKkONSh2OPYNYeF/2H+0pXNe+MXhyY+vJlcRrqXLS52s4VjdeksVc05o/oe"
                                    + "NVckeqgmNhmEnLUNRGQFNOptrB0+g+hcdDQBFOkgeS/dS8iiMp5VQUShKOyQ5/twWOEQ"
                                    + "oJ3ZYRZGIyN8cErUfOUCQBwJOfdspMgbwom3//b5z9+alNOeZDOQRkI5vgvV8s+CvtSn"
                                    + "nMVt9WZMXmY+4uUP9/wZXmw2oBwlJmS9kUKslIHiMNzU07t1y6xMUMhYugxR5GatSN5k"
                                    + "H+36ylJATWVyuuj3Ub/q88cnaiT0jYtsAS4cpJUcEi60+j8qyuc5dAgMBAAGjNjA0MA4"
                                    + "GA1UdDwEB/wQEAwIDmDAiBgsrBgEEAYLwFwEBAQQTMBGAD0RSSVZJTkdfTElDRU5DRTA"
                                    + "NBgkqhkiG9w0BAQsFAAOCAYEANly4rGh8NaE3OwX54kOB8WBO2z/FBDDSi5VByHmMl4V"
                                    + "Pd8Pz26F1kS8qhcKjG6DuaX5UnX33GM6DuLv3nP3uiWEnv/lcitma2LC+qgJp4ItCw2E"
                                    + "MBLiof+dKzms4HqTHyKcPBpxBO6RPkvY5YQDEF0YiW17O31O2ltZTsc9ZsX5M1IiVwbO"
                                    + "ieTDtHy2M/K6Bol/JU/H/L1lAfpZ7khADZmEymjh/6Aw2v18Re37SWl86HxU4t862VNf"
                                    + "ogWO1nlgmgEwoCDgQ6OzR6dhGHJQfXymCJCB3wpA2x3i9rd2L8qrzxX9p5uInCK4+WKS"
                                    + "mhggB31s6dJwS5vAp5D6/i19aMgJqVFfxq/FUA1wkx/flgoC/Xb8MMTDTLo4/ekINdXX"
                                    + "jbQboVii2PGZKAK6FQNZ0FYC7WlA65gBBCZzvQ8imLwBQuy/kLvWbWXVDF5lzMdohijB"
                                    + "nuo4O4fenbAcy51CUvxAjgK7G9FQCyZ39gCPrpy3VVAcjbr9Njk15plcs1yAbGoUDCAE"
                                    + "SgAO1NMBkegQwBTWooNohw8CgIQhfq6dqolvIYDlBIFWThZo34qmRIQe2KKS4SCrxHT5"
                                    + "syjX0X1jtmHPIjZNifbiEAy7Jzzn1xlNWIwetnVoJBcnNumx4r0nmqRrCkRZLlgP4wwM"
                                    + "hwBV56X4TQOUMF8H1ESfmrWIMM9O+vhEJB5QuoAFRPaMcNkYTvbeAvAkhwxfbb8Ac3IW"
                                    + "JPakxORI8jeSop73yc9blxfV1D2ki4yjB2fI7uEXkRBOP/IQ301e7m+fQFLTZ1m1nZiz"
                                    + "Hh+s5GBcApwn92AsfRvgRnSXrc24qoqqvthm4fp9RbnO0d89RqO4Pxu6f1y9BqJ5RMhV"
                                    + "A6Vl+5vsU0nNhiH4Jki9N8dGmX3CTnwf51VUK5aeQwLIgCWaPjE4xC7YX9Fd8WUnsp1/"
                                    + "JllMhAQF7fym40usrHuVt9htd5E2p8zxRidA8NqWNV2rXTGWO5hUSwCAMdfgz431BZSO"
                                    + "fLPZHHg+g4qu+dcLerBqvMggVQLsGB10omwv4oJwiACqFAwgBEoADohVhusZuxzj2ldV"
                                    + "MOKIw+v59l/vWwSgHEIYbIcHNg03EHNLWA7EzrEny+jXyaKERPK8pxASewVJTQo3qYm3"
                                    + "Ezr9QuEy5XG2WfATe1OZuchJxK+IpHRN7o1ZxHf9cCXa22KA4bAKUgb/gSKC6hr9bjMu"
                                    + "06qyb/P+TzWNLTv4OX51dE6iI4WwltsQnPg4BRcrWjvoqkgPi1AKVd+no4J3H2tc0b7a"
                                    + "s/KJCPgR7HMTtuxp/eooR0zPRB/bZFkywrdGbCECshb11G+j1iBYaFHc1ewcmcNjufZV"
                                    + "bZ60pR4JfZUcpiRZJO13ZNnfX7ugc2vK/tL1hM963Y4BfvKXnmQeiLojlpilPxOFET+n"
                                    + "1yodR8J/i1GWzV41Nwx2PFEQv0VofkOZp28mHgQsAM8omReGZqyKEf+oAWjFWY0l1M88"
                                    + "3URQSr0CV04U6iSbS6qeSzL5YkP4CNny0n4Pt79UJWyVA+nHAThnsz4relhfk82At5IL"
                                    + "ASx2zgOkeIJVm5UnTC2ywMkcIARDR0uX8mLLaAhocZv/4kdenjmzEE1nkHW7ks7qh+II"
                                    + "J0YbSPwVkGiIc7BbgXGE8cSGwKuul83Yy/z1InbhBl2B1drEuOjoA";
    
    @Test(expected = IllegalArgumentException.class)
    public void constructor_shouldFailConstructionForNullAttributes() {
        new SimpleProfile(null);
    }

    @Test
    public void is_shouldReturnBooleanValueForExistingKey() {
        Profile profile = new SimpleProfile(asList(SOME_KEY, TRUE));

        boolean result = profile.is(SOME_KEY, false);

        assertTrue(result);
    }

    @Test
    public void is_shouldReturnDefaultBooleanForNonExistingKey() {
        List<Attribute> list = Collections.emptyList();
        Profile profile = new SimpleProfile(list);

        boolean result = profile.is(SOME_KEY, false);

        assertFalse(result);
    }

    @Test
    public void is_shouldReturnDefaultBooleanForMismatchingType() {
        Profile p = new SimpleProfile(asList(SOME_KEY, "String"));

        boolean result = p.is(SOME_KEY, false);

        assertFalse(result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void is_shouldThrowExceptionForNullAttributeName() {
        List<Attribute> list = Collections.emptyList();
        Profile profile = new SimpleProfile(list);

        profile.is(null, false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getAttribute_shouldThrowExceptionForNullAttributeName() {
        List<Attribute> list = Collections.emptyList();
        Profile profile = new SimpleProfile(list);

        profile.getAttribute(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getAttributeTyped_shouldThrowExceptionForNullAttributeName() {
        List<Attribute> list = Collections.emptyList();
        Profile profile = new SimpleProfile(list);

        profile.getAttribute(null, String.class);
    }

    @Test
    public void getAttribute_shouldReturnStringValueForExistingKey() {
        Profile profile = new SimpleProfile(asList(SOME_KEY, STRING_VALUE));

        String result = profile.getAttribute(SOME_KEY);

        assertEquals(STRING_VALUE, result);
    }

    @Test
    public void getAttribute_shouldReturnNullValueForNonExistingKey() {
        List<Attribute> list = Collections.emptyList();
        Profile profile = new SimpleProfile(list);

        assertNull(profile.getAttribute(SOME_KEY));
        assertNull(profile.getAttribute(SOME_KEY, Integer.class));
    }

    @Test
    public void getAttribute_shouldReturnNullValueForMismatchingType() {
        Profile profile = new SimpleProfile(asList(SOME_KEY, INTEGER_VALUE));

        assertNull(profile.getAttribute(SOME_KEY));
        assertNull(profile.getAttribute(SOME_KEY, String.class));
    }

    @Test
    public void getAttribute_shouldReturnIntegerValueForExistingKey() {
        Profile profile = new SimpleProfile(asList(SOME_KEY, INTEGER_VALUE));

        Integer result = profile.getAttribute(SOME_KEY, Integer.class);

        assertEquals(INTEGER_VALUE, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findAttributeStartingWith_shouldThrowExceptionForNullAttributeName() {
        Profile profile = new SimpleProfile(asList(SOME_KEY, INTEGER_VALUE));

        profile.findAttributeStartingWith(null, Object.class);
    }

    @Test
    public void findAttributeStartingWith_shouldReturnNullWhenNoMatchingName() {
        Profile profile = new SimpleProfile(asList(SOME_KEY, INTEGER_VALUE));

        Integer result = profile.findAttributeStartingWith(STARTS_WITH, Integer.class);

        assertNull(result);
    }

    @Test
    public void findAttributeStartingWith_shouldReturnNullForMismatchingType() {
        Profile profile = new SimpleProfile(asList(STARTS_WITH, INTEGER_VALUE));

        String result = profile.findAttributeStartingWith(STARTS_WITH, String.class);

        assertNull(result);
    }

    @Test
    public void findAttributeStartingWith_shouldReturnValueForMatchingKey() {
        Profile profile = new SimpleProfile(asList(STARTS_WITH + ":restOfKey", INTEGER_VALUE));

        Integer result = profile.findAttributeStartingWith(STARTS_WITH, Integer.class);

        assertEquals(INTEGER_VALUE, result);
    }

    @Test
    public void getAttributeSourcesShouldIncludeDrivingLicence() throws ParseException, IOException {
        com.yoti.api.client.spi.remote.proto.AttrProto.Attribute attribute = buildAnchoredAttribute(GIVEN_NAMES_ATTRIBUTE, "A_Given_NAME", DL_ANCHOR);
        
        Profile profile = new SimpleProfile(singletonList(AttributeConverter.convertAttribute(attribute)));
        
        Set<String> sources = profile.getAttributeObject(GIVEN_NAMES_ATTRIBUTE).getSources();
        assertTrue(sources.contains(DRIVING_LICENCE_SOURCE_TYPE));
    }

    private com.yoti.api.client.spi.remote.proto.AttrProto.Attribute buildAnchoredAttribute(String name, String value, String rawAnchor)
            throws InvalidProtocolBufferException {
        Anchor anchor = Anchor.parseFrom(Base64.getDecoder().decode(rawAnchor));
        com.yoti.api.client.spi.remote.proto.AttrProto.Attribute attribute = com.yoti.api.client.spi.remote.proto.AttrProto.Attribute.newBuilder()
                .setName(name)
                .setValue(ByteString.copyFromUtf8(value))
                .addAnchors(anchor)
                .build();
        return attribute;
    }
    
    
    private List<Attribute> asList(String key, Object o) {
        return singletonList(new Attribute(key, o, null));
    }

}
