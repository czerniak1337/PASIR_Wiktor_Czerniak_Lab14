package pk.wc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import pk.wc.pasir_wiktor_czerniak.dto.LoginDto;
import pk.wc.pasir_wiktor_czerniak.dto.UserDto;
import pk.wc.pasir_wiktor_czerniak.repository.UserRepository;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testy integracyjne dla AuthController.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    private static final String TEST_USERNAME =
            "test_user_integration";

    private static final String TEST_PASSWORD =
            "SecurePassword123";

    @BeforeEach
    void setUp() {

        mockMvc =
                MockMvcBuilders
                        .webAppContextSetup(
                                webApplicationContext
                        )
                        .build();

        userRepository.deleteAll();
    }

    private String generateUniqueEmail() {

        return "test_" +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8)
                + "@pk.pl";
    }

    @Test
    @Order(1)
    @DisplayName(
            "Powinien zarejestrować nowego użytkownika"
    )
    void shouldRegisterNewUser() throws Exception {

        String email =
                generateUniqueEmail();

        UserDto userDto =
                new UserDto();

        userDto.setUsername(
                TEST_USERNAME
        );

        userDto.setEmail(email);

        userDto.setPassword(
                TEST_PASSWORD
        );

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        userDto
                                                )
                                )
                )

                .andDo(print())

                .andExpect(status().isOk())

                .andExpect(
                        jsonPath("$.id").exists()
                )

                .andExpect(
                        jsonPath("$.username")
                                .value(TEST_USERNAME)
                )

                .andExpect(
                        jsonPath("$.email")
                                .value(email)
                )

                .andExpect(
                        jsonPath("$.password")
                                .exists()
                )

                .andExpect(
                        jsonPath("$.password")
                                .value(
                                        not(TEST_PASSWORD)
                                )
                );
    }

    @Test
    @Order(2)
    @DisplayName(
            "Powinien zalogować użytkownika i zwrócić JWT"
    )
    void shouldLoginAndReturnJwtToken()
            throws Exception {

        String email =
                generateUniqueEmail();

        UserDto userDto =
                new UserDto();

        userDto.setUsername(
                TEST_USERNAME
        );

        userDto.setEmail(email);

        userDto.setPassword(
                TEST_PASSWORD
        );

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(
                                objectMapper
                                        .writeValueAsString(
                                                userDto
                                        )
                        )
        );

        LoginDto loginDto =
                new LoginDto();

        loginDto.setEmail(email);

        loginDto.setPassword(
                TEST_PASSWORD
        );

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        loginDto
                                                )
                                )
                )

                .andDo(print())

                .andExpect(status().isOk())

                .andExpect(
                        jsonPath("$.token")
                                .exists()
                )

                .andExpect(
                        jsonPath("$.token")
                                .value(
                                        matchesPattern(
                                                "^[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_.+/=]*$"
                                        )
                                )
                );
    }

    @Test
    @Order(3)
    @DisplayName(
            "Powinien zwrócić 401 przy złym haśle"
    )
    void shouldReturn401WhenLoginWithWrongPassword()
            throws Exception {

        String email =
                generateUniqueEmail();

        UserDto userDto =
                new UserDto();

        userDto.setUsername(
                TEST_USERNAME
        );

        userDto.setEmail(email);

        userDto.setPassword(
                TEST_PASSWORD
        );

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .content(
                                objectMapper
                                        .writeValueAsString(
                                                userDto
                                        )
                        )
        );

        LoginDto loginDto =
                new LoginDto();

        loginDto.setEmail(email);

        loginDto.setPassword(
                "WrongPassword"
        );

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper
                                                .writeValueAsString(
                                                        loginDto
                                                )
                                )
                )

                .andDo(print())

                .andExpect(
                        status().isUnauthorized()
                );
    }
}