package com.example.hosptial;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VisitSummaryDraftRequest(
        @NotBlank
        @Size(max = 5000)
        String clinicianNotes
) {}