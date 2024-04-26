package top.melody.cloud.game.bloom.framework.security.config;

import cn.iocoder.yudao.framework.security.config.AuthorizeRequestsCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import top.melody.cloud.game.bloom.enums.ApiConstants;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;

/**
 * Member 模块的 Security 配置
 */
@Configuration("productSecurityConfiguration")
public class SecurityConfiguration {

    @Bean("productAuthorizeRequestsCustomizer")
    public AuthorizeRequestsCustomizer authorizeRequestsCustomizer() {
        return new AuthorizeRequestsCustomizer() {

            @Override
            public void customize(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authorizationManagerRequestMatcherRegistry) {
                authorizationManagerRequestMatcherRegistry
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator").anonymous()
                        .requestMatchers("/actuator/**").anonymous()
                        .requestMatchers("/druid/**").anonymous()
                        .requestMatchers("/" + ApiConstants.PREFIX + "/**").permitAll();
            }
        };
    }

}