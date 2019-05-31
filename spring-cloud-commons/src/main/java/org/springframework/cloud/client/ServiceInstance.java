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

package org.springframework.cloud.client;

import java.net.URI;
import java.util.Map;

/**
 * Represents an instance of a service in a discovery system.
 *
 * @author Spencer Gibb
 * @author Tim Ysewyn
 *
 * 		cloud 层面的
 */
public interface ServiceInstance {

	/**
	 * @return The unique instance ID as registered.
	 */
	default String getInstanceId() {
		return null;
	}

	/**
	 * @return The service ID as registered.
	 * 		每个服务实例有自己的id
	 */
	String getServiceId();

	/**
	 * @return The hostname of the registered service instance.
	 *		获取服务实例主机名
	 */
	String getHost();

	/**
	 * @return The port of the registered service instance.
	 * 		获取端口号
	 */
	int getPort();

	/**
	 * @return Whether the port of the registered service instance uses HTTPS.
	 * 		是否使用了 https
	 */
	boolean isSecure();

	/**
	 * @return The service URI address.
	 * 		获取url信息
	 */
	URI getUri();

	/**
	 * @return The key / value pair metadata associated with the service instance.
	 * 		获取该服务的元数据信息  以 KV 形式保存
	 */
	Map<String, String> getMetadata();

	/**
	 * @return The scheme of the service instance.
	 */
	default String getScheme() {
		return null;
	}

}
