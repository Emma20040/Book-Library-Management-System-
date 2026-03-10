package com.library.management_system.services;
import com.library.management_system.services.AbstractBaseReportService;
import freemarker.template.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeneralReport extends  AbstractBaseReportService{
    @Value("${app.template.statement:statement.ftl}")
    private String template;
    private final PaymentService paymentService;
    private final BookService bookService;
    private final FileStorageService fileStorageService;
    private final UserService userService;


    protected GeneralReport(Configuration freemarkerConfiguration,
                            PaymentService paymentService,
                            BookService bookService,
                            FileStorageService fileStorageService,
                            UserService userService) {
        super(freemarkerConfiguration);
        this.paymentService = paymentService;
        this.bookService = bookService;
        this.fileStorageService = fileStorageService;
        this.userService = userService;

    }

    @Override
    public String getTemplate() {
        return template;
    }

    @Override
    public Map<String, Object> getVariables() {
        Map<String, Object> data = new HashMap<>();
        LocalDateTime printed_date = LocalDateTime.now();



        data.put("customerName", "Baburao Ganpatrao Apte");
        data.put("accountNumber", "123456789012");
        data.put("email", "customer@email.com");
        data.put("phoneNumber", "+91 9876543210");
        data.put("address", "123 Bank Street, New Delhi, India");
        data.put("accountType", "Savings Account");
        data.put("balance", "₹50,000.00");
        data.put("branch", "New Delhi - Connaught Place");

        // Sample transaction history
        List<Map<String, String>> transactions = createTransactions();
        data.put("transactions", transactions);
        return data;
    }

    private List<Map<String, String>> createTransactions() {
        List<Map<String, String>> transactions = new ArrayList<>();
        transactions.add(createTransaction("2025-03-01", "ATM Withdrawal", "-₹5,000", "Debit", "₹45,000"));
        transactions.add(createTransaction("2025-02-25", "Salary Credit", "+₹5,000", "Credit", "₹50,000"));
        transactions.add(createTransaction("2025-02-20", "Online Shopping", "-₹2,000", "Debit", "₹4,800"));
        return transactions;
    }

    private Map<String, String> createTransaction(
            String date,
            String description,
            String amount,
            String type,
            String balanceAfter
    ) {
        Map<String, String> transaction = new HashMap<>();
        transaction.put("date", date);
        transaction.put("description", description);
        transaction.put("amount", amount);
        transaction.put("type", type);
        transaction.put("balanceAfter", balanceAfter);
        return transaction;
    }
}
