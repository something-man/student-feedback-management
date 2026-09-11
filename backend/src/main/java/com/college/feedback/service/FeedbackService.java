package com.college.feedback.service;

import com.college.feedback.dto.request.AnswerSubmitRequest;
import com.college.feedback.dto.request.FeedbackAssignRequest;
import com.college.feedback.dto.request.FeedbackCreateRequest;
import com.college.feedback.dto.request.FeedbackResponseRequest;
import com.college.feedback.dto.request.QuestionCreateRequest;
import com.college.feedback.dto.response.FeedbackFormDto;
import com.college.feedback.dto.response.FeedbackResponseDto;
import com.college.feedback.dto.response.QuestionDto;
import com.college.feedback.entity.*;
import com.college.feedback.entity.enums.AssignmentStatus;
import com.college.feedback.entity.enums.FormStatus;
import com.college.feedback.entity.enums.QuestionType;
import com.college.feedback.entity.enums.Role;
import com.college.feedback.entity.enums.TargetAudience;
import com.college.feedback.exception.BadRequestException;
import com.college.feedback.exception.ConflictException;
import com.college.feedback.exception.ResourceNotFoundException;
import com.college.feedback.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FeedbackService {

    private final FeedbackFormRepository formRepository;
    private final QuestionRepository questionRepository;
    private final FeedbackAssignmentRepository assignmentRepository;
    private final FeedbackResponseRepository responseRepository;
    private final ResponseAnswerRepository answerRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public FeedbackService(FeedbackFormRepository formRepository,
                           QuestionRepository questionRepository,
                           FeedbackAssignmentRepository assignmentRepository,
                           FeedbackResponseRepository responseRepository,
                           ResponseAnswerRepository answerRepository,
                           UserRepository userRepository,
                           NotificationService notificationService) {
        this.formRepository = formRepository;
        this.questionRepository = questionRepository;
        this.assignmentRepository = assignmentRepository;
        this.responseRepository = responseRepository;
        this.answerRepository = answerRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public FeedbackFormDto createFeedbackForm(FeedbackCreateRequest request, User creator) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new BadRequestException("Feedback form title cannot be empty");
        }

        FormStatus initialStatus = request.getStatus() != null ? request.getStatus() : FormStatus.PUBLISHED;

        FeedbackForm form = new FeedbackForm(
                request.getTitle().trim(),
                request.getDescription(),
                request.getCategory(),
                initialStatus,
                request.getTargetAudience() != null ? request.getTargetAudience() : TargetAudience.STUDENTS,
                request.getTargetDepartment(),
                request.getStartDate() != null ? request.getStartDate() : LocalDateTime.now(),
                request.getEndDate() != null ? request.getEndDate() : request.getDeadline(),
                request.getAllowAnonymous() != null ? request.getAllowAnonymous() : true,
                creator
        );

        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            int order = 1;
            for (QuestionCreateRequest qReq : request.getQuestions()) {
                if (qReq.getQuestionText() == null || qReq.getQuestionText().trim().isEmpty()) {
                    throw new BadRequestException("Question text cannot be empty");
                }
                Question q = new Question(
                        form,
                        qReq.getQuestionText().trim(),
                        qReq.getQuestionType() != null ? qReq.getQuestionType() : QuestionType.RATING,
                        qReq.getRequired() != null ? qReq.getRequired() : true,
                        qReq.getDisplayOrder() != null ? qReq.getDisplayOrder() : order++
                );
                form.addQuestion(q);
            }
        }

        FeedbackForm savedForm = formRepository.save(form);

        if (initialStatus == FormStatus.PUBLISHED) {
            assignFormToTargetUsers(savedForm);
        }

        return FeedbackFormDto.fromEntity(savedForm);
    }

    @Transactional
    public FeedbackFormDto updateFeedbackForm(UUID formId, FeedbackCreateRequest request) {
        FeedbackForm form = formRepository.findById(formId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback form not found with id: " + formId));

        if (form.getStatus() == FormStatus.CLOSED) {
            throw new BadRequestException("Cannot modify a closed feedback form");
        }

        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            form.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) form.setDescription(request.getDescription());
        if (request.getCategory() != null) form.setCategory(request.getCategory());
        if (request.getStatus() != null) form.setStatus(request.getStatus());
        if (request.getTargetAudience() != null) form.setTargetAudience(request.getTargetAudience());
        if (request.getTargetDepartment() != null) form.setTargetDepartment(request.getTargetDepartment());
        if (request.getStartDate() != null) form.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) form.setEndDate(request.getEndDate());
        if (request.getDeadline() != null) form.setDeadline(request.getDeadline());
        if (request.getAllowAnonymous() != null) form.setAllowAnonymous(request.getAllowAnonymous());

        long responseCount = responseRepository.countByFormId(formId);
        if (request.getQuestions() != null && !request.getQuestions().isEmpty()) {
            if (responseCount > 0) {
                throw new BadRequestException("Cannot modify questions for a feedback form that already has responses");
            }
            form.getQuestions().clear();
            int order = 1;
            for (QuestionCreateRequest qReq : request.getQuestions()) {
                if (qReq.getQuestionText() == null || qReq.getQuestionText().trim().isEmpty()) {
                    throw new BadRequestException("Question text cannot be empty");
                }
                Question q = new Question(
                        form,
                        qReq.getQuestionText().trim(),
                        qReq.getQuestionType() != null ? qReq.getQuestionType() : QuestionType.RATING,
                        qReq.getRequired() != null ? qReq.getRequired() : true,
                        qReq.getDisplayOrder() != null ? qReq.getDisplayOrder() : order++
                );
                form.addQuestion(q);
            }
        }

        FeedbackForm updated = formRepository.save(form);
        return FeedbackFormDto.fromEntity(updated);
    }

    @Transactional
    public FeedbackFormDto publishFeedbackForm(UUID formId) {
        FeedbackForm form = formRepository.findById(formId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback form not found with id: " + formId));

        if (form.getTitle() == null || form.getTitle().trim().isEmpty()) {
            throw new BadRequestException("Feedback form title cannot be empty");
        }
        if (form.getCategory() == null || form.getCategory().trim().isEmpty()) {
            throw new BadRequestException("Feedback form category is required before publishing");
        }
        if (form.getQuestions() == null || form.getQuestions().isEmpty()) {
            throw new BadRequestException("Feedback form must contain at least one question before publishing");
        }
        for (Question q : form.getQuestions()) {
            if (q.getQuestionText() == null || q.getQuestionText().trim().isEmpty()) {
                throw new BadRequestException("All questions must have valid text before publishing");
            }
        }
        if (form.getEndDate() != null && form.getStartDate() != null && form.getEndDate().isBefore(form.getStartDate())) {
            throw new BadRequestException("End date cannot be earlier than start date");
        }

        form.setStatus(FormStatus.PUBLISHED);
        form.setIsActive(true);
        FeedbackForm saved = formRepository.save(form);

        assignFormToTargetUsers(saved);

        return FeedbackFormDto.fromEntity(saved);
    }

    @Transactional
    public FeedbackFormDto closeFeedbackForm(UUID formId) {
        FeedbackForm form = formRepository.findById(formId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback form not found with id: " + formId));

        form.setStatus(FormStatus.CLOSED);
        form.setIsActive(false);
        FeedbackForm saved = formRepository.save(form);
        return FeedbackFormDto.fromEntity(saved);
    }

    @Transactional
    public void deleteFeedbackForm(UUID formId) {
        FeedbackForm form = formRepository.findById(formId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback form not found with id: " + formId));

        long responseCount = responseRepository.countByFormId(formId);
        if (responseCount > 0) {
            throw new BadRequestException("Cannot delete feedback form with active responses. Close the form instead.");
        }

        formRepository.delete(form);
    }

    @Transactional
    public QuestionDto addQuestionToForm(UUID formId, QuestionCreateRequest req) {
        FeedbackForm form = formRepository.findById(formId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback form not found with id: " + formId));

        if (form.getStatus() == FormStatus.CLOSED) {
            throw new BadRequestException("Cannot add questions to a closed feedback form");
        }

        long responseCount = responseRepository.countByFormId(formId);
        if (responseCount > 0) {
            throw new BadRequestException("Cannot add questions to a feedback form that already has responses");
        }

        if (req.getQuestionText() == null || req.getQuestionText().trim().isEmpty()) {
            throw new BadRequestException("Question text cannot be empty");
        }

        int nextOrder = form.getQuestions().size() + 1;
        Question question = new Question(
                form,
                req.getQuestionText().trim(),
                req.getQuestionType() != null ? req.getQuestionType() : QuestionType.RATING,
                req.getRequired() != null ? req.getRequired() : true,
                req.getDisplayOrder() != null ? req.getDisplayOrder() : nextOrder
        );
        Question saved = questionRepository.save(question);
        return QuestionDto.fromEntity(saved);
    }

    @Transactional
    public QuestionDto updateQuestion(UUID questionId, QuestionCreateRequest req) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));

        if (question.getForm() != null) {
            if (question.getForm().getStatus() == FormStatus.CLOSED) {
                throw new BadRequestException("Cannot edit question of a closed feedback form");
            }
            long responseCount = responseRepository.countByFormId(question.getForm().getId());
            if (responseCount > 0) {
                throw new BadRequestException("Cannot edit questions for a feedback form that already has responses");
            }
        }

        if (req.getQuestionText() != null && !req.getQuestionText().trim().isEmpty()) {
            question.setQuestionText(req.getQuestionText().trim());
        }
        if (req.getQuestionType() != null) {
            question.setQuestionType(req.getQuestionType());
        }
        if (req.getRequired() != null) {
            question.setRequired(req.getRequired());
        }
        if (req.getDisplayOrder() != null) {
            question.setDisplayOrder(req.getDisplayOrder());
        }

        Question saved = questionRepository.save(question);
        return QuestionDto.fromEntity(saved);
    }

    @Transactional
    public void deleteQuestion(UUID questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));

        if (question.getForm() != null) {
            if (question.getForm().getStatus() == FormStatus.CLOSED) {
                throw new BadRequestException("Cannot delete question from a closed feedback form");
            }
            long responseCount = responseRepository.countByFormId(question.getForm().getId());
            if (responseCount > 0) {
                throw new BadRequestException("Cannot delete questions for a feedback form that already has responses");
            }
        }

        questionRepository.delete(question);
    }

    @Transactional
    public void assignFormToTargetUsers(FeedbackForm form) {
        List<User> targetUsers = new ArrayList<>();
        TargetAudience audience = form.getTargetAudience();

        if (audience == TargetAudience.STUDENTS || audience == TargetAudience.BOTH) {
            if (form.getTargetDepartment() != null && !form.getTargetDepartment().isBlank()) {
                targetUsers.addAll(userRepository.findByRoleAndDepartment(Role.STUDENT, form.getTargetDepartment()));
            } else {
                targetUsers.addAll(userRepository.findByRole(Role.STUDENT));
            }
        }

        if (audience == TargetAudience.FACULTY || audience == TargetAudience.BOTH) {
            if (form.getTargetDepartment() != null && !form.getTargetDepartment().isBlank()) {
                targetUsers.addAll(userRepository.findByRoleAndDepartment(Role.FACULTY, form.getTargetDepartment()));
            } else {
                targetUsers.addAll(userRepository.findByRole(Role.FACULTY));
            }
        }

        for (User u : targetUsers) {
            if (!assignmentRepository.existsByFormIdAndUserId(form.getId(), u.getId())) {
                FeedbackAssignment assignment = new FeedbackAssignment(form, u);
                assignmentRepository.save(assignment);

                notificationService.sendNotification(
                        u,
                        "New Feedback Assigned",
                        "Feedback form \"" + form.getTitle() + "\" is now open for responses.",
                        "FEEDBACK_ASSIGNED",
                        "/feedback/" + form.getId()
                );
            }
        }
    }

    @Transactional
    public FeedbackFormDto assignFeedbackForm(UUID formId, FeedbackAssignRequest request, User admin) {
        FeedbackForm form = formRepository.findById(formId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback form not found with id: " + formId));

        if (form.getStatus() != FormStatus.PUBLISHED) {
            throw new BadRequestException("Only published feedback forms can be assigned to users");
        }

        if (request.getDeadline() != null && request.getDeadline().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Assignment deadline cannot be in the past");
        }

        List<User> targetUsers = new ArrayList<>();
        if (request.getUserIds() != null && !request.getUserIds().isEmpty()) {
            targetUsers = userRepository.findAllById(request.getUserIds());
            if (targetUsers.isEmpty()) {
                throw new BadRequestException("No valid users found for the provided user IDs");
            }
        } else {
            String dept = request.getTargetDepartment() != null ? request.getTargetDepartment() : form.getTargetDepartment();
            TargetAudience audience = request.getTargetAudience() != null ? request.getTargetAudience() : form.getTargetAudience();

            if (audience == TargetAudience.STUDENTS || audience == TargetAudience.BOTH) {
                if (dept != null && !dept.isBlank()) {
                    targetUsers.addAll(userRepository.findByRoleAndDepartment(Role.STUDENT, dept));
                } else {
                    targetUsers.addAll(userRepository.findByRole(Role.STUDENT));
                }
            }
            if (audience == TargetAudience.FACULTY || audience == TargetAudience.BOTH) {
                if (dept != null && !dept.isBlank()) {
                    targetUsers.addAll(userRepository.findByRoleAndDepartment(Role.FACULTY, dept));
                } else {
                    targetUsers.addAll(userRepository.findByRole(Role.FACULTY));
                }
            }
        }

        LocalDateTime deadline = request.getDeadline() != null ? request.getDeadline() : form.getDeadline();

        for (User u : targetUsers) {
            if (!assignmentRepository.existsByFormIdAndUserId(form.getId(), u.getId())) {
                FeedbackAssignment assignment = new FeedbackAssignment(form, u, admin, deadline);
                assignmentRepository.save(assignment);

                notificationService.sendNotification(
                        u,
                        "New Feedback Assigned",
                        "Feedback form \"" + form.getTitle() + "\" has been assigned to you.",
                        "FEEDBACK_ASSIGNED",
                        "/feedback/" + form.getId()
                );
            }
        }

        return getFormDetails(formId, null);
    }

    public List<FeedbackFormDto> getAllForms() {
        return formRepository.findAll().stream().map(form -> {
            FeedbackFormDto dto = FeedbackFormDto.fromEntity(form);
            dto.setTotalResponses(responseRepository.countByFormId(form.getId()));
            dto.setAverageRating(responseRepository.getAverageRatingForForm(form.getId()));
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public List<FeedbackFormDto> getFormsAssignedToUser(UUID userId) {
        List<FeedbackAssignment> assignments = assignmentRepository.findAllWithFormByUserId(userId);
        LocalDateTime now = LocalDateTime.now();

        return assignments.stream()
                .filter(assignment -> assignment.getForm() != null && assignment.getForm().getStatus() != FormStatus.DRAFT)
                .map(assignment -> {
                    FeedbackForm form = assignment.getForm();
                    FeedbackFormDto dto = FeedbackFormDto.fromEntity(form);

                    // Check for expired status
                    if (assignment.getStatus() != AssignmentStatus.COMPLETED &&
                            assignment.getDeadline() != null &&
                            assignment.getDeadline().isBefore(now)) {
                        assignment.setStatus(AssignmentStatus.EXPIRED);
                        assignmentRepository.save(assignment);
                    }

                    dto.setUserStatus(assignment.getStatus());
                    return dto;
                }).collect(Collectors.toList());
    }

    @Transactional
    public FeedbackFormDto getFormDetails(UUID formId, UUID userId) {
        FeedbackForm form = formRepository.findById(formId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback form not found with id: " + formId));

        FeedbackFormDto dto = FeedbackFormDto.fromEntity(form);
        dto.setTotalResponses(responseRepository.countByFormId(formId));
        dto.setAverageRating(responseRepository.getAverageRatingForForm(formId));

        if (userId != null) {
            assignmentRepository.findByUserIdAndFormId(userId, formId)
                    .ifPresent(assignment -> {
                        LocalDateTime now = LocalDateTime.now();
                        if (assignment.getStatus() == AssignmentStatus.PENDING) {
                            assignment.setStatus(AssignmentStatus.IN_PROGRESS);
                            assignmentRepository.save(assignment);
                        } else if (assignment.getStatus() != AssignmentStatus.COMPLETED &&
                                assignment.getDeadline() != null &&
                                assignment.getDeadline().isBefore(now)) {
                            assignment.setStatus(AssignmentStatus.EXPIRED);
                            assignmentRepository.save(assignment);
                        }
                        dto.setUserStatus(assignment.getStatus());
                    });
        }

        return dto;
    }

    @Transactional
    public FeedbackFormDto submitResponse(UUID formId, FeedbackResponseRequest request, User user) {
        FeedbackForm form = formRepository.findById(formId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback form not found with id: " + formId));

        if (form.getStatus() != FormStatus.PUBLISHED || !Boolean.TRUE.equals(form.getIsActive())) {
            throw new BadRequestException("This feedback form is not currently open for responses");
        }

        if (form.getStartDate() != null && form.getStartDate().isAfter(LocalDateTime.now())) {
            throw new BadRequestException("This feedback form is not yet open for responses");
        }

        if (form.getDeadline() != null && form.getDeadline().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("The deadline for this feedback form has passed");
        }

        // Verify role authorization
        if (user.getRole() == Role.STUDENT && form.getTargetAudience() == TargetAudience.FACULTY) {
            throw new BadRequestException("Students are not authorized to submit faculty feedback");
        }
        if (user.getRole() == Role.FACULTY && form.getTargetAudience() == TargetAudience.STUDENTS) {
            throw new BadRequestException("Faculty members are not authorized to submit student feedback");
        }

        // Check assignment or auto-provision assignment
        Optional<FeedbackAssignment> assignmentOpt = assignmentRepository.findByUserIdAndFormId(user.getId(), formId);
        FeedbackAssignment assignment;
        if (assignmentOpt.isPresent()) {
            assignment = assignmentOpt.get();
            if (assignment.getStatus() == AssignmentStatus.COMPLETED) {
                throw new ConflictException("You have already submitted this feedback.");
            }
        } else {
            if (responseRepository.existsByFormIdAndUserId(formId, user.getId())) {
                throw new ConflictException("You have already submitted this feedback.");
            }
            assignment = new FeedbackAssignment(form, user);
        }

        // Validate Answers
        if (request.getAnswers() == null || request.getAnswers().isEmpty()) {
            throw new BadRequestException("At least one answer must be submitted");
        }

        Map<UUID, Question> formQuestions = form.getQuestions().stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        Map<UUID, AnswerSubmitRequest> answerMap = new HashMap<>();
        for (AnswerSubmitRequest ansReq : request.getAnswers()) {
            if (!formQuestions.containsKey(ansReq.getQuestionId())) {
                throw new BadRequestException("Invalid question ID for this feedback form: " + ansReq.getQuestionId());
            }
            if (ansReq.getRatingValue() != null && (ansReq.getRatingValue() < 1 || ansReq.getRatingValue() > 5)) {
                throw new BadRequestException("Rating values must be between 1 and 5");
            }
            if (ansReq.getTextAnswer() != null && ansReq.getTextAnswer().length() > 2000) {
                throw new BadRequestException("Text answer exceeds the maximum allowed length of 2000 characters");
            }
            answerMap.put(ansReq.getQuestionId(), ansReq);
        }

        // Enforce required questions
        for (Question q : form.getQuestions()) {
            if (Boolean.TRUE.equals(q.getRequired())) {
                AnswerSubmitRequest submitted = answerMap.get(q.getId());
                if (submitted == null) {
                    throw new BadRequestException("Answer is required for question: \"" + q.getQuestionText() + "\"");
                }
                if (q.getQuestionType() == QuestionType.RATING || q.getQuestionType() == QuestionType.STAR_RATING) {
                    if (submitted.getRatingValue() == null || submitted.getRatingValue() < 1) {
                        throw new BadRequestException("Rating is required for question: \"" + q.getQuestionText() + "\"");
                    }
                } else {
                    if (submitted.getTextAnswer() == null || submitted.getTextAnswer().trim().isEmpty()) {
                        throw new BadRequestException("Answer is required for question: \"" + q.getQuestionText() + "\"");
                    }
                }
            }
        }

        // Anonymity Handling
        boolean isAnonymous = Boolean.TRUE.equals(request.getIsAnonymous()) && Boolean.TRUE.equals(form.getAllowAnonymous());
        User responseUser = isAnonymous ? null : user;

        // Calculate Overall Rating
        Double calculatedRating = request.getOverallRating();
        if (calculatedRating == null) {
            double sum = 0;
            int count = 0;
            for (AnswerSubmitRequest ansReq : request.getAnswers()) {
                if (ansReq.getRatingValue() != null && ansReq.getRatingValue() > 0) {
                    sum += ansReq.getRatingValue();
                    count++;
                }
            }
            if (count > 0) {
                calculatedRating = Math.round((sum / count) * 10.0) / 10.0;
            }
        }

        FeedbackResponse response = new FeedbackResponse(form, responseUser, isAnonymous, calculatedRating);

        for (AnswerSubmitRequest ansReq : request.getAnswers()) {
            Question question = formQuestions.get(ansReq.getQuestionId());
            ResponseAnswer answer = new ResponseAnswer(question, ansReq.getRatingValue(), ansReq.getTextAnswer());
            response.addAnswer(answer);
        }

        responseRepository.save(response);

        // Update Assignment status to COMPLETED
        assignment.setStatus(AssignmentStatus.COMPLETED);
        assignment.setCompletedAt(LocalDateTime.now());
        assignmentRepository.save(assignment);

        // Notify Admins
        List<User> admins = userRepository.findByRole(Role.ADMIN);
        for (User admin : admins) {
            notificationService.sendNotification(
                    admin,
                    "New Feedback Response",
                    "New response received for \"" + form.getTitle() + "\"",
                    "FEEDBACK_RESPONSE",
                    "/feedback/" + form.getId()
            );
        }

        return getFormDetails(formId, user.getId());
    }

    public List<FeedbackResponse> getFormResponses(UUID formId) {
        return responseRepository.findByFormId(formId);
    }

    public List<FeedbackResponseDto> getFormResponsesAsDto(UUID formId) {
        return responseRepository.findByFormId(formId).stream()
                .map(FeedbackResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<FeedbackResponseDto> getFilteredResponses(UUID formId, String category) {
        return getFilteredResponses(formId, category, null, null, null);
    }

    public List<FeedbackResponseDto> getFilteredResponses(UUID formId, String category, String department, LocalDateTime startDate, LocalDateTime endDate) {
        List<FeedbackResponse> responses;
        if (formId != null) {
            responses = responseRepository.findByFormId(formId);
        } else {
            responses = responseRepository.findAll();
        }

        return responses.stream()
                .filter(r -> category == null || category.isBlank() || (r.getForm() != null && category.equalsIgnoreCase(r.getForm().getCategory())))
                .filter(r -> department == null || department.isBlank() || (r.getForm() != null && department.equalsIgnoreCase(r.getForm().getTargetDepartment())))
                .filter(r -> startDate == null || (r.getSubmittedAt() != null && !r.getSubmittedAt().isBefore(startDate)))
                .filter(r -> endDate == null || (r.getSubmittedAt() != null && !r.getSubmittedAt().isAfter(endDate)))
                .map(FeedbackResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
}
