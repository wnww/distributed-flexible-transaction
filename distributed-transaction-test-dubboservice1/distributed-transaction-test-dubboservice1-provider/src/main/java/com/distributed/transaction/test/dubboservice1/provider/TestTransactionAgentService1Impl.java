package com.distributed.transaction.test.dubboservice1.provider;

import org.springframework.stereotype.Service;

import com.distributed.transaction.common.CommonResponse;
import com.distributed.transaction.test.dubboservice1.api.DubboWithDistributedTransactionAgentService1;

/**
 * 
 * @author yubing
 * 
 */
@Service("dubboWithDistributedTransactionAgentService1")
public class TestTransactionAgentService1Impl implements DubboWithDistributedTransactionAgentService1 {

	public CommonResponse service1() {
		System.out.println("execute=================================== service1 ===================");
//		StackTraceElement[] stack = (new Throwable()).getStackTrace();
//		System.out.println("编号	类名                                      方法名            行数");
//		String str = " ";
//		for (int i = 0; i < stack.length; i++) {
//			StackTraceElement ste = stack[i];
//			// System.out.println(ste.getClassName() + "." + ste.getMethodName()
//			// + "(...);");
//			// System.out.println(i + "--" + ste.getMethodName());
//			if (i == 0) {
//				for (int j = 0; j < (89 - ste.getClassName().length()); j++) {
//					str = str + " ";
//				}
//			}
//			System.out.println(i + "	" + ste.getClassName() + str + ste.getMethodName() + "	" + ste.getLineNumber());
//			// System.out.println(i + "--" + ste.getLineNumber());
//			str = " ";
//		}
		throw new RuntimeException();
		//return new CommonResponse();

	}

	public CommonResponse service1rollback() {
		System.out.println("execute----------------------------------- service1rollback-----------");
		return new CommonResponse();

	}

}
