package vn.edu.fpt.fschool.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.fschool.dto.RequestDtos.*;
import vn.edu.fpt.fschool.model.RequestEntity;
import vn.edu.fpt.fschool.model.User;
import vn.edu.fpt.fschool.repository.RequestRepository;
import vn.edu.fpt.fschool.repository.UserRepository;

@Service
public class RequestService {

  private final RequestRepository requestRepository;
  private final UserRepository userRepository;
  private final StudentAccessResolver studentAccessResolver;
  private final vn.edu.fpt.fschool.mapper.AcademicMapper academicMapper;

  public RequestService(
      RequestRepository requestRepository,
      UserRepository userRepository,
      StudentAccessResolver studentAccessResolver,
      vn.edu.fpt.fschool.mapper.AcademicMapper academicMapper) {
    this.requestRepository = requestRepository;
    this.userRepository = userRepository;
    this.studentAccessResolver = studentAccessResolver;
    this.academicMapper = academicMapper;
  }

  @Transactional(readOnly = true)
  public List<RequestResponse> getMyRequests(String email) {
    User user =
        userRepository
            .findByEmailIgnoreCase(email)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

    return academicMapper.toRequestResponseList(
        requestRepository.findByRequester_UserId(user.getUserId()));
  }

  @Transactional
  public RequestResponse createRequest(String email, CreateRequestDto dto) {
    User requester =
        userRepository
            .findByEmailIgnoreCase(email)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

    Long targetStudentId = studentAccessResolver.resolveTargetStudentId(requester, dto.studentId());
    User student =
        targetStudentId.equals(requester.getUserId())
            ? requester
            : userRepository
                .findById(targetStudentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy học sinh"));

    RequestEntity entity = new RequestEntity();
    entity.setStudent(student);
    entity.setRequester(requester);
    entity.setRequestType(dto.requestType());
    entity.setTitle(dto.title());
    entity.setContent(dto.content());
    entity.setAttachmentUrl(dto.attachmentUrl());
    entity.setStatus("PENDING");

    RequestEntity saved = requestRepository.save(entity);
    return academicMapper.toRequestResponse(saved);
  }
}
