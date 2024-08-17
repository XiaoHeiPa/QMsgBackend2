package org.cubewhy.chat.conventer;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class MessageContentConverter implements AttributeConverter<JSONObject, String> {
    @Override
    public String convertToDatabaseColumn(JSONObject attribute) {
        return JSON.toJSONString(attribute);
    }

    @Override
    public JSONObject convertToEntityAttribute(String dbData) {
        return JSONObject.parse(dbData);
    }
}
