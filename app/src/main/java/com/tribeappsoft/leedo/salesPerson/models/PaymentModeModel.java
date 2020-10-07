package com.tribeappsoft.leedo.salesPerson.models;

public class PaymentModeModel {
    private int payment_id;
    private String payment_mode;
    private String payment_description;

    public PaymentModeModel()
    {

    }

    public PaymentModeModel(String payment_mode, String payment_description) {
        this.payment_mode = payment_mode;
        this.payment_description = payment_description;
    }

    public PaymentModeModel(int payment_id, String payment_mode) {
        this.payment_id = payment_id;
        this.payment_mode = payment_mode;
    }

    public int getPayment_id() {
        return payment_id;
    }

    public void setPayment_id(int payment_id) {
        this.payment_id = payment_id;
    }

    public String getPayment_mode() {
        return payment_mode;
    }

    public void setPayment_mode(String payment_mode) {
        this.payment_mode = payment_mode;
    }

    public String getPayment_description() {
        return payment_description;
    }

    public void setPayment_description(String payment_description) {
        this.payment_description = payment_description;
    }
}
