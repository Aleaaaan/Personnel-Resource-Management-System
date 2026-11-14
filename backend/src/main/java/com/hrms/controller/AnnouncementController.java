package com.hrms.controller;

import com.hrms.common.ApiResponse;
import com.hrms.entity.Announcement;
import com.hrms.entity.SysUser;
import com.hrms.service.AnnouncementService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    private final AnnouncementService announcementService;

    public AnnouncementController(AnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @GetMapping
    public ApiResponse<List<Announcement>> list() {
        return ApiResponse.ok(announcementService.list());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ApiResponse<Announcement> create(@RequestBody Announcement announcement, Authentication authentication) {
        return ApiResponse.ok(announcementService.create(announcement, (SysUser) authentication.getPrincipal()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ApiResponse<Announcement> update(@PathVariable Long id, @RequestBody Announcement announcement) {
        return ApiResponse.ok(announcementService.update(id, announcement));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        announcementService.delete(id);
        return ApiResponse.ok();
    }
}
