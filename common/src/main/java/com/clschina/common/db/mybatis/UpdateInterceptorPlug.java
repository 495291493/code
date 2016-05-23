package com.clschina.common.db.mybatis;

import java.util.Properties;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;

@Intercepts({@Signature(  
        type=Executor.class,method="update",  
        args={MappedStatement.class,Object.class})})
public class UpdateInterceptorPlug implements Interceptor{
	
	@Override
	public Object intercept(Invocation invocation) throws Throwable {
        Object parameter = invocation.getArgs()[1];  
        if(parameter instanceof AbstractDataEntry) {  
            ((AbstractDataEntry)parameter).addModifyFlag();
        }
        return invocation.proceed();  
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
	}

}
