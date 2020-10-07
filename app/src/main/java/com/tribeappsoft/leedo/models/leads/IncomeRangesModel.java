package com.tribeappsoft.leedo.models.leads;

import java.io.Serializable;

/*
 * Created by ${ROHAN} on 13/9/19.
 */
public class IncomeRangesModel implements Serializable
{

    public int getIncome_range_id() {
        return income_range_id;
    }

    public void setIncome_range_id(int income_range_id) {
        this.income_range_id = income_range_id;
    }

    public String getIncome_range() {
        return income_range;
    }

    public void setIncome_range(String income_range) {
        this.income_range = income_range;
    }

    private int income_range_id;
    private String income_range;

    public IncomeRangesModel() {

    }
}
