package com.softappsuganda.cheapinternationalsmsapp.models;

import com.google.firebase.Timestamp;

public class Message {
    String phone;
    String sms_text;
    String status;
    String carrier;
    Timestamp  created_at;
    String device_token;
    String send_via;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSms_text() {
        return sms_text;
    }

    public void setSms_text(String sms_text) {
        this.sms_text = sms_text;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCarrier() {
        return carrier;
    }

    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }

    public Timestamp  getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Timestamp created_at) {
        this.created_at = created_at;
    }

    public String getDevice_token() {
        return device_token;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }

    public String getSend_via() {
        return send_via;
    }

    public void setSend_via(String send_via) {
        this.send_via = send_via;
    }
}
