package com.example.query.controller;




import model.QueryTask;
import model.User;
import repository.QueryRepository;
import repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.google.gson.JsonObject;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Random;

import javax.sql.DataSource;

@Controller
public class AppController {
	

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QueryRepository queryRepository;
    
    @Autowired
    private DataSource dataSource;

   
    @PostConstruct
    public void test() throws Exception {
        try (java.sql.Connection conn = dataSource.getConnection()) {
            System.out.println(conn.getMetaData().getURL());
        }
    }

    // Helper: Generate Random Number Captcha
    private String generateCaptcha() {
        Random random = new Random();
        return String.valueOf(100000 + random.nextInt(900000));
    }

    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        if (session.getAttribute("user") != null) {
            return "redirect:/home";
        }
        String captcha = generateCaptcha();
        session.setAttribute("captcha", captcha);
        model.addAttribute("captcha", captcha);
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@RequestParam String username, @RequestParam String email,
                                  @RequestParam String userType, @RequestParam String password,
                                  @RequestParam String retypePassword, 
                                  @RequestParam(required = false) String teamLeadName, Model model) {
        
        if(!password.equals(retypePassword)) {
            model.addAttribute("error", "Passwords do not match!");
            return "register";
        }

        if(userRepository.findByUsernameOrEmail(username, email).isPresent()) {
            model.addAttribute("error", "Username or Email already exists!");
            return "register";
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setUserType(userType);
        user.setPassword(password);

        // Requirement Mandate: Save dynamic parameters in JSON format
        JsonObject json = new JsonObject();
        if("Employee".equalsIgnoreCase(userType) && teamLeadName != null) {
            json.addProperty("teamLead", teamLeadName);
        }
        user.setJsonDetails(json.toString());

        userRepository.save(user);
        return "redirect:/?success=Registered successfully. Please login.";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam String loginId, @RequestParam String password,
                               @RequestParam String captchaInput, HttpSession session, Model model) {
        String sessionCaptcha = (String) session.getAttribute("captcha");
        if(sessionCaptcha == null || !sessionCaptcha.equals(captchaInput)) {
            model.addAttribute("error", "Invalid Captcha Code!");
            model.addAttribute("captcha", generateCaptcha());
            return "login";
        }

        Optional<User> userOpt = userRepository.findByUsernameOrEmail(loginId, loginId);
        if(userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            session.setAttribute("user", userOpt.get());
            return "redirect:/home";
        } else {
            model.addAttribute("error", "Invalid Username/Email or Password!");
            model.addAttribute("captcha", generateCaptcha());
            return "login";
        }
    }
    @PostMapping("/admin-login")
    public String processAdminLogin(@RequestParam String adminloginid, @RequestParam String password, HttpSession session, Model model) {
        
        if ("admin".equals(adminloginid) && "123".equals(password)) {
            
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setUserType("Admin");
            session.setAttribute("user", adminUser);
            
            return "redirect:/response";
        } else {
            model.addAttribute("error", "Invalid Admin Username or Password!");
            return "adminlogin";
        }
    }

    @GetMapping("/home")
    public String home(HttpSession session, Model model) {
        if(session.getAttribute("user") == null) return "redirect:/";
        model.addAttribute("user", session.getAttribute("user"));
        return "home";
    }

    @GetMapping("/employee")
    public String employeeModule(HttpSession session, Model model) {
        if(session.getAttribute("user") == null) return "redirect:/";
        return "employee";
    }

    @GetMapping("/student")
    public String studentModule(HttpSession session, Model model) {
        if(session.getAttribute("user") == null) return "redirect:/";
        return "student";
    }

    @PostMapping("/raiseQuery")
    public String raiseQuery(@RequestParam String targetType, @RequestParam String priority,
                             @RequestParam String question, @RequestParam String description,
                             HttpSession session) {
        User user = (User) session.getAttribute("user");
        if(user == null) return "redirect:/";

        QueryTask task = new QueryTask();
        task.setDateRaised(LocalDate.now().toString());
        task.setRaiserName(user.getUsername());
        task.setRaiserType(user.getUserType());
        task.setTargetType(targetType);
        task.setQuestion(question);
        task.setPriority(priority);
        task.setDescription(description); 
        queryRepository.save(task);
        return "redirect:/report?success=Query Raised Successfully";
    }

    @GetMapping("/report")
    public String reportModule(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if(user == null) return "redirect:/";
        
        // If Admin, they can view everything via report/response
        if("Admin".equalsIgnoreCase(user.getUserType())) {
            model.addAttribute("queries", queryRepository.findAll());
        } else {
            // Students and Employees see only queries raised by themselves
            model.addAttribute("queries", queryRepository.findByRaiserName(user.getUsername()));
        }
        return "report";
    }

    @GetMapping("/response")
    public String responseModule(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if(user == null) return "redirect:/";
        
        // Secure the response module so only Admin can access response management
        if(!"Admin".equalsIgnoreCase(user.getUserType())) {
            return "redirect:/home?error=UnauthorizedAccess";
        }
        
        model.addAttribute("queries", queryRepository.findAll());
        return "reponse";
    }
    @GetMapping("/viewResponse/{id}")
    public String viewQueryResponse(@PathVariable Long id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if(user == null) return "redirect:/";

        Optional<QueryTask> taskOpt = queryRepository.findById(id);
        if(taskOpt.isPresent()) {
            QueryTask task = taskOpt.get();
            // Ensure students/employees can only view their own queries
            if(!"Admin".equalsIgnoreCase(user.getUserType()) && !task.getRaiserName().equals(user.getUsername())) {
                return "redirect:/home?error=Unauthorized";
            }
            model.addAttribute("query", task);
            return "view-response"; // New HTML page for viewing response details
        }
        return "redirect:/report";
    }

    @PostMapping("/submitResponse")
    public String submitResponse(@RequestParam Long queryId, @RequestParam String responder,
                                 @RequestParam String responseDescription, @RequestParam String status,
                                 @RequestParam(required = false) String holdReason, 
                                 @RequestParam(required = false) String holdDate) {
        Optional<QueryTask> taskOpt = queryRepository.findById(queryId);
        if(taskOpt.isPresent()) {
            QueryTask task = taskOpt.get();
            task.setResponder(responder);
            task.setResponseDescription(responseDescription);
            task.setStatus(status);
            if("on hold".equalsIgnoreCase(status)) {
                task.setHoldReason(holdReason);
                task.setHoldDate(holdDate);
            } else {
                task.setHoldReason(null);
                task.setHoldDate(null);
            }
            queryRepository.save(task);
        }
        return "redirect:/response";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/?loggedout";
    }
    @GetMapping("/admin-login")
    public String adminLoginPage() {
        return "adminlogin"; // This looks for adminlogin.html in src/main/resources/templates/
    }
}