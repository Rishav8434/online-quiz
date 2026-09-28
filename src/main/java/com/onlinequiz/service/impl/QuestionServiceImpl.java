package com.onlinequiz.service.impl;

import com.onlinequiz.dto.request.CreateOptionRequest;
import com.onlinequiz.dto.request.CreateQuestionRequest;
import com.onlinequiz.dto.request.UpdateQuestionRequest;
import com.onlinequiz.dto.response.QuestionResponse;
import com.onlinequiz.entity.Option;
import com.onlinequiz.entity.Question;
import com.onlinequiz.entity.Quiz;
import com.onlinequiz.exception.BadRequestException;
import com.onlinequiz.exception.ResourceNotFoundException;
import com.onlinequiz.mapper.QuizMapper;
import com.onlinequiz.repository.OptionRepository;
import com.onlinequiz.repository.QuestionRepository;
import com.onlinequiz.repository.QuizRepository;
import com.onlinequiz.service.QuestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuestionServiceImpl implements QuestionService {

    private static final Logger log = LoggerFactory.getLogger(QuestionServiceImpl.class);

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final QuizMapper quizMapper;

    public QuestionServiceImpl(QuizRepository quizRepository,
                               QuestionRepository questionRepository,
                               OptionRepository optionRepository,
                               QuizMapper quizMapper) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.quizMapper = quizMapper;
    }

    @Override
    @Transactional
    public QuestionResponse addQuestionToQuiz(Long quizId, CreateQuestionRequest request, Long userId) {
        Quiz quiz = quizRepository.findByIdAndIsDeletedFalse(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", "id", quizId));

        validateOptions(request.getOptions());

        Double marks = request.getMarks() != null ? request.getMarks() : 1.0;
        Double negativeMarks = request.getNegativeMarks() != null ? request.getNegativeMarks() : (quiz.isNegativeMarking() ? quiz.getNegativeMarks() : 0.0);

        Question question = new Question(request.getText(), marks, negativeMarks, quiz);

        for (CreateOptionRequest oReq : request.getOptions()) {
            Option option = new Option(oReq.getText(), Boolean.TRUE.equals(oReq.getIsCorrect()), question);
            question.addOption(option);
        }

        quiz.addQuestion(question);
        Question savedQuestion = questionRepository.save(question);
        quizRepository.save(quiz);

        log.info("Added question id={} to quiz id={} by userId={}", savedQuestion.getId(), quizId, userId);
        return quizMapper.toQuestionResponse(savedQuestion, true);
    }

    @Override
    @Transactional
    public QuestionResponse updateQuestion(Long questionId, UpdateQuestionRequest request, Long userId) {
        Question question = questionRepository.findByIdWithOptions(questionId)
                .filter(q -> !q.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Question", "id", questionId));

        validateOptions(request.getOptions());

        question.setText(request.getText());
        if (request.getMarks() != null) {
            question.setMarks(request.getMarks());
        }
        if (request.getNegativeMarks() != null) {
            question.setNegativeMarks(request.getNegativeMarks());
        }

        // Replace options
        question.getOptions().clear();
        for (CreateOptionRequest oReq : request.getOptions()) {
            Option option = new Option(oReq.getText(), Boolean.TRUE.equals(oReq.getIsCorrect()), question);
            question.addOption(option);
        }

        Question saved = questionRepository.save(question);
        Quiz quiz = question.getQuiz();
        if (quiz != null) {
            quiz.recalculateTotalMarks();
            quizRepository.save(quiz);
        }

        log.info("Updated question id={} by userId={}", questionId, userId);
        return quizMapper.toQuestionResponse(saved, true);
    }

    @Override
    @Transactional
    public void deleteQuestion(Long questionId, Long userId) {
        Question question = questionRepository.findById(questionId)
                .filter(q -> !q.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Question", "id", questionId));

        question.setDeleted(true);
        questionRepository.save(question);

        Quiz quiz = question.getQuiz();
        if (quiz != null) {
            quiz.recalculateTotalMarks();
            quizRepository.save(quiz);
        }

        log.info("Soft-deleted question id={} by userId={}", questionId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public QuestionResponse getQuestionById(Long questionId) {
        Question question = questionRepository.findByIdWithOptions(questionId)
                .filter(q -> !q.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Question", "id", questionId));
        return quizMapper.toQuestionResponse(question, true);
    }

    private void validateOptions(List<CreateOptionRequest> options) {
        if (options == null || options.size() < 2) {
            throw new BadRequestException("Question must have at least 2 options");
        }

        long correctCount = options.stream()
                .filter(opt -> Boolean.TRUE.equals(opt.getIsCorrect()))
                .count();

        if (correctCount == 0) {
            throw new BadRequestException("At least one option must be marked as correct");
        }
        if (correctCount > 1) {
            throw new BadRequestException("Standard MCQ mode requires exactly one correct option");
        }
    }
}
