package com.nortal.library.api.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class DevAuthConfig {
  private static final Logger log = LoggerFactory.getLogger(DevAuthConfig.class);

  @Value("${library.security.print-demo-token:false}")
  private boolean printDemoToken;

  @Bean
  CommandLineRunner demoTokenPrinter() {
    return args -> {
      if (!printDemoToken) {
        return;
      }
      try {
        RSAPublicKey publicKey = toPublicKey(PUBLIC_KEY_PEM);
        RSAPrivateKey privateKey = toPrivateKey(PRIVATE_KEY_PEM);
        RSAKey rsaKey = new RSAKey.Builder(publicKey).privateKey(privateKey).build();
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsaKey)));
        Instant now = Instant.now();
        JwtClaimsSet claims =
            JwtClaimsSet.builder()
                .subject("m1")
                .issuer("dev-local")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .build();
        String token = encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
        log.info("Demo JWT (Bearer): {}", token);
      } catch (Exception e) {
        log.warn("Could not generate demo token: {}", e.getMessage());
      }
    };
  }

  private RSAPublicKey toPublicKey(String pem) throws Exception {
    String normalized =
        pem.replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s", "");
    byte[] decoded = Base64.getDecoder().decode(normalized);
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
    return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(keySpec);
  }

  private RSAPrivateKey toPrivateKey(String pem) throws Exception {
    String normalized =
        pem.replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");
    byte[] decoded = Base64.getDecoder().decode(normalized);
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
    return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(keySpec);
  }

  private static final String PUBLIC_KEY_PEM =
      """
            -----BEGIN PUBLIC KEY-----
            MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAu6YV7jzS+S+jhpNe2yBy
            vly+ba30zYp27zMpznPqk9eoUoDfeTKFPk50UZavCMTThk6TK1KcQHJiZeRTXyji
            XRhnQK4PBkUKZdbFI+PxqzJ7lp8uWFbXhqKBl1jJ+CSgcSWa+cQe8y0KN6DldecV
            aWvcsqQP7lKXojb0Pxdy2HPa7DPFHy5aVaaZxdOb4CUQ/5W5xi7IoW8i8oXos9Hp
            R8/BeQBQK2nH4H4VJxNaiDzQk2V8gMj9YnAfxCajlJVX8Kf+RdS3+6/i1mS0Gm5z
            5hzVDFmrurjrbVXpBMMNPM2+ToB7vx13vxfZK0mm4OSXfBHZ2zNb0+Hw9uPXCBqT
            QwIDAQAB
            -----END PUBLIC KEY-----
            """;

  private static final String PRIVATE_KEY_PEM =
      """
            -----BEGIN PRIVATE KEY-----
            MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC7phXuPNL5L6OG
            k17bIHK+XL5trfTNinbvMynOc+qT16hSgN95MoU+TnRRlq8IxNOGTpMrUpxAcmJl
            5FNfKOJdGGdArg8GQQpl1sUj4/GrMnuWny5YVteGoYGXWMn4JKBxJZr5xB7zLQo3
            oOV15xVpa9yypA/uUpeiNvQ/F3LYc9rsM8UfLlpVppnF05vgJRD/lbnGLsihbyLy
            heiz0elHz8F5AFAracfgh4UnE1qIPNCQZXyAyP1icB/ENqOUlVfwp/5F1Lf7r+LW
            ZLQabnPmHNUMWau6uOttVekEww08zb5OgHu/XXe/F9krSabg5Jd8EdnbM1vT4fD2
            49cIGpNDAgMBAAECggEBAKoOH0qAfgPNxVMlGDZnfX56VFFIomRb0ijCkWz9/Hvi
            DLgqmFYW4GZ6eRBSfAvJwe4E2K6fP5ew3z5zDQ6O56UGx9SEsn7TFwO2LPa28DZQ
            isx2wLLlC0YHhgcRxQB+dHJeKpWfwMGpOJKywh1LMslbwpdN7OTMWXuRYBxD8+5k
            YMFPwzcAezP4cQdReyA3Ze0bf8pahkYhAzDo2XBg2u/syFnDFazQTkUdECcr/W+7
            5KbOL9ztf64/eU2r2HOvSBoNqBWcSpAFCovTRC6ftqXMB9q1mJIdo8DLGcgklsxg
            YmPRFpzVQGfCthYvR/x7ksuvVDcfZLY76lGckbbMCOECgYEA7OQ7U0mRfgCJQYMn
            XqeWuP+0LZgS/4Y8kT8Ml18M8ikVKzFwk83qlwEFAv+F8YCFK8aRy/XEl1k8aePC
            X+6xInjzYrQFRG+V6+6wPhzT8n1KXLR2nSFWtNnldJK9mIG61r4Q5oWsD5gDJL/B
            QjhqFCPk5d0YLkKmcnfz8gjklfsCgYEAw7ToBgoy14mErU5sW18LdwxHESZbYVpo
            lPEzx0yec7cC4E/+L8FSguNyyb13aKRqD2k9qaaFaIXuoqZT3OTbfpSycgqI8sDf
            P9eWD8Fk2f3gL8oXqgXp7TTfeE3ANYYiVa8y7EUze/bu6xYzXpLIcOy08+jm6Q0A
            RFm+xbaKM+cCgYEAoJhtQxAbNsVXLf7q2sDkvxp5pm3oa7QAbAuhPQb3R0vAgxNP
            dpMlx9SUOyHp+PG8nb58PO+1wjTyzqjXeoQOfG4w8f379dsvNADhzkwNqiOwLUNn
            rXzwIf1q9kTLcM57w3m/nYf7i/8v6JtJyJp0nMR+dY6i9ijqmeNPjWfqh40CgYAO
            i9eTH+L+JkzSbRqZG6b28ntR+jplH8U+8iwGCLYLNBi3FIkHqlf2DvzFZa4vTVQP
            d0unhrCNQNoLhki4ojCWoYz/vAYcGBmmT3VLuBxMbv1sxHf82lU81oqi38NnEdxX
            R9MDJzSwgEo5OSg4M356g3zQChQompaB7e3EBwKBgFJNfYgHHuG7+rtVXx3ovK4O
            TpzKIEuD/X1X3XNwqz5qqB2XzC/edD26zps4bnLSQFZp7TtfByAO6+vhyUzhm/FH
            CEN1Kufm6/YDs1CWMTjZAKJ5+2IuWof25aAmdWeAtzBSMsWeDe8hgWtrqgnL847x
            kvOIMrxmBmMCd6gs1uIn
            -----END PRIVATE KEY-----
            """;
}
