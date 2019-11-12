package com.yoti.api.client.spi.remote;

import com.yoti.api.client.ExtraData;
import com.yoti.api.client.ExtraDataException;
import com.yoti.api.client.ProfileException;

import java.security.Key;

public class ExtraDataReader extends EncryptedDataReader {

    private final ExtraDataConverter extraDataConverter;

    private ExtraDataReader(ExtraDataConverter extraDataConverter) {
        this.extraDataConverter = extraDataConverter;
    }

    static ExtraDataReader newInstance() {
        return new ExtraDataReader(
                ExtraDataConverter.newInstance()
        );
    }

    @Override
    ExtraData read(byte[] encryptedBytes, Key secretKey) throws ProfileException, ExtraDataException {
        ExtraData extraData = null;
        if (encryptedBytes != null && encryptedBytes.length > 0) {
            byte[] extraDataBytes = decryptBytes(encryptedBytes, secretKey);
            extraData = extraDataConverter.read(extraDataBytes);
        } else {
            extraData = new SimpleExtraData();
        }

        return extraData;
    }
}
