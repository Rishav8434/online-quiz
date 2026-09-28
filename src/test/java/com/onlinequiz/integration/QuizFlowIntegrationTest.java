package com.onlinequiz.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlinequiz.dto.request.*;
import com.onlinequiz.dto.response.AttemptResponse;
import com.onlinequiz.dto.response.AttemptResultResponse;
import com.onlinequiz.dto.response.AuthResponse;
import com.onlinequiz.dto.response.QuizResponse;
import com.onlinequiz.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class QuizFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmailService emailService;

    @Test
    @DisplayName("End-to-End Flow: Register -> Login -> Create Quiz -> Publish -> Attempt -> Submit -> Result")
    void testCompleteQuizLifecycleFlow() throws Exception {
        // 1. Register Admin
        RegisterRequest adminRegister = new RegisterRequest("e2e_admin", "admin@e2e.com", "Admin@123456", "E2E Admin", "ROLE_ADMIN");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminRegister)))
                .andExpect(status().isCreated());

        // 2. Login Admin & Obtain Token
        LoginRequest adminLogin = new LoginRequest("e2e_admin", "Admin@123456");
        MvcResult adminLoginRes = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminLogin)))
                .andExpect(status().isOk())
                .andReturn();
        AuthResponse adminAuth = objectMapper.readValue(adminLoginRes.getResponse().getContentAsString(), AuthResponse.class);
        String adminToken = "Bearer " + adminAuth.getAccessToken();

        // 3. Register Participant
        RegisterRequest userRegister = new RegisterRequest("e2e_student", "student@e2e.com", "Student@123456", "E2E Student", "ROLE_PARTICIPANT");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegister)))
                .andExpect(status().isCreated());

        // 4. Login Participant & Obtain Token
        LoginRequest userLogin = new LoginRequest("e2e_student", "Student@123456");
        MvcResult userLoginRes = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userLogin)))
                .andExpect(status().isOk())
                .andReturn();
        AuthResponse userAuth = objectMapper.readValue(userLoginRes.getResponse().getContentAsString(), AuthResponse.class);
        String userToken = "Bearer " + userAuth.getAccessToken();

        // 5. Admin Creates Quiz with Negative Marking configured
        CreateOptionRequest opt1 = new CreateOptionRequest("Jakarta EE", true);
        CreateOptionRequest opt2 = new CreateOptionRequest("C++ library", false);
        CreateQuestionRequest q1 = new CreateQuestionRequest("What is Spring Boot?", 4.0, 1.0, List.of(opt1, opt2));

        CreateQuizRequest createQuiz = new CreateQuizRequest("E2E Integration Quiz", "Full integration test quiz", 20, List.of(q1));
        createQuiz.setNegativeMarking(true);
        createQuiz.setNegativeMarks(1.0);
        createQuiz.setPassPercentage(50.0);

        MvcResult createQuizRes = mockMvc.perform(post("/api/quizzes")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createQuiz)))
                .andExpect(status().isCreated())
                .andReturn();
        QuizResponse quizResponse = objectMapper.readValue(createQuizRes.getResponse().getContentAsString(), QuizResponse.class);
        Long quizId = quizResponse.getId();
        Long questionId = quizResponse.getQuestions().get(0).getId();
        Long correctOptionId = quizResponse.getQuestions().get(0).getOptions().stream()
                .filter(o -> Boolean.TRUE.equals(o.getIsCorrect()))
                .findFirst().orElseThrow().getId();

        // 6. Admin Publishes Quiz
        mockMvc.perform(post("/api/quizzes/" + quizId + "/publish")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());

        // 7. Participant Starts Quiz Attempt
        MvcResult startAttemptRes = mockMvc.perform(post("/api/quizzes/" + quizId + "/start")
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andReturn();
        AttemptResponse attemptResponse = objectMapper.readValue(startAttemptRes.getResponse().getContentAsString(), AttemptResponse.class);
        Long attemptId = attemptResponse.getAttemptId();
        assertTrue(attemptResponse.getRemainingSeconds() > 0);

        // 8. Participant Submits Correct Answer
        SubmitAnswerRequest ans = new SubmitAnswerRequest(questionId, correctOptionId);
        SubmitQuizRequest submitRequest = new SubmitQuizRequest(List.of(ans));

        MvcResult submitRes = mockMvc.perform(post("/api/quizzes/" + quizId + "/submit?attemptId=" + attemptId)
                        .header("Authorization", userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(submitRequest)))
                .andExpect(status().isOk())
                .andReturn();
        AttemptResultResponse resultResponse = objectMapper.readValue(submitRes.getResponse().getContentAsString(), AttemptResultResponse.class);

        assertEquals(4.0, resultResponse.getScore());
        assertEquals(4.0, resultResponse.getMaxScore());
        assertEquals(100.0, resultResponse.getPercentage());
        assertEquals(1, resultResponse.getCorrectAnswers());
        assertTrue(resultResponse.isPassed());

        // 9. Participant Retrieves Result
        MvcResult getResultRes = mockMvc.perform(get("/api/results/" + attemptId)
                        .header("Authorization", userToken))
                .andExpect(status().isOk())
                .andReturn();
        AttemptResultResponse retrievedResult = objectMapper.readValue(getResultRes.getResponse().getContentAsString(), AttemptResultResponse.class);
        assertEquals(4.0, retrievedResult.getScore());
    }
}
