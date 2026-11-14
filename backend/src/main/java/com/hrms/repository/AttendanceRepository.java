package com.hrms.repository;

import com.hrms.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long>, JpaSpecificationExecutor<Attendance> {

    Optional<Attendance> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);

    long countByWorkDate(LocalDate workDate);

    long countByEmployeeIdAndStatus(Long employeeId, String status);

    @Query("select a.status, count(a) from Attendance a where a.workDate between :start and :end group by a.status")
    List<Object[]> countStatusBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("select a.status, count(a) from Attendance a where a.employeeId = :employeeId " +
            "and a.workDate between :start and :end group by a.status")
    List<Object[]> countStatusBetweenByEmployee(@Param("employeeId") Long employeeId,
                                                @Param("start") LocalDate start,
                                                @Param("end") LocalDate end);
}
