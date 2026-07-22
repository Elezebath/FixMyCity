package lv.acnbootcamp.fixmycity.service;

import lv.acnbootcamp.fixmycity.dto.user.UserAdminResponse;
import lv.acnbootcamp.fixmycity.entity.user.Role;
import lv.acnbootcamp.fixmycity.entity.user.User;
import lv.acnbootcamp.fixmycity.exception.user.CompanyAlreadyExistsException;
import lv.acnbootcamp.fixmycity.exception.user.EmailAlreadyExistsException;
import lv.acnbootcamp.fixmycity.exception.user.UserNotFoundException;
import lv.acnbootcamp.fixmycity.repository.CategoryRepository;
import lv.acnbootcamp.fixmycity.repository.CompanyRepository;
import lv.acnbootcamp.fixmycity.repository.UserRepository;
import lv.acnbootcamp.fixmycity.service.AuditLogService;
import lv.acnbootcamp.fixmycity.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import lv.acnbootcamp.fixmycity.dto.company.CompanyCreateRequest;
import lv.acnbootcamp.fixmycity.entity.Category;
import lv.acnbootcamp.fixmycity.entity.Company;
import lv.acnbootcamp.fixmycity.exception.category.CategoryNotFoundException;
import lv.acnbootcamp.fixmycity.repository.CategoryRepository;
import lv.acnbootcamp.fixmycity.repository.CompanyRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    // for internal use of userService.createUser(), userService.updateUserRole()
    @Mock
    private AuditLogService auditLogService;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = User.builder()
                .id(1L)
                .email("citizen@example.com")
                .password("hashed-password")
                .fullName("Jonas Jonaitis")
                .role(Role.CITIZEN)
                .enabled(true)
                .createdAt(LocalDateTime.of(2026, 1, 1, 12, 0))
                .build();
    }

    @Nested
    class GetAllUsers {

        @Test
        void returnsAllUsersMappedToResponse() {
            User secondUser = User.builder()
                    .id(2L)
                    .email("admin@example.com")
                    .password("hashed-password")
                    .fullName("Admin Vardenis")
                    .role(Role.ADMIN)
                    .enabled(true)
                    .createdAt(LocalDateTime.of(2026, 2, 1, 9, 0))
                    .build();

            when(userRepository.findAll()).thenReturn(List.of(existingUser, secondUser));

            List<UserAdminResponse> result = userService.getAllUsers();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getEmail()).isEqualTo("citizen@example.com");
            assertThat(result.get(1).getRole()).isEqualTo(Role.ADMIN);
        }

        @Test
        void returnsEmptyListWhenNoUsersExist() {
            when(userRepository.findAll()).thenReturn(List.of());

            List<UserAdminResponse> result = userService.getAllUsers();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class GetUserById {

        @Test
        void returnsMappedResponseWhenUserExists() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

            UserAdminResponse response = userService.getUserById(1L);

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getEmail()).isEqualTo("citizen@example.com");
            assertThat(response.getFullName()).isEqualTo("Jonas Jonaitis");
            assertThat(response.getRole()).isEqualTo(Role.CITIZEN);
            assertThat(response.isEnabled()).isTrue();
        }

        @Test
        void throwsUserNotFoundExceptionWhenUserDoesNotExist() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(99L))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("99");

            verify(userRepository, never()).save(any());
        }

        @Test
        void returnsNullCompanyIdWhenUserHasNoCompany(){
            existingUser.setCompany(null);

            when(userRepository.findById(1L))
                    .thenReturn(Optional.of(existingUser));
            UserAdminResponse response = userService.getUserById(1L);

            assertThat(response.getCompanyId()).isNull();
        }

        @Test
        void returnsCompanyIdWhenUserHasCompany(){
            Company company = Company.builder()
                    .companyId(5L)
                    .build();

            existingUser.setCompany(company);

            when(userRepository.findById(1L))
                    .thenReturn(Optional.of(existingUser));
            UserAdminResponse response = userService.getUserById(1L);

            assertThat(response.getCompanyId()).isEqualTo(5L);
        }
    }

    @Nested
    class UpdateUserRole {

        @Test
        void updatesRoleAndPersistsChange() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UserAdminResponse response = userService.updateUserRole(1L, Role.ADMIN);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());

            assertThat(captor.getValue().getRole()).isEqualTo(Role.ADMIN);
            assertThat(response.getRole()).isEqualTo(Role.ADMIN);
        }

        @Test
        void throwsUserNotFoundExceptionWhenUserDoesNotExist() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUserRole(99L, Role.ADMIN))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    class UpdateUserStatus {

        @Test
        void disablesUserAndPersistsChange() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UserAdminResponse response = userService.updateUserStatus(1L, false);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());

            assertThat(captor.getValue().isEnabled()).isFalse();
            assertThat(response.isEnabled()).isFalse();
        }

        @Test
        void throwsUserNotFoundExceptionWhenUserDoesNotExist() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUserStatus(99L, true))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    class CreateUser {

        @Test
        void createsUserWithEncodedPasswordAndGivenRole() {
            when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(passwordEncoder.encode("plainPassword")).thenReturn("encoded-password");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User u = invocation.getArgument(0);
                u.setId(5L);
                return u;
            });

            UserAdminResponse response = userService.createUser(
                    "new@example.com", "plainPassword", "New Person", Role.MANAGER, null);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            verify(companyRepository, never()).save(any());
            verify(categoryRepository, never()).findByCategoryIdAndIsDeletedFalse(any());

            User saved = captor.getValue();
            assertThat(saved.getPassword()).isEqualTo("encoded-password");
            assertThat(saved.isEnabled()).isTrue();
            assertThat(saved.getRole()).isEqualTo(Role.MANAGER);

            assertThat(response.getEmail()).isEqualTo("new@example.com");
            assertThat(response.getRole()).isEqualTo(Role.MANAGER);
        }

        @Test
        void throwsEmailAlreadyExistsExceptionWhenEmailTaken() {
            when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

            assertThatThrownBy(() ->
                    userService.createUser("taken@example.com", "pass1234", "Someone", Role.CITIZEN, null))
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessageContaining("taken@example.com");

            verify(userRepository, never()).save(any());
            verify(passwordEncoder, never()).encode(any());
        }

        @Test
        void createsCompanyWhenRoleIsCompany() {
            CompanyCreateRequest request = new CompanyCreateRequest();
            request.setCompanyName("Test Company");
            request.setRegistrationNo("123456");
            request.setCategoryId(1L);
            request.setContactEmail("company@test.com");
            request.setContactPhone("+37120000000");
            request.setAddress("Riga");

            Category category = Category.builder()
                    .categoryId(1L)
                    .name("Roads")
                    .description("Road issues")
                    .build();

            when(userRepository.existsByEmail("company@example.com")).thenReturn(false);
            when(passwordEncoder.encode("password")).thenReturn("encoded");

            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(10L);
                return user;
            });

            when(categoryRepository.findByCategoryIdAndIsDeletedFalse(1L))
                    .thenReturn(Optional.of(category));

            when(companyRepository.save(any(Company.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            userService.createUser(
                    "company@example.com",
                    "password",
                    "Company User",
                    Role.COMPANY,
                    request);

            ArgumentCaptor<Company> companyCaptor = ArgumentCaptor.forClass(Company.class);

            verify(companyRepository).save(companyCaptor.capture());
            verify(categoryRepository).findByCategoryIdAndIsDeletedFalse(1L);

            Company savedCompany = companyCaptor.getValue();

            assertThat(savedCompany.getCompanyName()).isEqualTo("Test Company");
            assertThat(savedCompany.getRegistrationNo()).isEqualTo("123456");
            assertThat(savedCompany.getCategory()).isEqualTo(category);
            assertThat(savedCompany.getUser().getId()).isEqualTo(10L);
            assertThat(savedCompany.getActive()).isTrue();
        }

        @Test
        void throwsCategoryNotFoundExceptionWhenCategoryDoesNotExist() {
            CompanyCreateRequest request = new CompanyCreateRequest();
                    request.setCompanyName("Test Company");
                    request.setRegistrationNo("123456");
                    request.setCategoryId(99L);

            when(userRepository.existsByEmail("company@example.com")).thenReturn(false);
            when(passwordEncoder.encode("password")).thenReturn("encoded");

            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(10L);
                return user;
            });

            when(categoryRepository.findByCategoryIdAndIsDeletedFalse(99L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    userService.createUser(
                            "company@example.com",
                            "password",
                            "Company User",
                            Role.COMPANY,
                            request))
                    .isInstanceOf(CategoryNotFoundException.class);

            verify(companyRepository, never()).save(any());
            verify(userRepository).save(any(User.class));
            verify(companyRepository, never()).save(any());
        }

        @Test
        void throwsCompanyAlreadyExistsExceptionWhenCompanyNameAlreadyExists() {
            CompanyCreateRequest request = new CompanyCreateRequest();
            request.setCompanyName("Test Company");
            request.setRegistrationNo("123456");
            request.setCategoryId(1L);
            request.setContactEmail("company@test.com");
            request.setContactPhone("+37120000000");
            request.setAddress("Riga");

            when(userRepository.existsByEmail("company@example.com")).thenReturn(false);

            when(companyRepository.existsByCompanyNameIgnoreCase("Test Company"))
                    .thenReturn(true);

            assertThatThrownBy(() ->
                    userService.createUser(
                            "company@example.com",
                            "password",
                            "Company User",
                            Role.COMPANY,
                            request))
                    .isInstanceOf(CompanyAlreadyExistsException.class);

            verify(userRepository, never()).save(any());
            verify(categoryRepository, never()).findByCategoryIdAndIsDeletedFalse(any());
            verify(companyRepository, never()).save(any());
        }
    }

    @Nested
    class UpdateUserProfile {

        @Test
        void updatesEmailAndFullName() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
            when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UserAdminResponse response = userService.updateUserProfile(1L, "updated@example.com", "Updated Name");

            assertThat(response.getEmail()).isEqualTo("updated@example.com");
            assertThat(response.getFullName()).isEqualTo("Updated Name");
        }

        @Test
        void allowsKeepingSameEmail() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UserAdminResponse response = userService.updateUserProfile(
                    1L, "citizen@example.com", "New Full Name");

            assertThat(response.getEmail()).isEqualTo("citizen@example.com");
            verify(userRepository, never()).existsByEmail(any());
        }

        @Test
        void throwsEmailAlreadyExistsExceptionWhenNewEmailTakenByAnotherUser() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
            when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

            assertThatThrownBy(() ->
                    userService.updateUserProfile(1L, "taken@example.com", "Whatever"))
                    .isInstanceOf(EmailAlreadyExistsException.class);

            verify(userRepository, never()).save(any());
        }

        @Test
        void throwsUserNotFoundExceptionWhenUserDoesNotExist() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUserProfile(99L, "x@example.com", "X"))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }
}