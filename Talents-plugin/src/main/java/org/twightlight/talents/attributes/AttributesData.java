package org.twightlight.talents.attributes;

import java.util.HashMap;
import java.util.Map;

public class AttributesData {
    private Map<AttributeShopType, Integer> attributeShopData;

    public AttributesData() {
        attributeShopData = new HashMap<>();

        for (AttributeShopType type : AttributeShopType.values()) {
            attributeShopData.put(type, 0);
        }
    }

    public Map<AttributeShopType, Integer> getAttributeShopData() {
        return attributeShopData;
    }

    public void resetAttributeShopData() {
        attributeShopData.replaceAll((t, v) -> 0);
    }

    public int getLevelOfAttribute(AttributeShopType type) {
        return attributeShopData.getOrDefault(type, 0);
    }
}
