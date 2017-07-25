package it.cnit.gaia.rulesengine.configuration;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * The context provider used to get beans for objects where the autowiring
 * function cannot be used i.e. the GaiaRule because they are instantiated at runtime
 * using Java Reflection
 */
@Component
public class ContextProvider implements ApplicationContextAware {
	private static ApplicationContext CONTEXT;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		CONTEXT = applicationContext;
	}

	/**
	 * Get the bean by class
	 * @param beanClass
	 * @param <T>
	 * @return
	 */
	public static <T> T getBean(Class<T> beanClass) {
		try {
			T bean = CONTEXT.getBean(beanClass);
			return bean;
		}
		catch (NoSuchBeanDefinitionException e){
			return null;
		}
	}

	/**
	 * Get the bean by classname
	 * @param beanName
	 * @return
	 */
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