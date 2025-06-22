package com.prestige.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EquivalentProductInfo {

    private Integer id;

    private Integer groupId;

    private Integer productId;

    public EquivalentProductInfo(EquivalentProductInfo equivalentProductInfo) {

        if (equivalentProductInfo != null) {
            this.id = equivalentProductInfo.getId();
            this.groupId = equivalentProductInfo.getGroupId();
            this.productId = equivalentProductInfo.getProductId();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        EquivalentProductInfo other = (EquivalentProductInfo)obj;
        return  Objects.equals(id, other.id) &&
                Objects.equals(groupId, other.groupId) &&
                Objects.equals(productId, other.productId);
    }
}
