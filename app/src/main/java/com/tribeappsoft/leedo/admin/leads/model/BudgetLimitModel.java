package com.tribeappsoft.leedo.admin.leads.model;

import java.io.Serializable;

public class BudgetLimitModel implements Serializable {

    private int budget_limit_id;
    private String budget_limit;

    public int getBudget_limit_id() {
        return budget_limit_id;
    }

    public void setBudget_limit_id(int budget_limit_id) {
        this.budget_limit_id = budget_limit_id;
    }

    public String getBudget_limit() {
        return budget_limit;
    }

    public void setBudget_limit(String budget_limit) {
        this.budget_limit = budget_limit;
    }

    public BudgetLimitModel() {
    }
}
