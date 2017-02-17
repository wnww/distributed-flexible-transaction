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
		System.out.println("execute=================================== service2 start ===================");
		StackTraceElement[] stack = (new Throwable()).getStackTrace();
		System.out
				.println("编号	类名                                      方法名            行数");
		String str = " ";
		for (int i = 0; i < stack.length; i++) {
			StackTraceElement ste = stack[i];
			// System.out.println(ste.getClassName() + "." + ste.getMethodName()
			// + "(...);");
			// System.out.println(i + "--" + ste.getMethodName());
			if (i == 0) {
				for (int j = 0; j < (89 - ste.getClassName().length()); j++) {
					str = str + " ";
				}
			}
			System.out.println(i + "	" + ste.getClassName() + str
					+ ste.getMethodName() + "	" + ste.getLineNumber());
			// System.out.println(i + "--" + ste.getLineNumber());
			str = " ";
		}
		System.out.println("execute=================================== service2 end ===================");
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
