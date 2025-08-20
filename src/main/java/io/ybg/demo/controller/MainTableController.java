package io.ybg.demo.controller;

import io.ybg.demo.service.MainTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/main")
public class MainTableController {

    @Autowired
    private MainTableService mainTableService;

    @PostMapping("/save-all")
    public CompletableFuture<String> saveAllMainToJson() {
        return mainTableService.saveAllMainTablesToJson("./data")
                .exceptionally(throwable -> "Failed to save JSON files: " + throwable.getMessage());
    }

    @PostMapping("/{id}/save")
    public CompletableFuture<String> saveMainToJson(@PathVariable Long id) {
        return mainTableService.saveMainTableToJson(id, "./data")
                .thenApply(v -> "Saved main_" + id + ".json successfully")
                .exceptionally(throwable -> "Failed to save main_" + id + ".json: " + throwable.getMessage());
    }
}