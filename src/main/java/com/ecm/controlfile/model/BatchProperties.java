package com.ecm.controlfile.model;

import java.util.List;

public class BatchProperties {
    private String base;
    private String region;
    private String consolidated;
    private String corporate;
    private String balance;
    private String part;
    private List<OpCo> opCos;

    public List<OpCo> getOpCos() {
        return opCos;
    }

    public void setOpCos(List<OpCo> opCos) {
        this.opCos = opCos;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setConsolidated(String consolidated) {
        this.consolidated = consolidated;
    }

    public void setCorporate(String corporate) {
        this.corporate = corporate;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public void setPart(String part) {
        this.part = part;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getRegion() {
        return region;
    }

    public String getConsolidated() {
        return consolidated;
    }

    public String getCorporate() {
        return corporate;
    }

    public String getBalance() {
        return balance;
    }

    public String getPart() {
        return part;
    }

    @Override
    public String toString() {
        return "BatchProperties{" +
                "opCos=" + opCos +
                '}';
    }
}