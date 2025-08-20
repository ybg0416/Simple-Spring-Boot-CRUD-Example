package io.ybg.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.ybg.demo.dto.FailedIds;
import io.ybg.demo.mybatis.entity.MainTable;
import io.ybg.demo.mybatis.mapper.MainTableMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class MainTableService {

    private static final Logger logger = LoggerFactory.getLogger(MainTableService.class);
    private static final String FAILED_IDS_FILE = "src/main/resources/failed_ids.json";
    private static final String FAILED_IDS_BACKUP_FILE = "src/main/resources/failed_ids_backup.json";
    private static final int MAX_RETRY_COUNT = 3;

    @Autowired
    private MainTableMapper mainTableMapper;

    @Autowired
    private ObjectMapper objectMapper;

    public CompletableFuture<String> saveAllMainTablesToJson(String outputPath) {
        // 디렉토리 생성
        File directory = new File(outputPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 성공/실패/재시도 카운터
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicInteger retrySuccessCount = new AtomicInteger(0);
        AtomicInteger retryFailureCount = new AtomicInteger(0);
        List<Long> newFailedIds = new ArrayList<>();
        List<Long> permanentFailedIds = new ArrayList<>();

        // 이전 실패 ID 로드
        Map<Long, Integer> previousFailedIds = loadFailedIds();

        // 재시도 처리
        List<CompletableFuture<Void>> retryFutures = previousFailedIds.entrySet().stream()
                .filter(entry -> entry.getValue() < MAX_RETRY_COUNT) // 최대 재시도 횟수 미만만 처리
                .map(entry -> saveSingleMainTableAsync(entry.getKey(), outputPath, retrySuccessCount, retryFailureCount, newFailedIds, permanentFailedIds, true, entry.getValue() + 1))
                .toList();

        // 재시도 완료 대기
        CompletableFuture<Void> retryFuture = CompletableFuture.allOf(retryFutures.toArray(new CompletableFuture[0]));

        // 모든 MainTable 데이터 처리
        return retryFuture.thenCompose(v -> {
            List<CompletableFuture<Void>> futures = mainTableMapper.selectAllMainWithSub().stream()
                    .filter(mainTable -> !previousFailedIds.containsKey(mainTable.getId())) // 이미 재시도한 ID 제외
                    .map(mainTable -> saveSingleMainTableAsync(mainTable.getId(), outputPath, successCount, failureCount, newFailedIds, permanentFailedIds, false, 1))
                    .toList();

            return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenApply(v2 -> {
                        // 실패 ID 저장
                        saveFailedIds(newFailedIds, permanentFailedIds);

                        // 최종 결과 로깅
                        int total = successCount.get() + failureCount.get() + retrySuccessCount.get() + retryFailureCount.get();
                        String summary = String.format(
                                "JSON save completed: Total=%d, Success=%d, Failure=%d, RetrySuccess=%d, RetryFailure=%d, PermanentFailure=%d",
                                total, successCount.get(), failureCount.get(), retrySuccessCount.get(), retryFailureCount.get(), permanentFailedIds.size());
                        logger.info(summary);
                        return summary;
                    });
        });
    }

    @Async
    public CompletableFuture<Void> saveSingleMainTableAsync(Long id, String outputPath,
                                                            AtomicInteger successCount, AtomicInteger failureCount,
                                                            List<Long> failedIds, List<Long> permanentFailedIds,
                                                            boolean isRetry, int retryCount) {
        return CompletableFuture.runAsync(() -> {
            try {
                MainTable mainTable = mainTableMapper.selectMainById(id);
                if (mainTable == null) {
                    throw new IllegalArgumentException("MainTable with id " + id + " not found");
                }

                File outputFile = new File(outputPath + "/main_" + id + ".json");
                objectMapper.writeValue(outputFile, mainTable);
                successCount.incrementAndGet();
                logger.info("{}Saved main_{}.json successfully (attempt={})", isRetry ? "Retry: " : "", id, retryCount);
            } catch (Exception e) {
                failureCount.incrementAndGet();
                synchronized (failedIds) {
                    if (retryCount >= MAX_RETRY_COUNT) {
                        permanentFailedIds.add(id);
                        logger.warn("Permanently failed main_{}.json after {} attempts: {}", id, retryCount, e.getMessage());
                    } else {
                        failedIds.add(id);
                        logger.error("{}Failed to save main_{}.json (attempt={}): {}", isRetry ? "Retry: " : "", id, retryCount, e.getMessage());
                    }
                }
            }
        });
    }

    private Map<Long, Integer> loadFailedIds() {
        try {
            File file = new File(FAILED_IDS_FILE);
            if (!file.exists()) {
                return new HashMap<>();
            }
            FailedIds failedIds = objectMapper.readValue(file, FailedIds.class);
            return failedIds.getIds();
        } catch (IOException e) {
            logger.error("Failed to load failed_ids.json: {}", e.getMessage());
            // 백업 파일 시도
            try {
                File backupFile = new File(FAILED_IDS_BACKUP_FILE);
                if (backupFile.exists()) {
                    FailedIds failedIds = objectMapper.readValue(backupFile, FailedIds.class);
                    logger.info("Loaded failed_ids from backup");
                    return failedIds.getIds();
                }
            } catch (IOException ex) {
                logger.error("Failed to load failed_ids_backup.json: {}", ex.getMessage());
            }
            return new HashMap<>();
        }
    }

    private void saveFailedIds(List<Long> failedIds, List<Long> permanentFailedIds) {
        try {
            // 영구 실패 ID를 제외한 실패 ID만 저장
            FailedIds failedIdsDto = new FailedIds();
            Map<Long, Integer> failedIdMap = failedIds.stream()
                    .collect(Collectors.toMap(id -> id, id -> loadFailedIds().getOrDefault(id, 0) + 1));
            failedIdsDto.setIds(failedIdMap);

            // failed_ids.json 저장
            File file = new File(FAILED_IDS_FILE);
            objectMapper.writeValue(file, failedIdsDto);
            logger.info("Saved failed_ids.json with {} IDs", failedIds.size());

            // 백업 파일 생성
            Files.copy(file.toPath(), Path.of(FAILED_IDS_BACKUP_FILE), StandardCopyOption.REPLACE_EXISTING);
            logger.info("Created backup of failed_ids.json");

            // 영구 실패 ID 저장
            if (!permanentFailedIds.isEmpty()) {
                FailedIds permanentFailedIdsDto = new FailedIds();
                Map<Long, Integer> permanentFailedIdMap = permanentFailedIds.stream()
                        .collect(Collectors.toMap(id -> id, id -> MAX_RETRY_COUNT));
                permanentFailedIdsDto.setIds(permanentFailedIdMap);
                objectMapper.writeValue(new File("./data/permanent_failed_ids.json"), permanentFailedIdsDto);
                logger.info("Saved permanent_failed_ids.json with {} IDs", permanentFailedIds.size());
            }
        } catch (IOException e) {
            logger.error("Failed to save failed_ids.json or permanent_failed_ids.json: {}", e.getMessage());
        }
    }

    @Async
    public CompletableFuture<Void> saveMainTableToJson(Long id, String outputPath) {
        return CompletableFuture.runAsync(() -> {
            try {
                MainTable mainTable = mainTableMapper.selectMainById(id);
                if (mainTable == null) {
                    throw new IllegalArgumentException("MainTable with id " + id + " not found");
                }

                File directory = new File(outputPath);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                File outputFile = new File(outputPath + "/main_" + id + ".json");
                objectMapper.writeValue(outputFile, mainTable);
                logger.info("Saved main_{}.json successfully", id);
            } catch (Exception e) {
                logger.error("Failed to save main_{}.json: {}", id, e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
}