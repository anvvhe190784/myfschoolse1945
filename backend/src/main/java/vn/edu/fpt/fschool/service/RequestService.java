package vn.edu.fpt.fschool.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.fschool.dto.RequestDtos.*;
import vn.edu.fpt.fschool.model.RequestEntity;
import vn.edu.fpt.fschool.model.User;
import vn.edu.fpt.fschool.repository.RequestRepository;
import vn.edu.fpt.fschool.repository.UserRepository;

import java.util.List;

@Service
public class RequestService {

    private final RequestRepository requestRepository;
    private final UserRepository userRepository;

    public RequestService(RequestRepository requestRepository, UserRepository userRepository) {
        this.requestRepository = requestRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<RequestResponse> getMyRequests(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        return requestRepository.findByRequester_UserId(user.getUserId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public RequestResponse createRequest(String email, CreateRequestDto dto) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        RequestEntity entity = new RequestEntity();
        entity.setStudent(user);
        entity.setRequester(user);
        entity.setRequestType(dto.requestType());
        entity.setTitle(dto.title());
        entity.setContent(dto.content());
        entity.setAttachmentUrl(dto.attachmentUrl());
        entity.setStatus("PENDING");

        RequestEntity saved = requestRepository.save(entity);
        return toResponse(saved);
    }

    private RequestResponse toResponse(RequestEntity r) {
        String studentName = r.getStudent() != null ? r.getStudent().getFullName() : "N/A";
        String rollNumber = r.getStudent() != null ? r.getStudent().getRollNumber() : "N/A";

        return new RequestResponse(
                r.getRequestId(),
                r.getStudent() != null ? r.getStudent().getUserId() : null,
                studentName,
                rollNumber,
                r.getRequestType(),
                r.getTitle(),
                r.getContent(),
                r.getAttachmentUrl(),
                r.getStatus(),
                r.getResponseNote(),
                r.getCreatedAt()
        );
    }
}
