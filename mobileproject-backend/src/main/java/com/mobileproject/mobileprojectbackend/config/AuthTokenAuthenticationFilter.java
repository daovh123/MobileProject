package com.mobileproject.mobileprojectbackend.config;

import com.mobileproject.mobileprojectbackend.auth.AuthTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Servlet filter xác thực JWT token cho mỗi request HTTP.
 *
 * <p>Filter này được đặt TRƯỚC {@code UsernamePasswordAuthenticationFilter} trong chuỗi lọc bảo mật.
 * Nó thực hiện:</p>
 * <ol>
 *   <li>Đọc header {@code Authorization} từ request</li>
 *   <li>Gọi {@link AuthTokenService#resolveUsername(String)} để xác minh token và trích xuất username</li>
 *   <li>Nếu token hợp lệ, tạo {@code UsernamePasswordAuthenticationToken} và đặt vào
 *       {@code SecurityContextHolder} để các thành phần downstream có thể truy cập thông tin người dùng</li>
 * </ol>
 *
 * <p>Lưu ý: Filter chỉ xử lý khi chưa có authentication trong context (tránh ghi đè xác thực đã có).</p>
 *
 * @see AuthTokenService
 * @see SecurityConfig
 */
@Component
public class AuthTokenAuthenticationFilter extends OncePerRequestFilter {

    private final AuthTokenService authTokenService;

    /**
     * Constructor injection cho AuthTokenService.
     *
     * @param authTokenService service xử lý phát hành và xác minh JWT token
     */
    public AuthTokenAuthenticationFilter(AuthTokenService authTokenService) {
        this.authTokenService = authTokenService;
    }

    /**
     * Xử lý xác thực cho mỗi request HTTP.
     * Trích xuất token từ header Authorization, xác minh, và đặt authentication vào SecurityContext.
     *
     * @param request     HTTP request hiện tại
     * @param response    HTTP response
     * @param filterChain chuỗi filter để tiếp tục xử lý
     * @throws ServletException nếu có lỗi servlet
     * @throws IOException      nếu có lỗi I/O
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader != null
                && !authorizationHeader.isBlank()
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            authTokenService.resolveUsername(authorizationHeader).ifPresent(username -> {
                UsernamePasswordAuthenticationToken authentication =
                        UsernamePasswordAuthenticationToken.authenticated(username, null, List.of());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }

        filterChain.doFilter(request, response);
    }
}
