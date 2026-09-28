package com.onlinequiz.config;

import com.onlinequiz.entity.*;
import com.onlinequiz.enums.AttemptStatus;
import com.onlinequiz.enums.QuizStatus;
import com.onlinequiz.enums.RoleType;
import com.onlinequiz.repository.QuizAttemptRepository;
import com.onlinequiz.repository.QuizRepository;
import com.onlinequiz.repository.RoleRepository;
import com.onlinequiz.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository attemptRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           QuizRepository quizRepository,
                           QuizAttemptRepository attemptRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.quizRepository = quizRepository;
        this.attemptRepository = attemptRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Initialize Roles
        Role adminRole = roleRepository.findByName(RoleType.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(RoleType.ROLE_ADMIN)));

        Role participantRole = roleRepository.findByName(RoleType.ROLE_PARTICIPANT)
                .orElseGet(() -> roleRepository.save(new Role(RoleType.ROLE_PARTICIPANT)));

        // 2. Initialize Default Admin User
        User adminUser = userRepository.findByUsername("admin").orElseGet(() -> {
            User admin = new User(
                    "admin",
                    "admin@onlinequiz.com",
                    passwordEncoder.encode("Admin@123456"),
                    "System Administrator"
            );
            admin.setRoles(Set.of(adminRole, participantRole));
            User saved = userRepository.save(admin);
            logger.info("Initialized default admin user: admin / Admin@123456");
            return saved;
        });

        // 3. Initialize Default Participant User
        User participantUser = userRepository.findByUsername("john").orElseGet(() -> {
            User participant = new User(
                    "john",
                    "john@onlinequiz.com",
                    passwordEncoder.encode("John@123456"),
                    "John Doe"
            );
            participant.setRoles(Set.of(participantRole));
            User saved = userRepository.save(participant);
            logger.info("Initialized default participant user: john / John@123456");
            return saved;
        });

        // 4. Initialize Seed Quizzes if none exist
        if (quizRepository.count() == 0) {
            // --- QUIZ 1: Java 21 & Spring Boot 3 ---
            Quiz javaQuiz = new Quiz(
                    "Java 21 & Spring Boot 3 Architecture",
                    "Demonstrate mastery of modern Java 21 features (Virtual Threads, Pattern Matching, Records) and Spring Boot 3 enterprise fundamentals.",
                    5,
                    adminUser
            );
            javaQuiz.setStatus(QuizStatus.PUBLISHED);

            Question q1 = new Question("Which feature was finalized in Java 21 for lightweight concurrency?", javaQuiz);
            Option q1_opt1 = new Option("Virtual Threads (Project Loom)", true, q1);
            q1.addOption(q1_opt1);
            q1.addOption(new Option("Reactive Streams", false, q1));
            q1.addOption(new Option("Async/Await Syntax", false, q1));
            q1.addOption(new Option("Coroutines", false, q1));
            javaQuiz.addQuestion(q1);

            Question q2 = new Question("What is the minimum Java baseline required by Spring Boot 3.x?", javaQuiz);
            q2.addOption(new Option("Java 11", false, q2));
            Option q2_opt2 = new Option("Java 17", true, q2);
            q2.addOption(q2_opt2);
            q2.addOption(new Option("Java 21", false, q2));
            q2.addOption(new Option("Java 8", false, q2));
            javaQuiz.addQuestion(q2);

            Question q3 = new Question("Which pattern matching enhancement was officially finalized in Java 21?", javaQuiz);
            Option q3_opt1 = new Option("Pattern Matching for switch expressions and statements", true, q3);
            q3.addOption(q3_opt1);
            q3.addOption(new Option("Regular expression matching in String literals", false, q3));
            q3.addOption(new Option("Dynamic dispatch pattern matching", false, q3));
            q3.addOption(new Option("XML tag pattern matching", false, q3));
            javaQuiz.addQuestion(q3);

            Question q4 = new Question("In Spring Security 6, which class replaces WebSecurityConfigurerAdapter?", javaQuiz);
            Option q4_opt1 = new Option("SecurityFilterChain bean", true, q4);
            q4.addOption(q4_opt1);
            q4.addOption(new Option("WebSecurityBuilder", false, q4));
            q4.addOption(new Option("SecurityConfigurationAdapter", false, q4));
            q4.addOption(new Option("HttpFirewall", false, q4));
            javaQuiz.addQuestion(q4);

            javaQuiz = quizRepository.save(javaQuiz);
            logger.info("Initialized seed quiz: Java 21 & Spring Boot 3 Architecture");

            // --- QUIZ 2: Full-Stack JavaScript & CSS ---
            Quiz webQuiz = new Quiz(
                    "Modern Full-Stack JavaScript & Web Standards",
                    "Evaluate your knowledge of modern JavaScript ES2024, CSS Grid, Flexbox, Web Workers, and DOM performance optimization.",
                    8,
                    adminUser
            );
            webQuiz.setStatus(QuizStatus.PUBLISHED);

            Question w1 = new Question("Which CSS display property creates a two-dimensional grid layout system?", webQuiz);
            Option w1_opt1 = new Option("display: grid", true, w1);
            w1.addOption(w1_opt1);
            w1.addOption(new Option("display: flex", false, w1));
            w1.addOption(new Option("display: inline-block", false, w1));
            w1.addOption(new Option("display: table-row", false, w1));
            webQuiz.addQuestion(w1);

            Question w2 = new Question("In JavaScript, which method creates a deep copy of a structured cloneable object natively?", webQuiz);
            Option w2_opt1 = new Option("structuredClone()", true, w2);
            w2.addOption(w2_opt1);
            w2.addOption(new Option("Object.assign()", false, w2));
            w2.addOption(new Option("JSON.copy()", false, w2));
            w2.addOption(new Option("Object.freeze()", false, w2));
            webQuiz.addQuestion(w2);

            Question w3 = new Question("What does the CSS property 'backdrop-filter: blur()' achieve?", webQuiz);
            Option w3_opt1 = new Option("Blurs the area behind an element (frosted glass effect)", true, w3);
            w3.addOption(w3_opt1);
            w3.addOption(new Option("Blurs the element's inner text", false, w3));
            w3.addOption(new Option("Hides the background image", false, w3));
            w3.addOption(new Option("Creates an outer shadow", false, w3));
            webQuiz.addQuestion(w3);

            Question w4 = new Question("Which Promise combinator short-circuits as soon as any promise fulfills or rejects?", webQuiz);
            w4.addOption(new Option("Promise.all()", false, w4));
            w4.addOption(new Option("Promise.allSettled()", false, w4));
            Option w4_opt3 = new Option("Promise.race()", true, w4);
            w4.addOption(w4_opt3);
            w4.addOption(new Option("Promise.any()", false, w4));
            webQuiz.addQuestion(w4);

            webQuiz = quizRepository.save(webQuiz);
            logger.info("Initialized seed quiz: Modern Full-Stack JavaScript & Web Standards");

            // --- QUIZ 3: Database Engineering & SQL Mastery ---
            Quiz sqlQuiz = new Quiz(
                    "Database Engineering & SQL Performance Mastery",
                    "Test your expertise in relational indexing, B-Tree vs Hash indices, ACID transaction isolation levels, and query optimization.",
                    10,
                    adminUser
            );
            sqlQuiz.setStatus(QuizStatus.PUBLISHED);

            Question s1 = new Question("Which SQL isolation level prevents Dirty Reads, Non-Repeatable Reads, and Phantom Reads?", sqlQuiz);
            s1.addOption(new Option("SERIALIZABLE", true, s1));
            s1.addOption(new Option("REPEATABLE READ", false, s1));
            s1.addOption(new Option("READ COMMITTED", false, s1));
            s1.addOption(new Option("READ UNCOMMITTED", false, s1));
            sqlQuiz.addQuestion(s1);

            Question s2 = new Question("What is the primary advantage of a Covering Index in MySQL / InnoDB?", sqlQuiz);
            s2.addOption(new Option("The query can be satisfied completely from the index without looking up rows in the clustered table", true, s2));
            s2.addOption(new Option("It takes zero disk space", false, s2));
            s2.addOption(new Option("It automatically partitions tables across servers", false, s2));
            s2.addOption(new Option("It encrypts column data", false, s2));
            sqlQuiz.addQuestion(s2);

            Question s3 = new Question("Which SQL statement clause is evaluated BEFORE the SELECT column list and aggregate functions?", sqlQuiz);
            s3.addOption(new Option("WHERE and GROUP BY", true, s3));
            s3.addOption(new Option("ORDER BY", false, s3));
            s3.addOption(new Option("LIMIT / OFFSET", false, s3));
            s3.addOption(new Option("HAVING (after SELECT)", false, s3));
            sqlQuiz.addQuestion(s3);

            quizRepository.save(sqlQuiz);
            logger.info("Initialized seed quiz: Database Engineering & SQL Performance Mastery");

            // --- QUIZ 4: Cloud Native DevOps & Containers ---
            Quiz cloudQuiz = new Quiz(
                    "Cloud Native DevOps, Docker & Kubernetes",
                    "Assess containerization principles, multi-stage Docker builds, Kubernetes Pod lifecycles, and CI/CD pipelines.",
                    6,
                    adminUser
            );
            cloudQuiz.setStatus(QuizStatus.PUBLISHED);

            Question c1 = new Question("What is the primary benefit of multi-stage Docker builds?", cloudQuiz);
            c1.addOption(new Option("Produces much smaller, secure production images by excluding build tools and SDKs", true, c1));
            c1.addOption(new Option("Allows running multiple OS kernels in one container", false, c1));
            c1.addOption(new Option("Bypasses container networking", false, c1));
            c1.addOption(new Option("Replaces Kubernetes pods", false, c1));
            cloudQuiz.addQuestion(c1);

            Question c2 = new Question("In Kubernetes, which controller manages stateful applications with stable network identities and persistent storage?", cloudQuiz);
            c2.addOption(new Option("StatefulSet", true, c2));
            c2.addOption(new Option("Deployment", false, c2));
            c2.addOption(new Option("DaemonSet", false, c2));
            c2.addOption(new Option("ReplicaSet", false, c2));
            cloudQuiz.addQuestion(c2);

            quizRepository.save(cloudQuiz);
            logger.info("Initialized seed quiz: Cloud Native DevOps, Docker & Kubernetes");

            // 5. Seed initial completed attempts for participant "john" so dashboards are populated
            if (attemptRepository.count() == 0) {
                // Attempt 1: Java Quiz (100% Score)
                LocalDateTime t1Start = LocalDateTime.now().minusHours(3);
                QuizAttempt att1 = new QuizAttempt(javaQuiz, participantUser, t1Start);
                att1.setEndTime(t1Start.plusMinutes(2).plusSeconds(34));
                att1.setStatus(AttemptStatus.SUBMITTED);
                att1.setTotalQuestions(4);
                att1.setAttemptedQuestions(4);
                att1.setCorrectAnswers(4);
                att1.setWrongAnswers(0);
                att1.setUnansweredQuestions(0);
                att1.setScore(4.0);
                att1.setMaxScore(4.0);
                att1.setPercentage(100.0);
                att1.setPassed(true);

                att1.addUserAnswer(new UserAnswer(att1, q1, q1_opt1, true, 1.0));
                att1.addUserAnswer(new UserAnswer(att1, q2, q2_opt2, true, 1.0));
                att1.addUserAnswer(new UserAnswer(att1, q3, q3_opt1, true, 1.0));
                att1.addUserAnswer(new UserAnswer(att1, q4, q4_opt1, true, 1.0));

                attemptRepository.save(att1);

                // Attempt 2: Web Quiz (75% Score)
                LocalDateTime t2Start = LocalDateTime.now().minusHours(1);
                QuizAttempt att2 = new QuizAttempt(webQuiz, participantUser, t2Start);
                att2.setEndTime(t2Start.plusMinutes(4).plusSeconds(12));
                att2.setStatus(AttemptStatus.SUBMITTED);
                att2.setTotalQuestions(4);
                att2.setAttemptedQuestions(4);
                att2.setCorrectAnswers(3);
                att2.setWrongAnswers(1);
                att2.setUnansweredQuestions(0);
                att2.setScore(3.0);
                att2.setMaxScore(4.0);
                att2.setPercentage(75.0);
                att2.setPassed(true);

                att2.addUserAnswer(new UserAnswer(att2, w1, w1_opt1, true, 1.0));
                att2.addUserAnswer(new UserAnswer(att2, w2, w2_opt1, true, 1.0));
                att2.addUserAnswer(new UserAnswer(att2, w3, w3_opt1, true, 1.0));
                att2.addUserAnswer(new UserAnswer(att2, w4, null, false, 0.0));

                attemptRepository.save(att2);

                logger.info("Initialized 2 sample completed quiz attempts for user: john");
            }
        }
    }
}
