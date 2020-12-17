package com.ajiranet.networkbackend.Objects;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Device {
    @Setter private String name;
    @Setter private String type;
    @Setter private int strength;
    @Setter private int deviceNumber;
    private final List<Device> connectedDeviceList = new ArrayList<Device>();

    public void setConnectedDeviceList(Device connectedDeviceList) {
        this.connectedDeviceList.add(connectedDeviceList);
    }
}
