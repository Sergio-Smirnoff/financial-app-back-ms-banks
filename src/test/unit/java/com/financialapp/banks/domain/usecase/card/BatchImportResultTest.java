package com.financialapp.banks.domain.usecase.card;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BatchImportResultTest {

    @Test
    void exposesCountsAndErrors() {
        var result = new BatchImportResult(3, 1, List.of("row 2 invalid"));
        assertThat(result.imported()).isEqualTo(3);
        assertThat(result.skipped()).isEqualTo(1);
        assertThat(result.errors()).containsExactly("row 2 invalid");
    }

    @Test
    void valueSemantics() {
        var a = new BatchImportResult(3, 1, List.of("e"));
        var b = new BatchImportResult(3, 1, List.of("e"));
        var c = new BatchImportResult(0, 0, List.of());
        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b).isNotEqualTo(c);
        assertThat(a.toString()).contains("imported=3");
    }
}
