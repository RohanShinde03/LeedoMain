package com.tribeappsoft.leedo.salesPerson.models;

public class TokensModel {

    private int token_type_id;
    private String token_type;
    private String short_code;
    private String amount;
    private int is_approval;
    private String token_document_path;
    private String payment_link;
    private String payment_invoice_id;
    private String default_amount;



    public TokensModel()
    {

    }

    public int getToken_type_id() {
        return token_type_id;
    }

    public void setToken_type_id(int token_type_id) {
        this.token_type_id = token_type_id;
    }

    public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public String getToken_document_path() {
        return token_document_path;
    }

    public void setToken_document_path(String token_document_path) {
        this.token_document_path = token_document_path;
    }

    public String getShort_code() {
        return short_code;
    }

    public void setShort_code(String short_code) {
        this.short_code = short_code;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public int getIs_approval() {
        return is_approval;
    }

    public void setIs_approval(int is_approval) {
        this.is_approval = is_approval;
    }


    public String getPayment_link() {
        return payment_link;
    }

    public void setPayment_link(String payment_link) {
        this.payment_link = payment_link;
    }

    public String getPayment_invoice_id() {
        return payment_invoice_id;
    }

    public void setPayment_invoice_id(String payment_invoice_id) {
        this.payment_invoice_id = payment_invoice_id;
    }

    public String getDefault_amount() {
        return default_amount;
    }

    public void setDefault_amount(String default_amount) {
        this.default_amount = default_amount;
    }

}
