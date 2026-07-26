package org.workshop.marketcanvas.sharedkernel.infrastructure;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.workshop.marketcanvas.sharedkernel.domain.AssetId;

import java.util.UUID;

@Converter(autoApply = true)
public class AssetIdConverter
implements AttributeConverter<AssetId, UUID> {
    @Override
    public UUID convertToDatabaseColumn(AssetId attribute) {
        return attribute == null ? null :  attribute.value();
    }

    @Override
    public AssetId convertToEntityAttribute(UUID dbData) {
        return dbData == null ? null : new AssetId(dbData);
    }
}
