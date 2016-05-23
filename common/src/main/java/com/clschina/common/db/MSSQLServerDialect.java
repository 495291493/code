package com.clschina.common.db;

import java.sql.Types;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.dialect.SQLServerDialect;


public class MSSQLServerDialect extends SQLServerDialect {
	private static Log log = LogFactory.getLog(MSSQLServerDialect.class);
	public MSSQLServerDialect(){
		if(log.isTraceEnabled()){
			log.trace("MSSQLServerDialect constructor function called.");
		}
		registerHibernateType( Types.CHAR, Hibernate.STRING.getName() );

	}
}
