package io.ybg.demo.mybatis.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class MainTable {
    private Long id;

    @JsonProperty("mainData")
    private String data;

    @JsonProperty("insertedAt")
    private LocalDateTime insertedAt;

    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    @JsonProperty("subTable1List")
    private List<SubTable1> subTable1List;

    @JsonProperty("subTable2List")
    private List<SubTable2> subTable2List;
}