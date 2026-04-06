package com.studyplanner.web;

import com.studyplanner.domain.Admin;
import com.studyplanner.domain.Question;
import com.studyplanner.domain.Quiz;
import com.studyplanner.domain.User;
import com.studyplanner.repository.QuizRepository;
import com.studyplanner.repository.UserRepository;
import com.studyplanner.service.AuthenticationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final AuthenticationService authenticationService;

    public AdminController(UserRepository userRepository,
                          QuizRepository quizRepository,
                          AuthenticationService authenticationService) {
        this.userRepository = userRepository;
        this.quizRepository = quizRepository;
        this.authenticationService = authenticationService;
    }

    @GetMapping
    public String home(@AuthenticationPrincipal UserDetails principal, Model model) {
        requireAdmin(principal);
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("quizzes", quizRepository.findByOrderByTitleAsc());
        return "admin/home";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@AuthenticationPrincipal UserDetails principal, @PathVariable Long id) {
        requireAdmin(principal);
        User self = authenticationService.findByUsername(principal.getUsername());
        if (self.getId().equals(id)) {
            throw new IllegalArgumentException("Cannot delete own account");
        }
        userRepository.deleteById(id);
        return "redirect:/admin";
    }

    @PostMapping("/questions")
    public String addQuestion(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam Long quizId,
            @RequestParam String text,
            @RequestParam String optionA,
            @RequestParam String optionB,
            @RequestParam String optionC,
            @RequestParam String optionD,
            @RequestParam int correctIndex,
            @RequestParam(required = false) String topic
    ) {
        requireAdmin(principal);
        Quiz quiz = quizRepository.findById(quizId).orElseThrow();
        String t = topic != null && !topic.isBlank() ? topic : "General";
        Question q = new Question(text, optionA, optionB, optionC, optionD, correctIndex, t, quiz);
        quiz.getQuestions().add(q);
        quizRepository.save(quiz);
        return "redirect:/admin";
    }

    private void requireAdmin(UserDetails principal) {
        User u = authenticationService.findByUsername(principal.getUsername());
        if (!(u instanceof Admin)) {
            throw new IllegalStateException("Admin role required");
        }
    }
}
