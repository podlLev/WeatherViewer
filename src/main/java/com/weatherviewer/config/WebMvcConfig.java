package com.weatherviewer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

/**
 * Lets any request switch the active locale by adding a {@code lang} query
 * parameter (e.g. {@code ?lang=uk}). The new locale is then persisted on
 * the session by {@link AppConfig#localeResolver()}, so it applies to
 * subsequent requests too, not just the one carrying the parameter.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

}
