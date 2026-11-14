package com.hrms.service;

import com.hrms.common.BusinessException;
import com.hrms.entity.Attendance;
import com.hrms.entity.Department;
import com.hrms.entity.Employee;
import com.hrms.entity.SysUser;
import com.hrms.repository.AttendanceRepository;
import com.hrms.repository.DepartmentRepository;
import com.hrms.repository.EmployeeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    /** 上班时间 09:00，下班时间 18:00 */
    private static final LocalTime WORK_START = LocalTime.of(9, 0);
    private static final LocalTime WORK_END = LocalTime.of(18, 0);

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;

    public AttendanceService(AttendanceRepository attendanceRepository,
                             EmployeeRepository employeeRepository,
                             DepartmentRepository departmentRepository) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
    }

    public Page<Attendance> page(Long employeeId, String month, String status, int page, int size) {
        Specification<Attendance> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (employeeId != null) {
                predicates.add(cb.equal(root.get("employeeId"), employeeId));
            }
            if (StringUtils.hasText(month)) {
                YearMonth yearMonth = YearMonth.parse(month);
                predicates.add(cb.between(root.get("workDate"), yearMonth.atDay(1), yearMonth.atEndOfMonth()));
            }
            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Attendance> result = attendanceRepository.findAll(specification,
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "workDate", "id")));
        fillNames(result.getContent());
        return result;
    }

    public Attendance todayRecord(Long employeeId) {
        if (employeeId == null) {
            return null;
        }
        return attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, LocalDate.now()).orElse(null);
    }

    @Transactional
    public Attendance checkIn(SysUser user) {
        Long employeeId = user.getEmployeeId();
        if (employeeId == null) {
            throw new BusinessException("当前账号未关联员工档案，无法打卡");
        }
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now().withNano(0);
        Attendance attendance = attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, today)
                .orElseGet(() -> {
                    Attendance created = new Attendance();
                    created.setEmployeeId(employeeId);
                    created.setWorkDate(today);
                    return created;
                });
        if (attendance.getCheckIn() != null) {
            throw new BusinessException("今日已打过卡，无需重复打卡");
        }
        attendance.setCheckIn(now);
        attendance.setStatus(now.isAfter(WORK_START) ? "LATE" : "NORMAL");
        Attendance saved = attendanceRepository.save(attendance);
        fillNames(java.util.Collections.singletonList(saved));
        return saved;
    }

    @Transactional
    public Attendance checkOut(SysUser user) {
        Long employeeId = user.getEmployeeId();
        if (employeeId == null) {
            throw new BusinessException("当前账号未关联员工档案，无法签退");
        }
        Attendance attendance = attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, LocalDate.now())
                .orElseThrow(() -> new BusinessException("今日尚未打卡，无法签退"));
        if (attendance.getCheckOut() != null) {
            throw new BusinessException("今日已完成签退");
        }
        LocalTime now = LocalTime.now().withNano(0);
        attendance.setCheckOut(now);
        if (now.isBefore(WORK_END) && !"LATE".equals(attendance.getStatus())) {
            attendance.setStatus("EARLY_LEAVE");
        }
        Attendance saved = attendanceRepository.save(attendance);
        fillNames(java.util.Collections.singletonList(saved));
        return saved;
    }

    public Map<String, Object> monthStats(Long employeeId, String month) {
        YearMonth yearMonth = StringUtils.hasText(month) ? YearMonth.parse(month) : YearMonth.now();
        List<Object[]> rows = employeeId == null
                ? attendanceRepository.countStatusBetween(yearMonth.atDay(1), yearMonth.atEndOfMonth())
                : attendanceRepository.countStatusBetweenByEmployee(employeeId, yearMonth.atDay(1), yearMonth.atEndOfMonth());
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("normal", 0L);
        stats.put("late", 0L);
        stats.put("earlyLeave", 0L);
        stats.put("leave", 0L);
        stats.put("absent", 0L);
        long total = 0L;
        for (Object[] row : rows) {
            String status = String.valueOf(row[0]);
            long count = ((Number) row[1]).longValue();
            total += count;
            if ("NORMAL".equals(status)) {
                stats.put("normal", count);
            } else if ("LATE".equals(status)) {
                stats.put("late", count);
            } else if ("EARLY_LEAVE".equals(status)) {
                stats.put("earlyLeave", count);
            } else if ("LEAVE".equals(status)) {
                stats.put("leave", count);
            } else if ("ABSENT".equals(status)) {
                stats.put("absent", count);
            }
        }
        stats.put("total", total);
        return stats;
    }

    private void fillNames(Collection<Attendance> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<Long> employeeIds = records.stream().map(Attendance::getEmployeeId).distinct()
                .collect(Collectors.toList());
        Map<Long, Employee> employeeMap = new HashMap<>();
        employeeRepository.findAllById(employeeIds).forEach(e -> employeeMap.put(e.getId(), e));
        Map<Long, String> departmentMap = departmentRepository.findAll().stream()
                .collect(Collectors.toMap(Department::getId, Department::getName, (a, b) -> a));
        for (Attendance record : records) {
            Employee employee = employeeMap.get(record.getEmployeeId());
            if (employee != null) {
                record.setEmployeeName(employee.getName());
                record.setEmpNo(employee.getEmpNo());
                record.setDepartmentName(departmentMap.get(employee.getDepartmentId()));
            }
        }
    }
}
