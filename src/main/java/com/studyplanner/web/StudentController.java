package com.studyplanner.web;

import com.studyplanner.domain.Exam;
import com.studyplanner.domain.MockExam;
import com.studyplanner.domain.Quiz;
import com.studyplanner.domain.QuizAttempt;
import com.studyplanner.domain.Student;
import com.studyplanner.domain.StudyPlan;
import com.studyplanner.domain.StudySession;
import com.studyplanner.domain.User;
import com.studyplanner.repository.ExamRepository;
import com.studyplanner.repository.QuizAttemptRepository;
import com.studyplanner.repository.StudentRepository;
import com.studyplanner.repository.StudyPlanRepository;
import com.studyplanner.repository.UserRepository;
import com.studyplanner.service.AuthenticationService;
import com.studyplanner.service.QuizService;
import com.studyplanner.service.ReminderService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/student")
public class StudentController {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final ExamRepository examRepository;
    private final QuizService quizService;
    private final ReminderService reminderService;
    private final AuthenticationService authenticationService;
    private final QuizAttemptRepository quizAttemptRepository;

    public StudentController(UserRepository userRepository,
                             StudentRepository studentRepository,
                             StudyPlanRepository studyPlanRepository,
                             ExamRepository examRepository,
                             QuizService quizService,
                             ReminderService reminderService,
                             AuthenticationService authenticationService,
                             QuizAttemptRepository quizAttemptRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.studyPlanRepository = studyPlanRepository;
        this.examRepository = examRepository;
        this.quizService = quizService;
        this.reminderService = reminderService;
        this.authenticationService = authenticationService;
        this.quizAttemptRepository = quizAttemptRepository;
    }

    @GetMapping
    public String home(@AuthenticationPrincipal UserDetails principal, Model model) {
        Student student = requireStudent(principal);
        model.addAttribute("student", student);
        model.addAttribute("plans", studyPlanRepository.findByStudentOrderByTitleAsc(student));
        model.addAttribute("exams", examRepository.findByStudentOrderByDeadlineAsc(student));
        model.addAttribute("quizzes", quizService.listQuizzes());
        model.addAttribute("attempts", quizService.attemptsForStudent(student));
        model.addAttribute("progress", quizService.progressForStudent(student));
        model.addAttribute("weakTopics", quizService.weakTopics(student));
        model.addAttribute("reminders", reminderService.listForUser(student.getId()));
        return "student/home";
    }

    @PostMapping("/plans")
    public String addPlan(@AuthenticationPrincipal UserDetails principal,
                          @RequestParam String title) {
        Student student = requireStudent(principal);
        StudyPlan plan = new StudyPlan(title, student);
        student.getStudyPlans().add(plan);
        studyPlanRepository.save(plan);
        return "redirect:/student";
    }

    @PostMapping("/plans/{planId}/sessions")
    public String addSession(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long planId,
            @RequestParam String topic,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        Student student = requireStudent(principal);
        StudyPlan plan = studyPlanRepository.findById(planId).orElseThrow();
        if (!plan.getStudent().getId().equals(student.getId())) {
            throw new IllegalArgumentException("Plan not owned by student");
        }
        Instant s = start.toInstant(ZoneOffset.UTC);
        Instant e = end.toInstant(ZoneOffset.UTC);
        StudySession session = new StudySession(topic, s, e, plan);
        plan.getSessions().add(session);
        studyPlanRepository.save(plan);
        User user = userRepository.findById(student.getId()).orElseThrow();
        Instant remind = s.minusSeconds(3600);
        if (remind.isBefore(Instant.now())) {
            remind = Instant.now().plusSeconds(300);
        }
        reminderService.scheduleStudySessionReminder(user, session, remind);
        return "redirect:/student";
    }

    @PostMapping("/exams")
    public String addExam(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam String title,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime deadline
    ) {
        Student student = requireStudent(principal);
        Exam exam = new Exam(title, deadline.toInstant(ZoneOffset.UTC), student);
        student.getExams().add(exam);
        examRepository.save(exam);
        User user = userRepository.findById(student.getId()).orElseThrow();
        reminderService.scheduleDefaultExamReminder(user, exam);
        return "redirect:/student";
    }

    @GetMapping("/quizzes/{id}/take")
    public String takeQuiz(@PathVariable Long id, Model model) {
        Quiz quiz = quizService.getQuiz(id);
        model.addAttribute("quiz", quiz);
        model.addAttribute("isMock", quiz instanceof MockExam);
        return "student/quiz-take";
    }

    @PostMapping("/quizzes/{id}/submit")
    public String submitQuiz(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id,
            @RequestParam Map<String, String> allParams
    ) {
        Student student = requireStudent(principal);
        Quiz quiz = quizService.getQuiz(id);
        Map<Long, Integer> answers = allParams.entrySet().stream()
                .filter(e -> e.getKey().startsWith("q") && e.getKey().length() > 1)
                .collect(Collectors.toMap(
                        e -> Long.parseLong(e.getKey().substring(1)),
                        e -> Integer.parseInt(e.getValue())
                ));
        QuizAttempt attempt = quizService.submitQuiz(student, quiz, answers);
        return "redirect:/student/quizzes/result/" + attempt.getId();
    }

    @GetMapping("/quizzes/result/{attemptId}")
    public String result(@AuthenticationPrincipal UserDetails principal,
                         @PathVariable Long attemptId,
                         Model model) {
        Student student = requireStudent(principal);
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId).orElseThrow();
        if (!attempt.getStudent().getId().equals(student.getId())) {
            throw new IllegalArgumentException("Attempt not found");
        }
        model.addAttribute("attempt", attempt);
        return "student/quiz-result";
    }

    @PostMapping("/mock-exam")
    public String createMockExam(@RequestParam String topics) {
        List<String> list = Arrays.stream(topics.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        MockExam exam = quizService.createMockExam(list);
        return "redirect:/student/quizzes/" + exam.getId() + "/take";
    }

    private Student requireStudent(UserDetails principal) {
        User u = authenticationService.findByUsername(principal.getUsername());
        if (!(u instanceof Student s)) {
            throw new IllegalStateException("Student role required");
        }
        return studentRepository.findById(s.getId()).orElseThrow();
    }
}
