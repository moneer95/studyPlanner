package com.studyplanner.web;

import com.studyplanner.domain.Admin;
import com.studyplanner.domain.User;
import com.studyplanner.repository.UserRepository;
import com.studyplanner.service.AuthenticationService;
import com.studyplanner.service.quiz.QuestionBank;
import com.studyplanner.service.quiz.QuizCatalog;
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

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final QuizCatalog quizCatalog;
    private final QuestionBank questionBank;
    private final AuthenticationService authenticationService;
    private final AjaxRedirect ajaxRedirect;

    public AdminController(UserRepository userRepository,
                          QuizCatalog quizCatalog,
                          QuestionBank questionBank,
                          AuthenticationService authenticationService,
                          AjaxRedirect ajaxRedirect) {
        this.userRepository = userRepository;
        this.quizCatalog = quizCatalog;
        this.questionBank = questionBank;
        this.authenticationService = authenticationService;
        this.ajaxRedirect = ajaxRedirect;
    }

    @GetMapping
    public String home(@AuthenticationPrincipal UserDetails principal, Model model) {
        requireAdmin(principal);
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("quizzes", quizCatalog.listQuizzes());
        return "admin/home";
    }

    @PostMapping("/users/{id}/delete")
    public Object deleteUser(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id,
            @RequestHeader(value = AjaxFormHeader.NAME, required = false) String ajax
    ) {
        requireAdmin(principal);
        User self = authenticationService.findByUsername(principal.getUsername());
        if (self.getId().equals(id)) {
            throw new IllegalArgumentException("Cannot delete own account");
        }
        userRepository.deleteById(id);
        return ajaxRedirect.redirectOrJson(AjaxFormHeader.isAjax(ajax), "/admin", "User removed");
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
        requireAdmin(principal);
        questionBank.addQuestionToQuiz(quizId, text, optionA, optionB, optionC, optionD, correctIndex, topic);
        return ajaxRedirect.redirectOrJson(AjaxFormHeader.isAjax(ajax), "/admin", "Question added");
    }

    private void requireAdmin(UserDetails principal) {
        User u = authenticationService.findByUsername(principal.getUsername());
        if (!(u instanceof Admin)) {
            throw new IllegalStateException("Admin role required");
        }
    }
}
