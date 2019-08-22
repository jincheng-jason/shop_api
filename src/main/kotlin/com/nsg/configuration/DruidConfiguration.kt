package com.nsg.configuration

import com.alibaba.druid.support.http.StatViewServlet
import com.alibaba.druid.support.http.WebStatFilter
import org.springframework.boot.context.embedded.FilterRegistrationBean
import org.springframework.boot.context.embedded.ServletRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Created by lijc on 16/2/23.
 */

@Configuration
open class DruidConfiguration {


    @Bean
    open fun druidServlet(): ServletRegistrationBean {
        return ServletRegistrationBean(StatViewServlet(), "/druid/*")
    }

//    @Bean
//    open fun druidDataSource(): DataSource {
//
//        val druidDataSource = DruidDataSource()
//        druidDataSource.driverClassName = env!!.getProperty("spring.datasource.driverClassName")
//        druidDataSource.url = env.getProperty("spring.datasource.url")
//        druidDataSource.username = env.getProperty("spring.datasource.username")
//        druidDataSource.password = env.getProperty("spring.datasource.password")
//        try {
//            druidDataSource.setFilters("stat, wall")
//        } catch (e: SQLException) {
//            e.printStackTrace()
//        }
//
//        return druidDataSource
//    }

    @Bean
    open fun filterRegistrationBean(): FilterRegistrationBean {
        val filterRegistrationBean = FilterRegistrationBean()
        filterRegistrationBean.setFilter(WebStatFilter())
        filterRegistrationBean.addUrlPatterns("/*")
        filterRegistrationBean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*")
        return filterRegistrationBean
    }

}