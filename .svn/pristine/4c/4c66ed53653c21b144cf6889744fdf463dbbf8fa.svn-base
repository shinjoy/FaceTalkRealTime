package com.nomadsoft.chat.db.dao;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;

import com.nomadsoft.chat.config.Config;
import com.nomadsoft.chat.config.Constants;

public class DBConnectionPool {
	
	static BasicDataSource dataSource = null;

	
	public static void loadDataSource(String url, String dbName, String dbPass) {
		dataSource = new BasicDataSource();
		dataSource.setDriverClassName("net.sourceforge.jtds.jdbc.Driver"); //
		dataSource.setUrl(url);
		dataSource.setTestWhileIdle(true);
		dataSource.setTestOnBorrow(true);
		dataSource.setValidationQuery("select 1");
		dataSource.setValidationQueryTimeout(5); //5분
		dataSource.setUsername(dbName);
		dataSource.setPassword(dbPass);
		
		/*
		maxActive (최대 active connection 개수)
		- 기본값:8
		minIdle
		-현재 사용되지 않으며 Pool 에 저장될 수 있는 최소한의 커넥션 수
		-기본값:0 (기본값을 사용하게 되면 Pool이 없을 수 있으므로 필요에 따라 설정한다)
		maxIdle
		- 말그대로 Idel 상태 사용되지 않고 Pool 에 저장될수 있는 max connection 수
		- 기본값:8(maxActive 수와 동일하게 설정 상황에 따라 달라 질수 있으나 성질이 비슷하다 보니 동일시 하는 경우가 많다)
		maxWait
		- connection 사용률이 높아지면서 Pool 이 비었을때의 대기시간 0.0001초
		- 기본값 : -1 은 무한정 기다리는 값
		testOnBorrow 
		- connection Pool 에서 해당 Connection 이 유효한지 유효성 검사를 할것인지 말것인지 여부를 설정한다.
		- 기본값  : false /true 일 경우 validationQuery 를 매번 수행한다.
		testOnRetrun 
		- 유효성 검사 시점이 pool을 반환할때 수행한다.
		timeBetweenEvictionRunsMillis 
		- 리소스만 차지하며 사용되지 않는 Connection 을 Pool 에서 제거하는 시간 설정이다.
		- 위에서 말하는 minIdle과 MaxIdle과 같이 사용할 경우 세가지 조건을 모두 고려해서 설정하는 것이 좋다
		- 기본값 : -1 , 단위 0,0001초
		testWhileIdle 
		-  Connection 의 유효성 테스트를 할것인가 결정하는 설정이다.
		- 기본값 : false
		validationQuery 
		- testOnBorrow 시에 실행되며 Connection 유효성 검사시 사용할 쿼리문을 작성한다
		- DB 에 따라 쿼리문은 달라진다.
		*/
	}
	
	public static Connection getConnection() {
		Connection result =null;

		try {
			result = dataSource.getConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return result;
	}
}
