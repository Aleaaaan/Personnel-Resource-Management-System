package com.hrms.service;

import com.hrms.repository.AttendanceRepository;
import com.hrms.repository.DepartmentRepository;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.LeaveRequestRepository;
import com.hrms.repository.PositionRepository;
import com.hrms.repository.SalaryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final SalaryRepository salaryRepository;

    public DashboardService(EmployeeRepository employeeRepository,
                            DepartmentRepository departmentRepository,
                            PositionRepository positionRepository,
                            AttendanceRepository attendanceRepository,
                            LeaveRequestRepository leaveRequestRepository,
                            SalaryRepository salaryRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
        this.attendanceRepository = attendanceRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.salaryRepository = salaryRepository;
    }

    public Map<String, Object> overview() {
        Map<String, Object> data = new LinkedHashMap<>();
        long activeCount = employeeRepository.countByStatus("ACTIVE");
        long resignedCount = employeeRepository.countByStatus("RESIGNED");
        data.put("employeeCount", employeeRepository.count());
        data.put("activeCount", activeCount);
        data.put("resignedCount", resignedCount);
        data.put("departmentCount", departmentRepository.count());
        data.put("positionCount", positionRepository.count());
        data.put("pendingLeaveCount", leaveRequestRepository.countByStatus("PENDING"));
        data.put("todayAttendanceCount", attendanceRepository.countByWorkDate(LocalDate.now()));
        data.put("monthSalaryTotal", salaryRepository.sumNetSalaryByMonth(YearMonth.now().toString()));

        data.put("deptDistribution", toNameValueList(employeeRepository.countByDepartment()));

        List<Map<String, Object>> statusDistribution = new ArrayList<>();
        statusDistribution.add(nameValue("在职", activeCount));
        statusDistribution.add(nameValue("离职", resignedCount));
        data.put("statusDistribution", statusDistribution);

        data.put("educationDistribution", toNameValueList(employeeRepository.countByEducation()));

        data.put("salaryTrend", salaryTrend());
        data.put("attendanceStats", attendanceStats());
        return data;
    }

    private Map<String, Object> salaryTrend() {
        YearMonth start = YearMonth.now().minusMonths(5);
        Map<String, BigDecimal> sums = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            sums.put(YearMonth.now().minusMonths(i).toString(), BigDecimal.ZERO);
        }
        for (Object[] row : salaryRepository.sumByMonthFrom(start.toString())) {
            if (row[0] != null) {
                sums.put(String.valueOf(row[0]), (BigDecimal) row[1]);
            }
        }
        Map<String, Object> trend = new LinkedHashMap<>();
        List<String> months = new ArrayList<>(sums.keySet());
        List<BigDecimal> values = new ArrayList<>(sums.values());
        trend.put("months", months);
        trend.put("values", values);
        return trend;
    }

    private Map<String, Object> attendanceStats() {
        YearMonth current = YearMonth.now();
        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("正常", 0L);
        stats.put("迟到", 0L);
        stats.put("早退", 0L);
        stats.put("请假", 0L);
        stats.put("缺勤", 0L);
        for (Object[] row : attendanceRepository.countStatusBetween(current.atDay(1), current.atEndOfMonth())) {
            String status = String.valueOf(row[0]);
            long count = ((Number) row[1]).longValue();
            if ("NORMAL".equals(status)) {
                stats.put("正常", count);
            } else if ("LATE".equals(status)) {
                stats.put("迟到", count);
            } else if ("EARLY_LEAVE".equals(status)) {
                stats.put("早退", count);
            } else if ("LEAVE".equals(status)) {
                stats.put("请假", count);
            } else if ("ABSENT".equals(status)) {
                stats.put("缺勤", count);
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("labels", new ArrayList<>(stats.keySet()));
        result.put("values", new ArrayList<>(stats.values()));
        return result;
    }

    private List<Map<String, Object>> toNameValueList(List<Object[]> rows) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Object[] row : rows) {
            String name = row[0] == null ? "未填写" : String.valueOf(row[0]);
            long value = ((Number) row[1]).longValue();
            list.add(nameValue(name, value));
        }
        return list;
    }

    private Map<String, Object> nameValue(String name, long value) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("name", name);
        item.put("value", value);
        return item;
    }
}
