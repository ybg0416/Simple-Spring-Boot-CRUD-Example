package io.ybg.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class FailedIds {
    @JsonProperty("failedIds")
    private Map<Long, Integer> ids = new HashMap<>(); // id -> 재시도 횟수
}