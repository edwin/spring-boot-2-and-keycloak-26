package com.edw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * <pre>
 *  com.edw.controller.AdminController
 * </pre>
 *
 * @author Muhammad Edwin < edwin at redhat dot com >
 * 26 Aug 2026 9:52
 */
@Controller
public class AdminController {
    @GetMapping(path = "/admin/index")
    public String index() {
        return "admin-index";
    }

    @GetMapping(path = "/admin/logout")
    public String logout(HttpServletRequest request) throws Exception {
        request.logout();
        return "redirect:http://localhost:8080/realms/spring-boot/protocol/openid-connect/logout?post_logout_redirect_uri=http://localhost:8081/&client_id=spring-boot-client";
    }
}
