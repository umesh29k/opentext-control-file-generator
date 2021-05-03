package com.ecm.batchprocess.model;

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

    @Override
    public String toString() {
        return "BatchProperties{" +
                "opCos=" + opCos +
                '}';
    }
}