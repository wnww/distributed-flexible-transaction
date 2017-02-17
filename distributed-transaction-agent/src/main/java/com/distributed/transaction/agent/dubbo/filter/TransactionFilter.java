package com.distributed.transaction.agent.dubbo.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.distributed.transaction.agent.ParticipantTracer;
import com.distributed.transaction.agent.spring.TransactionableDubboServiceCenter;
import com.distributed.transaction.api.Participant;
import com.distributed.transaction.api.Transaction;
import com.distributed.transaction.api.TransactionInvocation;
import com.distributed.transaction.common.CommonResponse;
import com.distributed.transaction.common.constants.DistributeTransactionConstants;
import com.distributed.transaction.common.util.StringUtil;

/**
 * 
 * @author yubing
 * 
 */

@Activate(group = { Constants.PROVIDER })
public class TransactionFilter implements Filter {

	private static Logger logger = LoggerFactory.getLogger(TransactionFilter.class);

	private TransactionableDubboServiceCenter transactionableDubboServiceCenter;

	private ParticipantTracer participantTracer;

	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		System.out.println("================ 这里调用了");
		String serviceName = invoker.getInterface().getName();
		Class serviceType = invoker.getInterface();
		String transactionableRollbackMethod = "";
		String transactionCommitMethod = "";
		Participant participant = null;

		try {
			String methodName = invocation.getMethodName();
			Class[] parameterTypes = invocation.getParameterTypes();
			Object[] argumentValues = invocation.getArguments();
			transactionableRollbackMethod = transactionableDubboServiceCenter
					.getRollBackMethodOftransactionableAnnotationOnMethod(serviceType, methodName, parameterTypes);
			if (StringUtil.isNullOrEmpty(transactionableRollbackMethod)) {
				return invoke(invoker, invocation);
			} else {
				// RPC上下文信息
				RpcContext context = RpcContext.getContext();
				// 根据反射机制获取目标服务接口名、方法名后，构造TransactionInvocation的POJO对象
				TransactionInvocation commitInvocation = participantTracer.generateTransactionInvocation(serviceType,
						methodName, argumentValues, parameterTypes);

				TransactionInvocation rollbackInvocation = participantTracer.generateTransactionInvocation(serviceType,
						transactionableRollbackMethod, argumentValues, parameterTypes);
				String localHostAddress = context.getLocalAddressString();
				int localHostPort = context.getLocalPort();
				if (context.isConsumerSide()) {
					String transactionUUID = invocation.getAttachment(DistributeTransactionConstants.TRANSACTION_ID);
					Transaction transactionExist = participantTracer.getTransactionByTransactionUUID(transactionUUID);
					participant = participantTracer.generateParticipant(transactionExist, localHostAddress,
							localHostPort, serviceName, methodName, commitInvocation, rollbackInvocation);
					participantTracer.enrollParticipant(transactionExist, participant);

				} else if (context.isProviderSide()) {
					String transactionUUID = RpcContext.getContext().getAttachment(DistributeTransactionConstants.TRANSACTION_ID);
	            	System.out.println("进入方法   =============== "+transactionUUID);
					if (transactionUUID == null || "".equals(transactionUUID)) {
						Transaction transaction = participantTracer.createTransaction();
						participant = participantTracer.generateParticipant(transaction, localHostAddress,
								localHostPort, serviceName, methodName, commitInvocation, rollbackInvocation);
						participantTracer.enrollParticipant(transaction, participant);
					} else {
						Transaction transactionExist = participantTracer.getTransactionByTransactionUUID(transactionUUID);
						participant = participantTracer.generateParticipant(transactionExist, localHostAddress,
								localHostPort, serviceName, methodName, commitInvocation, rollbackInvocation);
						participantTracer.enrollParticipant(transactionExist, participant);

					}
					doWorkBeforeInvoke(participant, invocation);
				}
				System.out.println("before =============== "
						+ RpcContext.getContext().getAttachment(DistributeTransactionConstants.TRANSACTION_ID));
				Result result = invoker.invoke(invocation);
				if (isTriggerTransactionRollback(result)) {
					throw new RpcException("distributed transaction trigger transaction rollback");
				}

				return result;

			}

		} catch (RpcException e) {
			try {
				// if the dubbo service method have marked the
				// "@Transactionable" annotation,if there is any RpcException
				// during this dubbo service method excuting,we must rollback
				// the transaction
				if (!StringUtil.isNullOrEmpty(transactionableRollbackMethod)) {
					if (participant != null) {
						participantTracer.rollback(participant.getTransactionUUID());
					}
				}
			} catch (Exception ex) {
				logger.error("rollback the transaction occur error");
			}
			e.printStackTrace();
			throw new RpcException("occur exception,and rollback the transaction");

		} finally {
			doWorkAfterInvoke();
		}
	}

	private boolean isTriggerTransactionRollback(Result result) {
		CommonResponse commonResponse = (CommonResponse) result.getValue();
		if (result.getException() != null
				|| (commonResponse != null && commonResponse.getCode() != DistributeTransactionConstants.DUBBO_SERVICE_RETURN_SUCCESS)) {
			return true;

		}
		return false;
	}

	private void doWorkBeforeInvoke(Participant participant, Invocation invocation) {
		participantTracer.setParentParticipant(participant);
		RpcContext context = RpcContext.getContext();
		context.setAttachment(DistributeTransactionConstants.TRANSACTION_ID, (participant != null && participant
				.getTransactionUUID() != null) ? participant.getTransactionUUID() : null);

	}

	private void doWorkAfterInvoke() {
		participantTracer.removeParentParticipant();
	}

	public TransactionableDubboServiceCenter getTransactionableDubboServiceCenter() {
		return transactionableDubboServiceCenter;
	}

	public void setTransactionableDubboServiceCenter(TransactionableDubboServiceCenter transactionableDubboServiceCenter) {
		this.transactionableDubboServiceCenter = transactionableDubboServiceCenter;
	}

	public ParticipantTracer getParticipantTracer() {
		return participantTracer;
	}

	public void setParticipantTracer(ParticipantTracer participantTracer) {
		this.participantTracer = participantTracer;
	}

	static {
		logger.info("===============================================================================distributeTransactionAgent filter is loading distribute-transaction-agent.xml file...");
		String resourceName = "classpath*:distribute-transaction-agent.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { resourceName });
		context.start();
	}

}
