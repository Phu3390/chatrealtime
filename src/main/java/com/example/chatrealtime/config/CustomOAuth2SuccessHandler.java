// package com.example.acuth.config;

// import java.io.IOException;
// import java.time.Duration;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Lazy;
// import org.springframework.http.HttpHeaders;
// import org.springframework.http.ResponseCookie;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.oauth2.core.user.OAuth2User;
// import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
// import org.springframework.stereotype.Component;

// import com.example.auth.dto.response.Auth.AuthResponse;
// import com.example.auth.service.Auth.AuthService;
// import com.example.common.exception.AppException;
// import com.fasterxml.jackson.databind.ObjectMapper;

// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;

// @Component
// public class CustomOAuth2SuccessHandler
//                 implements AuthenticationSuccessHandler {

//         @Autowired
//         @Lazy
//         private AuthService loginGoogleService;

//         @Autowired
//         @Lazy
//         private ObjectMapper objectMapper;

//         @Value("${server.frontend.url}")
//         String frontendUrl;

//         @Override
//         public void onAuthenticationSuccess(
//                         HttpServletRequest request,
//                         HttpServletResponse response,
//                         Authentication authentication) throws IOException, ServletException {
//                 try {

//                         OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

//                         String email = oauthUser.getAttribute("email");
//                         String fullName = oauthUser.getAttribute("name");
//                         Boolean emailVerified = oauthUser.getAttribute("email_verified");

//                         System.out.println("SUCCESS HANDLER RUN");

//                         AuthResponse authResponse = loginGoogleService.loginWithGoogle(
//                                         email,
//                                         fullName,
//                                         emailVerified);

//                         ResponseCookie accessCookie = ResponseCookie.from("accessToken", authResponse.getToken())
//                                         .httpOnly(false)
//                                         .secure(false)
//                                         .sameSite("Lax")
//                                         .path("/")
//                                         .maxAge(Duration.ofHours(2))
//                                         .build();

//                         response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
//                         response.sendRedirect(
//                                         frontendUrl + "/oauthsuccess");
//                 } catch (AppException ex) {
//                         response.sendRedirect(
//                                         frontendUrl + "/login?error=" + ex.getCode());
//                 }
//         }
// }