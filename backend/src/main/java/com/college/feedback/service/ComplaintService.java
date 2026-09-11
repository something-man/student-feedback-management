package com.college.feedback.service;

import com.college.feedback.dto.request.ComplaintCreateRequest;
import com.college.feedback.dto.request.ComplaintUpdateRequest;
import com.college.feedback.dto.response.ComplaintDto;
import com.college.feedback.dto.response.PublicComplaintDto;
import com.college.feedback.entity.Complaint;
import com.college.feedback.entity.IssueUpdate;
import com.college.feedback.entity.Notification;
import com.college.feedback.entity.User;
import com.college.feedback.entity.enums.IssueStatus;
import com.college.feedback.entity.enums.IssueType;
import com.college.feedback.entity.enums.Priority;
import com.college.feedback.exception.ResourceNotFoundException;
import com.college.feedback.repository.ComplaintRepository;
import com.college.feedback.repository.IssueUpdateRepository;
import com.college.feedback.repository.NotificationRepository;
import com.college.feedback.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ComplaintService {

    private static final Logger log = LoggerFactory.getLogger(ComplaintService.class);

    private final ComplaintRepository complaintRepository;
    private final IssueUpdateRepository issueUpdateRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public ComplaintService(ComplaintRepository complaintRepository,
                            IssueUpdateRepository issueUpdateRepository,
                            NotificationRepository notificationRepository,
                            UserRepository userRepository) {
        this.complaintRepository = complaintRepository;
        this.issueUpdateRepository = issueUpdateRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    private String generateTicketNumber() {
        int randomNum = 100 + new Random().nextInt(900);
        String ticket = "CMP-" + randomNum;
        while (complaintRepository.existsByTicketNumber(ticket)) {
            randomNum = 100 + new Random().nextInt(900);
            ticket = "CMP-" + randomNum;
        }
        return ticket;
    }

    /**
     * Rule-based priority determination based on content heuristics.
     */
    public Priority determinePriority(String subject, String description, Priority userRequestedPriority) {
        String combined = ((subject != null ? subject : "") + " " + (description != null ? description : "")).toLowerCase();

        if (combined.contains("fire") || combined.contains("emergency") || combined.contains("explosion") ||
                combined.contains("electric shock") || combined.contains("hazard") || combined.contains("medical emergency") ||
                combined.contains("severe injury") || combined.contains("danger")) {
            return Priority.CRITICAL;
        }

        if (combined.contains("broken") || combined.contains("power outage") || combined.contains("water leak") ||
                combined.contains("no water") || combined.contains("wifi outage") || combined.contains("urgent") ||
                combined.contains("malfunction") || combined.contains("server down") || combined.contains("exam conflict")) {
            return Priority.HIGH;
        }

        if (combined.contains("minor") || combined.contains("suggestion") || combined.contains("routine") ||
                combined.contains("typo") || combined.contains("formatting")) {
            return Priority.LOW;
        }

        return userRequestedPriority != null ? userRequestedPriority : Priority.MEDIUM;
    }

    @Transactional
    public ComplaintDto createComplaint(ComplaintCreateRequest request, User student) {
        String ticketNumber = generateTicketNumber();
        Priority calculatedPriority = determinePriority(request.getSubject(), request.getDescription(), request.getPriority());

        Complaint complaint = new Complaint(
                ticketNumber,
                student,
                request.getCategory().toUpperCase(),
                request.getSubject(),
                request.getDescription(),
                calculatedPriority,
                IssueStatus.PENDING
        );

        // Default 48-hour resolution target SLA
        complaint.setTargetResolutionTime(LocalDateTime.now().plusHours(48));

        // Public Portal Eligibility Rule: High or Critical issues are eligible for public tracking
        if (calculatedPriority == Priority.HIGH || calculatedPriority == Priority.CRITICAL) {
            complaint.setPublicVisible(true);
            complaint.setPublicPublishedAt(LocalDateTime.now());
        } else {
            complaint.setPublicVisible(false);
        }

        Complaint savedComplaint = complaintRepository.save(complaint);

        // Initial Issue Update Audit Log
        IssueUpdate initialUpdate = new IssueUpdate(
                IssueType.COMPLAINT,
                savedComplaint,
                null,
                student,
                "PENDING",
                "Complaint lodged by " + student.getFullName() + " (Priority: " + calculatedPriority.name() + ")"
        );
        issueUpdateRepository.save(initialUpdate);

        // Notify Student of Successful Submission
        Notification notification = new Notification(
                student,
                "Complaint Lodged",
                "Your complaint " + ticketNumber + " has been submitted and is pending review.",
                "COMPLAINT_CREATED",
                "/complaints/" + savedComplaint.getId()
        );
        notificationRepository.save(notification);

        return mapToDtoWithUpdates(savedComplaint);
    }

    public List<ComplaintDto> getStudentComplaints(UUID studentId) {
        return complaintRepository.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
                .map(this::mapToDtoWithUpdates)
                .collect(Collectors.toList());
    }

    public List<ComplaintDto> getAllComplaints() {
        return complaintRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToDtoWithUpdates)
                .collect(Collectors.toList());
    }

    public List<ComplaintDto> getFilteredComplaints(IssueStatus status, Priority priority, String category, String assignedCell) {
        return complaintRepository.findWithFilters(status, priority, category, assignedCell).stream()
                .map(this::mapToDtoWithUpdates)
                .collect(Collectors.toList());
    }

    public ComplaintDto getComplaintById(UUID complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + complaintId));
        return mapToDtoWithUpdates(complaint);
    }

    public ComplaintDto getStudentComplaintById(UUID complaintId, UUID studentId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + complaintId));
        if (!complaint.getStudent().getId().equals(studentId)) {
            throw new ResourceNotFoundException("Complaint not found with id: " + complaintId);
        }
        return mapToDtoWithUpdates(complaint);
    }

    /**
     * Returns only public-eligible complaints with zero student PII.
     */
    public List<PublicComplaintDto> getPublicComplaints() {
        // Auto-check for overdue pending issues that qualify for public display
        checkAndPublishOverdueComplaints();

        return complaintRepository.findByPublicVisibleTrueAndStatusNotOrderByCreatedAtDesc(IssueStatus.CLOSED).stream()
                .map(PublicComplaintDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ComplaintDto updateComplaintStatus(UUID complaintId, ComplaintUpdateRequest request, User admin) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + complaintId));

        IssueStatus oldStatus = complaint.getStatus();
        IssueStatus newStatus = request.getStatus() != null ? request.getStatus() : oldStatus;
        String oldAssignedCell = complaint.getAssignedCell();

        // 1. Update priority if provided
        if (request.getPriority() != null) {
            complaint.setPriority(request.getPriority());
            if (request.getPriority() == Priority.HIGH || request.getPriority() == Priority.CRITICAL) {
                if (!Boolean.TRUE.equals(complaint.getPublicVisible()) && newStatus != IssueStatus.CLOSED) {
                    complaint.setPublicVisible(true);
                    complaint.setPublicPublishedAt(LocalDateTime.now());
                }
            }
        }

        // 2. Update assignment
        if (request.getAssignedCell() != null && !request.getAssignedCell().isBlank()) {
            complaint.setAssignedCell(request.getAssignedCell());
        }
        if (request.getAssignedToUserId() != null) {
            userRepository.findById(request.getAssignedToUserId()).ifPresent(complaint::setAssignedTo);
        }

        // 3. Update admin note
        if (request.getAdminNote() != null) {
            complaint.setAdminNote(request.getAdminNote());
        }

        // 4. Update public visibility override if explicitly sent
        if (request.getPublicVisible() != null) {
            complaint.setPublicVisible(request.getPublicVisible());
            if (request.getPublicVisible()) {
                complaint.setPublicPublishedAt(LocalDateTime.now());
            }
        }

        // 5. Handle Status transitions and Resolution Verification
        complaint.setStatus(newStatus);

        if (newStatus == IssueStatus.RESOLVED) {
            complaint.setResolvedAt(LocalDateTime.now());
        } else if (newStatus == IssueStatus.CLOSED) {
            // Resolution Verification Enforcement: only admin verification transitions issue to CLOSED
            complaint.setVerifiedAt(LocalDateTime.now());
            if (complaint.getResolvedAt() == null) {
                complaint.setResolvedAt(LocalDateTime.now());
            }
            // Verified closed issues are automatically removed from active public portal
            complaint.setPublicVisible(false);
            complaint.setPublicRemovedAt(LocalDateTime.now());
        }

        Complaint saved = complaintRepository.save(complaint);

        // 6. Formulate descriptive audit comment
        String updateComment = request.getAdminNote() != null && !request.getAdminNote().isBlank()
                ? request.getAdminNote()
                : generateStatusChangeComment(oldStatus, newStatus, complaint.getAssignedCell());

        IssueUpdate update = new IssueUpdate(
                IssueType.COMPLAINT,
                saved,
                null,
                admin,
                newStatus.name(),
                updateComment
        );
        update.setOldStatus(oldStatus != null ? oldStatus.name() : null);
        issueUpdateRepository.save(update);

        // 7. Dispatch targeted Student Notification
        sendStudentStatusNotification(complaint, oldStatus, newStatus, oldAssignedCell);

        return mapToDtoWithUpdates(saved);
    }

    private String generateStatusChangeComment(IssueStatus oldStatus, IssueStatus newStatus, String assignedCell) {
        if (newStatus == IssueStatus.RESOLVED) {
            return "Issue marked as resolved. Pending administrative verification.";
        } else if (newStatus == IssueStatus.CLOSED) {
            return "Resolution verified by administrative authority. Ticket closed.";
        } else if (newStatus == IssueStatus.ESCALATED) {
            return "Complaint escalated due to priority / SLA requirements.";
        } else if (newStatus == IssueStatus.IN_PROGRESS && assignedCell != null) {
            return "Assigned to " + assignedCell + " for investigation and resolution.";
        } else {
            return "Status changed from " + (oldStatus != null ? oldStatus.name() : "PENDING") + " to " + newStatus.name();
        }
    }

    private void sendStudentStatusNotification(Complaint complaint, IssueStatus oldStatus, IssueStatus newStatus, String oldAssignedCell) {
        if (complaint.getStudent() == null) return;

        String title;
        String message;

        if (newStatus == IssueStatus.RESOLVED) {
            title = "Complaint Resolved";
            message = "Your complaint " + complaint.getTicketNumber() + " has been marked as resolved and is awaiting administrative verification.";
        } else if (newStatus == IssueStatus.CLOSED) {
            title = "Complaint Verified & Closed";
            message = "Your complaint " + complaint.getTicketNumber() + " has been verified and closed.";
        } else if (newStatus == IssueStatus.ESCALATED) {
            title = "Complaint Escalated";
            message = "Your complaint " + complaint.getTicketNumber() + " has been escalated for high-priority resolution.";
        } else if (complaint.getAssignedCell() != null && !complaint.getAssignedCell().equals(oldAssignedCell)) {
            title = "Complaint Assigned";
            message = "Your complaint " + complaint.getTicketNumber() + " has been assigned to " + complaint.getAssignedCell() + ".";
        } else {
            title = "Complaint Status Updated";
            message = "Your complaint " + complaint.getTicketNumber() + " is now " + newStatus.name() + ".";
        }

        Notification notification = new Notification(
                complaint.getStudent(),
                title,
                message,
                "COMPLAINT_STATUS",
                "/complaints/" + complaint.getId()
        );
        notificationRepository.save(notification);
    }

    /**
     * Checks open complaints that have exceeded their target SLA resolution time (e.g. 48h)
     * and flags them as eligible for public portal tracking and escalation.
     */
    @Transactional
    public void checkAndPublishOverdueComplaints() {
        List<Complaint> openComplaints = complaintRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (Complaint c : openComplaints) {
            if (c.getStatus() != IssueStatus.RESOLVED && c.getStatus() != IssueStatus.CLOSED) {
                if (c.getTargetResolutionTime() != null && c.getTargetResolutionTime().isBefore(now)) {
                    if (!Boolean.TRUE.equals(c.getPublicVisible())) {
                        c.setPublicVisible(true);
                        c.setPublicPublishedAt(now);
                        complaintRepository.save(c);
                        log.info("Complaint {} exceeded SLA; marked visible on public portal.", c.getTicketNumber());
                    }
                }
            }
        }
    }

    private ComplaintDto mapToDtoWithUpdates(Complaint complaint) {
        ComplaintDto dto = ComplaintDto.fromEntity(complaint);
        List<IssueUpdate> updates = issueUpdateRepository.findByComplaintIdOrderByCreatedAtAsc(complaint.getId());
        dto.setUpdates(updates.stream().map(com.college.feedback.dto.response.IssueUpdateDto::fromEntity).collect(Collectors.toList()));
        return dto;
    }
}
