package com.college.feedback.service;

import com.college.feedback.dto.request.RequestCreateRequest;
import com.college.feedback.dto.request.RequestUpdateRequest;
import com.college.feedback.dto.response.IssueUpdateDto;
import com.college.feedback.dto.response.RequestDto;
import com.college.feedback.entity.IssueUpdate;
import com.college.feedback.entity.Notification;
import com.college.feedback.entity.Request;
import com.college.feedback.entity.User;
import com.college.feedback.entity.enums.IssueType;
import com.college.feedback.entity.enums.RequestStatus;
import com.college.feedback.exception.ResourceNotFoundException;
import com.college.feedback.repository.IssueUpdateRepository;
import com.college.feedback.repository.NotificationRepository;
import com.college.feedback.repository.RequestRepository;
import com.college.feedback.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class RequestService {

    private final RequestRepository requestRepository;
    private final IssueUpdateRepository issueUpdateRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public RequestService(RequestRepository requestRepository,
                          IssueUpdateRepository issueUpdateRepository,
                          NotificationRepository notificationRepository,
                          UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.issueUpdateRepository = issueUpdateRepository;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    private String generateRequestNumber() {
        int randomNum = 200 + new Random().nextInt(800);
        String reqNum = "REQ-" + randomNum;
        while (requestRepository.existsByRequestNumber(reqNum)) {
            randomNum = 200 + new Random().nextInt(800);
            reqNum = "REQ-" + randomNum;
        }
        return reqNum;
    }

    @Transactional
    public RequestDto createRequest(RequestCreateRequest req, User student) {
        String requestNumber = generateRequestNumber();
        Request request = new Request(
                requestNumber,
                student,
                req.getCategory().toUpperCase(),
                req.getTitle(),
                req.getDetails(),
                RequestStatus.PENDING
        );

        Request savedRequest = requestRepository.save(request);

        // Initial audit update
        IssueUpdate initialUpdate = new IssueUpdate(
                IssueType.REQUEST,
                null,
                savedRequest,
                student,
                "PENDING",
                "Request submitted by " + student.getFullName()
        );
        issueUpdateRepository.save(initialUpdate);

        // Notify Student
        Notification notification = new Notification(
                student,
                "Request Submitted",
                "Your service request " + requestNumber + " has been submitted successfully.",
                "REQUEST_CREATED",
                "/requests/" + savedRequest.getId()
        );
        notificationRepository.save(notification);

        return mapToDtoWithUpdates(savedRequest);
    }

    public List<RequestDto> getStudentRequests(UUID studentId) {
        return requestRepository.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
                .map(this::mapToDtoWithUpdates)
                .collect(Collectors.toList());
    }

    public List<RequestDto> getAllRequests() {
        return requestRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::mapToDtoWithUpdates)
                .collect(Collectors.toList());
    }

    public List<RequestDto> getFilteredRequests(RequestStatus status, String category, String assignedCell) {
        return requestRepository.findWithFilters(status, category, assignedCell).stream()
                .map(this::mapToDtoWithUpdates)
                .collect(Collectors.toList());
    }

    public RequestDto getRequestById(UUID requestId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + requestId));
        return mapToDtoWithUpdates(request);
    }

    public RequestDto getStudentRequestById(UUID requestId, UUID studentId) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + requestId));
        if (!request.getStudent().getId().equals(studentId)) {
            throw new ResourceNotFoundException("Request not found with id: " + requestId);
        }
        return mapToDtoWithUpdates(request);
    }

    @Transactional
    public RequestDto updateRequestStatus(UUID requestId, RequestUpdateRequest req, User admin) {
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found with id: " + requestId));

        RequestStatus oldStatus = request.getStatus();
        RequestStatus newStatus = req.getStatus() != null ? req.getStatus() : oldStatus;
        String oldAssignedCell = request.getAssignedCell();

        request.setStatus(newStatus);

        if (req.getAssignedCell() != null && !req.getAssignedCell().isBlank()) {
            request.setAssignedCell(req.getAssignedCell());
        }
        if (req.getAssignedToUserId() != null) {
            userRepository.findById(req.getAssignedToUserId()).ifPresent(request::setAssignedTo);
        }
        if (req.getAdminNote() != null) {
            request.setAdminNote(req.getAdminNote());
        }
        if (newStatus == RequestStatus.COMPLETED || newStatus == RequestStatus.RESOLVED) {
            request.setResolvedAt(LocalDateTime.now());
        }

        Request saved = requestRepository.save(request);

        String note = req.getAdminNote() != null && !req.getAdminNote().isBlank()
                ? req.getAdminNote()
                : generateRequestUpdateComment(oldStatus, newStatus, request.getAssignedCell());

        IssueUpdate update = new IssueUpdate(
                IssueType.REQUEST,
                null,
                saved,
                admin,
                newStatus.name(),
                note
        );
        update.setOldStatus(oldStatus != null ? oldStatus.name() : null);
        issueUpdateRepository.save(update);

        // Notify Student
        sendStudentRequestNotification(request, oldStatus, newStatus, oldAssignedCell);

        return mapToDtoWithUpdates(saved);
    }

    private String generateRequestUpdateComment(RequestStatus oldStatus, RequestStatus newStatus, String assignedCell) {
        if (newStatus == RequestStatus.APPROVED) {
            return "Request approved by administrative authority.";
        } else if (newStatus == RequestStatus.REJECTED) {
            return "Request rejected.";
        } else if (newStatus == RequestStatus.COMPLETED) {
            return "Service request fulfillment completed.";
        } else if (newStatus == RequestStatus.IN_PROGRESS && assignedCell != null) {
            return "Assigned to " + assignedCell + " for processing.";
        } else {
            return "Request status updated from " + (oldStatus != null ? oldStatus.name() : "PENDING") + " to " + newStatus.name();
        }
    }

    private void sendStudentRequestNotification(Request request, RequestStatus oldStatus, RequestStatus newStatus, String oldAssignedCell) {
        if (request.getStudent() == null) return;

        String title;
        String message;

        if (newStatus == RequestStatus.APPROVED) {
            title = "Request Approved";
            message = "Your request " + request.getRequestNumber() + " has been approved.";
        } else if (newStatus == RequestStatus.COMPLETED) {
            title = "Request Completed";
            message = "Your request " + request.getRequestNumber() + " has been completed.";
        } else if (newStatus == RequestStatus.REJECTED) {
            title = "Request Update";
            message = "Your request " + request.getRequestNumber() + " has been rejected.";
        } else if (request.getAssignedCell() != null && !request.getAssignedCell().equals(oldAssignedCell)) {
            title = "Request Assigned";
            message = "Your request " + request.getRequestNumber() + " has been assigned to " + request.getAssignedCell() + ".";
        } else {
            title = "Request Status Updated";
            message = "Your request " + request.getRequestNumber() + " is now " + newStatus.name() + ".";
        }

        Notification notification = new Notification(
                request.getStudent(),
                title,
                message,
                "REQUEST_STATUS",
                "/requests/" + request.getId()
        );
        notificationRepository.save(notification);
    }

    private RequestDto mapToDtoWithUpdates(Request request) {
        RequestDto dto = RequestDto.fromEntity(request);
        List<IssueUpdate> updates = issueUpdateRepository.findByRequestIdOrderByCreatedAtAsc(request.getId());
        dto.setUpdates(updates.stream().map(IssueUpdateDto::fromEntity).collect(Collectors.toList()));
        return dto;
    }
}
