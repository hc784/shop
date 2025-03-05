package com.oauthlogin.api.controller.auth;

import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.oauthlogin.api.entity.auth.AuthReqModel;
import com.oauthlogin.api.entity.auth.SignUpRequest;
import com.oauthlogin.api.entity.user.User;
import com.oauthlogin.api.entity.user.UserRefreshToken;
import com.oauthlogin.api.repository.user.UserRefreshTokenRepository;
import com.oauthlogin.api.repository.user.UserRepository;
import com.oauthlogin.common.MyApiResponse;
import com.oauthlogin.config.properties.AppProperties;
import com.oauthlogin.oauth.entity.ProviderType;
import com.oauthlogin.oauth.entity.RoleType;
import com.oauthlogin.oauth.entity.UserPrincipal;
import com.oauthlogin.oauth.exception.BadRequestException;
import com.oauthlogin.oauth.token.AuthToken;
import com.oauthlogin.oauth.token.AuthTokenProvider;
import com.oauthlogin.utils.CookieUtil;
import com.oauthlogin.utils.HeaderUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.Date;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Auth API")
public class AuthController {

    private final AppProperties appProperties;
    private final AuthTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserRefreshTokenRepository userRefreshTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final static long THREE_DAYS_MSEC = 259200000;
    private final static String REFRESH_TOKEN = "refresh_token";
	
