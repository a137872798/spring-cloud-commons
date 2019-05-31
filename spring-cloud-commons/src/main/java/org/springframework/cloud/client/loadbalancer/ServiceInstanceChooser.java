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

import org.springframework.cloud.client.ServiceInstance;

/**
 * Implemented by classes which use a load balancer to choose a server to send a request
 * to.
 *
 * @author Ryan Baxter
 *
 * 		该接口是 均衡负载接口的 父级接口 可以从给定的服务列表中 选择合适的服务
 */
public interface ServiceInstanceChooser {

	/**
	 * Chooses a ServiceInstance from the LoadBalancer for the specified service.
	 * @param serviceId The service ID to look up the LoadBalancer.
	 * @return A ServiceInstance that matches the serviceId.
	 * 		这里 服务id 代表的是一个 服务名
	 */
	ServiceInstance choose(String serviceId);

}
