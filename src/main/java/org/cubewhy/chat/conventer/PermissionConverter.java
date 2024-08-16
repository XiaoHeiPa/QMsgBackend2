package org.cubewhy.chat.conventer;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.cubewhy.chat.entity.Permission;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Converter
public class PermissionConverter implements AttributeConverter<Set<Permission>, String> {

    @Override
    public String convertToDatabaseColumn(Set<Permission> attribute) {
        return attribute.stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));
    }

    @Override
    public Set<Permission> convertToEntityAttribute(String dbData) {
        return Arrays.stream(dbData.split(","))
                .map(Permission::valueOf)
                .collect(Collectors.toSet());
    }
}
