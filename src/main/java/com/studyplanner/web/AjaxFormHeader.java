package com.studyplanner.web;

public final class AjaxFormHeader {

    public static final String NAME = "X-Ajax-Form";

    private AjaxFormHeader() {
    }

    public static boolean isAjax(String value) {
        return "1".equals(value);
    }
}
