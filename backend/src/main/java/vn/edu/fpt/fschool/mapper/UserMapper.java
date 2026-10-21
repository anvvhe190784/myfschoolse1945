package vn.edu.fpt.fschool.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import vn.edu.fpt.fschool.dto.AdminDtos.UserResponse;
import vn.edu.fpt.fschool.dto.AuthDtos.UserMeResponse;
import vn.edu.fpt.fschool.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
  UserResponse toUserResponse(User user);

  List<UserResponse> toUserResponseList(List<User> users);

  UserMeResponse toUserMeResponse(User user);
}
