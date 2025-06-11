package com.vestigas.model;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Invoice")
public class Invoice {
    private String seller;
    private String buyer;
    private double amount;

    @XmlElement(name = "Seller")
    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    @XmlElement(name = "Buyer")
    public String getBuyer() {
        return buyer;
    }

    public void setBuyer(String buyer) {
        this.buyer = buyer;
    }

    @XmlElement(name = "Amount")
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "seller='" + seller + '\'' +
                ", buyer='" + buyer + '\'' +
                ", amount=" + amount +
                '}';
    }
}
