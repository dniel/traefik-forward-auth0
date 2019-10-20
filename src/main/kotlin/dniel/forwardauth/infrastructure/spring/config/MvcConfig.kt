package dniel.forwardauth.infrastructure.spring.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
@EnableWebMvc
class MvcConfig : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler(
                "/img/**",
                "/css/**",
                "/js/**",
                "/libs/**",
                "/ui/**")
                .addResourceLocations(
                        "classpath:/static/img/",
                        "classpath:/static/css/",
                        "classpath:/static/js/",
                        "classpath:/static/libs/",
                        "classpath:/static/ui/");
    }

    override fun addViewControllers(registry: ViewControllerRegistry) {
        registry.addViewController("/ui").setViewName("redirect:/ui/")
        registry.addViewController("/ui/").setViewName("forward:/ui/index.html")
    }
}