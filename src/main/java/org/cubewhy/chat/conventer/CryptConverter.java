package org.cubewhy.chat.conventer;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.SneakyThrows;
import org.cubewhy.chat.util.CryptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Converter(autoApply = true)
@Component
public class CryptConverter implements AttributeConverter<String, String> {

    static CryptUtil cryptUtil;

    @Autowired
    public void setCryptUtil(CryptUtil cryptUtil) {
        CryptConverter.cryptUtil = cryptUtil;
    }

    @SneakyThrows
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        return cryptUtil.encryptString(attribute);
    }

    @SneakyThrows
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return cryptUtil.decryptStringToString(dbData);
    }
}
