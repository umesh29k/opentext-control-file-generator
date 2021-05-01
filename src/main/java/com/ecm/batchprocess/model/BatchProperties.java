package com.ecm.batchprocess.model;

import java.util.List;

public class BatchProperties {
    private List<BatchProperties> opCos;
    private String region;
    private String siteIds;
    private String siteNames;
    public BatchProperties(String region, String siteIds, String siteNames, List<BatchProperties> opCos) {
        this.region = region;
        this.siteIds = siteIds;
        this.siteNames = siteNames;
    }

    // Without a default constructor, Jackson will throw an exception
    public BatchProperties() {
    }

    public List<BatchProperties> getOpCos() {
        return opCos;
    }

    public void setOpCos(List<BatchProperties> opCos) {
        this.opCos = opCos;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getSiteIds() {
        return siteIds;
    }

    public void setSiteIds(String siteIds) {
        this.siteIds = siteIds;
    }

    public String getSiteNames() {
        return siteNames;
    }

    public void setSiteNames(String siteNames) {
        this.siteNames = siteNames;
    }

    @Override
    public String toString() {
        return "BatchProperties{" +
                "opCos=" + opCos +
                ", region='" + region + '\'' +
                ", siteIds='" + siteIds + '\'' +
                ", siteNames='" + siteNames + '\'' +
                '}';
    }
}