# MyFSchool SE1945 Monorepo

Hệ thống quản lý trường học MyFSchool (SE1945) được tổ chức theo mô hình Monorepo tiêu chuẩn.

## Cấu trúc dự án

```text
myfschoolse1945/
├── backend/            # Dịch vụ backend Spring Boot 4.1 (Java 21, REST API, JPA, Security, Docker)
├── mobile/             # Ứng dụng di động Flutter 3.x (Dart, Android, iOS, Web)
├── sql/                # Scripts khởi tạo cơ sở dữ liệu PostgreSQL (Schema & Seed data)
├── .gitignore          # Cấu hình bỏ qua file cho toàn bộ Monorepo
└── README.md           # Tài liệu hướng dẫn
```

---

## 1. Khởi chạy Backend & Database

### Yêu cầu:
- JDK 21+
- Docker & Docker Compose

### Các bước thực hiện:
1. Di chuyển vào thư mục `backend`:
   ```bash
   cd backend
   ```
2. Khởi động PostgreSQL và Redis thông qua Docker:
   ```bash
   docker compose up -d
   ```
   *(Scripts trong `../sql` sẽ tự động được nạp vào cơ sở dữ liệu `fschool_db`)*

3. Khởi chạy ứng dụng Spring Boot:
   ```bash
   ./gradlew bootRun
   # Trên Windows (cmd / PowerShell):
   .\gradlew.bat bootRun
   ```
4. Swagger UI / OpenAPI docs: `http://localhost:8080/swagger-ui.html`

---

## 2. Khởi chạy Mobile (Flutter)

### Yêu cầu:
- Flutter SDK 3.13+
- Android Studio / VS Code (Flutter extension)

### Các bước thực hiện:
1. Di chuyển vào thư mục `mobile`:
   ```bash
   cd mobile
   ```
2. Cài đặt dependencies:
   ```bash
   flutter pub get
   ```
3. Chạy kiểm thử:
   ```bash
   flutter test
   ```
4. Khởi chạy ứng dụng:
   ```bash
   flutter run
   ```

*Lưu ý khi kết nối API từ Mobile tới Backend:*
- Chạy trên **Android Emulator**: API trỏ về `http://10.0.2.2:8080/api/v1`
- Chạy trên **Thiết bị thật (Physical device)**: Đổi IP về địa chỉ mạng LAN của máy chạy backend (vd: `http://192.168.1.x:8080/api/v1`)
- Chạy trên **Web / Desktop / iOS Simulator**: Trỏ về `http://localhost:8080/api/v1`
