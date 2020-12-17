package com.ajiranet.networkbackend.Services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;


@Service public class ObjectMapperService {
    private static ObjectMapper objectMapper = null;

    private ObjectMapperService() {

    }

    public ObjectMapper getObjectMapper()
    {
        if (objectMapper == null)
            synchronized (ObjectMapperService.class) {
                if(objectMapper == null) {
                    objectMapper = new ObjectMapper();
                }
            }
        return objectMapper;
    }
}
