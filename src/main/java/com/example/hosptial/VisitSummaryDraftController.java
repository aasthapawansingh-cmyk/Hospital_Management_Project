package com.example.hosptial;

import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/appointment")
public class VisitSummaryDraftController {

    private final AppointmentRepository appointments;
    private final RecordAccessService access;
    private final AIService aiService;

    public VisitSummaryDraftController(
            AppointmentRepository appointments,
            RecordAccessService access,
            AIService aiService
    ) {
        this.appointments = appointments;
        this.access = access;
        this.aiService = aiService;
    }

    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/{id}/ai-summary-draft")
    public VisitSummaryDraftResponse createDraft(
            @PathVariable Long id,
            @Valid @RequestBody VisitSummaryDraftRequest request,
            @AuthenticationPrincipal UserDetails user
    ) {
        Appointment appointment = appointments.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found"));

        if (!access.isAssignedClinician(user.getUsername(), appointment)) {
            throw new AccessDeniedException("Only the assigned doctor can draft this summary");
        }

        String draft = aiService.draftVisitSummary(request.clinicianNotes().trim());
        return new VisitSummaryDraftResponse(draft, true);
    }
}