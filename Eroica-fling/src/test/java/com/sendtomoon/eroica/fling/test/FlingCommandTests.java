package com.sendtomoon.eroica.fling.test;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.dubbo.config.ServiceConfig;
import com.sendtomoon.eroica.eoapp.test.BaseSARTest;
import com.sendtomoon.eroica.eoapp.test.SARContextConfiguration;
import com.sendtomoon.eroica.fling.FlingCommandBean;
import com.sendtomoon.eroica.fling.FlingCommandReceiptor;
import com.sendtomoon.eroica.fling.msg.FlingCommandMsg;
import com.sendtomoon.eroica.fling.msg.FlingReceiptMsg;
import com.sendtomoon.eroica.fling.receipt.CommandReceiptService;
import com.sendtomoon.eroica.pizza.Pizza;

@SARContextConfiguration(protocols = { "dubbo" })
public class FlingCommandTests extends BaseSARTest {

	private ServiceConfig<CommandReceiptService> serviceConfig;

	@Before
	public void init() {
		serviceConfig = new ServiceConfig<CommandReceiptService>();
		serviceConfig.setInterface(CommandReceiptService.class);
		serviceConfig.setRef(new CommandReceiptService() {
			@Override
			public void receipt(FlingReceiptMsg receiptMsg) {
				if (logger.isDebugEnabled()) {
					logger.debug("druidSqlMsg:" + receiptMsg.toString());
				}
				System.err.println("---" + Thread.currentThread() + "," + receiptMsg);

			}
		});
		serviceConfig.export();
	}

	@Test
	public void test() throws Exception {

		FlingCommandBean cmd = new FlingCommandBean();
		cmd.setCommandReceiptor(new FlingCommandReceiptor() {
			@Override
			public void receipt(FlingReceiptMsg receiptMsg) {
				System.err.println("---" + Thread.currentThread() + "," + receiptMsg);
			}
		});
		cmd.setPizzaManager(Pizza.getManager());
		cmd.afterPropertiesSet();
		FlingCommandMsg msg = new FlingCommandMsg();
		msg.setDomainId("local2");
		msg.setEoappName("local");
		msg.setActionType("restartup");
		msg.setTargetType("eoapp");
		cmd.send(msg);
		//
		System.in.read();
		cmd.destroy();
		System.in.read();
	}
}
