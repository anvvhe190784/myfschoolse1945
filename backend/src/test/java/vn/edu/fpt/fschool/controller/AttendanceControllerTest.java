package vn.edu.fpt.fschool.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import vn.edu.fpt.fschool.dto.AttendanceDtos.*;
import vn.edu.fpt.fschool.service.AttendanceService;

@SpringBootTest
class AttendanceControllerTest {

  @Autowired private WebApplicationContext context;

  @MockitoBean private AttendanceService attendanceService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
  }

  @Test
  @DisplayName("GET /api/v1/schedules/{id}/attendance - Chưa đăng nhập trả về 403 Forbidden")
  void getAttendance_unauthenticated_returnsForbidden() throws Exception {
    mockMvc.perform(get("/api/v1/schedules/1/attendance")).andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("GET /api/v1/schedules/{id}/attendance - LECTURER xem điểm danh thành công 200 OK")
  @WithMockUser(username = "lecturer@fe.edu.vn", roles = {"LECTURER"})
  void getAttendance_asLecturer_returnsOk() throws Exception {
    ScheduleAttendanceResponse mockResponse =
        new ScheduleAttendanceResponse(
            1L,
            10L,
            "SE1945",
            "PRM393",
            LocalDate.of(2026, 10, 21),
            1,
            List.of(
                new AttendanceItemResponse(101L, "SE194501", "Nguyen Van A", "PRESENT", "Co mat")),
            1,
            1,
            0,
            0);

    when(attendanceService.getScheduleAttendance(eq(1L), eq("lecturer@fe.edu.vn")))
        .thenReturn(mockResponse);

    mockMvc
        .perform(get("/api/v1/schedules/1/attendance"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.scheduleId").value(1))
        .andExpect(jsonPath("$.classCode").value("SE1945"))
        .andExpect(jsonPath("$.totalStudents").value(1))
        .andExpect(jsonPath("$.presentCount").value(1));
  }

  @Test
  @DisplayName("GET /api/v1/schedules/{id}/attendance - STUDENT xem điểm danh thành công 200 OK")
  @WithMockUser(username = "student@fpt.edu.vn", roles = {"STUDENT"})
  void getAttendance_asStudent_returnsOk() throws Exception {
    ScheduleAttendanceResponse mockResponse =
        new ScheduleAttendanceResponse(
            1L,
            10L,
            "SE1945",
            "PRM393",
            LocalDate.of(2026, 10, 21),
            1,
            List.of(
                new AttendanceItemResponse(101L, "SE194501", "Nguyen Van A", "PRESENT", null)),
            1,
            1,
            0,
            0);

    when(attendanceService.getScheduleAttendance(eq(1L), eq("student@fpt.edu.vn")))
        .thenReturn(mockResponse);

    mockMvc
        .perform(get("/api/v1/schedules/1/attendance"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.students[0].studentId").value(101))
        .andExpect(jsonPath("$.students[0].status").value("PRESENT"));
  }

  @Test
  @DisplayName("PUT /api/v1/schedules/{id}/attendance - LECTURER lưu điểm danh thành công 200 OK")
  @WithMockUser(username = "lecturer@fe.edu.vn", roles = {"LECTURER"})
  void saveAttendance_asLecturer_returnsOk() throws Exception {
    ScheduleAttendanceResponse mockResponse =
        new ScheduleAttendanceResponse(
            1L,
            10L,
            "SE1945",
            "PRM393",
            LocalDate.of(2026, 10, 21),
            1,
            List.of(
                new AttendanceItemResponse(101L, "SE194501", "Nguyen Van A", "PRESENT", "On time")),
            1,
            1,
            0,
            0);

    when(attendanceService.saveScheduleAttendance(
            eq(1L), any(TakeAttendanceRequest.class), eq("lecturer@fe.edu.vn")))
        .thenReturn(mockResponse);

    String requestJson =
        """
        {
          "entries": [
            {
              "studentId": 101,
              "status": "PRESENT",
              "note": "On time"
            }
          ]
        }
        """;

    mockMvc
        .perform(
            put("/api/v1/schedules/1/attendance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.scheduleId").value(1))
        .andExpect(jsonPath("$.presentCount").value(1));
  }

  @Test
  @DisplayName("PUT /api/v1/schedules/{id}/attendance - STUDENT bị từ chối 403 Forbidden")
  @WithMockUser(username = "student@fpt.edu.vn", roles = {"STUDENT"})
  void saveAttendance_asStudent_returnsForbidden() throws Exception {
    String requestJson =
        """
        {
          "entries": [
            {
              "studentId": 101,
              "status": "PRESENT"
            }
          ]
        }
        """;

    mockMvc
        .perform(
            put("/api/v1/schedules/1/attendance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("PUT /api/v1/schedules/{id}/attendance - Dữ liệu invalid trả về 400 Bad Request")
  @WithMockUser(username = "lecturer@fe.edu.vn", roles = {"LECTURER"})
  void saveAttendance_invalidStatus_returnsBadRequest() throws Exception {
    String invalidJson =
        """
        {
          "entries": [
            {
              "studentId": 101,
              "status": "INVALID_STATUS"
            }
          ]
        }
        """;

    mockMvc
        .perform(
            put("/api/v1/schedules/1/attendance")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
        .andExpect(status().isBadRequest());
  }
}
