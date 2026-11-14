package com.hrms.config;

import com.hrms.entity.*;
import com.hrms.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 初始化演示数据
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final SysUserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRepository;
    private final SalaryRepository salaryRepository;
    private final AnnouncementRepository announcementRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(SysUserRepository userRepository,
                      DepartmentRepository departmentRepository,
                      PositionRepository positionRepository,
                      EmployeeRepository employeeRepository,
                      AttendanceRepository attendanceRepository,
                      LeaveRequestRepository leaveRepository,
                      SalaryRepository salaryRepository,
                      AnnouncementRepository announcementRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
        this.leaveRepository = leaveRepository;
        this.salaryRepository = salaryRepository;
        this.announcementRepository = announcementRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        Map<String, Long> departments = seedDepartments();
        Map<String, Long> positions = seedPositions();
        List<Employee> employees = seedEmployees(departments, positions);
        seedUsers(employees);
        seedAttendance(employees);
        seedLeaves(employees);
        seedSalaries(employees);
        seedAnnouncements();
    }

    private Map<String, Long> seedDepartments() {
        Map<String, Long> map = new HashMap<>();
        String[][] data = {
                {"技术部", "TECH", "王强", "负责产品研发与技术支撑"},
                {"产品部", "PROD", "陈晨", "负责产品规划与交互设计"},
                {"市场部", "MKT", "孙浩", "负责市场推广与品牌运营"},
                {"人事部", "HR", "李静", "负责招聘、培训与员工关系"},
                {"财务部", "FIN", "郑爽", "负责财务核算与薪酬发放"}
        };
        for (String[] item : data) {
            Department department = new Department();
            department.setName(item[0]);
            department.setCode(item[1]);
            department.setManagerName(item[2]);
            department.setDescription(item[3]);
            map.put(item[0], departmentRepository.save(department).getId());
        }
        return map;
    }

    private Map<String, Long> seedPositions() {
        Map<String, Long> map = new HashMap<>();
        String[][] data = {
                {"Java开发工程师", "P6", "负责后端服务设计与开发"},
                {"前端开发工程师", "P5", "负责前端页面与交互开发"},
                {"测试工程师", "P5", "负责产品质量保障"},
                {"产品经理", "P6", "负责产品需求与规划"},
                {"UI设计师", "P5", "负责视觉与界面设计"},
                {"市场专员", "P4", "负责市场活动执行"},
                {"招聘专员", "P4", "负责招聘渠道与面试安排"},
                {"会计", "P5", "负责财务核算与报表"}
        };
        for (String[] item : data) {
            Position position = new Position();
            position.setName(item[0]);
            position.setLevel(item[1]);
            position.setDescription(item[2]);
            map.put(item[0], positionRepository.save(position).getId());
        }
        return map;
    }

    private List<Employee> seedEmployees(Map<String, Long> departments, Map<String, Long> positions) {
        List<Employee> list = new ArrayList<>();
        list.add(employee("EMP001", "王强", "男", "13800001001", "wangqiang@hrms.com",
                "技术部", "Java开发工程师", "2019-03-01", "ACTIVE", "25000", "本科", departments, positions));
        list.add(employee("EMP002", "李娜", "女", "13800001002", "lina@hrms.com",
                "技术部", "前端开发工程师", "2020-07-15", "ACTIVE", "20000", "本科", departments, positions));
        list.add(employee("EMP003", "刘洋", "男", "13800001003", "liuyang@hrms.com",
                "技术部", "测试工程师", "2021-05-10", "ACTIVE", "15000", "本科", departments, positions));
        list.add(employee("EMP004", "陈晨", "女", "13800001004", "chenchen@hrms.com",
                "产品部", "产品经理", "2018-09-03", "ACTIVE", "26000", "硕士", departments, positions));
        list.add(employee("EMP005", "赵敏", "女", "13800001005", "zhaomin@hrms.com",
                "产品部", "UI设计师", "2021-11-22", "ACTIVE", "14000", "本科", departments, positions));
        list.add(employee("EMP006", "孙浩", "男", "13800001006", "sunhao@hrms.com",
                "市场部", "市场专员", "2022-02-14", "ACTIVE", "12000", "本科", departments, positions));
        list.add(employee("EMP007", "周婷", "女", "13800001007", "zhouting@hrms.com",
                "市场部", "市场专员", "2022-08-01", "ACTIVE", "11500", "大专", departments, positions));
        list.add(employee("EMP008", "李静", "女", "13800001008", "lijing@hrms.com",
                "人事部", "招聘专员", "2019-06-18", "ACTIVE", "13000", "本科", departments, positions));
        list.add(employee("EMP009", "吴磊", "男", "13800001009", "wulei@hrms.com",
                "技术部", "Java开发工程师", "2023-04-06", "ACTIVE", "18000", "本科", departments, positions));
        list.add(employee("EMP010", "郑爽", "女", "13800001010", "zhengshuang@hrms.com",
                "财务部", "会计", "2020-01-09", "ACTIVE", "15500", "本科", departments, positions));
        list.add(employee("EMP011", "冯涛", "男", "13800001011", "fengtao@hrms.com",
                "技术部", "前端开发工程师", "2024-03-11", "ACTIVE", "17000", "本科", departments, positions));
        list.add(employee("EMP012", "何雪", "女", "13800001012", "hexue@hrms.com",
                "人事部", "招聘专员", "2021-07-05", "RESIGNED", "12500", "大专", departments, positions));
        return employeeRepository.saveAll(list);
    }

    private Employee employee(String empNo, String name, String gender, String phone, String email,
                              String departmentName, String positionName, String hireDate, String status,
                              String salary, String education,
                              Map<String, Long> departments, Map<String, Long> positions) {
        Employee employee = new Employee();
        employee.setEmpNo(empNo);
        employee.setName(name);
        employee.setGender(gender);
        employee.setPhone(phone);
        employee.setEmail(email);
        employee.setDepartmentId(departments.get(departmentName));
        employee.setPositionId(positions.get(positionName));
        employee.setHireDate(LocalDate.parse(hireDate));
        employee.setStatus(status);
        employee.setBaseSalary(new BigDecimal(salary));
        employee.setEducation(education);
        employee.setBirthday(LocalDate.parse(hireDate).minusYears(24));
        employee.setIdCard("3101011990010100" + empNo.substring(empNo.length() - 2));
        employee.setAddress("上海市浦东新区张江路 " + (100 + Integer.parseInt(empNo.substring(3))) + " 号");
        return employee;
    }

    private void seedUsers(List<Employee> employees) {
        Map<String, Employee> byNo = new HashMap<>();
        employees.forEach(e -> byNo.put(e.getEmpNo(), e));

        userRepository.save(user("admin", "admin123", "张伟", "ADMIN", null));
        userRepository.save(user("hr", "hr123", "李静", "HR", byNo.get("EMP008").getId()));
        userRepository.save(user("emp001", "123456", "王强", "EMPLOYEE", byNo.get("EMP001").getId()));
    }

    private SysUser user(String username, String password, String realName, String role, Long employeeId) {
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRealName(realName);
        user.setRole(role);
        user.setEmployeeId(employeeId);
        user.setStatus(1);
        return user;
    }

    private void seedAttendance(List<Employee> employees) {
        Random random = new Random(20260101L);
        List<Employee> active = new ArrayList<>();
        employees.forEach(e -> {
            if ("ACTIVE".equals(e.getStatus())) {
                active.add(e);
            }
        });
        LocalDate today = LocalDate.now();
        List<Attendance> records = new ArrayList<>();
        for (int i = 10; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                continue;
            }
            for (Employee employee : active) {
                if (random.nextInt(100) < 4) {
                    continue;
                }
                Attendance attendance = new Attendance();
                attendance.setEmployeeId(employee.getId());
                attendance.setWorkDate(date);
                int minuteOffset = random.nextInt(36) - 10;
                LocalTime checkIn = LocalTime.of(9, 0).plusMinutes(minuteOffset);
                LocalTime checkOut = LocalTime.of(18, 0).plusMinutes(random.nextInt(70));
                attendance.setCheckIn(checkIn);
                attendance.setCheckOut(checkOut);
                boolean late = checkIn.isAfter(LocalTime.of(9, 0));
                boolean early = checkOut.isBefore(LocalTime.of(18, 0));
                attendance.setStatus(late ? "LATE" : (early ? "EARLY_LEAVE" : "NORMAL"));
                records.add(attendance);
            }
        }
        attendanceRepository.saveAll(records);
    }

    private void seedLeaves(List<Employee> employees) {
        Map<String, Employee> byNo = new HashMap<>();
        employees.forEach(e -> byNo.put(e.getEmpNo(), e));

        leaveRepository.save(leave(byNo.get("EMP002"), "年假",
                LocalDate.now().plusDays(3), LocalDate.now().plusDays(5), "家中有事，需要回老家处理", "PENDING", null, null));
        leaveRepository.save(leave(byNo.get("EMP003"), "病假",
                LocalDate.now().minusDays(2), LocalDate.now().minusDays(1), "感冒发烧，医院就诊", "APPROVED", "李静", "注意休息"));
        leaveRepository.save(leave(byNo.get("EMP006"), "事假",
                LocalDate.now().plusDays(7), LocalDate.now().plusDays(7), "办理个人证件", "PENDING", null, null));
        leaveRepository.save(leave(byNo.get("EMP005"), "调休",
                LocalDate.now().minusDays(9), LocalDate.now().minusDays(9), "周末加班调休", "REJECTED", "李静", "当周项目紧张，建议下周调休"));
    }

    private LeaveRequest leave(Employee employee, String type, LocalDate start, LocalDate end,
                               String reason, String status, String approver, String comment) {
        LeaveRequest leave = new LeaveRequest();
        leave.setEmployeeId(employee.getId());
        leave.setType(type);
        leave.setStartDate(start);
        leave.setEndDate(end);
        leave.setDays(BigDecimal.valueOf(start.until(end).getDays() + 1L));
        leave.setReason(reason);
        leave.setStatus(status);
        if (approver != null) {
            leave.setApproverName(approver);
            leave.setApproveTime(LocalDateTime.now().minusDays(1));
        }
        leave.setComment(comment);
        return leave;
    }

    private void seedSalaries(List<Employee> employees) {
        List<Salary> list = new ArrayList<>();
        for (int m = 2; m >= 0; m--) {
            YearMonth month = YearMonth.now().minusMonths(m);
            for (Employee employee : employees) {
                if (!"ACTIVE".equals(employee.getStatus())) {
                    continue;
                }
                BigDecimal base = employee.getBaseSalary();
                BigDecimal allowance = new BigDecimal("800");
                BigDecimal bonus = month.equals(YearMonth.now()) ? BigDecimal.ZERO
                        : base.multiply(new BigDecimal("0.08")).setScale(2, BigDecimal.ROUND_HALF_UP);
                BigDecimal social = base.multiply(new BigDecimal("0.105")).setScale(2, BigDecimal.ROUND_HALF_UP);
                BigDecimal taxable = base.subtract(social).subtract(new BigDecimal("5000"));
                BigDecimal tax = taxable.compareTo(BigDecimal.ZERO) > 0
                        ? taxable.multiply(new BigDecimal("0.03")).setScale(2, BigDecimal.ROUND_HALF_UP)
                        : BigDecimal.ZERO;
                BigDecimal net = base.add(allowance).add(bonus).subtract(social).subtract(tax)
                        .setScale(2, BigDecimal.ROUND_HALF_UP);

                Salary salary = new Salary();
                salary.setEmployeeId(employee.getId());
                salary.setSalaryMonth(month.toString());
                salary.setBaseSalary(base);
                salary.setBonus(bonus);
                salary.setAllowance(allowance);
                salary.setDeduction(BigDecimal.ZERO);
                salary.setSocialSecurity(social);
                salary.setTax(tax);
                salary.setNetSalary(net);
                salary.setStatus(m == 0 ? "DRAFT" : "PAID");
                if (m > 0) {
                    salary.setPayDate(month.atEndOfMonth());
                }
                list.add(salary);
            }
        }
        salaryRepository.saveAll(list);
    }

    private void seedAnnouncements() {
        Announcement first = new Announcement();
        first.setTitle("关于 2026 年度绩效考核启动的通知");
        first.setContent("各位同事：本年度绩效考核工作将于本月 20 日正式启动，请各部门负责人于本周五前完成自评与互评安排，感谢配合。");
        first.setPublisher("人事部");
        announcementRepository.save(first);

        Announcement second = new Announcement();
        second.setTitle("办公区网络升级维护公告");
        second.setContent("本周六 22:00 - 次日 02:00 将对办公区网络进行升级维护，期间内网及 VPN 可能短暂中断，请提前保存工作内容。");
        second.setPublisher("技术部");
        announcementRepository.save(second);

        Announcement third = new Announcement();
        third.setTitle("员工年度体检安排");
        third.setContent("年度体检定于下月上旬进行，体检机构与预约方式详见附件，请在人事系统个人资料中确认联系方式。");
        third.setPublisher("人事部");
        announcementRepository.save(third);
    }
}