    @PostMapping("/signup")
    @ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "성공",
    	    		content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                                "header": {
                                    "code": 200,
                                    "message": "SUCCESS"
                                },
                                "body": {
                                    "token": "eyJhbGciOiJIUzI1NiJ9..."
                                }
                            }
                            """
                    ))),
    	    @ApiResponse(responseCode = "404", description = "해당 ID의 유저가 존재하지 않습니다."),
    	})
    public MyApiResponse registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        // userId 중복 체크
        if (userRepository.existsByUserId(signUpRequest.getUserId())) {
            throw new BadRequestException("이미 사용 중인 아이디입니다.");
        }

        // 사용자 생성
        User user = new User(
            signUpRequest.getUserId(),
            signUpRequest.getUsername(),
            signUpRequest.getEmail(),
            "N", // emailVerifiedYn
            "", // profileImageUrl
            ProviderType.LOCAL,
            RoleType.USER,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        // 비밀번호 암호화
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

        // 사용자 저장
        User result = userRepository.save(user);

        return MyApiResponse.success("message", "회원가입이 완료되었습니다.");
    }

    // ... existing code ...
    
    @PostMapping("/login")
    @ApiResponses(value = {
    	    @ApiResponse(responseCode = "200", description = "성공",
    	    		content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                                "header": {
                                    "code": 200,
                                    "message": "SUCCESS"
                                },
                                "body": {
                                    "token": "eyJhbGciOiJIUzI1NiJ9..."
                                }
                            }
                            """
                    ))),
    	    @ApiResponse(responseCode = "404", description = "해당 ID의 유저가 존재하지 않습니다."),
    	})
    public MyApiResponse login(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody AuthReqModel authReqModel
    ) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authReqModel.getId(),
                        authReqModel.getPassword()
                )
        );

        String userId = authReqModel.getId();
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Date now = new Date();
        AuthToken accessToken = tokenProvider.createAuthToken(
                userId,
                ((UserPrincipal) authentication.getPrincipal()).getRoleType().getCode(),
                new Date(now.getTime() + appProperties.getAuth().getTokenExpiry())
        );

        long refreshTokenExpiry = appProperties.getAuth().getRefreshTokenExpiry();
        AuthToken refreshToken = tokenProvider.createAuthToken(
                appProperties.getAuth().getTokenSecret(),
                new Date(now.getTime() + refreshTokenExpiry)
        );

        // userId refresh token 으로 DB 확인
        UserRefreshToken userRefreshToken = userRefreshTokenRepository.findByUserId(userId);
        if (userRefreshToken == null) {
            // 없는 경우 새로 등록
            userRefreshToken = new UserRefreshToken(userId, refreshToken.getToken());
            userRefreshTokenRepository.saveAndFlush(userRefreshToken);
        } else {
            // DB에 refresh 토큰 업데이트
            userRefreshToken.setRefreshToken(refreshToken.getToken());
        }

        int cookieMaxAge = (int) refreshTokenExpiry / 60;
        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN);
        CookieUtil.addCookie(response, REFRESH_TOKEN, refreshToken.getToken(), cookieMaxAge);

        return MyApiResponse.success("token", accessToken.getToken());
    }

    @GetMapping("/refresh")
    public MyApiResponse refreshToken (HttpServletRequest request, HttpServletResponse response) {
        // access token 확인
        String accessToken = HeaderUtil.getAccessToken(request);
        AuthToken authToken = tokenProvider.convertAuthToken(accessToken);
        if (!authToken.validate()) {
            return MyApiResponse.invalidAccessToken();
        }

        // expired access token 인지 확인
        Claims claims = authToken.getExpiredTokenClaims();
        if (claims == null) {
            return MyApiResponse.notExpiredTokenYet();
        }

        String userId = claims.getSubject();
        RoleType roleType = RoleType.of(claims.get("role", String.class));

        // refresh token
        String refreshToken = CookieUtil.getCookie(request, REFRESH_TOKEN)
                .map(Cookie::getValue)
                .orElse((null));
        AuthToken authRefreshToken = tokenProvider.convertAuthToken(refreshToken);

        if (authRefreshToken.validate()) {
            return MyApiResponse.invalidRefreshToken();
        }

        // userId refresh token 으로 DB 확인
        UserRefreshToken userRefreshToken = userRefreshTokenRepository.findByUserIdAndRefreshToken(userId, refreshToken);
        if (userRefreshToken == null) {
            return MyApiResponse.invalidRefreshToken();
        }

        Date now = new Date();
        AuthToken newAccessToken = tokenProvider.createAuthToken(
                userId,
                roleType.getCode(),
                new Date(now.getTime() + appProperties.getAuth().getTokenExpiry())
        );

        long validTime = authRefreshToken.getTokenClaims().getExpiration().getTime() - now.getTime();

        // refresh 토큰 기간이 3일 이하로 남은 경우, refresh 토큰 갱신
        if (validTime <= THREE_DAYS_MSEC) {
            // refresh 토큰 설정
            long refreshTokenExpiry = appProperties.getAuth().getRefreshTokenExpiry();

            authRefreshToken = tokenProvider.createAuthToken(
                    appProperties.getAuth().getTokenSecret(),
                    new Date(now.getTime() + refreshTokenExpiry)
            );

            // DB에 refresh 토큰 업데이트
            userRefreshToken.setRefreshToken(authRefreshToken.getToken());

            int cookieMaxAge = (int) refreshTokenExpiry / 60;
            CookieUtil.deleteCookie(request, response, REFRESH_TOKEN);
            CookieUtil.addCookie(response, REFRESH_TOKEN, authRefreshToken.getToken(), cookieMaxAge);
        }

        return MyApiResponse.success("token", newAccessToken.getToken());
    }
    
    @PostMapping("/logout")
    public MyApiResponse logout(HttpServletRequest request, HttpServletResponse response) {
        // 1. 쿠키에서 Refresh Token 가져오기
        String refreshToken = CookieUtil.getCookie(request, REFRESH_TOKEN)
                .map(Cookie::getValue)
                .orElse(null);

        if (refreshToken != null) {
            // 2. DB에서 해당 Refresh Token 삭제
            userRefreshTokenRepository.deleteByRefreshToken(refreshToken);
        }

        // 3. 쿠키에서 Refresh Token 삭제
        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN);

        // 4. (선택) SecurityContext에서 사용자 정보 제거
        SecurityContextHolder.clearContext();

        return MyApiResponse.success("message", "로그아웃이 완료되었습니다.");
    }
    
}
