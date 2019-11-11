package com.yoti.api.client.spi.remote;

import com.yoti.api.client.Attribute;
import com.yoti.api.client.Profile;
import com.yoti.api.client.ProfileException;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;

class ProfileReader extends EncryptedDataReader {

    private final AttributeListConverter attributeListConverter;

    private ProfileReader(AttributeListConverter attributeListConverter) {
        this.attributeListConverter = attributeListConverter;
    }

    static ProfileReader newInstance() {
        return new ProfileReader(AttributeListConverter.newInstance());
    }

    @Override
    Profile read(byte[] encryptedProfileBytes, Key secretKey) throws ProfileException {
        List<Attribute<?>> attributeList = new ArrayList<>();
        if (encryptedProfileBytes != null && encryptedProfileBytes.length > 0) {
            byte[] profileData = decryptBytes(encryptedProfileBytes, secretKey);
            attributeList = attributeListConverter.parseAttributeList(profileData);
        }
        return new SimpleProfile(attributeList);
    }

}
