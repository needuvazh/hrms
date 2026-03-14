package com.company.hrms.attendance.service.impl;

import com.company.hrms.attendance.model.*;
import com.company.hrms.attendance.repository.*;
import com.company.hrms.attendance.service.*;

import com.company.hrms.attendance.model.AssignShiftCommandDto;
import com.company.hrms.attendance.model.AttendanceQueryDto;
import com.company.hrms.attendance.model.CreateShiftCommandDto;
import com.company.hrms.attendance.model.RecordPunchCommandDto;
import com.company.hrms.attendance.model.AttendanceRecordDto;
import com.company.hrms.attendance.repository.AttendanceRepository;
import com.company.hrms.attendance.model.AttendanceStatus;
import com.company.hrms.attendance.model.PunchEventDto;
import com.company.hrms.attendance.model.PunchType;
import com.company.hrms.attendance.model.ShiftDto;
import com.company.hrms.attendance.model.ShiftAssignmentDto;
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

        StepVerifier.create(attendanceApplicationService.createShift(new CreateShiftCommandDto(
                                "GEN",
                                "General",
                                LocalTime.of(9, 0),
                                LocalTime.of(18, 0)))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(shift -> assertEquals("default", shift.tenantId()))
                .verifyComplete();

        UUID shiftId = repository.lastShiftId;

        StepVerifier.create(attendanceApplicationService.assignShift(new AssignShiftCommandDto(
                                employeeId,
                                shiftId,
                                LocalDate.parse("2026-03-01"),
                                null))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .assertNext(assignment -> assertEquals(employeeId, assignment.employeeId()))
                .verifyComplete();

        StepVerifier.create(attendanceApplicationService.recordPunch(new RecordPunchCommandDto(
                                employeeId,
                                PunchType.IN,
                                Instant.parse("2026-03-10T04:00:00Z"),
                                "MANUAL"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier.create(attendanceApplicationService.recordPunch(new RecordPunchCommandDto(
                                employeeId,
                                PunchType.OUT,
                                Instant.parse("2026-03-10T12:00:00Z"),
                                "MANUAL"))
                        .contextWrite(ReactorTenantContext.withTenantId("default")))
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier.create(attendanceApplicationService.attendanceByEmployee(new AttendanceQueryDto(
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

        repository.seedShift(new ShiftDto(shiftId, "tenant-a", "GEN", "General", LocalTime.of(9, 0), LocalTime.of(18, 0), true, Instant.now(), Instant.now()));
        repository.seedShiftAssignment(new ShiftAssignmentDto(UUID.randomUUID(), "tenant-a", employeeId, shiftId, LocalDate.now().minusDays(1), null, true, Instant.now(), Instant.now()));
        repository.seedAttendanceRecord(new AttendanceRecordDto(UUID.randomUUID(), "tenant-a", employeeId, LocalDate.now(), shiftId, AttendanceStatus.PRESENT, Instant.now().minusSeconds(3600), Instant.now(), Instant.now(), Instant.now()));

        StepVerifier.create(attendanceApplicationService.attendanceByEmployee(new AttendanceQueryDto(employeeId, LocalDate.now(), LocalDate.now()))
                        .contextWrite(ReactorTenantContext.withTenantId("tenant-b")))
                .verifyComplete();
    }

    @Test
    void recordOutWithoutInFails() {
        UUID employeeId = UUID.randomUUID();
        UUID shiftId = UUID.randomUUID();
        repository.seedShift(new ShiftDto(shiftId, "default", "GEN", "General", LocalTime.of(9, 0), LocalTime.of(18, 0), true, Instant.now(), Instant.now()));
        repository.seedShiftAssignment(new ShiftAssignmentDto(UUID.randomUUID(), "default", employeeId, shiftId, LocalDate.parse("2026-03-10"), null, true, Instant.now(), Instant.now()));

        StepVerifier.create(attendanceApplicationService.recordPunch(new RecordPunchCommandDto(
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
                () -> attendanceApplicationService.createShift(new CreateShiftCommandDto(
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

        private final Map<UUID, ShiftDto> shifts = new ConcurrentHashMap<>();
        private final Map<UUID, ShiftAssignmentDto> assignments = new ConcurrentHashMap<>();
        private final Map<UUID, AttendanceRecordDto> records = new ConcurrentHashMap<>();
        private final Map<UUID, PunchEventDto> punches = new ConcurrentHashMap<>();
        private volatile UUID lastShiftId;

        @Override
        public Mono<ShiftDto> saveShift(ShiftDto shift) {
            shifts.put(shift.id(), shift);
            lastShiftId = shift.id();
            return Mono.just(shift);
        }

        @Override
        public Mono<ShiftDto> findShiftById(String tenantId, UUID shiftId) {
            ShiftDto shift = shifts.get(shiftId);
            if (shift == null || !tenantId.equals(shift.tenantId()) || !shift.active()) {
                return Mono.empty();
            }
            return Mono.just(shift);
        }

        @Override
        public Mono<ShiftAssignmentDto> saveShiftAssignment(ShiftAssignmentDto assignment) {
            assignments.put(assignment.id(), assignment);
            return Mono.just(assignment);
        }

        @Override
        public Mono<ShiftAssignmentDto> findActiveShiftAssignment(String tenantId, UUID employeeId, LocalDate attendanceDate) {
            return Flux.fromIterable(assignments.values())
                    .filter(ShiftAssignmentDto::active)
                    .filter(assignment -> tenantId.equals(assignment.tenantId()))
                    .filter(assignment -> employeeId.equals(assignment.employeeId()))
                    .filter(assignment -> !assignment.effectiveFrom().isAfter(attendanceDate))
                    .filter(assignment -> assignment.effectiveTo() == null || !assignment.effectiveTo().isBefore(attendanceDate))
                    .next();
        }

        @Override
        public Mono<AttendanceRecordDto> saveAttendanceRecord(AttendanceRecordDto attendanceRecord) {
            records.put(attendanceRecord.id(), attendanceRecord);
            return Mono.just(attendanceRecord);
        }

        @Override
        public Mono<AttendanceRecordDto> updateAttendanceRecord(AttendanceRecordDto attendanceRecord) {
            records.put(attendanceRecord.id(), attendanceRecord);
            return Mono.just(attendanceRecord);
        }

        @Override
        public Mono<AttendanceRecordDto> findAttendanceRecordByEmployeeAndDate(String tenantId, UUID employeeId, LocalDate attendanceDate) {
            return Flux.fromIterable(records.values())
                    .filter(record -> tenantId.equals(record.tenantId()))
                    .filter(record -> employeeId.equals(record.employeeId()))
                    .filter(record -> attendanceDate.equals(record.attendanceDate()))
                    .next();
        }

        @Override
        public Mono<PunchEventDto> savePunchEvent(PunchEventDto punchEvent) {
            punches.put(punchEvent.id(), punchEvent);
            return Mono.just(punchEvent);
        }

        @Override
        public Flux<AttendanceRecordDto> findAttendanceByEmployeeAndDateRange(String tenantId, UUID employeeId, LocalDate fromDate, LocalDate toDate) {
            return Flux.fromIterable(records.values())
                    .filter(record -> tenantId.equals(record.tenantId()))
                    .filter(record -> employeeId.equals(record.employeeId()))
                    .filter(record -> !record.attendanceDate().isBefore(fromDate) && !record.attendanceDate().isAfter(toDate));
        }

        void seedShift(ShiftDto shift) {
            shifts.put(shift.id(), shift);
        }

        void seedShiftAssignment(ShiftAssignmentDto assignment) {
            assignments.put(assignment.id(), assignment);
        }

        void seedAttendanceRecord(AttendanceRecordDto record) {
            records.put(record.id(), record);
        }
    }
}
