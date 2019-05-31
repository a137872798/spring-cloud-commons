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

import java.net.URI;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.support.HttpRequestWrapper;

/**
 * @author Ryan Baxter
 * 		HttpRequestWrapper 就是一个 HttpRequest 的普通包装类  所有逻辑会委托到内部的 request 对象
 */
public class ServiceRequestWrapper extends HttpRequestWrapper {

	/**
	 * 服务实例对象
	 */
	private final ServiceInstance instance;

	/**
	 * 均衡负载对象
	 */
	private final LoadBalancerClient loadBalancer;

	public ServiceRequestWrapper(HttpRequest request, ServiceInstance instance,
			LoadBalancerClient loadBalancer) {
		super(request);
		this.instance = instance;
		this.loadBalancer = loadBalancer;
	}

	/**
	 * 这里重写了 getUrl 方法 将服务名 通过均衡负载对象 替换成了  ip:port 的格式
	 * @return
	 */
	@Override
	public URI getURI() {
		URI uri = this.loadBalancer.reconstructURI(this.instance, getRequest().getURI());
		return uri;
	}

}
