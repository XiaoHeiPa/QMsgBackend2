package org.cubewhy.chat.conventer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.SneakyThrows;
import org.cubewhy.chat.util.CryptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Converter
@Component
public class MessageContentConverter implements AttributeConverter<JSONObject, String> {
    static CryptUtil cryptUtil;

    @Autowired
    public void setCryptUtil(CryptUtil cryptUtil) {
        MessageContentConverter.cryptUtil = cryptUtil;
    }

    @SneakyThrows
    @Override
    public String convertToDatabaseColumn(JSONObject attribute) {
        return cryptUtil.encryptString(JSON.toJSONString(attribute));
    }

    @SneakyThrows
    @Override
    public JSONObject convertToEntityAttribute(String dbData) {
        return JSONObject.parse(cryptUtil.decryptStringToString(dbData));
    }
}
