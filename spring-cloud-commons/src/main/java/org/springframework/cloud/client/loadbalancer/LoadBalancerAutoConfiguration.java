/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.client.loadbalancer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

/**
 * Auto-configuration for Ribbon (client-side load balancing).
 *
 * @author Spencer Gibb
 * @author Dave Syer
 * @author Will Tran
 * @author Gang Li
 * 		均衡负载相关的自动装配 配置类  看来每个 spring cloud 都会在需要的时候 通过一个自动配置的类 来加载实现关键逻辑的类
 *
 * 		@Bean SmartInitializingSingleton
 * 	 	@Bean LoadBalancerInterceptor
 * 	 	@Bean RestTemplateCustomizer    这3个bean 就是实现了一个功能： 构建了 基于ribbon的均衡负载拦截器 并添加到自定义的拦截链 中 并针对 @Loadbalanced
 * 	 									 修饰的RestTemplate 进行处理
 * 	 	SmartInitializingSingleton 接口是一个作用在Bean 生命周期上的接口 当bean 准备进行初始化的时候就会 自动调用这个bean 的逻辑 做对应处理
 * 	    到这里就是自动将拦截链 添加到 RestTemplate 尾部
 */
@Configuration
//代表 该bean 的生效条件是 必须存在 RestTemplate 类
@ConditionalOnClass(RestTemplate.class)
//必须要有 LoadBalancerClient 的实现类
@ConditionalOnBean(LoadBalancerClient.class)
@EnableConfigurationProperties(LoadBalancerRetryProperties.class)
public class LoadBalancerAutoConfiguration {

	/**
	 * 这里将自动注入的 bean 使用均衡负载的 注解进行装饰了
	 */
	@LoadBalanced
	@Autowired(required = false)
	private List<RestTemplate> restTemplates = Collections.emptyList();

	@Autowired(required = false)
	private List<LoadBalancerRequestTransformer> transformers = Collections.emptyList();

	/**
	 * 注入一个 具备均衡负载功能的 bean 对象
	 * @param restTemplateCustomizers
	 * @return
	 */
	@Bean
	public SmartInitializingSingleton loadBalancedRestTemplateInitializerDeprecated(
			//因为要使用lambda 表达式 所以使用final 修饰  RestTemplateCustomizer 相当于是 一个 过滤链 每个对象会在这里被依次处理
			final ObjectProvider<List<RestTemplateCustomizer>> restTemplateCustomizers) {
		//  返回一个实现接口方法的对象 ifAvailable() 方法表示 能否从beanFactory 中获取到对应的bean 无法获取的情况下 可以获取的情况下就挨个对 元素进行处理
		return () -> restTemplateCustomizers.ifAvailable(customizers -> {
			for (RestTemplate restTemplate : LoadBalancerAutoConfiguration.this.restTemplates) {
				for (RestTemplateCustomizer customizer : customizers) {
					//这个 类能够就是执行将拦截器 添加到RestTemplate 上的 功能的类
					customizer.customize(restTemplate);
				}
			}
		});
	}

	@Bean
	@ConditionalOnMissingBean
	public LoadBalancerRequestFactory loadBalancerRequestFactory(
			LoadBalancerClient loadBalancerClient) {
		return new LoadBalancerRequestFactory(loadBalancerClient, this.transformers);
	}

	@Configuration
	@ConditionalOnMissingClass("org.springframework.retry.support.RetryTemplate")
	static class LoadBalancerInterceptorConfig {

		/**
		 * 注入了一个能够对请求进行拦截并进行均衡负载的类 该类就会拦截请求并通过ribbon 进行均衡负载
		 * @param loadBalancerClient
		 * @param requestFactory
		 * @return
		 */
		@Bean
		public LoadBalancerInterceptor ribbonInterceptor(
				LoadBalancerClient loadBalancerClient,
				LoadBalancerRequestFactory requestFactory) {
			return new LoadBalancerInterceptor(loadBalancerClient, requestFactory);
		}

		/**
		 * 注入一个自定义的 RestTemplate  这里使用的入参是上面 注入进去到的 ribbonInterceptor
		 * @param loadBalancerInterceptor
		 * @return
		 */
		@Bean
		@ConditionalOnMissingBean
		public RestTemplateCustomizer restTemplateCustomizer(
				final LoadBalancerInterceptor loadBalancerInterceptor) {
			return restTemplate -> {
				//这里为每个RestTemplate 对象增加一个 拦截器 注意这里并没有直接起作用
				List<ClientHttpRequestInterceptor> list = new ArrayList<>(
						restTemplate.getInterceptors());
				list.add(loadBalancerInterceptor);
				restTemplate.setInterceptors(list);
			};
		}

	}

	/**
	 * Auto configuration for retry mechanism.
	 */
	@Configuration
	@ConditionalOnClass(RetryTemplate.class)
	public static class RetryAutoConfiguration {

		@Bean
		@ConditionalOnMissingBean
		public LoadBalancedRetryFactory loadBalancedRetryFactory() {
			return new LoadBalancedRetryFactory() {
			};
		}

	}

	/**
	 * Auto configuration for retry intercepting mechanism.
	 */
	@Configuration
	@ConditionalOnClass(RetryTemplate.class)
	public static class RetryInterceptorAutoConfiguration {

		@Bean
		@ConditionalOnMissingBean
		public RetryLoadBalancerInterceptor ribbonInterceptor(
				LoadBalancerClient loadBalancerClient,
				LoadBalancerRetryProperties properties,
				LoadBalancerRequestFactory requestFactory,
				LoadBalancedRetryFactory loadBalancedRetryFactory) {
			return new RetryLoadBalancerInterceptor(loadBalancerClient, properties,
					requestFactory, loadBalancedRetryFactory);
		}

		@Bean
		@ConditionalOnMissingBean
		public RestTemplateCustomizer restTemplateCustomizer(
				final RetryLoadBalancerInterceptor loadBalancerInterceptor) {
			return restTemplate -> {
				List<ClientHttpRequestInterceptor> list = new ArrayList<>(
						restTemplate.getInterceptors());
				list.add(loadBalancerInterceptor);
				restTemplate.setInterceptors(list);
			};
		}

	}

}
