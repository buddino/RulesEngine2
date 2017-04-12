package it.cnit.gaia.rulesengine.configuration;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ContextProvider implements ApplicationContextAware {
	private static ApplicationContext CONTEXT;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		CONTEXT = applicationContext;
	}

	public static <T> T getBean(Class<T> beanClass) {
		try {
			T bean = CONTEXT.getBean(beanClass);
			return bean;
		}
		catch (NoSuchBeanDefinitionException e){
			return null;
		}
	}

	public static Object getBean(String beanName) {
		try {
			Object bean = CONTEXT.getBean(beanName);
			return bean;
		}
		catch (NoSuchBeanDefinitionException e){
			return null;
		}
	}

}