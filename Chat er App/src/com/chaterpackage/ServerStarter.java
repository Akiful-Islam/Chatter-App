package com.chaterpackage;

public class ServerStarter {
    public static void main(String[] args) {
        try {
            new Thread(new ChatErAppUI(true, "Server")).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
