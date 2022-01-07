package com.chaterpackage;

public class ClientStarter {

    public static void main(String[] args) {
        try {
            new Thread(new ChatErAppUI(false, "Client")).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
