package com.bank.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling log file operations with secure temporary file handling.
 */
@RestController
@RequestMapping("/api/logs")
@Tag(name = "Log Controller", description = "API для работы с лог-файлами")
public class LogController {

  private static final String LOG_FILE_PATH = "./bank.log";
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final Set<PosixFilePermission> TEMP_FILE_PERMISSIONS =
          EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);

  @GetMapping("/download")
  @Operation(summary = "Скачать лог-файл", description = "Скачивает логи за указанную дату.")
  @ApiResponse(responseCode = "200", description = "Логи успешно загружены")
  @ApiResponse(responseCode = "404", description = "Логи не найдены")
  @ApiResponse(responseCode = "400", description = "Неверный формат даты")
  public ResponseEntity<Resource> downloadLogFile(
          @Parameter(description = "Дата в формате **yyyy-MM-dd**", required = true, example = "2023-10-01")
          @RequestParam(name = "date") String dateStr) throws IOException {

    // Validate date format
    try {
      LocalDate.parse(dateStr, DATE_FORMATTER);
    } catch (DateTimeParseException e) {
      return ResponseEntity.badRequest().build();
    }

    // Check if file exists
    Path logPath = Paths.get(LOG_FILE_PATH).toAbsolutePath().normalize();
    if (!Files.exists(logPath)) {
      return ResponseEntity.notFound().build();
    }

    // Read and filter logs
    String filteredLogs;
    try (var lines = Files.lines(logPath, StandardCharsets.UTF_8)) {
      filteredLogs = lines
              .filter(line -> line.contains(dateStr))
              .collect(Collectors.joining("\n"));
    }

    if (filteredLogs.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    // Create secure temp file
    Path tempLogFile = createSecureTempFile();
    try {
      Files.writeString(tempLogFile, filteredLogs, StandardCharsets.UTF_8);
      setSecureFilePermissions(tempLogFile);
    } catch (IOException e) {
      Files.deleteIfExists(tempLogFile);
      return ResponseEntity.internalServerError().build();
    }

    Resource resource = new UrlResource(tempLogFile.toUri());
    tempLogFile.toFile().deleteOnExit();

    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=logs-" + dateStr + ".log")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource);
  }

  private Path createSecureTempFile() throws IOException {
    // Create in system temp directory with secure permissions
    String prefix = "bank-logs-" + UUID.randomUUID();
    Path tempDir = Paths.get(System.getProperty("java.io.tmpdir")).toAbsolutePath().normalize();

    // Verify temp directory is secure
    if (!Files.isDirectory(tempDir) || !Files.isWritable(tempDir)) {
      throw new IOException("Temporary directory is not accessible");
    }

    return Files.createTempFile(tempDir, prefix, ".log");
  }

  private void setSecureFilePermissions(Path file) throws IOException {
    try {
      Files.setPosixFilePermissions(file, TEMP_FILE_PERMISSIONS);
    } catch (UnsupportedOperationException e) {
      // Fallback for non-POSIX systems
      file.toFile().setReadable(true, true);
      file.toFile().setWritable(true, true);
      file.toFile().setExecutable(false);
    }
  }

  @GetMapping("/view")
  @Operation(summary = "Просмотреть логи", description = "Возвращает логи за указанную дату в виде текста.")
  @ApiResponse(responseCode = "200", description = "Логи успешно получены")
  @ApiResponse(responseCode = "404", description = "Логи не найдены")
  @ApiResponse(responseCode = "400", description = "Неверный формат даты")
  public ResponseEntity<String> viewLogFile(
          @Parameter(description = "Дата в формате **yyyy-MM-dd**", required = true, example = "2023-10-01")
          @RequestParam(name = "date") String dateStr) throws IOException {

    // Date format validation
    try {
      LocalDate.parse(dateStr, DATE_FORMATTER);
    } catch (DateTimeParseException e) {
      return ResponseEntity.badRequest().build();
    }

    // File existence check with path normalization
    Path logPath = Paths.get(LOG_FILE_PATH).toAbsolutePath().normalize();
    if (!Files.exists(logPath)) {
      return ResponseEntity.notFound().build();
    }

    // Log filtering
    String filteredLogs;
    try (Stream<String> lines = Files.lines(logPath, StandardCharsets.UTF_8)) {
      filteredLogs = lines
              .filter(line -> line.contains(dateStr))
              .collect(Collectors.joining("\n"));
    }

    if (filteredLogs.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok()
            .contentType(MediaType.TEXT_PLAIN)
            .body(filteredLogs);
  }
}