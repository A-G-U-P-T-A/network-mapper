package com.ajiranet.networkbackend.Objects;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Connections {
    private String source;
    private List<String> targets;
}
