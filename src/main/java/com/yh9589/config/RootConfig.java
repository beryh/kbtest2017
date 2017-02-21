package com.yh9589.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Created by beryh on 2017-02-14.
 */
@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = {"com.yh9589"}, excludeFilters = {@ComponentScan.Filter(type= FilterType.ANNOTATION, value=EnableWebMvc.class)})
public class RootConfig {
}
