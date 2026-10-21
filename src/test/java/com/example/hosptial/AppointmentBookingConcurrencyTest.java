package com.example.hosptial;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AppointmentBookingConcurrencyTest {
    @Autowired private AppointmentService appointmentService;
    @Autowired private PatientRepository patientRepository;
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private AppointmentRepository appointmentRepository;

    @Test
    void onlyOneConcurrentPatientRequestCanReserveTheSameDoctorSlot() throws Exception {
        String suffix = UUID.randomUUID().toString();
        Doctor doctor = doctorRepository.save(new Doctor(null, "Dr. Concurrent", "General", "8888888888"));
        Patient first = new Patient(null, "Patient One", 30, "F", "1111111111", "A");
        first.setAccountUsername("one-" + suffix);
        Patient second = new Patient(null, "Patient Two", 31, "F", "2222222222", "B");
        second.setAccountUsername("two-" + suffix);
        patientRepository.save(first);
        patientRepository.save(second);

        LocalDate date = LocalDate.now().plusDays(10);
        LocalTime time = LocalTime.of(10, 30);
        CountDownLatch start = new CountDownLatch(1);
        Callable<Boolean> firstRequest = () -> requestWhenReleased(start, doctor.getId(), date, time, first.getAccountUsername());
        Callable<Boolean> secondRequest = () -> requestWhenReleased(start, doctor.getId(), date, time, second.getAccountUsername());

        var executor = Executors.newFixedThreadPool(2);
        try {
            var firstResult = executor.submit(firstRequest);
            var secondResult = executor.submit(secondRequest);
            start.countDown();

            assertEquals(1, (firstResult.get() ? 1 : 0) + (secondResult.get() ? 1 : 0));
        } finally {
            executor.shutdownNow();
        }

        assertEquals(1L, appointmentRepository.findAll().stream()
                .filter(a -> doctor.getId().equals(a.getDoctor().getId()) && date.equals(a.getDate()) && time.equals(a.getTime()))
                .count());
    }

    private boolean requestWhenReleased(CountDownLatch start, Long doctorId, LocalDate date, LocalTime time, String username)
            throws InterruptedException {
        start.await();
        PatientAppointmentRequestDTO request = new PatientAppointmentRequestDTO();
        request.setDoctorId(doctorId);
        request.setDate(date);
        request.setTime(time);
        request.setReason("Concurrency check");
        try {
            appointmentService.requestForPatient(request, username);
            return true;
        } catch (AppointmentAlreadyBookedException alreadyTaken) {
            return false;
        }
    }
}
