package com.distributed.transaction.test.dubboservice2.provider;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.distributed.transaction.api.Transactionable;
import com.distributed.transaction.common.CommonResponse;
import com.distributed.transaction.test.dubboservice1.api.DubboWithDistributedTransactionAgentService1;
import com.distributed.transaction.test.dubboservice2.api.DubboWithDistributedTransactionAgentService2;

/**
 * 
 * @author yubing
 *
 */
@Service("dubboWithDistributedTransactionAgentService2")
public class TestTransactionAgentService2Impl implements DubboWithDistributedTransactionAgentService2, Serializable{

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3338766213997335199L;
	@Autowired
	DubboWithDistributedTransactionAgentService1 dubboWithDistributedTransactionAgentService1;
	

	public CommonResponse service2() {
		System.out.println("execute=================================== service2 ===================");
		dubboWithDistributedTransactionAgentService1.service1();
		return new CommonResponse();
		
	}

	public CommonResponse service2rollback() {
		System.out.println("execute----------------------------------- service2rollback -----------");
		return new CommonResponse();
		
	}
	
	public CommonResponse service2Commit(){
		System.out.println("execute----------------------------------- service2Commit -----------");
		return new CommonResponse();
	}

}
