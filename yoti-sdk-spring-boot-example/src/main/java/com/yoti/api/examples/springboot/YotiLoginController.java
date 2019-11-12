package com.yoti.api.examples.springboot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.util.*;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yoti.api.attributes.AttributeConstants;
import com.yoti.api.client.*;
import com.yoti.api.client.shareurl.DynamicScenario;
import com.yoti.api.client.shareurl.DynamicShareException;
import com.yoti.api.client.shareurl.SimpleDynamicScenarioBuilder;
import com.yoti.api.client.shareurl.extension.*;
import com.yoti.api.client.shareurl.policy.*;
import com.yoti.api.client.spi.remote.IssuingAttribute;
import com.yoti.api.client.spi.remote.KeyStreamVisitor;
import com.yoti.api.client.spi.remote.call.HttpMethod;
import com.yoti.api.client.spi.remote.call.SignedRequest;
import com.yoti.api.client.spi.remote.call.SignedRequestBuilder;
import com.yoti.api.spring.YotiClientProperties;
import com.yoti.api.spring.YotiProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.thymeleaf.util.StringUtils;

@Configuration
@ConditionalOnClass(YotiClient.class)
@EnableConfigurationProperties({ YotiClientProperties.class, YotiProperties.class })
@Controller
@EnableWebMvc
public class YotiLoginController extends WebMvcConfigurerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(YotiLoginController.class);

    static {
        System.setProperty("yoti.api.url", "https://stg1.api.internal.yoti.com/api/v1");
    }

    private final YotiClient client;
    private final YotiClientProperties properties;
    private final KeyPairSource keyPairSource;

    private final String ATTRIBUTE_API_URL = System.getProperty("yoti.api.url") + "/attribute-registry";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
    }

    @Autowired
    public YotiLoginController(final YotiClient client, YotiClientProperties properties, KeyPairSource keyPairSource) {
        this.client = client;
        this.properties = properties;
        this.keyPairSource = keyPairSource;
    }

    @RequestMapping("/")
    public String initialThirdPartyAttributeShare(final Model model) {

        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR_OF_DAY, 5);

        Date expiryDate = calendar.getTime();

        ThirdPartyAttributeExtensionBuilder thirdPartyAttributeExtensionBuilder = ExtensionBuilderFactory.newInstance()
                .createThirdPartyAttributeExtensionBuilder();

        Extension<ThirdPartyAttributeContent> thirdPartyAttributeExtension = thirdPartyAttributeExtensionBuilder
                .withExpiryDate(expiryDate)
                .withDefinition("com.rakuten.id")
                .build();

        DynamicPolicy dynamicPolicy = new SimpleDynamicPolicyBuilder()
                .withSelfie()
                .build();

        DynamicScenario dynamicScenario = new SimpleDynamicScenarioBuilder()
                .withCallbackEndpoint("/issue")
                .withPolicy(dynamicPolicy)
                .withExtension(thirdPartyAttributeExtension)
                .build();

        try {
            String shareUrl = client.createShareUrl(dynamicScenario).getUrl();
            model.addAttribute("yotiShareUrl", shareUrl);
        } catch (DynamicShareException e) {
            LOG.error(e.getMessage());
        }

        model.addAttribute("clientSdkId", properties.getClientSdkId());

        return "dynamic-share";
    }

    /**
     * This endpoint is the "Callback URL" which will be called by user's browser after user logs in. It's a GET endpoint.
     * We will pass you a token inside url query string (/login?token=token-value)
     */
    @RequestMapping("/issue")
    public String issueAttributeAndGetInShare(@RequestParam("token") final String token, final Model model) throws Exception {
        ActivityDetails activityDetails;
        HumanProfile humanProfile;

        try {
            activityDetails = client.getActivityDetails(token);
            humanProfile = activityDetails.getUserProfile();
        } catch (final ProfileException profileException) {
            LOG.info("Could not get profile", profileException);
            model.addAttribute("error", profileException.getMessage());
            return "error";
        }

        ExtraData extraData = activityDetails.getExtraData();
        AttributeIssuanceDetails attributeIssuanceDetails = extraData.getAttributeIssuanceDetails();

        String attributeIssuanceDetailsToken = attributeIssuanceDetails.getToken();
        List<AttributeDefinition> issuingAttributes = attributeIssuanceDetails.getIssuingAttributes();

        List<IssuingAttribute> attributesToIssue = new ArrayList<>();
        for (AttributeDefinition attrDef : issuingAttributes) {
            IssuingAttribute issuingAttribute = new IssuingAttribute(attrDef, "some-value");
            attributesToIssue.add(issuingAttribute);
        }

        ThirdPartyAttributeIssuingPayload thirdPartyAttributeIssuingPayload = new ThirdPartyAttributeIssuingPayload(
                attributeIssuanceDetailsToken,
                attributesToIssue
        );

        ObjectMapper objectMapper = new ObjectMapper();

        byte[] payload = objectMapper.writeValueAsString(thirdPartyAttributeIssuingPayload).getBytes();

        SignedRequest signedRequest = SignedRequestBuilder.newInstance()
                .withKeyPair(keyPairSource.getFromStream(new KeyStreamVisitor()))
                .withBaseUrl(ATTRIBUTE_API_URL)
                .withEndpoint("/attributes")
                .withHttpMethod(HttpMethod.HTTP_POST)
                .withHeader("X-Yoti-Auth-Id", this.properties.getClientSdkId())
                .withPayload(payload)
                .build();

        /**
         * Do request to /attribute here
         */
        try {
            LOG.info("Issuing attribute to user");
            signedRequest.execute(null);
        } catch (IllegalArgumentException ex) {
            // This is ok for now
        }

        WantedAttribute wantedThirdPartyAttribute = WantedAttributeBuilder.newInstance()
                .withName("com.rakuten.id")
                .withAcceptSelfAsserted(true)
                .build();

        DynamicPolicy dynamicPolicy = new SimpleDynamicPolicyBuilder()
                .withSelfie()
                .withWantedAttribute(wantedThirdPartyAttribute)
                .build();

        DynamicScenario dynamicScenario = new SimpleDynamicScenarioBuilder()
                .withCallbackEndpoint("/login")
                .withPolicy(dynamicPolicy)
                .build();

        try {
            String shareUrl = client.createShareUrl(dynamicScenario).getUrl();
            model.addAttribute("yotiShareUrl", shareUrl);
        } catch (DynamicShareException e) {
            LOG.error(e.getMessage());
        }

        model.addAttribute("clientSdkId", properties.getClientSdkId());

        return "dynamic-share";
    }

    /**
     * This endpoint is the "Callback URL" which will be called by user's browser after user logs in. It's a GET endpoint.
     * We will pass you a token inside url query string (/login?token=token-value)
     */
    @RequestMapping("/login")
    public String doLogin(@RequestParam("token") final String token, final Model model) {
        ActivityDetails activityDetails;
        HumanProfile humanProfile;

        try {
            activityDetails = client.getActivityDetails(token);
            humanProfile = activityDetails.getUserProfile();
        } catch (final ProfileException profileException) {
            LOG.info("Could not get profile", profileException);
            model.addAttribute("error", profileException.getMessage());
            return "error";
        }

        // load application logo into ui model
        Attribute<Image> applicationLogo = activityDetails.getApplicationProfile().getApplicationLogo();
        if (applicationLogo != null) {
            model.addAttribute("appLogo", applicationLogo.getValue().getBase64Content());
        }

        // load humanProfile data into ui model
        Attribute<Image> selfie = humanProfile.getSelfie();
        if (selfie != null) {
            model.addAttribute("base64Selfie", selfie.getValue().getBase64Content());
        }
        Attribute<String> fullName = humanProfile.getFullName();
        if (fullName != null) {
            model.addAttribute("fullName", fullName.getValue());
        }

        List<DisplayAttribute> displayAttributes = humanProfile.getAttributes().stream()
                .map(this::mapToDisplayAttribute)
                .filter(displayAttribute -> displayAttribute != null)
                .collect(Collectors.toList());

        model.addAttribute("displayAttributes", displayAttributes);

        return "profile";
    }

    private DisplayAttribute mapToDisplayAttribute(Attribute attribute) {
        switch (attribute.getName()) {
            case AttributeConstants.HumanProfileAttributes.FULL_NAME:
                return new DisplayAttribute("Full name", attribute, "yoti-icon-profile");
            case AttributeConstants.HumanProfileAttributes.GIVEN_NAMES:
                return new DisplayAttribute("Given names", attribute, "yoti-icon-profile");
            case AttributeConstants.HumanProfileAttributes.FAMILY_NAME:
                return new DisplayAttribute("Family name", attribute, "yoti-icon-profile");
            case AttributeConstants.HumanProfileAttributes.NATIONALITY:
                return new DisplayAttribute("Nationality", attribute, "yoti-icon-nationality");
            case AttributeConstants.HumanProfileAttributes.POSTAL_ADDRESS:
                return new DisplayAttribute("Address", attribute, "yoti-icon-address");
            case AttributeConstants.HumanProfileAttributes.STRUCTURED_POSTAL_ADDRESS:
                return new DisplayAttribute("Structured Postal Address", attribute, "yoti-icon-address");
            case AttributeConstants.HumanProfileAttributes.PHONE_NUMBER:
                return new DisplayAttribute("Mobile number", attribute, "yoti-icon-phone");
            case AttributeConstants.HumanProfileAttributes.EMAIL_ADDRESS:
                return new DisplayAttribute("Email address", attribute, "yoti-icon-email");
            case AttributeConstants.HumanProfileAttributes.DATE_OF_BIRTH:
                return new DisplayAttribute("Date of birth", attribute, "yoti-icon-calendar");
            case AttributeConstants.HumanProfileAttributes.SELFIE:
                return null; // Do nothing - we already display the selfie
            case AttributeConstants.HumanProfileAttributes.GENDER:
                return new DisplayAttribute("Gender", attribute, "yoti-icon-gender");

            default:
                if (attribute.getName().contains(":")) {
                    return handleAgeVerification(attribute);
                } else {
                    return handleProfileAttribute(attribute);
                }
        }
    }

    @RequestMapping("/register-definition")
    public String registerDefinition() throws Exception {
        KeyPair keyPair = keyPairSource.getFromStream(new KeyStreamVisitor());

        InputStream inputStream = this.getClass()
                .getClassLoader().getResourceAsStream("fixtures/attribute_definition.json");

        byte[] payload = readFile(inputStream).replaceAll("\n", "").getBytes();

        SignedRequest signedRequest = SignedRequestBuilder.newInstance()
                .withBaseUrl(ATTRIBUTE_API_URL)
                .withEndpoint("/definitions")
                .withKeyPair(keyPair)
                .withHttpMethod(HttpMethod.HTTP_POST)
                .withHeader("X-Yoti-Auth-Id", this.properties.getClientSdkId())
                .withPayload(payload)
                .build();

        ResponseClass response = signedRequest.execute(ResponseClass.class);

//        SignedRequest signedRequest = SignedRequestBuilder.newInstance()
//                .withBaseUrl("https://stg1.api.internal.yoti.com/api/v1/attribute-registry")
//                .withEndpoint("/internal/permissions/apps/" + this.properties.getClientSdkId() + "/issuance")
//                .withKeyPair(keyPair)
//                .withHttpMethod(HttpMethod.HTTP_POST)
//                .build();

        return "login";
    }

    private DisplayAttribute handleAgeVerification(Attribute attribute) {
        return new DisplayAttribute("Age Verification/", "Age verified", attribute, "yoti-icon-verified");
    }

    private DisplayAttribute handleProfileAttribute(Attribute attribute) {
        String attributeName = StringUtils.capitalize(attribute.getName());
        return new DisplayAttribute(attributeName, attribute, "yoti-icon-profile");
    }

    public static String readFile(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(in));

        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + System.lineSeparator());
        }

        return sb.toString();
    }

}
