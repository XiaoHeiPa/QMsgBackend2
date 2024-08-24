package org.cubewhy.chat.conventer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.SneakyThrows;
import org.cubewhy.chat.util.CryptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Converter
@Component
public class MessageContentConverter implements AttributeConverter<List<JSONObject>, String> {
    static CryptUtil cryptUtil;

    @Autowired
    public void setCryptUtil(CryptUtil cryptUtil) {
        MessageContentConverter.cryptUtil = cryptUtil;
    }

    @SneakyThrows
    @Override
    public String convertToDatabaseColumn(List<JSONObject> attribute) {
        return cryptUtil.encryptString(JSONArray.toJSONString(attribute));
    }

    @SneakyThrows
    @Override
    public List<JSONObject> convertToEntityAttribute(String dbData) {
        return JSONArray.parse(cryptUtil.decryptStringToString(dbData)).toList(JSONObject.class);
    }
}
