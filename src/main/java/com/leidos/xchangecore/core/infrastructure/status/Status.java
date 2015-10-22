package com.leidos.xchangecore.core.infrastructure.status;

public class Status {

    private String category;

    public void setCategory(String category) {

        this.category = category;
    }

    public void setName(String name) {

        this.name = name;
    }

    private String name;

    public Status() {

    }

    public Status(String name, String category) {

        this.name = name;
        this.category = category;
    }

    public String getCategory() {

        return category;
    }

    public String getName() {

        return name;
    }

}
