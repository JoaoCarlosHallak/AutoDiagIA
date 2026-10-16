package com.autodiag.AutoDiagIA.dto;

import java.util.List;



public record CollectionRequest(List<String> pids, long intervalMs) {
}
