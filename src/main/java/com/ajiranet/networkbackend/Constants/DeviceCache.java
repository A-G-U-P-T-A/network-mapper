package com.ajiranet.networkbackend.Constants;

import com.ajiranet.networkbackend.Objects.Device;

import java.util.HashMap;
import java.util.Map;

public class DeviceCache {
    public static final Map<String, Device> devices = new HashMap<>();
    public static Map<String, String> getDeviceInfo() {
        Map<String, String>deviceInfo = new HashMap<>();
        devices.forEach((key, device) -> deviceInfo.put(key, device.getType()));
        return deviceInfo;
    }
    public static String getDeviceNameFromNumber(int n) {
        for (String deviceName: devices.keySet()) {
            if(n==devices.get(deviceName).getDeviceNumber())
                return devices.get(deviceName).getName();
        }
        return null;
    }
}
