package com.chaterpackage;

import java.util.Arrays;

public class FileReceived {
    private int id;
    private String name;
    private byte[] data;

    public FileReceived(int id, String name, byte[] data) {
        this.id = id;
        this.name = name;
        this.data = data;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }


    @Override
    public String toString() {
        return "FileReceived{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
