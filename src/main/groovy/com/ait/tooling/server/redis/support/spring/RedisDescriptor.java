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
import java.util.Objects;

import com.ait.tooling.common.api.java.util.StringOps;
import com.ait.tooling.common.api.types.Activatable;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisDescriptor extends Activatable implements IRedisDescriptor
{
    private String    m_name;

    private JedisPool m_pool;

    public RedisDescriptor(final String name)
    {
        super(true);

        m_name = StringOps.toTrimOrNull(name);

        m_pool = new JedisPool(new JedisPoolConfig(), "localhost");
    }

    @Override
    public String getName()
    {
        return m_name;
    }

    @Override
    public void close() throws IOException
    {
        setActive(false);

        if (null != m_pool)
        {
            m_pool.destroy();

            m_pool = null;
        }
    }

    @Override
    public Jedis getDriver()
    {
        if (isActive())
        {
            if (null != m_pool)
            {
                return Objects.requireNonNull(m_pool.getResource());
            }
            else
            {
                throw new IllegalArgumentException("Redis Descriptor " + getName() + " is null");
            }
        }
        else
        {
            throw new IllegalArgumentException("Redis Descriptor " + getName() + " is not active");
        }
    }
}
