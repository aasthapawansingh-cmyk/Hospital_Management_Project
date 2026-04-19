package com.example.hosptial;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public class PatientServiceTest {

    @Test
    void getAllPatient_shouldReturnList() {
        // mock repository
        PatientRepository repo = mock(PatientRepository.class);

        // fake data
        List<Patient> patients = List.of(
            new Patient(1L, "A", 20, "F", "9999999999", "Delhi"),
            new Patient(2L, "B", 22, "M", "8888888888", "Noida")
        );

        // pageable + mock return
        Pageable pageable = Pageable.unpaged();
        when(repo.findAll(pageable)).thenReturn(new PageImpl<>(patients));

        // service uses mocked repo
        PatientService service = new PatientService(repo);

        // call method
        Page<Patient> result = service.getAllPatient(pageable);

        // verify
        assertEquals(2, result.getContent().size());
    }
}
