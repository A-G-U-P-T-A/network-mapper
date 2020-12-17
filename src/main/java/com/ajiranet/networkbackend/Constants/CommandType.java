package com.ajiranet.networkbackend.Constants;

public enum CommandType {
    CREATE("CREATE"), MODIFY("MODIFY"), FETCH("FETCH");
    private final String command;

    CommandType(String command) {
        this.command = command;
    }

    @Override public String toString() {
        return this.command;
    }
}
