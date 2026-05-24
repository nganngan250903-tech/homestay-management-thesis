package com.example.homestaymanager.security;

import com.example.homestaymanager.enums.AuthProvider;
import com.example.homestaymanager.enums.CustomerStatus;
import com.example.homestaymanager.model.Customer;
import com.example.homestaymanager.repository.CustomerRepository;
import com.example.homestaymanager.repository.EmployeeRepository;
import com.example.homestaymanager.service.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final String CUSTOMER = "CUSTOMER";
    private static final String CUSTOMER_ROLE = "CUSTOMER";

    private final CustomerRepository customerRepository;
    private final EmployeeRepository employeeRepository;
    private final JwtService jwtService;

    @Value("${app.oauth2.redirect-uri:http://localhost:5173/oauth2/redirect}")
    private String frontendRedirectUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        try {
            OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
            String email = normalizeEmail(oauthUser.getAttribute("email"));
            String name = oauthUser.getAttribute("name");
            String picture = oauthUser.getAttribute("picture");

            if (isBlank(email)) {
                response.sendRedirect(errorRedirect("missing_email"));
                return;
            }

            if (employeeRepository.findByEmail(email).isPresent()
                    && customerRepository.findByEmail(email).isEmpty()) {
                response.sendRedirect(errorRedirect("employee_email"));
                return;
            }

            Customer customer = customerRepository.findByEmail(email)
                    .map(existingCustomer -> updateGoogleCustomerDefaults(existingCustomer, name, picture))
                    .orElseGet(() -> createGoogleCustomer(email, name, picture));

            if (customer.getStatus() == CustomerStatus.LOCKED) {
                response.sendRedirect(errorRedirect("locked"));
                return;
            }

            String token = jwtService.generateToken(customer.getId(), customer.getEmail(), CUSTOMER, CUSTOMER_ROLE);
            String redirectUrl = UriComponentsBuilder.fromUriString(frontendRedirectUri)
                    .queryParam("token", token)
                    .build()
                    .toUriString();

            response.sendRedirect(redirectUrl);
        } catch (Exception exception) {
            response.sendRedirect(errorRedirect("oauth2_login_failed"));
        }
    }

    private Customer createGoogleCustomer(String email, String name, String picture) {
        Customer customer = new Customer();
        customer.setEmail(email);
        customer.setName(isBlank(name) ? email : name);
        customer.setImage(picture);
        customer.setPassword("");
        customer.setPhone("");
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setProvider(AuthProvider.GOOGLE);
        return customerRepository.save(customer);
    }

    private Customer updateGoogleCustomerDefaults(Customer customer, String name, String picture) {
        boolean changed = false;

        if (customer.getStatus() == null) {
            customer.setStatus(CustomerStatus.ACTIVE);
            changed = true;
        }
        if (customer.getProvider() == null) {
            customer.setProvider(AuthProvider.LOCAL);
            changed = true;
        }
        if (isBlank(customer.getName())) {
            customer.setName(isBlank(name) ? customer.getEmail() : name);
            changed = true;
        }
        if (customer.getPassword() == null) {
            customer.setPassword("");
            changed = true;
        }
        if (customer.getPhone() == null) {
            customer.setPhone("");
            changed = true;
        }
        if (isBlank(customer.getImage()) && !isBlank(picture)) {
            customer.setImage(picture);
            changed = true;
        }

        return changed ? customerRepository.save(customer) : customer;
    }

    private String errorRedirect(String error) {
        return UriComponentsBuilder.fromUriString(frontendRedirectUri)
                .queryParam("error", error)
                .build()
                .toUriString();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String normalizeEmail(String email) {
        return isBlank(email) ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
