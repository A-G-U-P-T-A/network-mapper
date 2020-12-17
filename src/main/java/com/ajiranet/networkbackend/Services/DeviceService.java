package com.ajiranet.networkbackend.Services;

import com.ajiranet.networkbackend.Constants.CommandType;
import com.ajiranet.networkbackend.Constants.DeviceCache;
import com.ajiranet.networkbackend.Constants.DeviceType;
import com.ajiranet.networkbackend.Objects.Connections;
import com.ajiranet.networkbackend.Objects.Device;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service public class DeviceService {

    @Autowired ObjectMapperService objectMapperService;

    public ResponseEntity<String> runCommand(String[] command, String dataObject) {
        switch (CommandType.valueOf(command[0])) {
            case CREATE:
                switch (command[1]) {
                    case "/devices":
                        return createNewDevice(dataObject);
                    case "/connections":
                        return createNewConnections(dataObject);
                    default:
                        return new ResponseEntity<>("Incorrect create command: " + command[1] ,HttpStatus.BAD_REQUEST);
                }
            case FETCH:
                if ("/devices".equals(command[1])) {
                    return fetchAllDevices();
                }
                else if(command[1].contains("/info-routes")) {
                    return fetchRoutes(command[1]);
                }
                return new ResponseEntity<>("Incorrect fetch command: " + command[1], HttpStatus.BAD_REQUEST);
            case MODIFY:
                return modifyDeviceStrength(command[1], dataObject);
            default:
                return new ResponseEntity<>("Incorrect command: " + command[0] ,HttpStatus.BAD_REQUEST);
        }
    }

    private ResponseEntity<String> fetchRoutes(String command) {
        String data[] = command.split("\\?");
        try {
            String fromData = data[1].split("&")[0].split("=")[1];
            String toData = data[1].split("&")[1].split("=")[1];
            return createRoute(fromData, toData);
        } catch (Exception e) {
            return new ResponseEntity<>("Cannot parse command correctly: " ,HttpStatus.BAD_REQUEST);
        }
    }

    private ResponseEntity<String> createRoute(String fromData, String toData) {
        if(DeviceCache.devices.get(fromData)==null)
            return new ResponseEntity<>("Cannot find start point: " + fromData ,HttpStatus.NOT_FOUND);
        if(DeviceCache.devices.get(toData)==null)
            return new ResponseEntity<>("Cannot find end point: " + toData ,HttpStatus.NOT_FOUND);
        Device referenceStartDevice = DeviceCache.devices.get(fromData);
        Device referenceEndDevice = DeviceCache.devices.get(toData);
        return parseNetwork(referenceStartDevice, referenceEndDevice);
    }

    private ResponseEntity<String> parseNetwork(Device referenceStartDevice, Device referenceEndDevice) {
        Graph parseGraph = new Graph(DeviceCache.devices.size());
        DeviceCache.devices.forEach((key, data) -> {
            data.getConnectedDeviceList().forEach(val -> {
                parseGraph.addEdge(data.getDeviceNumber(), val.getDeviceNumber());
            });
        });
        parseGraph.DFS(referenceStartDevice.getDeviceNumber());
        List<Integer>pathList = parseGraph.getPathList();
        if(pathList.size()==1)
            return new ResponseEntity<>("Cannot find any node connected to start point: " + referenceStartDevice.getName() ,HttpStatus.NOT_FOUND);
        if(!pathList.contains(referenceEndDevice.getDeviceNumber()))
            return new ResponseEntity<>("Cannot find path between nodes : " + referenceStartDevice.getName() + " " + referenceEndDevice.getName() ,HttpStatus.NOT_FOUND);
        String path = "";
        for (int device: pathList) {
            path=path+DeviceCache.getDeviceNameFromNumber(device)+"->";
            if(device==referenceEndDevice.getDeviceNumber())
                break;
        }
        return new ResponseEntity<>("Path Found: " + path ,HttpStatus.OK);
    }


    private ResponseEntity<String> modifyDeviceStrength(String modifyStrength, String dataObject) {
        if(dataObject==null)
            return new ResponseEntity<>("Bad Request No value provided",HttpStatus.BAD_REQUEST);
        try {
            String deviceName = modifyStrength.split("/devices/")[1].split("/")[0];
            if(deviceName==null)
                return new ResponseEntity<>("Bad Request No correct device name provided with nomenclature",HttpStatus.BAD_REQUEST);
            if(DeviceCache.devices.size()==0)
                return new ResponseEntity<>("No Device is present",HttpStatus.NOT_FOUND);
            if(!DeviceCache.devices.containsKey(deviceName))
                return new ResponseEntity<>("No Device is present with the name provided: " + deviceName,HttpStatus.NOT_FOUND);
            ObjectNode objectNode = objectMapperService.getObjectMapper().readValue(dataObject, ObjectNode.class);
            int strength = Integer.parseInt(String.valueOf(objectNode.get("value")));
            if(DeviceCache.devices.get(deviceName).getType().equals(DeviceType.REPEATER.toString()))
                return new ResponseEntity<>("Bad Request Cannot set strength for repeater",HttpStatus.BAD_REQUEST);
            DeviceCache.devices.get(deviceName).setStrength(strength);
            return new ResponseEntity<>("Strength has been set to value: " + strength, HttpStatus.BAD_REQUEST);
        } catch (JsonMappingException e) {
            return new ResponseEntity<>("Bad Request JsonMappingException",HttpStatus.BAD_REQUEST);
        } catch (JsonProcessingException e) {
            return new ResponseEntity<>("Bad Request JsonProcessingException",HttpStatus.BAD_REQUEST);
        } catch (NumberFormatException e) {
            return new ResponseEntity<>("Value Should be Integer",HttpStatus.BAD_REQUEST);
        }
    }

    private ResponseEntity<String> fetchAllDevices() {
        ObjectNode objectNode = objectMapperService.getObjectMapper().createObjectNode();
        if(DeviceCache.devices.size()!=0) {
            JsonNode jsonNode = objectMapperService.getObjectMapper().convertValue(DeviceCache.getDeviceInfo(), JsonNode.class);
            return new ResponseEntity<>(objectNode.set("devices", jsonNode).toString() ,HttpStatus.OK);
        }
        return new ResponseEntity<>("no device found" ,HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<String> createNewConnections(String connectionData) {
        if(connectionData==null)
            return new ResponseEntity<>("Bad Request No connection provided",HttpStatus.BAD_REQUEST);
        try {
            if(DeviceCache.devices.size()==0)
                new ResponseEntity<>("No Device Found", HttpStatus.NOT_FOUND);
            Connections connections = objectMapperService.getObjectMapper().readValue(connectionData, Connections.class);
            String source = connections.getSource();
            List<String>targets = connections.getTargets();
            Device sourceDevice = DeviceCache.devices.get(source);
            if(sourceDevice==null)
                return new ResponseEntity<>("No Source mentioned in request ", HttpStatus.BAD_REQUEST);
            for (String target: targets) {
                if (!target.equals(source)) {
                    if (DeviceCache.devices.get(target)!=null) {
                        if(!sourceDevice.getConnectedDeviceList().contains(DeviceCache.devices.get(target))) {
                            sourceDevice.setConnectedDeviceList(DeviceCache.devices.get(target));
                            DeviceCache.devices.get(target).setConnectedDeviceList(sourceDevice);
                        }
                    }
                }
            }
            return new ResponseEntity<>("New Connection Established: " + source, HttpStatus.OK);
        } catch (JsonProcessingException e) {
            return new ResponseEntity<>("Bad Request: " + e.getMessage() ,HttpStatus.BAD_REQUEST);
        }
    }

    private ResponseEntity<String> createNewDevice(String deviceData) {
        if(deviceData==null)
            return new ResponseEntity<>("Bad Request No device provided",HttpStatus.BAD_REQUEST);
        try {
            Device device = objectMapperService.getObjectMapper().readValue(deviceData, Device.class);
            if(!Arrays.asList(DeviceType.values()).toString().contains(device.getType())) {
                return new ResponseEntity<>("Incorrect Device Type: " + device.getType(), HttpStatus.BAD_REQUEST);
            }
            if(device.getType().equals(DeviceType.COMPUTER.toString()))
                device.setStrength(5);
            device.setDeviceNumber(DeviceCache.devices.size());
            if(DeviceCache.devices.get(device.getName())!=null)
                return new ResponseEntity<>("Device Already Exist: " + device.getName(),HttpStatus.OK);
            DeviceCache.devices.put(device.getName(), device);
            return new ResponseEntity<>("Device Created: " + device.getName(),HttpStatus.OK);
        } catch (JsonProcessingException e) {
            return new ResponseEntity<>("Bad Request: " + e.getMessage() ,HttpStatus.BAD_REQUEST);
        }
    }
}
