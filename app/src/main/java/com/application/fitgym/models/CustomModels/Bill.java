package com.application.fitgym.models.CustomModels;

import java.io.Serializable;

public class Bill implements Serializable {
    private String invoiceID,billAmount,billFor,billTitle,createdOn;

    public Bill(String id,String amount,String to,String title,String on){
        this.invoiceID=id;
        this.billAmount=amount;
        this.billFor=to;
        this.billTitle=title;
        this.createdOn=on;
    }

    public String getInvoiceID() {
        return invoiceID;
    }

    public void setInvoiceID(String invoiceID) {
        this.invoiceID = invoiceID;
    }

    public void setBillTitle(String billTitle) {
        this.billTitle = billTitle;
    }

    public String getBillTitle() {
        return billTitle;
    }

    public void setBillFor(String billFor) {
        this.billFor = billFor;
    }

    public String getBillFor() {
        return billFor;
    }

    public void setBillAmount(String billAmount) {
        this.billAmount = billAmount;
    }

    public String getBillAmount() {
        return billAmount;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public String getCreatedOn() {
        return createdOn;
    }
}
