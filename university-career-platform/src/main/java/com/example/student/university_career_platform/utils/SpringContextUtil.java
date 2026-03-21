package com.example.student.university_career_platform.utils;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SpringContextUtil implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NotNull ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    @SuppressWarnings("unused")
    public static <T> T getBean(Class<T> beanClass) {
        return applicationContext.getBean(beanClass);
    }
}