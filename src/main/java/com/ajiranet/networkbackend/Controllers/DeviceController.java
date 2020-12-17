package com.ajiranet.networkbackend.Controllers;

import com.ajiranet.networkbackend.Services.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController public class DeviceController {

    @Autowired DeviceService deviceService;

    @PostMapping(value = "/ajiranet/process") public ResponseEntity<String> process(@RequestBody String requestBody) {
        String[] lines = requestBody.split("\\r?\\n");
        String[] command = lines[0].split(" ");

        if(lines.length==3)
            return deviceService.runCommand(command, lines[2]);
        else
            return deviceService.runCommand(command, null);
    }
}
