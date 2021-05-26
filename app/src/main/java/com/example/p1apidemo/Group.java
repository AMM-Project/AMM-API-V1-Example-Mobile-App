package com.example.p1apidemo;

import java.util.ArrayList;
import java.util.List;

/*
data structure class to store an AMM Group
 */
public class Group {
    String id;
    List<String> parents;
    String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getParents() {
        List<String> ret = new ArrayList<>(parents);
        return ret;
    }

    public void setParents(List<String> parents) {
        this.parents = parents;
    }

    public Group(String id, String name, List<String> parents){
        this.id= id;
        this.name = name;
        this.parents = parents;

    }

}
