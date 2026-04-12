package com.studyplanner.web;

import com.studyplanner.domain.Intervention;
import com.studyplanner.domain.Progress;
import com.studyplanner.domain.Student;
import com.studyplanner.domain.StudyPlan;
import com.studyplanner.domain.Tutor;
import com.studyplanner.domain.User;
import com.studyplanner.repository.InterventionRepository;
import com.studyplanner.repository.StudentRepository;
import com.studyplanner.repository.StudyPlanRepository;
import com.studyplanner.repository.TutorRepository;
import com.studyplanner.service.AuthenticationService;
import com.studyplanner.service.quiz.QuestionBank;
import com.studyplanner.service.quiz.QuizCatalog;
import com.studyplanner.service.quiz.StudentProgressTracking;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/tutor")
public class TutorController {

    private final TutorRepository tutorRepository;
    private final StudentRepository studentRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final InterventionRepository interventionRepository;
    private final AuthenticationService authenticationService;
    private final QuizCatalog quizCatalog;
    private final QuestionBank questionBank;
    private final StudentProgressTracking studentProgressTracking;
    private final AjaxRedirect ajaxRedirect;

    public TutorController(TutorRepository tutorRepository,
                           StudentRepository studentRepository,
                           StudyPlanRepository studyPlanRepository,
                           InterventionRepository interventionRepository,
                           AuthenticationService authenticationService,
                           QuizCatalog quizCatalog,
                           QuestionBank questionBank,
                           StudentProgressTracking studentProgressTracking,
                           AjaxRedirect ajaxRedirect) {
        this.tutorRepository = tutorRepository;
        this.studentRepository = studentRepository;
        this.studyPlanRepository = studyPlanRepository;
        this.interventionRepository = interventionRepository;
        this.authenticationService = authenticationService;
        this.quizCatalog = quizCatalog;
        this.questionBank = questionBank;
        this.studentProgressTracking = studentProgressTracking;
        this.ajaxRedirect = ajaxRedirect;
    }

    @GetMapping
    public String home(@AuthenticationPrincipal UserDetails principal, Model model) {
        Tutor tutor = requireTutor(principal);
        List<Student> students = studentRepository.findAllByOrderByUsernameAsc();
        model.addAttribute("tutor", tutor);
        model.addAttribute("students", students);
        model.addAttribute("interventions", interventionRepository.findByTutorOrderByCreatedAtDesc(tutor));
        model.addAttribute("quizzes", quizCatalog.listQuizzes());
        return "tutor/home";
    }

    @PostMapping("/questions")
    public Object addQuestion(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam Long quizId,
            @RequestParam String text,
            @RequestParam String optionA,
            @RequestParam String optionB,
            @RequestParam String optionC,
            @RequestParam String optionD,
            @RequestParam int correctIndex,
            @RequestParam(required = false) String topic,
            @RequestHeader(value = AjaxFormHeader.NAME, required = false) String ajax
    ) {
        requireTutor(principal);
        questionBank.addQuestionToQuiz(quizId, text, optionA, optionB, optionC, optionD, correctIndex, topic);
        return ajaxRedirect.redirectOrJson(AjaxFormHeader.isAjax(ajax), "/tutor", "Question added");
    }

    @GetMapping("/students/{studentId}")
    public String studentDetail(@AuthenticationPrincipal UserDetails principal,
                                @PathVariable Long studentId,
                                Model model) {
        Tutor tutor = requireTutor(principal);
        Student student = studentRepository.findById(studentId).orElseThrow();
        List<Progress> progress = studentProgressTracking.progressForStudent(student);
        List<Progress> weak = studentProgressTracking.weakTopics(student);
        double classAvg = progress.isEmpty() ? 0
                : progress.stream().mapToDouble(Progress::getAverageScorePercent).average().orElse(0);
        model.addAttribute("tutor", tutor);
        model.addAttribute("student", student);
        model.addAttribute("progress", progress);
        model.addAttribute("weakTopics", weak);
        model.addAttribute("studentAvg", classAvg);
        model.addAttribute("interventions", interventionRepository.findByStudentOrderByCreatedAtDesc(student));
        return "tutor/student-detail";
    }

    @PostMapping("/interventions")
    public Object addIntervention(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam Long studentId,
            @RequestParam String notes,
            @RequestParam(required = false) Boolean outcomeEffective,
            @RequestHeader(value = AjaxFormHeader.NAME, required = false) String ajax
    ) {
        Tutor tutor = requireTutor(principal);
        Student student = studentRepository.findById(studentId).orElseThrow();
        Intervention i = new Intervention(tutor, student, notes, Instant.now());
        i.setOutcomeEffective(outcomeEffective);
        interventionRepository.save(i);
        return ajaxRedirect.redirectOrJson(
                AjaxFormHeader.isAjax(ajax),
                "/tutor/students/" + studentId,
                "Intervention saved"
        );
    }

    @PostMapping("/students/{studentId}/plans/{planId}/title")
    public Object updatePlanTitle(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long studentId,
            @PathVariable Long planId,
            @RequestParam String title,
            @RequestHeader(value = AjaxFormHeader.NAME, required = false) String ajax
    ) {
        requireTutor(principal);
        StudyPlan plan = studyPlanRepository.findById(planId).orElseThrow();
        if (!plan.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException("Invalid plan");
        }
        plan.setTitle(title);
        studyPlanRepository.save(plan);
        return ajaxRedirect.redirectOrJson(
                AjaxFormHeader.isAjax(ajax),
                "/tutor/students/" + studentId,
                "Plan title updated"
        );
    }

    @GetMapping("/report")
    public String classReport(@AuthenticationPrincipal UserDetails principal, Model model) {
        requireTutor(principal);
        List<Student> students = studentRepository.findAllByOrderByUsernameAsc();
        List<StudentSummary> rows = students.stream()
                .map(s -> {
                    List<Progress> p = studentProgressTracking.progressForStudent(s);
                    double avg = p.isEmpty() ? 0
                            : p.stream().mapToDouble(Progress::getAverageScorePercent).average().orElse(0);
                    boolean struggling = p.stream().anyMatch(x -> x.getAverageScorePercent() < 60);
                    return new StudentSummary(s.getId(), s.getUsername(), avg, struggling);
                })
                .sorted(Comparator.comparingDouble(StudentSummary::averageScore))
                .collect(Collectors.toList());
        model.addAttribute("rows", rows);
        return "tutor/report";
    }

    public record StudentSummary(Long id, String username, double averageScore, boolean struggling) {
    }

    private Tutor requireTutor(UserDetails principal) {
        User u = authenticationService.findByUsername(principal.getUsername());
        if (!(u instanceof Tutor t)) {
            throw new IllegalStateException("Tutor role required");
        }
        return tutorRepository.findById(t.getId()).orElseThrow();
    }
}
