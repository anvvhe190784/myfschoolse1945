package vn.edu.fpt.fschool.mapper;

import org.mapstruct.Mapper;
import vn.edu.fpt.fschool.dto.AdminDtos.UserResponse;
import vn.edu.fpt.fschool.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toUserResponse(User user);
    List<UserResponse> toUserResponseList(List<User> users);
}
