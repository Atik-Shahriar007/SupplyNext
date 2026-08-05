package com.example.scmbackend.inventory;

import lombok.Getter;
import lombok.Setter;

public class StockAdjustmentRequest {
    private Integer change;

    public Integer getChange() {
        return change;
    }

    public void setChange(Integer change) {
        this.change = change;
    }
}
