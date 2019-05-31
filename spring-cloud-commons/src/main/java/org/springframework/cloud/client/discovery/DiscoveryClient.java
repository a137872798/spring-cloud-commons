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

package org.springframework.cloud.client.discovery;

import java.util.List;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.core.Ordered;

/**
 * Represents read operations commonly available to discovery services such as Netflix
 * Eureka or consul.io.
 *
 * @author Spencer Gibb
 * @author Olga Maciaszek-Sharma 该接口定义了 springcloud 下的所有 具备服务发现功能的client 统一接口来屏蔽差异
 */
public interface DiscoveryClient extends Ordered {

	/**
	 * Default order of the discovery client.
	 */
	int DEFAULT_ORDER = 0;

	/**
	 * A human-readable description of the implementation, used in HealthIndicator.
	 * @return The description. 获取一个可读的描述信息
	 */
	String description();

	/**
	 * Gets all ServiceInstances associated with a particular serviceId.
	 * @param serviceId The serviceId to query.
	 * @return A List of ServiceInstance. 根据服务id 来获取对应的服务实例列表 看来 eureka 是没有直接定义serviceId
	 * 的概念的 这里应该是 做了适配
	 */
	List<ServiceInstance> getInstances(String serviceId);

	/**
	 * @return All known service IDs. 获取所有已知的 serviceId
	 */
	List<String> getServices();

	/**
	 * Default implementation for getting order of discovery clients.
	 * @return order
	 */
	@Override
	default int getOrder() {
		return DEFAULT_ORDER;
	}

}
