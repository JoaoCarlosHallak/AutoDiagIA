package com.autodiag.AutoDiagIA.service;

import com.autodiag.AutoDiagIA.dto.LogDTO;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Component
public class TelemetryAnalysisPreprocessorServiceImpl implements TelemetryAnalysisPreprocessorService {

    public String prepareData(List<LogDTO> logs) {

        if (logs == null || logs.isEmpty()) {
            throw new IllegalArgumentException(
                    "Não existem logs para preparar."
            );
        }

        List<LogDTO> orderedLogs = logs.stream()
                .peek(log -> {
                    if (log.getTimestamp() == null) {
                        throw new IllegalArgumentException(
                                "Foi encontrado um log sem timestamp."
                        );
                    }

                    if (log.getPid() == null || log.getValue() == null) {
                        throw new IllegalArgumentException(
                                "Foi encontrado um log sem PID ou valor."
                        );
                    }
                })
                .sorted(
                        Comparator.comparing(LogDTO::getTimestamp)
                )
                .toList();

        Instant start = orderedLogs.getFirst().getTimestamp();

        Map<String, LogDTO> pidMetadata = orderedLogs.stream()
                .collect(Collectors.toMap(
                        LogDTO::getPid,
                        log -> log,
                        (first, ignored) -> first,
                        TreeMap::new
                ));

        StringBuilder csv = new StringBuilder();

        csv.append("# start=").append(start).append('\n');
        csv.append("# pid,name,unit\n");

        pidMetadata.forEach((pid, log) -> {
            csv.append(csvEscape(pid)).append(',')
                    .append(csvEscape(log.getValueName())).append(',')
                    .append(csvEscape(log.getUnit()))
                    .append('\n');
        });

        csv.append("t_ns,pid,value\n");

        for (LogDTO log : orderedLogs) {
            long relativeNanos = Duration.between(
                    start,
                    log.getTimestamp()
            ).toNanos();

            csv.append(relativeNanos).append(',')
                    .append(csvEscape(log.getPid())).append(',')
                    .append(log.getValue())
                    .append('\n');
        }

        return csv.toString();
    }

    private String csvEscape(String value) {
        if (value == null) {
            return "";
        }

        return "\""
                + value.replace("\"", "\"\"")
                + "\"";
    }
}