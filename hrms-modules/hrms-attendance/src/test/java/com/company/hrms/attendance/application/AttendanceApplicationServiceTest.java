package com.company.hrms.attendance.application;

import com.company.hrms.attendance.api.AssignShiftCommand;
import com.company.hrms.attendance.api.AttendanceQuery;
import com.company.hrms.attendance.api.CreateShiftCommand;
import com.company.hrms.attendance.api.RecordPunchCommand;
import com.company.hrms.attendance.domain.AttendanceRecord;
import com.company.hrms.attendance.domain.AttendanceRepository;
import com.company.hrms.attendance.domain.AttendanceStatus;
import com.company.hrms.attendance.domain.PunchEvent;
import com.company.hrms.attendance.domain.PunchType;
import com.company.hrms.attendance.domain.Shift;
import com.company.hrms.attendance.domain.ShiftAssignment;
import com.company.hrms.platform.featuretoggle.api.EnablementGuard;
import com.company.hrms.platform.featuretoggle.api.FeatureToggleService;
import com.company.hrms.platform.starter.error.exception.HrmsException;
import com.company.hrms.platform.starter.tenancy.context.DefaultTenantContextAccessor;
import com.company.hrms.platform.starter.tenancy.context.ReactorTenantContext;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AttendanceApplicationServiceTest {

    private final InMemoryAttendanceRepository repository = new InMemoryAttendanceRepository();
    private final AttendanceApplicationService attendanceApplicationService = new AttendanceApplicationService(
            repository,
            new DefaultTenantContextAccessor(),
            new EnablementGuard(new EnabledFeatureToggleService()));

    @Test
    void createAssignPunchAndQueryFlowWorks() {
        UUID employeeId = UUID.randomUUID();

        StepVerifier.create(attendanceApplicationService.createShift(new CreateShiftCommand(
                                "GEN",
                                "General",
                                LocalTime.of(9, 0),
                                LocalTime.of(18, 0)))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(shift -> assertEquals("default", shift.tenantId()))
                .verifyComplete();

        UUID shiftId = repository.lastShiftId;

        StepVerifier.create(attendanceApplicationService.assignShift(new AssignShiftCommand(
                                employeeId,
                                shiftId,
                                LocalDate.now().minusDays(1),
                                null))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(assignment -> assertEquals(employeeId, assignment.employeeId()))
                .verifyComplete();

        StepVerifier.create(attendanceApplicationService.recordPunch(new RecordPunchCommand(
                                employeeId,
                                PunchType.IN,
                                Instant.parse("2026-03-10T04:00:00Z"),
                                "MANUAL"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier.create(attendanceApplicationService.recordPunch(new RecordPunchCommand(
                                employeeId,
                                PunchType.OUT,
                                Instant.parse("2026-03-10T12:00:00Z"),
                                "MANUAL"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier.create(attendanceApplicationService.attendanceByEmployee(new AttendanceQuery(
                                employeeId,
                                LocalDate.parse("2026-03-10"),
                                LocalDate.parse("2026-03-10")))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(record -> {
                    assertEquals(employeeId, record.employeeId());
                    assertEquals(AttendanceStatus.PRESENT, record.status());
                })
                .verifyComplete();
    }

    @Test
    void attendanceIsTenantIsolated() {
        UUID employeeId = UUID.randomUUID();
        UUID shiftId = UUID.randomUUID();

        repository.seedShift(new Shift(shiftId, "tenant-a", "GEN", "General", LocalTime.of(9, 0), LocalTime.of(18, 0), true, Instant.now(), Instant.now()));
        repository.seedShiftAssignment(new ShiftAssignment(UUID.randomUUID(), "tenant-a", employeeId, shiftId, LocalDate.now().minusDays(1), null, true, Instant.now(), Instant.now()));
        repository.seedAttendanceRecord(new AttendanceRecord(UUID.randomUUID(), "tenant-a", employeeId, LocalDate.now(), shiftId, AttendanceStatus.PRESENT, Instant.now().minusSeconds(3600), Instant.now(), Instant.now(), Instant.now()));

        StepVerifier.create(attendanceApplicationService.attendanceByEmployee(new AttendanceQuery(employeeId, LocalDate.now(), LocalDate.now()))
                        .contextWrite(ReactorTenantContext.withTenantId("tenant-b")))
                .verifyComplete();
    }

    @Test
    void recordOutWithoutInFails() {
        UUID employeeId = UUID.randomUUID();
        UUID shiftId = UUID.randomUUID();
        repository.seedShift(new Shift(shiftId, "default", "GEN", "General", LocalTime.of(9, 0), LocalTime.of(18, 0), true, Instant.now(), Instant.now()));
        repository.seedShiftAssignment(new ShiftAssignment(UUID.randomUUID(), "default", employeeId, shiftId, LocalDate.parse("2026-03-10"), null, true, Instant.now(), Instant.now()));

        StepVerifier.create(attendanceApplicationService.recordPunch(new RecordPunchCommand(
                                employeeId,
                                PunchType.OUT,
                                Instant.parse("2026-03-10T11:00:00Z"),
                                "MANUAL"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .expectErrorSatisfies(error -> {
                    assertInstanceOf(HrmsException.class, error);
                    HrmsException ex = (HrmsException) error;
                    assertEquals("PUNCH_IN_REQUIRED", ex.getErrorCode());
                })
                .verify();
    }

    @Test
    void createShiftValidationFailsWhenCodeMissing() {
        HrmsException ex = assertThrows(
                HrmsException.class,
                () -> attendanceApplicationService.createShift(new CreateShiftCommand(
                        "",
                        "General",
                        LocalTime.of(9, 0),
                        LocalTime.of(18, 0))));
        assertEquals("SHIFT_CODE_REQUIRED", ex.getErrorCode());
    }

    static class EnabledFeatureToggleService implements FeatureToggleService {
        @Override
        public Mono<Boolean> isModuleEnabled(String tenantCode, String moduleKey) {
            return Mono.just(true);
        }

        @Override
        public Mono<Boolean> isFeatureEnabled(String tenantCode, String featureKey) {
            return Mono.just(true);
        }

        @Override
        public Mono<Boolean> currentTenantHasModule(String moduleKey) {
            return Mono.just(true);
        }

        @Override
        public Mono<Boolean> currentTenantHasFeature(String featureKey) {
            return Mono.just(true);
        }
    }

    static class InMemoryAttendanceRepository implements AttendanceRepository {

        private final Map<UUID, Shift> shifts = new ConcurrentHashMap<>();
        private final Map<UUID, ShiftAssignment> assignments = new ConcurrentHashMap<>();
        private final Map<UUID, AttendanceRecord> records = new ConcurrentHashMap<>();
        private final Map<UUID, PunchEvent> punches = new ConcurrentHashMap<>();
        private volatile UUID lastShiftId;

        @Override
        public Mono<Shift> saveShift(Shift shift) {
            shifts.put(shift.id(), shift);
            lastShiftId = shift.id();
            return Mono.just(shift);
        }

        @Override
        public Mono<Shift> findShiftById(String tenantId, UUID shiftId) {
            Shift shift = shifts.get(shiftId);
            if (shift == null || !tenantId.equals(shift.tenantId()) || !shift.active()) {
                return Mono.empty();
            }
            return Mono.just(shift);
        }

        @Override
        public Mono<ShiftAssignment> saveShiftAssignment(ShiftAssignment assignment) {
            assignments.put(assignment.id(), assignment);
            return Mono.just(assignment);
        }

        @Override
        public Mono<ShiftAssignment> findActiveShiftAssignment(String tenantId, UUID employeeId, LocalDate attendanceDate) {
            return Flux.fromIterable(assignments.values())
                    .filter(ShiftAssignment::active)
                    .filter(assignment -> tenantId.equals(assignment.tenantId()))
                    .filter(assignment -> employeeId.equals(assignment.employeeId()))
                    .filter(assignment -> !assignment.effectiveFrom().isAfter(attendanceDate))
                    .filter(assignment -> assignment.effectiveTo() == null || !assignment.effectiveTo().isBefore(attendanceDate))
                    .next();
        }

        @Override
        public Mono<AttendanceRecord> saveAttendanceRecord(AttendanceRecord attendanceRecord) {
            records.put(attendanceRecord.id(), attendanceRecord);
            return Mono.just(attendanceRecord);
        }

        @Override
        public Mono<AttendanceRecord> updateAttendanceRecord(AttendanceRecord attendanceRecord) {
            records.put(attendanceRecord.id(), attendanceRecord);
            return Mono.just(attendanceRecord);
        }

        @Override
        public Mono<AttendanceRecord> findAttendanceRecordByEmployeeAndDate(String tenantId, UUID employeeId, LocalDate attendanceDate) {
            return Flux.fromIterable(records.values())
                    .filter(record -> tenantId.equals(record.tenantId()))
                    .filter(record -> employeeId.equals(record.employeeId()))
                    .filter(record -> attendanceDate.equals(record.attendanceDate()))
                    .next();
        }

        @Override
        public Mono<PunchEvent> savePunchEvent(PunchEvent punchEvent) {
            punches.put(punchEvent.id(), punchEvent);
            return Mono.just(punchEvent);
        }

        @Override
        public Flux<AttendanceRecord> findAttendanceByEmployeeAndDateRange(String tenantId, UUID employeeId, LocalDate fromDate, LocalDate toDate) {
            return Flux.fromIterable(records.values())
                    .filter(record -> tenantId.equals(record.tenantId()))
                    .filter(record -> employeeId.equals(record.employeeId()))
                    .filter(record -> !record.attendanceDate().isBefore(fromDate) && !record.attendanceDate().isAfter(toDate));
        }

        void seedShift(Shift shift) {
            shifts.put(shift.id(), shift);
        }

        void seedShiftAssignment(ShiftAssignment assignment) {
            assignments.put(assignment.id(), assignment);
        }

        void seedAttendanceRecord(AttendanceRecord record) {
            records.put(record.id(), record);
        }
    }
}
