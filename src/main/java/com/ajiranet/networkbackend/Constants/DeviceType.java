package com.ajiranet.networkbackend.Constants;

public enum DeviceType {
    COMPUTER("COMPUTER"), REPEATER("REPEATER");
    private final String deviceType;
    DeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
    @Override public String toString() {
        return this.deviceType;
    }
}
