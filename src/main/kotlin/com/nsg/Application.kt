package com.nsg

import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.embedded.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.web.filter.ShallowEtagHeaderFilter
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors.regex
import springfox.documentation.service.ApiInfo
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2
import java.util.*

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}

@EnableSwagger2
@SpringBootApplication
open class Application {

    private val log = LoggerFactory.getLogger(Application::class.java)

    @Bean
    open fun mallApi(): Docket = Docket(DocumentationType.SWAGGER_2)
            .apiInfo(apiInfo())
            .select()
            .paths(regex("/v1/mall.*"))
            .build()

    private fun apiInfo(): ApiInfo = ApiInfoBuilder()
            .title("CSL Mall API")
            .description("API program for CSL Mall API service")
            .termsOfServiceUrl("http://123.59.84.71:4000/4API%E7%BD%91%E5%85%B3/402API.html")
            .contact("lijc")
            .license("参考文档")
            .licenseUrl("http://123.59.84.71:4000/4API%E7%BD%91%E5%85%B3/402API.html")
            .version("2.0")
            .build()

    @Bean
    open fun filterRegistrationBean(): FilterRegistrationBean {
        val filterBean = FilterRegistrationBean()
        filterBean.setFilter(ShallowEtagHeaderFilter())
        filterBean.urlPatterns = Arrays.asList("*")
        return filterBean
    }


}

