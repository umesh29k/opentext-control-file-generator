package com.ecm.batchprocess.model;

import java.util.List;

public class BatchProperties {
    private List<OpCo> opCos;

    public List<OpCo> getOpCos() {
        return opCos;
    }

    public void setOpCos(List<OpCo> opCos) {
        this.opCos = opCos;
    }

    @Override
    public String toString() {
        return "BatchProperties{" +
                "opCos=" + opCos +
                '}';
    }
}