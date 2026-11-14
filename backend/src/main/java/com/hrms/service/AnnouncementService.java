package com.hrms.service;

import com.hrms.common.BusinessException;
import com.hrms.entity.Announcement;
import com.hrms.entity.SysUser;
import com.hrms.repository.AnnouncementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    public AnnouncementService(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    public List<Announcement> list() {
        return announcementRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    public Page<Announcement> page(int page, int size) {
        return announcementRepository.findAll(
                PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id")));
    }

    @Transactional
    public Announcement create(Announcement announcement, SysUser user) {
        if (!StringUtils.hasText(announcement.getTitle())) {
            throw new BusinessException("公告标题不能为空");
        }
        announcement.setId(null);
        announcement.setPublisher(user.getRealName());
        return announcementRepository.save(announcement);
    }

    @Transactional
    public Announcement update(Long id, Announcement request) {
        Announcement existing = announcementRepository.findById(id)
                .orElseThrow(() -> new BusinessException(404, "公告不存在"));
        if (!StringUtils.hasText(request.getTitle())) {
            throw new BusinessException("公告标题不能为空");
        }
        existing.setTitle(request.getTitle());
        existing.setContent(request.getContent());
        return announcementRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!announcementRepository.existsById(id)) {
            throw new BusinessException(404, "公告不存在");
        }
        announcementRepository.deleteById(id);
    }
}
