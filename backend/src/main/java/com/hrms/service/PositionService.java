package com.hrms.service;

import com.hrms.common.BusinessException;
import com.hrms.entity.Position;
import com.hrms.repository.EmployeeRepository;
import com.hrms.repository.PositionRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class PositionService {

    private final PositionRepository positionRepository;
    private final EmployeeRepository employeeRepository;

    public PositionService(PositionRepository positionRepository, EmployeeRepository employeeRepository) {
        this.positionRepository = positionRepository;
        this.employeeRepository = employeeRepository;
    }

    public List<Position> list() {
        return positionRepository.findAll(Sort.by("id"));
    }

    @Transactional
    public Position create(Position position) {
        if (!StringUtils.hasText(position.getName())) {
            throw new BusinessException("岗位名称不能为空");
        }
        if (positionRepository.existsByName(position.getName())) {
            throw new BusinessException("岗位名称已存在");
        }
        position.setId(null);
        return positionRepository.save(position);
    }

    @Transactional
    public Position update(Long id, Position position) {
        Position existing = positionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "岗位不存在"));
        if (!StringUtils.hasText(position.getName())) {
            throw new BusinessException("岗位名称不能为空");
        }
        if (positionRepository.existsByNameAndIdNot(position.getName(), id)) {
            throw new BusinessException("岗位名称已存在");
        }
        existing.setName(position.getName());
        existing.setLevel(position.getLevel());
        existing.setDescription(position.getDescription());
        return positionRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!positionRepository.existsById(id)) {
            throw new BusinessException(404, "岗位不存在");
        }
        if (employeeRepository.countByPositionId(id) > 0) {
            throw new BusinessException("该岗位下仍有员工，无法删除");
        }
        positionRepository.deleteById(id);
    }
}
