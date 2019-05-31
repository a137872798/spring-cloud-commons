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

import java.io.IOException;
import java.net.URI;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.Assert;

/**
 * @author Spencer Gibb
 * @author Dave Syer
 * @author Ryan Baxter
 * @author William Tran
 *
 * 		均衡负载对象的实现类
 */
public class LoadBalancerInterceptor implements ClientHttpRequestInterceptor {

	/**
	 * 内部维护了一个 均衡负载的实现类 实际调用应该是委托给它 该类 底层应该是通过 spring cloud ribbon 嫁接到 ribbon 上
	 */
	private LoadBalancerClient loadBalancer;

	/**
	 * 该工厂是用来生成 request 对象的 应该是针对原来调用链中的 原始request 做处理
	 */
	private LoadBalancerRequestFactory requestFactory;

	public LoadBalancerInterceptor(LoadBalancerClient loadBalancer,
			LoadBalancerRequestFactory requestFactory) {
		this.loadBalancer = loadBalancer;
		this.requestFactory = requestFactory;
	}

	public LoadBalancerInterceptor(LoadBalancerClient loadBalancer) {
		// for backwards compatibility
		this(loadBalancer, new LoadBalancerRequestFactory(loadBalancer));
	}

	/**
	 * 实现均衡负载的 核心类就在这里
	 * @param request 请求入参
	 * @param body 数据内容
	 * @param execution 这个是用来发起请求并生成结果的 执行器
	 * @return
	 * @throws IOException
	 */
	@Override
	public ClientHttpResponse intercept(final HttpRequest request, final byte[] body,
			final ClientHttpRequestExecution execution) throws IOException {
		//从请求中获取 原始url
		final URI originalUri = request.getURI();
		//这里获取的是服务名 应该是要针对服务名 做转换 变成 ip:port 的形式
		String serviceName = originalUri.getHost();
		Assert.state(serviceName != null,
				"Request URI does not contain a valid hostname: " + originalUri);

		//第一层 生成了一个函数对象
		//第二层 通过均衡负载对象 处理 传入的服务名 和请求对象 并返回结果
		return this.loadBalancer.execute(serviceName,
				this.requestFactory.createRequest(request, body, execution));
	}

}
