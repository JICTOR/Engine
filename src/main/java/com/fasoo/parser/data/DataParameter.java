package com.fasoo.parser.data;

/**
 * Created by hwan on 2015-07-09.
 */
public class DataParameter {
    private String name;
    private String type;

    public String getFullDeclaration(){
        return type + " " + name;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
}
