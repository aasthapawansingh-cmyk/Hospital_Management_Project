package com.example.hosptial;

public record VisitSummaryDraftResponse(
        String draft,
        boolean requiresClinicianReview
) {}