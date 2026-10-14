import 'package:flutter_test/flutter_test.dart';
import 'package:myfschoolse1945/vn/edu/fpt/model/model.dart';

void main() {
  group('Model Serialization & Deserialization Tests', () {
    test('UserModel mapping correctly from and to JSON', () {
      final json = {
        'userId': 1,
        'rollNumber': 'SE194501',
        'email': 'student@fpt.edu.vn',
        'fullName': 'Nguyễn Văn A',
        'role': 'STUDENT',
        'status': 'ACTIVE',
      };
      final user = UserModel.fromJson(json);
      expect(user.userId, 1);
      expect(user.rollNumber, 'SE194501');
      expect(user.email, 'student@fpt.edu.vn');
      expect(user.fullName, 'Nguyễn Văn A');
      expect(user.role, 'STUDENT');

      final serialized = user.toJson();
      expect(serialized['email'], 'student@fpt.edu.vn');
      expect(serialized['fullName'], 'Nguyễn Văn A');
    });

    test('AuthDtos LoginRequest and LoginResponse mapping', () {
      final req = const LoginRequest(
        identifier: 'student@fpt.edu.vn',
        password: 'password123',
      );
      expect(req.toJson()['identifier'], 'student@fpt.edu.vn');

      final resJson = {
        'token': 'mock-jwt-token',
        'tokenType': 'Bearer',
        'userId': 10,
        'rollNumber': 'SE194502',
        'email': 'user@fpt.edu.vn',
        'fullName': 'Trần Thị B',
        'role': 'STUDENT',
      };
      final res = LoginResponse.fromJson(resJson);
      expect(res.token, 'mock-jwt-token');
      expect(res.accessToken, 'mock-jwt-token');
      expect(res.userId, 10);
      expect(res.fullName, 'Trần Thị B');

      final refreshReq = const RefreshTokenRequest(refreshToken: 'sample-refresh');
      expect(refreshReq.toJson()['refreshToken'], 'sample-refresh');

      final refreshRes = TokenRefreshResponse.fromJson({
        'accessToken': 'new-access-token',
        'refreshToken': 'new-refresh-token',
        'tokenType': 'Bearer',
        'expiresIn': 900,
      });
      expect(refreshRes.accessToken, 'new-access-token');
      expect(refreshRes.refreshToken, 'new-refresh-token');
      expect(refreshRes.expiresIn, 900);
    });

    test('CourseModel and ClassModel mapping', () {
      final course = CourseModel.fromJson({
        'courseId': 101,
        'courseCode': 'PRM393',
        'courseName': 'Mobile Programming',
        'credits': 3,
      });
      expect(course.courseCode, 'PRM393');
      expect(course.credits, 3);

      final classObj = ClassModel.fromJson({
        'classId': 201,
        'classCode': 'SE1945',
        'courseId': 101,
        'semester': 'Fall2026',
      });
      expect(classObj.classCode, 'SE1945');
      expect(classObj.semester, 'Fall2026');
    });

    test('ScheduleModel mapping', () {
      final schedule = ScheduleModel.fromJson({
        'scheduleId': 1,
        'classId': 201,
        'classCode': 'SE1945',
        'courseName': 'Mobile Programming',
        'slotNumber': 2,
        'date': '2026-10-15',
        'startTime': '09:30:00',
        'endTime': '11:50:00',
        'room': 'BE-301',
        'lecturerName': 'Thầy Hưng',
        'status': 'UPCOMING',
      });
      expect(schedule.classCode, 'SE1945');
      expect(schedule.room, 'BE-301');
      expect(schedule.slotNumber, 2);
    });

    test('GradeModel mapping', () {
      final grade = GradeModel.fromJson({
        'gradeId': 1,
        'classId': 201,
        'assessmentName': 'Lab 1',
        'weight': 10.0,
        'score': 9.5,
        'status': 'PUBLISHED',
      });
      expect(grade.assessmentName, 'Lab 1');
      expect(grade.score, 9.5);
      expect(grade.weight, 10.0);
    });

    test('RequestModel and CreateRequestDto mapping', () {
      final reqDto = const CreateRequestDto(
        requestType: 'LEAVE_OF_ABSENCE',
        title: 'Xin nghỉ học vì ốm',
        content: 'Em bị sốt xuất huyết xin nghỉ 2 ngày',
      );
      expect(reqDto.toJson()['requestType'], 'LEAVE_OF_ABSENCE');

      final req = RequestModel.fromJson({
        'requestId': 5,
        'studentId': 1,
        'requestType': 'LEAVE_OF_ABSENCE',
        'title': 'Xin nghỉ học vì ốm',
        'content': 'Em bị sốt xuất huyết xin nghỉ 2 ngày',
        'status': 'PENDING',
      });
      expect(req.requestId, 5);
      expect(req.status, 'PENDING');
    });

    test('ClubModel and ClubMembershipModel mapping', () {
      final club = ClubModel.fromJson({
        'clubId': 1,
        'clubName': 'F-Code Club',
        'description': 'CLB Lập trình',
        'category': 'TECHNOLOGY',
        'memberCount': 50,
      });
      expect(club.clubName, 'F-Code Club');
      expect(club.category, 'TECHNOLOGY');

      final membership = ClubMembershipModel.fromJson({
        'membershipId': 1,
        'clubId': 1,
        'userId': 1,
        'role': 'LEADER',
      });
      expect(membership.role, 'LEADER');
    });

    test('NewsModel mapping', () {
      final news = NewsModel.fromJson({
        'newsId': 1,
        'title': 'Lễ hội thể thao FPT Schools',
        'summary': 'Giải thi đấu thể thao toàn trường',
        'content': 'Nội dung chi tiết...',
        'category': 'EVENT',
        'publishedAt': '2026-10-14T08:00:00Z',
      });
      expect(news.title, 'Lễ hội thể thao FPT Schools');
      expect(news.category, 'EVENT');
      expect(news.formattedDate, isNotEmpty);
    });
  });
}
