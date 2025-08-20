package io.ybg.demo.mybatis.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubTable1 {
    @JsonProperty("uniqueKey")
    private Long uniqueKey;

    @JsonProperty("mainId")
    private Long mainId;

    @JsonProperty("subData")
    private String subData;
}