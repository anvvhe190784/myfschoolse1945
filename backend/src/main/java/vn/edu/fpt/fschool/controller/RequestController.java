package vn.edu.fpt.fschool.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.fschool.dto.RequestDtos.*;
import vn.edu.fpt.fschool.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/requests")
@Tag(name = "4. Requests (Đơn từ)", description = "Quản lý và nộp đơn từ trực tuyến")
public class RequestController {

    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping
    @Operation(summary = "Danh sách đơn từ đã nộp (Use Case 6: ListRequest)", description = "Xem danh sách các đơn từ sinh viên/phụ huynh đã gửi kèm trạng thái")
    public ResponseEntity<List<RequestResponse>> getMyRequests(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(requestService.getMyRequests(email));
    }

    @PostMapping
    @Operation(summary = "Nộp đơn từ mới (Use Case 11: AddRequest)", description = "Gửi đơn xin nghỉ phép, miễn giảm điểm danh, phúc khảo điểm...")
    public ResponseEntity<RequestResponse> createRequest(
            Authentication authentication,
            @Valid @RequestBody CreateRequestDto dto
    ) {
        String email = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED).body(requestService.createRequest(email, dto));
    }
}
