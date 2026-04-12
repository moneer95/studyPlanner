package com.studyplanner.web;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AjaxRedirect {

    public Object redirectOrJson(boolean ajax, String locationPath, String toast) {
        String path = locationPath.startsWith("/") ? locationPath : "/" + locationPath;
        if (ajax) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("toast", toast, "path", path));
        }
        return "redirect:" + path;
    }
}
