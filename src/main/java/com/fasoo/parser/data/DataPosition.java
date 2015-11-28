package com.fasoo.parser.data;

/**
 * Created by hwan on 2015-07-22.
 */
public class DataPosition implements Comparable<DataPosition> {
    private int callPosition;
    private int lengthSimpleName;
    private int relationType;
    private String fqn;

    public int getCallPosition() {
        return callPosition;
    }

    public void setCallPosition(int callPosition) {
        this.callPosition = callPosition;
    }

    public int getSimpleNamelength() {
        return lengthSimpleName;
    }

    public void setSimpleNamelength(int simpleNamelength) {
        lengthSimpleName = simpleNamelength;
    }

    public int getRelationType() {
        return relationType;
    }

    public void setRelationType(int relationType) {
        this.relationType = relationType;
    }

    public String getFqn() {
        return fqn;
    }

    public void setFqn(String fqn) {
        this.fqn = fqn;
    }

    @Override
    public int compareTo(DataPosition compare){
        int comparePosition = compare.getCallPosition();
        return comparePosition - this.callPosition;
    }
}
