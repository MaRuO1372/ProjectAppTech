package com.titaniu.projectapptech.entity;

public class FirebaseData {

    private String os_id;
    private String  url;
    private String af_id;

    public String getCountries() {
        return countries;
    }

    public void setCountries(String countries) {
        this.countries = countries;
    }

    public String getAlert() {
        return alert;
    }

    public void setAlert(String alert) {
        this.alert = alert;
    }

    private String countries;
    private String alert;

    public String getOs_id() {
        return os_id;
    }

    public void setOs_id(String os_id) {
        this.os_id = os_id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAf_id() {
        return af_id;
    }

    public void setAf_id(String af_id) {
        this.af_id = af_id;
    }
}