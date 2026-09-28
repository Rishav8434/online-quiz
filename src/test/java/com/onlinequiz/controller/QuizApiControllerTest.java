package com.onlinequiz.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onlinequiz.controller.api.QuizApiController;
import com.onlinequiz.dto.response.QuizSummaryResponse;
import com.onlinequiz.enums.QuizStatus;
import com.onlinequiz.security.CustomUserDetailsService;
import com.onlinequiz.security.JwtAuthenticationEntryPoint;
import com.onlinequiz.security.JwtTokenProvider;
import com.onlinequiz.service.QuestionService;
import com.onlinequiz.service.QuizService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = QuizApiController.class)
@AutoConfigureMockMvc(addFilters = false)
class QuizApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QuizService quizService;

    @MockBean
    private QuestionService questionService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @DisplayName("GET /api/quizzes - Should return paginated list of published quizzes")
    void testGetPublishedQuizzes() throws Exception {
        QuizSummaryResponse summary = new QuizSummaryResponse(1L, "Java 21 Mastery", "Test description", 15, 5, 20.0, QuizStatus.PUBLISHED, false, 50.0, "Admin", LocalDateTime.now());
        when(quizService.getPublishedQuizzes(any(), any())).thenReturn(new PageImpl<>(List.of(summary)));

        mockMvc.perform(get("/api/quizzes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("Java 21 Mastery"));
    }
}
