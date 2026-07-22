package lv.acnbootcamp.fixmycity.service;

import lv.acnbootcamp.fixmycity.dto.company.CompanyCreateRequest;
import lv.acnbootcamp.fixmycity.dto.user.UserAdminResponse;
import lv.acnbootcamp.fixmycity.entity.user.Role;

import java.util.List;

public interface UserService {
    List<UserAdminResponse> getAllUsers();
    UserAdminResponse getUserById(Long id);
    UserAdminResponse createUser(String email, String rawPassword, String fullName, Role role,
                                 CompanyCreateRequest company);
    UserAdminResponse updateUserProfile(Long id, String email, String fullName);
    UserAdminResponse updateUserRole(Long id, Role newRole);
    UserAdminResponse updateUserStatus(Long id, boolean enabled);
}