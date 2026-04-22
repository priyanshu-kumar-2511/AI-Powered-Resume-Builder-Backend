package com.airesume.authservice.config;

import com.airesume.authservice.model.Role;
import com.airesume.authservice.model.User;
import com.airesume.authservice.model.ProviderType;
import com.airesume.authservice.repository.RoleRepository;
import com.airesume.authservice.repository.UserRepository;
import com.airesume.authservice.service.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");

            // LinkedIn OIDC support (if name/email are in different fields)
            if (email == null) email = oAuth2User.getAttribute("emailAddress");
            if (name == null) {
                String givenName = oAuth2User.getAttribute("given_name");
                String familyName = oAuth2User.getAttribute("family_name");
                name = ((givenName != null ? givenName : "") + (familyName != null ? " " + familyName : "")).trim();
            }

            if (email == null || email.isBlank()) {
                throw new IllegalStateException("OAuth provider did not return an email address");
            }

            final String finalEmail = email;
            final String finalName = (name == null || name.isBlank()) ? "User" : name;
            final ProviderType provider = resolveProvider(authentication);

            User user = userRepository.findByEmail(finalEmail)
                    .orElseGet(() -> createOAuthUser(finalEmail, finalName, provider));

            String token = jwtService.generateToken(user.getUsername());

            String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:4200/login-success")
                    .queryParam("token", token)
                    .build().toUriString();

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } catch (Exception exception) {
            log.error("OAuth2 success handling failed: {}", exception.getMessage(), exception);

            String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:4200/login")
                    .queryParam("oauthError", "true")
                    .queryParam("reason", exception.getMessage())
                    .build()
                    .toUriString();

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        }
    }

    private User createOAuthUser(String email, String name, ProviderType provider) {
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role(null, "ROLE_USER")));

        User newUser = User.builder()
                .email(email)
                .username(email)
                .fullName(name)
                .password("OAUTH2_USER")
                .mobileNumber("")
                .isActive(true)
                .enabled(true)
                .provider(provider)
                .build();

        newUser.getRoles().add(userRole);
        return userRepository.save(newUser);
    }

    private ProviderType resolveProvider(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken token) {
            String registrationId = token.getAuthorizedClientRegistrationId();
            if ("google".equalsIgnoreCase(registrationId)) {
                return ProviderType.GOOGLE;
            }
            if ("linkedin".equalsIgnoreCase(registrationId)) {
                return ProviderType.LINKEDIN;
            }
        }
        return ProviderType.OAUTH2;
    }
}
