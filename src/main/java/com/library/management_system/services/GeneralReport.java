//package com.library.management_system.services;
//
//import com.library.management_system.models.UserModel;
//import freemarker.template.Configuration;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.HashMap;
//import java.util.Map;
//
//@Service("general-report")
//public class GeneralReport extends AbstractBaseReportService {
//    @Value("${app.template.report:report.ftl}")
//    private String template;
//
//    private final PaymentService paymentService;
//    private final BookService bookService;
//    private final UserService userService;
//    private final ContactService contactService;
//
//    protected GeneralReport(Configuration freemarkerConfiguration,
//                            PaymentService paymentService,
//                            BookService bookService,
//                            FileStorageService fileStorageService,
//                            UserService userService,
//                            ContactService contactService) {
//        super(freemarkerConfiguration);
//        this.paymentService = paymentService;
//        this.bookService = bookService;
//        this.userService = userService;
//        this.contactService = contactService;
//    }
//
//    @Override
//    public String getTemplate() {
//        return template;
//    }
//
//    @Override
//    public Map<String, Object> getVariables() {
//        Map<String, Object> data = new HashMap<>();
//
//        // Current authenticated user info
//        UserModel currentUser = contactService.getCurrentAuthenticatedUser();
//        if (currentUser != null) {
//            String fullName = buildFullName(currentUser);
//            data.put("userName", fullName);
//            data.put("userEmail", currentUser.getEmail());
//            data.put("userRole", currentUser.getRole().toString());
//            data.put("userId", currentUser.getId().toString());
//        } else {
//            data.put("userName", "Unknown");
//            data.put("userEmail", "N/A");
//            data.put("userRole", "N/A");
//            data.put("userId", "N/A");
//        }
//
//        // Report generation timestamp
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        data.put("generatedAt", LocalDateTime.now().format(formatter));
//
//        // System statistics
//        data.put("totalTransactions", paymentService.getTotalTransactions());
//        data.put("totalUsers", userService.countUsers());
//        data.put("totalBooks", bookService.countBooks());
//
//        return data;
//    }
//
////    private String buildFullName(UserModel user) {
//        String first = user.getFirstName();
//        String last = user.getLastName();
//        if (first != null && last != null) {
//            return first + " " + last;
//        } else if (first != null) {
//            return first;
//        } else if (last != null) {
//            return last;
//        }
//        return user.getUsername();
//    }
//}
