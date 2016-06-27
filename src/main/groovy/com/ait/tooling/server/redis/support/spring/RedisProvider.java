/*
 * Copyright (c) 2014,2015,2016 Ahome' Innovation Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ait.tooling.server.redis.support.spring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import com.ait.tooling.common.api.java.util.StringOps;

@ManagedResource
public class RedisProvider implements BeanFactoryAware, IRedisProvider
{
    private static final Logger                           logger        = Logger.getLogger(RedisProvider.class);

    private final String                                  m_default_name;

    private final String                                  m_default_base;

    private final LinkedHashMap<String, IRedisDescriptor> m_descriptors = new LinkedHashMap<String, IRedisDescriptor>();

    public RedisProvider(final String default_base, final String default_name)
    {
        m_default_base = StringOps.requireTrimOrNull(default_base);

        m_default_name = StringOps.requireTrimOrNull(default_name);
    }

    @Override
    public String getRedisDefaultPropertiesBase()
    {
        return m_default_base;
    }

    @Override
    public String getRedisDefaultDescriptorName()
    {
        return m_default_name;
    }

    @Override
    public IRedisDescriptor getRedisDescriptor(final String name)
    {
        return m_descriptors.get(StringOps.requireTrimOrNull(name));
    }

    @Override
    @ManagedAttribute(description = "Get IRedisDescriptor names.")
    public List<String> getRedisDescriptorNames()
    {
        return Collections.unmodifiableList(new ArrayList<String>(m_descriptors.keySet()));
    }

    @Override
    public List<IRedisDescriptor> getRedisDescriptors()
    {
        return Collections.unmodifiableList(new ArrayList<IRedisDescriptor>(m_descriptors.values()));
    }

    @Override
    @ManagedOperation(description = "Close all Redis Descriptors")
    public void close() throws IOException
    {
        for (IRedisDescriptor descriptor : m_descriptors.values())
        {
            try
            {
                logger.info("Closing Redis Descriptor " + descriptor.getName());

                descriptor.close();
            }
            catch (Exception e)
            {
                logger.error("Error closing Redis Descriptor " + descriptor.getName(), e);
            }
        }
    }

    @Override
    public void setBeanFactory(final BeanFactory factory) throws BeansException
    {
        if (factory instanceof DefaultListableBeanFactory)
        {
            for (IRedisDescriptor descriptor : ((DefaultListableBeanFactory) factory).getBeansOfType(IRedisDescriptor.class).values())
            {
                descriptor.setActive(true);

                final String name = StringOps.requireTrimOrNull(descriptor.getName());

                if (null == m_descriptors.get(name))
                {
                    logger.info("Adding Redis Descriptor(" + name + ") class " + descriptor.getClass().getName());

                    m_descriptors.put(name, descriptor);
                }
                else
                {
                    logger.error("Duplicate Redis Descriptor(" + name + ") class " + descriptor.getClass().getName());
                }
            }
        }
    }
}
