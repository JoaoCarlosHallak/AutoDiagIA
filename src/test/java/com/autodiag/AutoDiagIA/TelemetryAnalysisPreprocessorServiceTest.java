package com.autodiag.AutoDiagIA;

import com.autodiag.AutoDiagIA.exception.InvalidTelemetryDataException;
import com.autodiag.AutoDiagIA.service.TelemetryAnalysisPreprocessorServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class TelemetryAnalysisPreprocessorServiceTest {

    @Test
    void deveLancarExcecaoDeDadosInvalidosQuandoNaoHaLogs() {
        TelemetryAnalysisPreprocessorServiceImpl preprocessor =
                new TelemetryAnalysisPreprocessorServiceImpl();

        assertThrows(
                InvalidTelemetryDataException.class,
                () -> preprocessor.prepareData(List.of())
        );
    }
}
