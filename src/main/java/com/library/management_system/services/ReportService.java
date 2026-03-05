package com.library.management_system.services;

import java.util.Map;

public interface ReportService {
    byte[] getReport();
    String getTemplate();
    Map<String, Object> getVariables();
}
