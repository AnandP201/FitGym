package com.application.fitgym.models;

import org.bson.types.ObjectId;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class payments extends RealmObject {

    @PrimaryKey
    private ObjectId _id=new ObjectId();

    @Required
    private String _partition;

    private String billInvoiceID,billFor,createdOn,billTitle,billAmount;


    public String getBillInvoiceID() {
        return billInvoiceID;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public String getBillAmount() {
        return billAmount;
    }

    public String getBillFor() {
        return billFor;
    }

    public String getBillTitle() {
        return billTitle;
    }




    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public void setBillAmount(String billAmount) {
        this.billAmount = billAmount;
    }


    public void setBillFor(String billFor) {
        this.billFor = billFor;
    }

    public void setBillInvoiceID(String billInvoiceID) {
        this.billInvoiceID = billInvoiceID;
    }

    public void setBillTitle(String billTitle) {
        this.billTitle = billTitle;
    }

    public void set_partition() {
        this._partition = "bills";
    }
}
